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

}
