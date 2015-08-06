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
                "//mods:abstract[@lang='ger' and @type='content' and text()='" + value + "']",
                modsDocument.getMods().getDomNode().getOwnerDocument());
    }

}