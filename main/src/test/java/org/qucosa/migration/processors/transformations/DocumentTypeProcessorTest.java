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

import org.custommonkey.xmlunit.XMLAssert;
import org.junit.Test;

public class DocumentTypeProcessorTest extends ProcessorTestBase {

    private MappingProcessor processor = new DocumentTypeProcessor();

    @Test
    public void extractsDocumentType() throws Exception {
        String type = "diploma_thesis";
        opusDocument.getOpus().getOpusDocument().setType(type);

        runProcessor(processor);

        XMLAssert.assertXpathExists(
                "//slub:documentType[text()='" + type + "']",
                infoDocument.getInfo().getDomNode().getOwnerDocument());
    }

    @Test
    public void updatesExistingDocumentType() throws Exception {
        opusDocument.getOpus().getOpusDocument().setType("new-type");
        infoDocument.getInfo().setDocumentType("old-type");

        runProcessor(processor);

        XMLAssert.assertXpathExists(
                "//slub:documentType[text()='new-type']",
                infoDocument.getInfo().getDomNode().getOwnerDocument());
    }

    @Test
    public void properlyEncodesJournalDocumentType() throws Exception {
        String type = "journal";
        opusDocument.getOpus().getOpusDocument().setType(type);

        runProcessor(processor);

        XMLAssert.assertXpathExists(
                "//slub:documentType[text()='periodical']",
                infoDocument.getInfo().getDomNode().getOwnerDocument());
    }


}