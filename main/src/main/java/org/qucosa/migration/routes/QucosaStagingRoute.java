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
import org.qucosa.camel.component.opus4.Opus4ResourceID;
import org.qucosa.camel.component.sword.SwordDeposit;
import org.qucosa.migration.processors.MetsGenerator;

import java.util.concurrent.TimeUnit;

public class QucosaStagingRoute extends RouteBuilder {

    private final Configuration config;

    public QucosaStagingRoute(Configuration configuration) {
        this.config = configuration;
    }

    @Override
    public void configure() throws Exception {
        from("direct:tenantMigration")
                .routeId("tenant-documents-route")
                .log("Processing elements of tenant resource: ${body}")
                .convertBodyTo(Opus4ResourceID.class)
                .to("qucosa:resources")
                .log("Found ${body.size} elements")
                .split(body()).parallelProcessing()
                .to("direct:documentTransformation");

        from("direct:documentTransformation")
                .routeId("build-deposit-route")
                .threads()
                .convertBodyTo(Opus4ResourceID.class)
                .setHeader("Slug", simple("qucosa:${body.identifier}"))
                .to("qucosa:documents")
                .setHeader("Qucosa-File-Url", constant(config.getString("qucosa.file.url")))
                .bean(MetsGenerator.class)
                .setHeader("Content-Type", constant("application/vnd.qucosa.mets+xml"))
                .setHeader("Collection", constant("qucosa:all"))
                .convertBodyTo(SwordDeposit.class)
                .to("direct:deposit");

        from("direct:deposit")
                .routeId("deposit-route")
                .errorHandler(deadLetterChannel("direct:dead")
                        .maximumRedeliveries(5)
                        .redeliveryDelay(TimeUnit.SECONDS.toMillis(3))
                        .asyncDelayedRedelivery()
                        .retryAttemptedLogLevel(LoggingLevel.WARN))
                .threads()
                .setHeader("X-No-Op", constant(config.getBoolean("sword.noop")))
                .to("sword:deposit");

        from("direct:dead")
                .routeId("dead-letter-route")
                .log(LoggingLevel.ERROR, "Failed:\n${body.body}");
    }
}
