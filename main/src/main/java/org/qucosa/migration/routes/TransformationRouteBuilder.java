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

import gov.loc.mods.v3.ModsDocument;
import noNamespace.OpusDocument;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.qucosa.migration.processors.transformations.MappingProcessor;
import org.qucosa.migration.processors.transformations.TitleInfoProcessor;

import static org.qucosa.migration.processors.aggregate.HashMapAggregationStrategy.aggregateHashBy;

public class TransformationRouteBuilder extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        configureTransformationPipeline();

        from("direct:transform")
                .routeId("transform")
                .to("direct:ds:prepare")
                .multicast(aggregateHashBy(header("DSID")))
                .parallelProcessing()
                .stopOnException()
                .to("direct:ds:qucosaxml", "direct:ds:mods")
                .end()
                .routingSlip(header("transformations")).ignoreInvalidEndpoints();

        from("direct:ds:prepare")
                .routeId("prepare-getting-datastream")
                .setHeader("PID", body())
                .setProperty("httpClient.authenticationPreemptive", constant(true))
                .setProperty("httpClient.authMethod", constant("Basic"))
                .setProperty("httpClient.authUsername", constant("fedoraAdmin"))
                .setProperty("httpClient.authPassword", constant("fedoraAdmin"))
                .setHeader(Exchange.HTTP_METHOD, constant("GET"))
                .setBody(constant(""));

        from("direct:ds:qucosaxml")
                .routeId("get-qucosaxml")
                .setHeader("DSID", constant("QUCOSA-XML"))
                .setHeader(Exchange.HTTP_PATH, simple("/objects/${header[PID]}/datastreams/${header[DSID]}/content"))
                .to("http://localhost:8080/fedora")
                .convertBodyTo(String.class)
                .bean(OpusDocument.Factory.class, "parse(${body})");

        from("direct:ds:mods")
                .routeId("get-mods")
                .setHeader("DSID", constant("MODS"))
                .setHeader(Exchange.HTTP_PATH, simple("/objects/${header[PID]}/datastreams/${header[DSID]}/content"))
                .to("http://localhost:8080/fedora")
                .convertBodyTo(String.class)
                .bean(ModsDocument.Factory.class, "parse(${body})");
    }

    private void configureTransformationPipeline() throws IllegalAccessException, InstantiationException {
        Class[] pipeline = {
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
                    .log("Processing...")
                    .process(mp);
        }
    }
}
