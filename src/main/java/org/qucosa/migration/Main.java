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

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.SystemConfiguration;
import org.qucosa.fedora.FedoraApiProvider;
import org.qucosa.opus.SourceRepositoryOpus4Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static java.lang.System.exit;

public class Main {


    private static final Logger log = LoggerFactory.getLogger(Main.class);
    private static final boolean PURGE_WHEN_PRESENT = true;

    public static void main(String[] args) {
        SourceRepositoryOpus4Provider qucosaProvider = new SourceRepositoryOpus4Provider();
        FedoraApiProvider fedoraProvider = new FedoraApiProvider();
        try {
            Configuration conf = getConfiguration();
            qucosaProvider.configure(conf);
            fedoraProvider.configure(conf);

            List<String> resourceNames = qucosaProvider.getResources("Opus/Document/%");

            RepositoryMigrator migrator = new RepositoryMigrator();
            migrator.migrateQucosaDocuments(qucosaProvider, fedoraProvider, resourceNames, PURGE_WHEN_PRESENT);

        } catch (Exception e) {
            log.error(e.getMessage());
            exit(1);
        } finally {
            qucosaProvider.release();
        }
    }


    private static Configuration getConfiguration() throws ConfigurationException {
        return new SystemConfiguration();
    }

}
