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

package org.qucosa.camel;

import org.apache.camel.builder.RouteBuilder;
import org.qucosa.opus.OpusResourceID;

/**
 * @author claussni
 * @date 23.03.15.
 */
public class QucosaStagingRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception {

        from("direct:qucosa-resources")
                .log("Processing elements of resource: ${body}")
                .convertBodyTo(OpusResourceID.class)
                .beanRef("qucosaDataSource", "children")
                .log("${body.size} elements")
                .split(body())
                .to("direct:qucosa-webapi");

        from("direct:qucosa-webapi")
                .log("Requesting Qucosa XML for ${body}")
                .beanRef("qucosaDataSource", "get")
                .to("direct:processing");

        from("direct:processing")
                .log("Processing...")
                .bean(OpusXmlBeansProcessor.class);
//                .to("stream:out");

    }
}
