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
        System.setProperty("sword.noop", String.valueOf(options.getNoop()));

        Configuration conf = new SystemConfiguration();
        MigrationContext ctx = null;
        try {
            Boolean isStaging = (options.getStageResource() != null);
            Boolean isTransforming = (options.getTransformResource() != null);

            ctx = new MigrationContext(conf, isStaging, isTransforming);
            ctx.start();

            if (isStaging) sendStagingExchange(options, ctx);
            if (isTransforming) sendTransformationExchange(options, ctx);
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

    private static void sendTransformationExchange(CommandLineOptions options, CamelContext ctx) {
        ProducerTemplate template = ctx.createProducerTemplate();
        String routingSlip = buildRoutingSlip(options);
        if (routingSlip.isEmpty()) {
            template.sendBody("direct:transforming", options.getTransformResource());
        } else {
            template.sendBodyAndHeader(
                    "direct:transforming", options.getTransformResource(),
                    "transformations", routingSlip);
        }
    }

    private static void sendStagingExchange(CommandLineOptions options, CamelContext ctx) {
        ProducerTemplate template = ctx.createProducerTemplate();
        template.sendBody("direct:staging", options.getStageResource());
    }

    private static String buildRoutingSlip(CommandLineOptions options) {
        StringBuilder sb = new StringBuilder();
        for (String m : options.getMappings()) {
            sb.append("direct:")
                    .append(m.toLowerCase())
                    .append(',');
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

}
