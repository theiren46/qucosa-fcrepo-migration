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

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import static java.lang.System.err;
import static java.lang.System.exit;

public class CommandLineOptions {

    @Option(
            name = "--document",
            aliases = "-d",
            usage = "ID of a single document to migrate",
            forbids = "--tenant")
    private Integer documentId = null;
    @Option(
            name = "--noop",
            aliases = "-n",
            usage = "Will issue SWORD Noop operation on deposit"
    )
    private Boolean noop = false;
    @Option(
            name = "--tenant",
            aliases = "-t",
            usage = "ID of tenant to migrate all of it's documents",
            forbids = "--document"
    )
    private String tenantId = null;

    public CommandLineOptions(String[] args) {
        CmdLineParser parser = new CmdLineParser(this);
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            err.println(e.getMessage());
            parser.printUsage(err);
            exit(1);
        }

    }

    public int getDocumentId() {
        return documentId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getMode() {
        if (documentId != null) return "document";
        if (tenantId != null) return "tenant";
        return "";
    }

    public Boolean getNoop() {
        return noop;
    }
}
