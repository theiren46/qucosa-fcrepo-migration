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
            name = "--tenant",
            aliases = "-t",
            usage = "ID of tenant to migrate all of it's documents",
            forbids = "--document")
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
}
