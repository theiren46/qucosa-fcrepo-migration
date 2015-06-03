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

import org.apache.camel.builder.RouteBuilder;
import org.qucosa.camel.component.opus4.Opus4ResourceID;
import org.qucosa.migration.processors.MetsGenerator;

public class QucosaStagingRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        from("direct:tenantMigration")
                .log("Processing elements of tenant resource: ${body}")
                .convertBodyTo(Opus4ResourceID.class)
                .to("qucosa:resources")
                .log("Found ${body.size} elements")
                .split(body()).parallelProcessing()
                .to("direct:documentTransformation");

        from("direct:documentTransformation")
                .threads()
                .convertBodyTo(Opus4ResourceID.class)
                .to("qucosa:documents")
                .to("direct:transformations");

        from("direct:transformations")
                .bean(MetsGenerator.class);
    }
}
