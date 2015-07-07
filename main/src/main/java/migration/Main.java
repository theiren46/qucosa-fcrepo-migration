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

package migration;

import migration.contexts.StagingContext;
import org.apache.camel.ProducerTemplate;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.SystemConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.System.exit;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            CommandLineOptions options = new CommandLineOptions(args);

            System.setProperty("sword.noop", String.valueOf(options.getNoop()));
            Configuration conf = new SystemConfiguration();

            StagingContext ctx = new StagingContext(conf);
            ctx.start();

            ProducerTemplate template = ctx.createProducerTemplate();

            switch (options.getMode()) {
                case "tenant":
                    template.sendBody("direct:tenantMigration", options.getTenantId());
                    break;
                case "document":
                    template.sendBody("direct:documentTransformation", "Opus/Document/" + options.getDocumentId());
                    break;
                default:
                    System.err.println("Nothing to migrate. No options given?");
                    break;
            }

            ctx.stop();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            exit(1);
        }
    }


}
