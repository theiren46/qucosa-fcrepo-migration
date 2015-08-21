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

public class RelationInfoProcessorTest extends ProcessorTestBase {

    final private MappingProcessor processor = new RelationInfoProcessor();

    @Test
    public void extractsSeriesRelation() throws Exception {
        final String urn = "urn:nbn:de:bsz:14-qucosa-38419";
        final String link = "http://nbn-resolving.de/" + urn;
        final String label = "Link zur Schriftenreihe";
        final BigInteger sortOrder = BigInteger.valueOf(201009);
        createReferenceUrn(urn, label, sortOrder);

        runProcessor(processor);

        final Document ownerDocument = modsDocument.getMods().getDomNode().getOwnerDocument();
        XMLAssert.assertXpathExists("//mods:relatedItem[" +
                "@type='series'" +
                " and @displayLabel='" + label + "'" +
                " and @xlink:href='" + link + "' ]", ownerDocument);
        XMLAssert.assertXpathExists("//mods:relatedItem/mods:identifier[@type='urn'" +
                " and text()='" + urn + "']", ownerDocument);
        XMLAssert.assertXpathExists("//mods:relatedItem/mods:titleInfo[mods:partNumber='" + sortOrder + "']", ownerDocument);
    }

    private void createReferenceUrn(String urn, String label, BigInteger sortOrder) {
        Reference refUrl = opusDocument.getOpus().getOpusDocument().addNewReferenceUrn();
        refUrl.setValue(urn);
        refUrl.setLabel(label);
        refUrl.setRelation("series");
        refUrl.setSortOrder(sortOrder);
    }


}
