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

import noNamespace.Reference;
import org.custommonkey.xmlunit.XMLAssert;
import org.junit.Test;
import org.w3c.dom.Document;

import java.math.BigInteger;

public class SourcesInfoProcessorTest extends ProcessorTestBase {

    final private MappingProcessor processor = new SourcesInfoProcessor();

    @Test
    public void extractsReferenceUrl() throws Exception {
        final String value = "http://dx.doi.org/10.13141/jve.vol5.no1.pp1-7";
        final String label = "Der Artikel ist zuerst in der Open Access-Zeitschrift \"Journal of Vietnamese Environment\" erschienen.";
        final BigInteger sortOrder = BigInteger.valueOf(10);
        Reference refUrl = opusDocument.getOpus().getOpusDocument().addNewReferenceUrl();
        refUrl.setValue(value);
        refUrl.setLabel(label);
        refUrl.setSortOrder(sortOrder);

        runProcessor(processor);

        final Document ownerDocument = modsDocument.getMods().getDomNode().getOwnerDocument();
        XMLAssert.assertXpathExists("//mods:relatedItem[@type='original'" +
                " and @displayLabel='" + label + "']", ownerDocument);
        XMLAssert.assertXpathExists("//mods:relatedItem/mods:identifier[@type='uri'" +
                " and text()='" + value + "']", ownerDocument);
        XMLAssert.assertXpathExists("//mods:relatedItem/mods:identifier[@type='uri'" +
                " and text()='" + value + "']", ownerDocument);
        XMLAssert.assertXpathExists("//mods:relatedItem/mods:titleInfo[mods:partNumber='" + sortOrder + "']", ownerDocument);
    }

}
