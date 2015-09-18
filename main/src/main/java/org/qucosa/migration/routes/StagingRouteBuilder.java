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

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.commons.configuration.Configuration;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.qucosa.camel.component.opus4.Opus4ResourceID;
import org.qucosa.camel.component.sword.SwordDeposit;
import org.qucosa.migration.processors.DepositMetsGenerator;
import org.qucosa.migration.processors.FileReaderProcessor;
import org.qucosa.migration.processors.PurgeFedoraObject;

import java.util.concurrent.TimeUnit;

public class StagingRouteBuilder extends RouteBuilder {

    private final Configuration config;

    public StagingRouteBuilder(Configuration configuration) {
        this.config = configuration;
    }

    static public String extractPID(HttpResponse httpResponse) throws Exception {
        Header locationHeader = httpResponse.getFirstHeader("Location");
        if (locationHeader == null) {
            throw new Exception("No location header in HTTP response.");
        }
        String locationStr = locationHeader.getValue();
        return locationStr.substring(locationStr.lastIndexOf('/') + 1);
    }

    @Override
    public void configure() throws Exception {
        errorHandler(deadLetterChannel("direct:dead")
                .maximumRedeliveries(5)
                .redeliveryDelay(TimeUnit.SECONDS.toMillis(3))
                .maximumRedeliveryDelay(TimeUnit.SECONDS.toMillis(60))
                .backOffMultiplier(2)
                .asyncDelayedRedelivery()
                .retryAttemptedLogLevel(LoggingLevel.WARN));

        from("direct:dead")
                .routeId("Failed")
                .errorHandler(noErrorHandler())
                .log(LoggingLevel.ERROR, "${body}")
                .setBody(simple("${body} ${exception}"))
                .to("file://target/output");

        from("direct:staging:file")
                .routeId("staging-file")
                .log("Staging resources listed in ${body}")
                .process(new FileReaderProcessor())
                .log("Found ${body.size} elements")
                .split(body()).parallelProcessing()
                .to("direct:staging");

        from("direct:staging")
                .routeId("staging")
                .log("Staging resource: ${body}")
                .convertBodyTo(Opus4ResourceID.class)
                .choice()
                .when(simple("${body.isDocumentId}")).to("direct:staging:document")
                .otherwise().to("direct:staging:tenant");

        from("direct:staging:tenant")
                .routeId("stage-tenant")
                .log("Processing elements of tenant resource: ${body}")
                .convertBodyTo(Opus4ResourceID.class)
                .to("opus4:resources")
                .log("Found ${body.size} elements")
                .split(body()).parallelProcessing()
                .to("direct:staging:document");

        from("direct:staging:document")
                .routeId("stage-document")
                .threads()
                .convertBodyTo(Opus4ResourceID.class)

                .choice()
                .when(constant(config.getBoolean("sword.purge")))
                .log("Purging Fedora object qucosa:${body.identifier}")
                .process(new PurgeFedoraObject(config))
                .end()

                .choice()
                .when(constant(config.getBoolean("sword.slugheader")))
                .setHeader("Slug", simple("qucosa:${body.identifier}"))
                .end()

                .to("opus4:documents")
                .setHeader("Qucosa-File-Url", constant(config.getString("qucosa.file.url")))

                .bean(DepositMetsGenerator.class)

                .to("direct:deposit");

        from("direct:deposit")
                .routeId("deposit-route")
                .setHeader("X-No-Op", constant(config.getBoolean("sword.noop")))
                .setHeader("X-On-Behalf-Of", constant(config.getString("sword.ownerID", null)))
                .setHeader("Content-Type", constant("application/vnd.qucosa.mets+xml"))
                .setHeader("Collection", constant(config.getString("sword.collection")))
                .convertBodyTo(SwordDeposit.class)
                .to("sword:deposit")
                .throttle(5).asyncDelayed()
                .choice().when(constant(config.getBoolean("transforming")))
                .transform(method(StagingRouteBuilder.class, "extractPID"))
                .to("direct:transform");
    }

}
