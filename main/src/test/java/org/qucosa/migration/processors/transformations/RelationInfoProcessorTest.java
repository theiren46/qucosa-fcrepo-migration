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
    public void mapSeriesReferenceToSeriesRelatedItem() throws Exception {
        final String urn = "urn:nbn:de:bsz:14-qucosa-38419";
        final String link = "http://nbn-resolving.de/" + urn;
        final String label = "Link zur Schriftenreihe";
        final BigInteger sortOrder = BigInteger.valueOf(201009);
        createReferenceUrn(urn, label, "series", sortOrder);

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

    @Test
    public void mapJournalReferenceToSeriesRelatedItem() throws Exception {
        final String urn = "urn:nbn:de:bsz:ch1-qucosa-62094";
        final String link = "http://nbn-resolving.de/" + urn;
        final BigInteger sortOrder = BigInteger.valueOf(20031);
        createReferenceUrn(urn, null, "journal", sortOrder);

        runProcessor(processor);

        final Document ownerDocument = modsDocument.getMods().getDomNode().getOwnerDocument();
        XMLAssert.assertXpathExists("//mods:relatedItem[" +
                "@type='series'" +
                " and @xlink:href='" + link + "' ]", ownerDocument);
        XMLAssert.assertXpathExists("//mods:relatedItem/mods:identifier[@type='urn'" +
                " and text()='" + urn + "']", ownerDocument);
        XMLAssert.assertXpathExists("//mods:relatedItem/mods:titleInfo[mods:partNumber='" + sortOrder + "']", ownerDocument);
    }

    @Test
    public void mapProceedingReferenceToSeriesRelatedItem() throws Exception {
        final String urn = "urn:nbn:de:swb:ch1-200300619";
        final String label = "isPartOf";
        final String link = "http://nbn-resolving.de/" + urn;
        createReferenceUrn(urn, label, "proceeding", null);

        runProcessor(processor);

        final Document ownerDocument = modsDocument.getMods().getDomNode().getOwnerDocument();
        XMLAssert.assertXpathExists("//mods:relatedItem[" +
                "@type='series'" +
                " and @xlink:href='" + link + "' ]", ownerDocument);
        XMLAssert.assertXpathExists("//mods:relatedItem/mods:identifier[@type='urn'" +
                " and text()='" + urn + "']", ownerDocument);
    }

    @Test
    public void mapIssueReferenceToConstituentRelatedItem() throws Exception {
        final String urn = "urn:nbn:de:bsz:14-qucosa-32825";
        final String link = "http://nbn-resolving.de/" + urn;
        final BigInteger sortOrder = BigInteger.valueOf(001);
        createReferenceUrn(urn, null, "issue", sortOrder);

        runProcessor(processor);

        final Document ownerDocument = modsDocument.getMods().getDomNode().getOwnerDocument();
        XMLAssert.assertXpathExists("//mods:relatedItem[" +
                "@type='constituent'" +
                " and @xlink:href='" + link + "' ]", ownerDocument);
        XMLAssert.assertXpathExists("//mods:relatedItem/mods:identifier[@type='urn'" +
                " and text()='" + urn + "']", ownerDocument);
    }

    private void createReferenceUrn(String urn, String label, String relation, BigInteger sortOrder) {
        Reference refUrl = opusDocument.getOpus().getOpusDocument().addNewReferenceUrn();
        refUrl.setValue(urn);
        refUrl.setLabel(label);
        refUrl.setRelation(relation);
        refUrl.setSortOrder(sortOrder);
    }


}
