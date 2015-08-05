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

import noNamespace.Date;
import org.custommonkey.xmlunit.XMLAssert;
import org.junit.Test;

import java.math.BigInteger;

public class DistributionInfoProcessorTest extends ProcessorTestBase {

    private MappingProcessor processor = new DistributionInfoProcessor();

    @Test
    public void extractsPublisherName() throws Exception {
        final String publisher = "Universitätsbibliothek Leipzig";
        opusDocument.getOpus().getOpusDocument().setPublisherName(publisher);

        runProcessor(processor);

        XMLAssert.assertXpathExists(
                "//mods:originInfo[@eventType='distribution']/" +
                        "mods:publisher[text()='" + publisher + "']",
                modsDocument.getMods().getDomNode().getOwnerDocument());
    }

    @Test
    public void extractsPublisherPlace() throws Exception {
        final String place = "Leipzig";
        opusDocument.getOpus().getOpusDocument().setPublisherPlace(place);

        runProcessor(processor);

        XMLAssert.assertXpathExists(
                "//mods:originInfo[@eventType='distribution']/mods:place/" +
                        "mods:placeTerm[@type='text' and text()='" + place + "']",
                modsDocument.getMods().getDomNode().getOwnerDocument());
    }

    @Test
    public void extractsServerDatePublished() throws Exception {
        Date sdp = opusDocument.getOpus().getOpusDocument().addNewServerDatePublished();
        sdp.setYear(BigInteger.valueOf(2009));
        sdp.setMonth(BigInteger.valueOf(6));
        sdp.setDay(BigInteger.valueOf(4));
        sdp.setHour(BigInteger.valueOf(12));
        sdp.setMinute(BigInteger.valueOf(9));
        sdp.setSecond(BigInteger.valueOf(40));
        sdp.setTimezone("GMT-2");

        runProcessor(processor);

        XMLAssert.assertXpathExists(
                "//mods:originInfo[@eventType='distribution']/" +
                        "mods:dateIssued[@encoding='iso8601' and @keyDate='yes' and text()='2009-06-04']",
                modsDocument.getMods().getDomNode().getOwnerDocument());
    }

}
