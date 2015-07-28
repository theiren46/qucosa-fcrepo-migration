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
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultExchange;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertTrue;

public class MappingProcessorTest {

    @Test
    public void setsChangesPropertyToExhangeIfProcessorReportsChanges() throws Exception {
        MappingProcessor mappingProcessor = new MappingProcessor() {
            @Override
            public ModsDocument process(OpusDocument opusDocument, ModsDocument modsDocument) {
                signalChanges();
                return null;
            }
        };


        Exchange exchange = new DefaultExchange(new DefaultCamelContext());
        exchange.getIn().setBody(new HashMap<String, Object>() {{
            put("QUCOSA-XML", null);
            put("MODS", null);
        }});

        mappingProcessor.process(exchange);

        assertTrue((Boolean) exchange.getProperty("MODS_CHANGES"));
    }

}
