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

import org.apache.camel.Exchange;
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
                .setHeader("parent", simple("${body}"))
                .setBody(constant("select r1.name as name from resources r1, resources r2 where r1.parent_id=r2.id and r2.name = :?parent"))
                .to("jdbc:qucosaDataSource?useHeadersAsParameters=true")
                .log("${body.size} elements")
                .split(body())
                .transform(simple("${body[name]}"))
                .transform(body(OpusResourceID.class))
                .to("direct:qucosa-webapi");

        from("direct:qucosa-webapi")
                .log("Requesting Qucosa XML for ${body}")
                .transform(simple("${body.identifier}"))
                .setHeader(Exchange.HTTP_METHOD, constant("GET"))
                .setHeader(Exchange.HTTP_PATH, body())
                .to("http://sdvqucosa-app04/opus4/webapi/document/")
                .to("direct:processing");

        from("direct:processing")
                .log("Processing...")
                .bean(OpusXmlBeansProcessor.class);
//                .to("stream:out");

    }
}
