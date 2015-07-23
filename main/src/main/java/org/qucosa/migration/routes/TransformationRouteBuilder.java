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
import org.apache.http.Header;
import org.apache.http.HttpResponse;

public class TransformationRouteBuilder extends RouteBuilder {
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
        from("direct:transform")
                .routeId("transforming")
                .bean(TransformationRouteBuilder.class, "extractPID")
                .log("${body}")
                .routingSlip(header("transformations")).ignoreInvalidEndpoints();
    }
}
