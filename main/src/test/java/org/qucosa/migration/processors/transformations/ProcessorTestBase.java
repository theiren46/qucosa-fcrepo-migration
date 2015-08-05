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
import org.apache.camel.Processor;
import org.apache.xmlbeans.XmlException;
import org.custommonkey.xmlunit.NamespaceContext;
import org.custommonkey.xmlunit.SimpleNamespaceContext;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;

import java.io.IOException;
import java.util.HashMap;

public class ProcessorTestBase<P extends Processor> {

    static {
        NamespaceContext ctx = new SimpleNamespaceContext(
                new HashMap() {{
                    put("mods", MappingProcessor.NS_MODS_V3);
                }});
        XMLUnit.setXpathNamespaceContext(ctx);
    }

    protected InfoDocument infoDocument;
    protected ModsDocument modsDocument;
    protected OpusDocument opusDocument;

    @Before
    public void setupBasisDatastreams() throws IOException, XmlException {
        modsDocument = ModsDocument.Factory.newInstance();
        modsDocument.addNewMods();

        opusDocument = OpusDocument.Factory.newInstance();
        opusDocument.addNewOpus().addNewOpusDocument();

        infoDocument = InfoDocument.Factory.newInstance();
        infoDocument.addNewInfo();
    }

    protected void runProcessor(MappingProcessor processor) throws Exception {
        processor.process(opusDocument, modsDocument, infoDocument);
    }
}
