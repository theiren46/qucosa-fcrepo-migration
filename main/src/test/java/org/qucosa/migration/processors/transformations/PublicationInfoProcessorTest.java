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
import noNamespace.Date;
import org.custommonkey.xmlunit.XMLAssert;
import org.junit.Test;

import java.math.BigInteger;

public class PublicationInfoProcessorTest extends ProcessorTestBase {

    private MappingProcessor processor = new PublicationInfoProcessor();

    @Test
    public void extractsLanguage() throws Exception {
        final String lang = "ger";
        inputOpusDocument.getOpus().getOpusDocument().addLanguage(lang);

        ModsDefinition outputMods = processor.process(inputOpusDocument, inputModsDocument).getMods();

        XMLAssert.assertXpathExists(
                "//mods:language[@usage='primary']/" +
                        "mods:languageTerm[@authority='iso639-2b' and @type='code' and text()='" + lang + "']",
                outputMods.getDomNode().getOwnerDocument());
    }

    @Test
    public void extractsCompletedDate() throws Exception {
        Date ocd = inputOpusDocument.getOpus().getOpusDocument().addNewCompletedDate();
        ocd.setYear(BigInteger.valueOf(2009));
        ocd.setMonth(BigInteger.valueOf(6));
        ocd.setDay(BigInteger.valueOf(4));
        ocd.setHour(BigInteger.valueOf(12));
        ocd.setMinute(BigInteger.valueOf(9));
        ocd.setSecond(BigInteger.valueOf(40));
        ocd.setTimezone("GMT-2");

        ModsDefinition outputMods = processor.process(inputOpusDocument, inputModsDocument).getMods();

        XMLAssert.assertXpathExists(
                "//mods:originInfo[@eventType='publication']/" +
                        "mods:dateOther[@encoding='iso8601' and @type='submission' and text()='2009-06-04']",
                outputMods.getDomNode().getOwnerDocument());
    }

    @Test
    public void extractsCompletedYear() throws Exception {
        inputOpusDocument.getOpus().getOpusDocument().setCompletedYear(BigInteger.valueOf(2009));

        ModsDefinition outputMods = processor.process(inputOpusDocument, inputModsDocument).getMods();

        XMLAssert.assertXpathExists(
                "//mods:originInfo[@eventType='publication']/" +
                        "mods:dateIssued[@encoding='iso8601' and text()='2009']",
                outputMods.getDomNode().getOwnerDocument());
    }

    @Test
    public void extractsEdition() throws Exception {
        inputOpusDocument.getOpus().getOpusDocument().setEdition("2nd. Edition");

        ModsDefinition outputMods = processor.process(inputOpusDocument, inputModsDocument).getMods();

        XMLAssert.assertXpathExists(
                "//mods:originInfo[@eventType='publication']/" +
                        "mods:edition[text()='2nd. Edition']",
                outputMods.getDomNode().getOwnerDocument());
    }

}
