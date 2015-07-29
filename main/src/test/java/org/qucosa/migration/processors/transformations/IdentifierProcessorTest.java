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

public class IdentifierProcessorTest extends ProcessorTestBase {

    private MappingProcessor processor = new IdentifierProcessor();

    @Test
    public void extractsIsbn() throws Exception {
        String isbn = "978-3-8439-2186-2";
        inputOpusDocument.getOpus().getOpusDocument().addNewIdentifierIsbn().setValue(isbn);

        ModsDefinition outputMods = processor.process(inputOpusDocument, inputModsDocument).getMods();

        XMLAssert.assertXpathExists(
                "//mods:identifier[@type='isbn' and text()='" + isbn + "']",
                outputMods.getDomNode().getOwnerDocument());
    }

    @Test
    public void extractsUrn() throws Exception {
        String urn = "urn:nbn:de:bsz:14-ds-1229936868096-20917";
        inputOpusDocument.getOpus().getOpusDocument().addNewIdentifierUrn().setValue(urn);

        ModsDefinition outputMods = processor.process(inputOpusDocument, inputModsDocument).getMods();

        XMLAssert.assertXpathExists(
                "//mods:identifier[@type='urn' and text()='" + urn + "']",
                outputMods.getDomNode().getOwnerDocument());
    }

    @Test
    public void extractsDoi() throws Exception {
        String doi = "10.3389/fnins.2015.00227";
        inputOpusDocument.getOpus().getOpusDocument().addNewIdentifierDoi().setValue(doi);

        ModsDefinition outputMods = processor.process(inputOpusDocument, inputModsDocument).getMods();

        XMLAssert.assertXpathExists(
                "//mods:identifier[@type='doi' and text()='" + doi + "']",
                outputMods.getDomNode().getOwnerDocument());
    }

    @Test
    public void extractsIssn() throws Exception {
        String issn = "1662-453X";
        inputOpusDocument.getOpus().getOpusDocument().addNewIdentifierIssn().setValue(issn);

        ModsDefinition outputMods = processor.process(inputOpusDocument, inputModsDocument).getMods();

        XMLAssert.assertXpathExists(
                "//mods:identifier[@type='issn' and text()='" + issn + "']",
                outputMods.getDomNode().getOwnerDocument());
    }

    @Test
    public void extractsPpn() throws Exception {
        String ppn = "303072784";
        inputOpusDocument.getOpus().getOpusDocument().addNewIdentifierPpn().setValue(ppn);

        ModsDefinition outputMods = processor.process(inputOpusDocument, inputModsDocument).getMods();

        XMLAssert.assertXpathExists(
                "//mods:identifier[@type='ppn' and text()='" + ppn + "']",
                outputMods.getDomNode().getOwnerDocument());
    }

}
