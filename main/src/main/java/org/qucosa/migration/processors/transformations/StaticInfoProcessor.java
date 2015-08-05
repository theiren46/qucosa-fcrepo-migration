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

import de.slubDresden.InfoDocument;
import gov.loc.mods.v3.ModsDefinition;
import gov.loc.mods.v3.ModsDocument;
import gov.loc.mods.v3.OriginInfoDefinition;
import gov.loc.mods.v3.PhysicalDescriptionDefinition;
import noNamespace.OpusDocument;

import javax.xml.xpath.XPathExpressionException;

import static gov.loc.mods.v3.DigitalOriginDefinition.BORN_DIGITAL;

public class StaticInfoProcessor extends MappingProcessor {
    @Override
    public void process(OpusDocument opusDocument, ModsDocument modsDocument, InfoDocument infoDocument) throws Exception {
        final ModsDefinition mods = modsDocument.getMods();

        ensureEdition(mods);
        ensurePhysicalDescription(mods);
    }

    private void ensurePhysicalDescription(ModsDefinition mods) throws XPathExpressionException {
        PhysicalDescriptionDefinition pdd = (PhysicalDescriptionDefinition)
                select("mods:physicalDescription", mods);

        if (pdd == null) {
            pdd = mods.addNewPhysicalDescription();
            signalChanges("MODS");
        }

        if (!nodeExists("mods:digitalOrigin", pdd)) {
            pdd.addDigitalOrigin(BORN_DIGITAL);
            signalChanges("MODS");
        }
    }

    private void ensureEdition(ModsDefinition mods) throws XPathExpressionException {
        OriginInfoDefinition oid = (OriginInfoDefinition)
                select("mods:originInfo[@eventType='distribution']", mods);

        if (oid == null) {
            oid = mods.addNewOriginInfo();
            oid.setEventType("distribution");
            signalChanges("MODS");
        }

        if (!nodeExists("mods:edition[text()='[Electronic ed.]']", oid)) {
            oid.addNewEdition().setStringValue("[Electronic ed.]");
            signalChanges("MODS");
        }
    }
}
