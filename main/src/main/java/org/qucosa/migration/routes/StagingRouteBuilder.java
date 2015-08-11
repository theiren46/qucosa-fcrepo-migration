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
import org.qucosa.migration.processors.MetsGenerator;

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
                .setHeader("Slug", simple("qucosa:${body.identifier}"))
                .to("opus4:documents")
                .setHeader("Qucosa-File-Url", constant(config.getString("qucosa.file.url")))
                .bean(MetsGenerator.class)
                .setHeader("Content-Type", constant("application/vnd.qucosa.mets+xml"))
                .setHeader("Collection", constant("qucosa:all"))
                .convertBodyTo(SwordDeposit.class)
                .to("direct:deposit");

        from("direct:deposit")
                .routeId("deposit-route")
                .errorHandler(deadLetterChannel("direct:deposit:dead")
                        .maximumRedeliveries(5)
                        .redeliveryDelay(TimeUnit.SECONDS.toMillis(3))
                        .asyncDelayedRedelivery()
                        .retryAttemptedLogLevel(LoggingLevel.WARN))
                .threads()
                .setHeader("X-No-Op", constant(config.getBoolean("sword.noop")))
                .setHeader("X-On-Behalf-Of", constant(config.getString("sword.ownerID", null)))
                .to("sword:deposit")
                .throttle(1).asyncDelayed()
                .choice().when(constant(config.getBoolean("transforming")))
                .transform(method(StagingRouteBuilder.class, "extractPID"))
                .to("direct:transform");

        from("direct:deposit:dead")
                .routeId("deposit-error")
                .log(LoggingLevel.ERROR, "Failed:\n${body.body}");
    }
}
