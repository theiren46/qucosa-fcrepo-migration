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

package org.qucosa.migration;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.SystemConfiguration;
import org.qucosa.migration.contexts.MigrationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.System.exit;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        CommandLineOptions options = new CommandLineOptions(args);
        System.setProperty("sword.noop", String.valueOf(options.isNoop()));
        System.setProperty("sword.slugheader", String.valueOf(options.useSlugHeader()));
        System.setProperty("sword.purge", String.valueOf(options.purgeBeforeDeposit()));
        System.setProperty("sword.collection", String.valueOf(options.getCollection()));

        if (options.getOwnerId() != null) {
            System.setProperty("sword.ownerID", options.getOwnerId());
        }

        MigrationContext ctx = null;
        try {
            Boolean hasStagingResource = (options.getStageResource() != null);
            Boolean hasStagingresourceFile = (!options.getIdFile().isEmpty());
            Boolean hasTransformResource = (options.getTransformResource() != null);

            Boolean isTransforming = hasTransformResource
                    || (options.getMappings().length > 0)
                    || (options.isStageTransform());

            Boolean isStaging = hasStagingResource || hasStagingresourceFile;

            System.setProperty("transforming", String.valueOf(isTransforming));

            Configuration conf = new SystemConfiguration();
            ctx = new MigrationContext(conf, isStaging, isTransforming);
            ctx.start();

            final String routingSlip = buildTransformationRoutingSlip(options);

            if (hasStagingResource) {
                sendExchange("direct:staging", options.getStageResource(), ctx, routingSlip);
            }
            if (hasStagingresourceFile) {
                sendExchange("direct:staging:file", options.getIdFile(), ctx, routingSlip);
            }
            if (hasTransformResource) {
                sendExchange("direct:transform", options.getTransformResource(), ctx, routingSlip);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            exit(1);
        } finally {
            if (ctx != null) try {
                ctx.stop();
            } catch (Exception e) {
                System.out.println("Error shutting down Camel: " + e.getMessage());
                e.printStackTrace();
                exit(1);
            }
        }
    }

    private static void sendExchange(String endpointUri, String resource, CamelContext ctx, String routingSlip) {
        ProducerTemplate template = ctx.createProducerTemplate();
        if (routingSlip.isEmpty()) {
            template.sendBody(endpointUri, resource);
        } else {
            template.sendBodyAndHeader(
                    endpointUri, resource,
                    "transformations", routingSlip);
        }
    }

    private static String buildTransformationRoutingSlip(CommandLineOptions options) {
        StringBuilder sb = new StringBuilder();
        for (String m : options.getMappings()) {
            sb.append("direct:transform:")
                    .append(m.toLowerCase())
                    .append(',');
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        } else {
            sb.append("direct:transform:all");
        }
        return sb.toString();
    }

}
