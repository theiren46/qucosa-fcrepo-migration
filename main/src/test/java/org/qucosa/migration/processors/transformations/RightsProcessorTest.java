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

public class RightsProcessorTest extends ProcessorTestBase {

    final private MappingProcessor processor = new RightsProcessor();

    @Test
    public void extractsVgWortOpenKey() throws Exception {
        String vgWortOpenKey = "6fd9288e617c4721b6f25624167249f6";
        opusDocument.getOpus().getOpusDocument().setVgWortOpenKey(vgWortOpenKey);

        runProcessor(processor);

        XMLAssert.assertXpathExists(
                "//slub:vgwortOpenKey[text()='" + vgWortOpenKey + "']",
                infoDocument.getInfo().getDomNode().getOwnerDocument());
    }

    @Test
    public void filtersUrlPrefixes() throws Exception {
        String prefix = "http://vg04.met.vgwort.de/";
        String vgWortOpenKey = "6fd9288e617c4721b6f25624167249f6";
        opusDocument.getOpus().getOpusDocument().setVgWortOpenKey(prefix + vgWortOpenKey);

        runProcessor(processor);

        XMLAssert.assertXpathExists(
                "//slub:vgwortOpenKey[text()='" + vgWortOpenKey + "']",
                infoDocument.getInfo().getDomNode().getOwnerDocument());
    }

}