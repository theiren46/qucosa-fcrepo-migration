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

public class StaticInfoProcessorTest extends ProcessorTestBase {

    private MappingProcessor processor = new StaticInfoProcessor();

    @Test
    public void setsEdition() throws Exception {
        runProcessor(processor);

        XMLAssert.assertXpathExists(
                "//mods:originInfo[@eventType='distribution']/" +
                        "mods:edition[text()='[Electronic ed.]']",
                modsDocument.getMods().getDomNode().getOwnerDocument());
    }

    @Test
    public void setsPhysicalDescription() throws Exception {
        runProcessor(processor);

        XMLAssert.assertXpathExists(
                "//mods:physicalDescription/" +
                        "mods:digitalOrigin[text()='born digital']",
                modsDocument.getMods().getDomNode().getOwnerDocument());
    }

    @Test
    public void slubAgreementIsSetToYes() throws Exception {
        runProcessor(processor);

        XMLAssert.assertXpathExists(
                "//slub:rights/" +
                        "slub:agreement[@given='yes']",
                infoDocument.getInfo().getDomNode().getOwnerDocument());
    }

}
