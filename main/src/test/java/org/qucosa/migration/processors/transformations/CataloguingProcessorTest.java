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

import noNamespace.Subject;
import noNamespace.Title;
import org.custommonkey.xmlunit.XMLAssert;
import org.junit.Test;

public class CataloguingProcessorTest extends ProcessorTestBase {

    final private MappingProcessor processor = new CataloguingProcessor();

    @Test
    public void extractsTitleAbstract() throws Exception {
        final String value = "Deutsches Abstract";
        Title oa = opusDocument.getOpus().getOpusDocument().addNewTitleAbstract();
        oa.setLanguage("ger");
        oa.setValue(value);

        runProcessor(processor);

        XMLAssert.assertXpathExists(
                "//mods:abstract[@lang='ger' and @type='summary' and text()='" + value + "']",
                modsDocument.getMods().getDomNode().getOwnerDocument());
    }

    @Test
    public void extractsTableOfContent() throws Exception {
        final String value = "Inhaltsverzeichnis";
        opusDocument.getOpus().getOpusDocument().setTableOfContent(value);

        runProcessor(processor);

        XMLAssert.assertXpathExists(
                "//mods:tableOfContents[text()='" + value + "']",
                modsDocument.getMods().getDomNode().getOwnerDocument());
    }

    @Test
    public void extractsSubjectDdc() throws Exception {
        Subject os = opusDocument.getOpus().getOpusDocument().addNewSubjectDdc();
        os.setType("ddc");
        os.setValue("004");

        runProcessor(processor);

        XMLAssert.assertXpathExists(
                "//mods:classification[@authority='ddc' and text()='004']",
                modsDocument.getMods().getDomNode().getOwnerDocument());
    }

    @Test
    public void extractsSubjectRvk() throws Exception {
        Subject os = opusDocument.getOpus().getOpusDocument().addNewSubjectRvk();
        os.setType("rvk");
        os.setValue("ST 270");

        runProcessor(processor);

        XMLAssert.assertXpathExists(
                "//mods:classification[@authority='rvk' and text()='ST 270']",
                modsDocument.getMods().getDomNode().getOwnerDocument());
    }

    @Test
    public void extractsSubjectUncontrolled() throws Exception {
        Subject os = opusDocument.getOpus().getOpusDocument().addNewSubjectUncontrolled();
        os.setType("uncontrolled");
        os.setLanguage("ger");
        os.setValue("A, B, C");

        runProcessor(processor);

        XMLAssert.assertXpathExists(
                "//mods:classification[@lang='ger' and text()='A, B, C']",
                modsDocument.getMods().getDomNode().getOwnerDocument());
    }
}
