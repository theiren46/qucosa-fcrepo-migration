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
import gov.loc.mods.v3.StringPlusLanguage;
import noNamespace.Title;
import org.custommonkey.xmlunit.XMLAssert;
import org.junit.Test;

import static org.junit.Assert.*;

public class TitleInfoProcessorTest extends ProcessorTestBase {

    private MappingProcessor processor = new TitleInfoProcessor();

    @Test
    public void hasCorrectLabel() {
        assertEquals("titleinfo", processor.getLabel());
    }

    @Test
    public void extractsTitleMain() throws Exception {
        final String language = "ger";
        final String value = "Effiziente Schemamigration in der modellgetriebenen Datenbankanwendungsentwicklung";
        addTitleMain(language, value);

        ModsDefinition outputMods = processor.process(inputOpusDocument, inputModsDocument).getMods();

        assertTrue("Expected a <mods:titleInfo> element", outputMods.getTitleInfoArray().length > 0);
        assertEquals(language, outputMods.getTitleInfoArray(0).getTitleArray(0).getLang());
        assertEquals(value,
                outputMods.getTitleInfoArray(0).getTitleArray(0).getStringValue());
    }

    @Test
    public void extractsTitleSub() throws Exception {
        final String language = "eng";
        final String value = "The Incredibly Strange Creatures Who Stopped Living and Became Mixed-Up Zombies";
        addTitleSub(language, value);

        ModsDefinition outputMods = processor.process(inputOpusDocument, inputModsDocument).getMods();

        assertTrue("Expected a <mods:titleInfo> element", outputMods.getTitleInfoArray().length > 0);
        assertEquals(language, outputMods.getTitleInfoArray(0).getSubTitleArray(0).getLang());
        assertEquals(value,
                outputMods.getTitleInfoArray(0).getSubTitleArray(0).getStringValue());
    }

    @Test
    public void extractsTitleAlternative() throws Exception {
        final String language = "ger";
        final String value = "Schülerecho";
        addTitleAlternative(language, value);

        ModsDefinition outputMods = processor.process(inputOpusDocument, inputModsDocument).getMods();

        XMLAssert.assertXpathExists(
                "//mods:titleInfo[@type='alternative']/mods:title[@lang='" + language + "' and text()='" + value + "']",
                outputMods.getDomNode().getOwnerDocument());
    }

    @Test
    public void extractsTitleParent() throws Exception {
        final String language = "ger";
        final String value = "Forschungsberichte des Instituts für Wirtschaftsinformatik";
        addTitleParent(language, value);

        ModsDefinition outputMods = processor.process(inputOpusDocument, inputModsDocument).getMods();

        XMLAssert.assertXpathExists(
                "//mods:relatedItem[@type='series']/mods:titleInfo/" +
                        "mods:title[@lang='" + language + "' and text()='" + value + "']",
                outputMods.getDomNode().getOwnerDocument());
    }


    @Test
    public void noChangesWhenTitleMainAlreadyPresent() throws Exception {
        final String language = "ger";
        final String value = "Effiziente Schemamigration in der modellgetriebenen Datenbankanwendungsentwicklung";
        addTitleMain(language, value);

        StringPlusLanguage title = inputModsDocument.getMods().addNewTitleInfo().addNewTitle();
        title.setLang(language);
        title.setStringValue(value);

        processor.process(inputOpusDocument, inputModsDocument);

        assertFalse(processor.hasChanges());
    }

    @Test
    public void noLanguageAttributeIfNoLanguageSpecified() throws Exception {
        final String value = "Effiziente Schemamigration in der modellgetriebenen Datenbankanwendungsentwicklung";
        addTitleMain(null, value);

        ModsDefinition outputMods = processor.process(inputOpusDocument, inputModsDocument).getMods();

        assertNull(outputMods.getTitleInfoArray(0).getTitleArray(0).getLang());
    }

    private void addTitleMain(String language, String value) {
        Title ot = inputOpusDocument.getOpus().getOpusDocument().addNewTitleMain();
        ot.setLanguage(language);
        ot.setValue(value);
    }

    private void addTitleSub(String language, String value) {
        Title ot = inputOpusDocument.getOpus().getOpusDocument().addNewTitleSub();
        ot.setLanguage(language);
        ot.setValue(value);
    }

    private void addTitleAlternative(String language, String value) {
        Title ot = inputOpusDocument.getOpus().getOpusDocument().addNewTitleAlternative();
        ot.setLanguage(language);
        ot.setValue(value);
    }

    private void addTitleParent(String language, String value) {
        Title ot = inputOpusDocument.getOpus().getOpusDocument().addNewTitleParent();
        ot.setLanguage(language);
        ot.setValue(value);
    }
}
