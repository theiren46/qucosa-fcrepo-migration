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
            name = "--collection",
            aliases = "-c",
            usage = "Name of the SWORD collection to ingest into",
            required = true
    )
    private String collection = null;
    @Option(
            name = "--stage-resource-file",
            aliases = "-f",
            usage = "Name of file to read Opus IDs from",
            forbids = "--stage-resource"
    )
    private String idFile = "";
    @Option(
            name = "--mappings",
            aliases = "-m",
            usage = "Comma separated list of mappings to apply when transforming a staged resource."
    )
    private String[] mappings = {};
    @Option(
            name = "--noop",
            aliases = "-n",
            usage = "Will issue SWORD Noop operation on deposit"
    )
    private Boolean noop = false;
    @Option(
            name = "--ownerID",
            aliases = "-o",
            usage = "Owner ID to be set for ingested documents",
            required = true
    )
    private String ownerId = null;
    @Option(
            name = "--purge",
            aliases = "-p",
            usage = "Try to purge object before doing deposit"
    )
    private Boolean purgeBeforeDeposit = false;
    @Option(
            name = "--stage-resource",
            aliases = "-s",
            usage = "Opus ID of a single document or tenant for staging",
            forbids = "--stage-resource-file"
    )
    private String stageResource = null;
    @Option(
            name = "--stage-transform",
            usage = "Applies transformation to staged resources if --stage-resource is given.\n" +
                    "Since it requires a successful deposit, it doesn't work with --noop.",
            forbids = "--noop"
    )
    private Boolean stageTransform = false;
    @Option(
            name = "--transform-resource",
            aliases = "-t",
            usage = "ID of already staged resource for transformation"
    )
    private String transformResource = null;
    @Option(
            name = "--use-slug",
            usage = "Given this option, the generated Fedora ID will reflect the original Opus ID",
            depends = "--stage-resource"
    )
    private Boolean useSlugHeader = false;

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

    public Boolean isNoop() {
        return noop;
    }

    public String[] getMappings() {
        return mappings;
    }

    public String getStageResource() {
        return stageResource;
    }

    public String getTransformResource() {
        return transformResource;
    }

    public Boolean isStageTransform() {
        return stageTransform;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public String getCollection() {
        return collection;
    }

    public Boolean purgeBeforeDeposit() {
        return purgeBeforeDeposit;
    }

    public String getIdFile() {
        return idFile;
    }

    public Boolean useSlugHeader() {
        return useSlugHeader;
    }
}
