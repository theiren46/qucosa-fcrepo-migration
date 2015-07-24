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

package org.qucosa.migration.processors.transformations;

import gov.loc.mods.v3.ModsDocument;
import noNamespace.OpusDocument;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import java.util.Map;

public abstract class MappingProcessor implements Processor {

    private String label;

    @Override
    public void process(Exchange exchange) throws Exception {
        Map m = (Map) exchange.getIn().getBody();
        ModsDocument result = process((OpusDocument) m.get("QUCOSA-XML"), (ModsDocument) m.get("MODS"));
        exchange.getIn().setBody(result);
    }

    public abstract ModsDocument process(OpusDocument opusDocument, ModsDocument modsDocument);

    public String getLabel() {
        if (label == null) {
            String classname = this.getClass().getSimpleName();
            if (classname.endsWith("Processor")) {
                label = classname.substring(0, classname.length() - 9).toLowerCase();
            }
        }
        return label;
    }
}
