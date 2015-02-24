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

import noNamespace.OpusDocument;
import org.qucosa.opus.Opus4ImmutableRepository;
import org.qucosa.opus.OpusResourceID;
import org.qucosa.sword.QucosaSwordDeposit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

public class Migrator {

    public static final float MB = 1024 ^ 2;
    private static final Logger log = LoggerFactory.getLogger(Migrator.class);
    private final Opus4ImmutableRepository opus;
    private final QucosaSwordDeposit sword;
    private final List<Object> depositReports;

    public Migrator(Opus4ImmutableRepository opusRepository, QucosaSwordDeposit swordDeposit) {
        this.opus = opusRepository;
        this.sword = swordDeposit;
        this.depositReports = new LinkedList<>();
    }

    public List<Object> getDepositReports() {
        return depositReports;
    }

    public void migrateChildren(OpusResourceID resourceID) throws Exception {
        depositReports.clear();
        int i = 0;
        Runtime runtime = Runtime.getRuntime();
        long total = runtime.totalMemory();
        long max = runtime.maxMemory();
        for (OpusResourceID id : opus.children(resourceID)) {
//            log.debug("Migrating object: " + id);
            OpusDocument inputDocument = opus.get(id);
            Object outputDocument = transform(inputDocument);
            Object ingestReceipt = sword.ingest(outputDocument);
            depositReports.add(ingestReceipt);

            if (i++ > 10) {
                i = 0;
                double used = ((total - runtime.freeMemory()) * 100) / total;
                log.debug(String.format("Used: %3.2f, Free: %.2fMB, Total: %.2fMB, Max: %.2fMB, #Reports: %d",
                        used, (runtime.freeMemory() / MB), (total / MB), (max / MB), depositReports.size()));
            }

        }
    }

    public Object transform(OpusDocument input) {
        // TODO Extraction and Transformation
        return null;
    }

}
