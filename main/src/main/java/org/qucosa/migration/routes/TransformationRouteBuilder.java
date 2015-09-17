/*
 * Copyright (C) 2015 Saxon State and University Library Dresden (SLUB)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.qucosa.migration.routes;

import de.slubDresden.InfoDocument;
import gov.loc.mods.v3.ModsDocument;
import noNamespace.OpusDocument;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http.BasicAuthenticationHttpClientConfigurer;
import org.apache.camel.component.http.HttpEndpoint;
import org.apache.camel.component.http.HttpOperationFailedException;
import org.apache.camel.model.RouteDefinition;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.qucosa.camel.component.sword.SwordDeposit;
import org.qucosa.migration.processors.DepositMetsGenerator;
import org.qucosa.migration.processors.HttpOperationFailedHelper;
import org.qucosa.migration.processors.transformations.*;

import java.util.concurrent.TimeUnit;

import static org.qucosa.migration.processors.aggregate.HashMapAggregationStrategy.aggregateHashBy;

public class TransformationRouteBuilder extends RouteBuilder {

    private final Configuration configuration;

    public TransformationRouteBuilder(Configuration conf) {
        this.configuration = conf;
    }

    @Override
    public void configure() throws Exception {
        configureTransformationPipeline();

        final String fedoraUri = getConfigValueOrThrowException("fedora.url");

        configureHttpBasicAuth(
                fedoraUri,
                getConfigValueOrThrowException("fedora.user"),
                getConfigValueOrThrowException("fedora.password"));

        configureHttpBasicAuth(
                getConfigValueOrThrowException("sword.url"),
                getConfigValueOrThrowException("sword.user"),
                getConfigValueOrThrowException("sword.password"));

        from("direct:transform")
                .routeId("transform")
                .multicast(aggregateHashBy(header("DSID")))
                .parallelProcessing()
                .stopOnException()
                .to("direct:ds:qucosaxml", "direct:ds:mods", "direct:ds:slubxml")
                .end()
                .routingSlip(header("transformations")).ignoreInvalidEndpoints()
                .to("direct:ds:update");

        final String datastreamPath = "/objects/${header[PID]}/datastreams/${header[DSID]}";

        from("direct:ds:qucosaxml")
                .routeId("get-qucosaxml")
                .threads()
                .setHeader("PID", body())
                .setHeader("DSID", constant("QUCOSA-XML"))
                .setHeader(Exchange.HTTP_METHOD, constant("GET"))
                .setHeader(Exchange.HTTP_PATH, simple(datastreamPath + "/content"))
                .setBody(constant(""))
                .to(fedoraUri)
                .convertBodyTo(String.class)
                .bean(OpusDocument.Factory.class, "parse(${body})");

        final ModsDocument modsDocumentTemplate = ModsDocument.Factory.newInstance();
        modsDocumentTemplate.addNewMods();

        from("direct:ds:mods")
                .routeId("get-mods")

                .onException(HttpOperationFailedException.class)
                .onWhen(method(HttpOperationFailedHelper.class, "isNotFound"))
                .setBody(constant(modsDocumentTemplate))
                .continued(true)
                .end()

                .threads()
                .setHeader("PID", body())
                .setHeader("DSID", constant("MODS"))
                .setHeader(Exchange.HTTP_METHOD, constant("GET"))
                .setHeader(Exchange.HTTP_PATH, simple(datastreamPath + "/content"))
                .setBody(constant(""))
                .to(fedoraUri)
                .convertBodyTo(String.class)
                .bean(ModsDocument.Factory.class, "parse(${body})");

        final InfoDocument infoDocumentTemplate = InfoDocument.Factory.newInstance();
        infoDocumentTemplate.addNewInfo();

        from("direct:ds:slubxml")
                .routeId("get-slubxml")

                .onException(HttpOperationFailedException.class)
                .onWhen(method(HttpOperationFailedHelper.class, "isNotFound"))
                .setBody(constant(infoDocumentTemplate))
                .continued(true)
                .end()

                .threads()
                .setHeader("PID", body())
                .setHeader("DSID", constant("SLUB-INFO"))
                .setHeader(Exchange.HTTP_METHOD, constant("GET"))
                .setHeader(Exchange.HTTP_PATH, simple(datastreamPath + "/content"))
                .setBody(constant(""))
                .to(fedoraUri)
                .convertBodyTo(String.class)
                .bean(InfoDocument.Factory.class, "parse(${body})");

        from("direct:ds:update")
                .routeId("update")
                .bean(DepositMetsGenerator.class)
                .setHeader("Qucosa-File-Url", constant(configuration.getString("qucosa.file.url")))
                .setHeader("Collection", constant(configuration.getString("sword.collection")))
                .setHeader("Content-Type", constant("application/vnd.qucosa.mets+xml"))
                .convertBodyTo(SwordDeposit.class)
                .to("direct:sword:update");

        from("direct:sword:update")
                .routeId("sword-update")
                .log("Updating ${header[PID]}")
                .errorHandler(deadLetterChannel("direct:dead")
                        .maximumRedeliveries(5)
                        .redeliveryDelay(TimeUnit.SECONDS.toMillis(3))
                        .maximumRedeliveryDelay(TimeUnit.SECONDS.toMillis(60))
                        .backOffMultiplier(2)
                        .asyncDelayedRedelivery()
                        .retryAttemptedLogLevel(LoggingLevel.WARN))
                .threads()
                .setHeader("X-No-Op", constant(configuration.getBoolean("sword.noop")))
                .setHeader("X-On-Behalf-Of", constant(configuration.getString("sword.ownerID", null)))
                .to("sword:update");
    }

    private void configureHttpBasicAuth(String uri, String user, String password) throws ConfigurationException {
        HttpEndpoint httpEndpoint = (HttpEndpoint) getContext().getEndpoint(uri);
        httpEndpoint.setHttpClientConfigurer(
                new BasicAuthenticationHttpClientConfigurer(false, user, password));
    }

    private void configureTransformationPipeline() throws IllegalAccessException, InstantiationException {
        Class[] pipeline = {
                CataloguingProcessor.class,
                DistributionInfoProcessor.class,
                DocumentTypeProcessor.class,
                IdentifierProcessor.class,
                InstitutionInfoProcessor.class,
                PersonInfoProcessor.class,
                PublicationInfoProcessor.class,
                RelationInfoProcessor.class,
                RightsProcessor.class,
                SourcesInfoProcessor.class,
                StaticInfoProcessor.class,
                TitleInfoProcessor.class
        };

        RouteDefinition all = from("direct:transform:all")
                .routeId("all-transformations")
                .log("Defaulting to perform all available transformations");

        for (Class c : pipeline) {
            MappingProcessor mp = (MappingProcessor) c.newInstance();
            String uri = "direct:transform:" + mp.getLabel();

            all.to(uri);
            from(uri)
                    .routeId("transform-" + mp.getLabel())
                    .log(LoggingLevel.DEBUG, "Processing...")
                    .process(mp);
        }
    }

    private String getConfigValueOrThrowException(String key) throws ConfigurationException {
        String val = configuration.getString(key, null);
        if (val == null) {
            throw new ConfigurationException("No config value for " + key);
        }
        return val;
    }
}
