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
import gov.loc.mods.v3.ModsDocument;
import noNamespace.OpusDocument;
import org.apache.xmlbeans.XmlException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TitleInfoProcessorTest {

    private static ModsDocument inputModsDocument;
    private static OpusDocument inputOpusDocument;
    private static MappingProcessor processor = new TitleInfoProcessor();

    private ModsDefinition outputMods;

    @BeforeClass
    public static void setup() throws IOException, XmlException {
        inputModsDocument = ModsDocument.Factory.parse(
                TitleInfoProcessorTest.class.getResourceAsStream("/mods/base_mods.xml"));
        inputOpusDocument = OpusDocument.Factory.parse(
                TitleInfoProcessorTest.class.getResourceAsStream("/opus/titleinfo.xml"));
    }

    @Before
    public void runProcessor() {
        outputMods = processor.process(inputOpusDocument, inputModsDocument).getMods();
    }

    @Test
    public void hasCorrectLabel() {
        assertEquals("titleinfo", processor.getLabel());
    }

    @Test
    public void extractsTitleMain() {
        assertTrue("Expected a <mods:titleInfo> element", outputMods.getTitleInfoArray().length > 0);
        assertEquals("ger", outputMods.getTitleInfoArray(0).getTitleArray(0).getLang());
        assertEquals("Effiziente Schemamigration in der modellgetriebenen Datenbankanwendungsentwicklung",
                outputMods.getTitleInfoArray(0).getTitleArray(0).getStringValue());
    }

    @Test
    public void extractsTitleSub() {
        assertTrue("Expected a <mods:titleInfo> element", outputMods.getTitleInfoArray().length > 0);
        assertEquals("eng", outputMods.getTitleInfoArray(0).getSubTitleArray(0).getLang());
        assertEquals("The Incredibly Strange Creatures Who Stopped Living and Became Mixed-Up Zombies",
                outputMods.getTitleInfoArray(0).getSubTitleArray(0).getStringValue());
    }

}
