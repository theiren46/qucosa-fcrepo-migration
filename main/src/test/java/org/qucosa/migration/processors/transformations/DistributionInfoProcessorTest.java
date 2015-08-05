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

import gov.loc.mods.v3.ModsDefinition;
import org.custommonkey.xmlunit.XMLAssert;
import org.junit.Test;

public class DistributionInfoProcessorTest extends ProcessorTestBase {

    private MappingProcessor processor = new DistributionInfoProcessor();

    @Test
    public void extractsPublisherName() throws Exception {
        final String publisher = "Universitätsbibliothek Leipzig";
        inputOpusDocument.getOpus().getOpusDocument().setPublisherName(publisher);

        ModsDefinition outputMods = processor.process(inputOpusDocument, inputModsDocument).getMods();

        XMLAssert.assertXpathExists(
                "//mods:originInfo[@eventType='distribution']/" +
                        "mods:publisher[text()='" + publisher + "']",
                outputMods.getDomNode().getOwnerDocument());

    }

}
