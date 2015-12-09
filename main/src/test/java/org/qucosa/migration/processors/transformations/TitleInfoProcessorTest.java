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
import gov.loc.mods.v3.TitleInfoDefinition;
import noNamespace.Title;
import org.custommonkey.xmlunit.XMLAssert;
import org.junit.Test;

import static org.junit.Assert.*;

public class TitleInfoProcessorTest extends ProcessorTestBase {

    private MappingProcessor processor = new TitleInfoProcessor();

    @Test
    public void extractsTitleMain() throws Exception {
        final String language = "ger";
        final String value = "Effiziente Schemamigration in der modellgetriebenen Datenbankanwendungsentwicklung";
        addTitleMain(language, value);

        runProcessor(processor);
        ModsDefinition outputMods = modsDocument.getMods();

        XMLAssert.assertXpathExists(
                "//mods:titleInfo[@lang='" + language + "' and @usage='primary']/mods:title[text()='" + value + "']",
                outputMods.getDomNode().getOwnerDocument());
    }

    @Test
    public void extractsTitleSub() throws Exception {
        final String language = "eng";
        final String value = "The Incredibly Strange Creatures Who Stopped Living and Became Mixed-Up Zombies";
        addTitleSub(language, value);

        runProcessor(processor);
        ModsDefinition outputMods = modsDocument.getMods();

        XMLAssert.assertXpathExists(
                "//mods:titleInfo[@lang='" + language + "']/mods:subTitle[text()='" + value + "']",
                outputMods.getDomNode().getOwnerDocument());
    }

    @Test
    public void extractsTitleAlternative() throws Exception {
        final String language = "ger";
        final String value = "Schülerecho";
        addTitleAlternative(language, value);

        runProcessor(processor);
        ModsDefinition outputMods = modsDocument.getMods();

        XMLAssert.assertXpathExists(
                "//mods:titleInfo[@lang='" + language + "' and @type='alternative']/mods:title[text()='" + value + "']",
                outputMods.getDomNode().getOwnerDocument());
    }

    @Test
    public void extractsTitleParent() throws Exception {
        final String language = "ger";
        final String value = "Forschungsberichte des Instituts für Wirtschaftsinformatik";
        addTitleParent(language, value);

        runProcessor(processor);
        ModsDefinition outputMods = modsDocument.getMods();

        XMLAssert.assertXpathExists(
                "//mods:relatedItem[@type='series']/mods:titleInfo[@lang='" + language + "']/" +
                        "mods:title[text()='" + value + "']",
                outputMods.getDomNode().getOwnerDocument());
    }


    @Test
    public void noChangesWhenTitleMainAlreadyPresent() throws Exception {
        final String language = "ger";
        final String value = "Effiziente Schemamigration in der modellgetriebenen Datenbankanwendungsentwicklung";
        addTitleMain(language, value);

        TitleInfoDefinition titleInfoDefinition = modsDocument.getMods().addNewTitleInfo();
        titleInfoDefinition.setLang(language);

        StringPlusLanguage title = titleInfoDefinition.addNewTitle();
        title.setStringValue(value);

        runProcessor(processor);

        assertFalse(processor.hasChanges());
    }

    @Test
    public void oneTitleInfoPerLanguage() throws Exception {
        addTitleMain("ger", "Deutscher Titel");
        addTitleMain("eng", "English Title");
        addTitleSub("ger", "Deutscher Untertitel");
        addTitleSub("eng", "English Sub Title");

        runProcessor(processor);
        ModsDefinition outputMods = modsDocument.getMods();

        XMLAssert.assertXpathExists("//mods:titleInfo[@lang='ger']", outputMods.getDomNode().getOwnerDocument());
        XMLAssert.assertXpathExists("//mods:titleInfo[@lang='eng']", outputMods.getDomNode().getOwnerDocument());
    }

    @Test
    public void noLanguageAttributeIfNoLanguageSpecified() throws Exception {
        final String value = "Effiziente Schemamigration in der modellgetriebenen Datenbankanwendungsentwicklung";
        addTitleMain(null, value);

        runProcessor(processor);
        ModsDefinition outputMods = modsDocument.getMods();

        assertNull(outputMods.getTitleInfoArray(0).getTitleArray(0).getLang());
    }

    private void addTitleMain(String language, String value) {
        Title ot = opusDocument.getOpus().getOpusDocument().addNewTitleMain();
        ot.setLanguage(language);
        ot.setValue(value);
    }

    private void addTitleSub(String language, String value) {
        Title ot = opusDocument.getOpus().getOpusDocument().addNewTitleSub();
        ot.setLanguage(language);
        ot.setValue(value);
    }

    private void addTitleAlternative(String language, String value) {
        Title ot = opusDocument.getOpus().getOpusDocument().addNewTitleAlternative();
        ot.setLanguage(language);
        ot.setValue(value);
    }

    private void addTitleParent(String language, String value) {
        Title ot = opusDocument.getOpus().getOpusDocument().addNewTitleParent();
        ot.setLanguage(language);
        ot.setValue(value);
    }
}
