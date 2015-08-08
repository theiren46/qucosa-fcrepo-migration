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

import de.slubDresden.InfoDocument;
import gov.loc.mods.v3.ModsDocument;
import noNamespace.OpusDocument;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultExchange;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.qucosa.migration.processors.transformations.MappingProcessor.MODS_CHANGES;
import static org.qucosa.migration.processors.transformations.MappingProcessor.SLUB_INFO_CHANGES;

public class MappingProcessorTest {

    @Test
    public void setsChangesPropertyToExchangeIfProcessorReportsChanges() throws Exception {
        MappingProcessor mappingProcessor = new MappingProcessor() {
            @Override
            public void process(OpusDocument opusDocument, ModsDocument modsDocument, InfoDocument infoDocument) {
                signalChanges(MODS_CHANGES);
                signalChanges(SLUB_INFO_CHANGES);
            }
        };

        Exchange exchange = new DefaultExchange(new DefaultCamelContext());
        exchange.getIn().setBody(new HashMap<String, Object>() {{
            put("QUCOSA-XML", null);
            put("MODS", null);
        }});

        mappingProcessor.process(exchange);

        assertTrue((Boolean) exchange.getProperty(MODS_CHANGES));
        assertTrue((Boolean) exchange.getProperty(SLUB_INFO_CHANGES));
    }

    @Test
    public void returnsMapInExchangeBody() throws Exception {
        MappingProcessor mappingProcessor = new MappingProcessor() {
            @Override
            public void process(OpusDocument opusDocument, ModsDocument modsDocument, InfoDocument infoDocument) throws Exception {
            }
        };

        Exchange exchange = new DefaultExchange(new DefaultCamelContext());
        exchange.getIn().setBody(new HashMap<String, Object>() {{
            put("QUCOSA-XML", null);
            put("MODS", null);
            put("SLUB-INFO", null);
        }});

        mappingProcessor.process(exchange);

        final Object body = exchange.getIn().getBody();
        assertTrue(body instanceof Map);
        assertTrue(((Map) body).containsKey("QUCOSA-XML"));
        assertTrue(((Map) body).containsKey("MODS"));
        assertTrue(((Map) body).containsKey("SLUB-INFO"));
    }

    @Test
    public void hasCorrectLabel() {
        MappingProcessor processor = new LabelTestProcessor();

        assertEquals("labeltest", processor.getLabel());
    }

    private class LabelTestProcessor extends MappingProcessor {
        @Override
        public void process(OpusDocument opusDocument, ModsDocument modsDocument, InfoDocument infoDocument) throws Exception {
        }
    }

}
