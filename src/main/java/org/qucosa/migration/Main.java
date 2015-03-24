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
import org.apache.commons.configuration.SystemConfiguration;
import org.qucosa.camel.CamelCavalry;
import org.qucosa.opus.Opus4ImmutableRepository;
import org.qucosa.sword.QucosaSwordDeposit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.System.exit;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        Configuration conf = getConfiguration();
        Opus4ImmutableRepository src = new Opus4ImmutableRepository();
        QucosaSwordDeposit dest = new QucosaSwordDeposit();
        try {
            src.configure(conf);
            dest.configure(conf);
            log.info("Configured source and destination repositories");

            new CamelCavalry().call();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            exit(1);
        } finally {
            src.release();
        }
    }

    private static Configuration getConfiguration() {
        return new SystemConfiguration();
    }

}