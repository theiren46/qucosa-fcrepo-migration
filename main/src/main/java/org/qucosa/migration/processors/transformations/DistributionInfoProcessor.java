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
import gov.loc.mods.v3.ModsDocument;
import gov.loc.mods.v3.OriginInfoDefinition;
import noNamespace.Document;
import noNamespace.OpusDocument;
import org.apache.xmlbeans.XmlString;

public class DistributionInfoProcessor extends MappingProcessor {
    @Override
    public ModsDocument process(OpusDocument opusDocument, ModsDocument modsDocument) throws Exception {
        final Document opus = opusDocument.getOpus().getOpusDocument();
        final ModsDefinition mods = modsDocument.getMods();

        final Boolean hasPublisherName = nodeExists("PublisherName[text()!='']", opus);

        if (hasPublisherName) {
            OriginInfoDefinition oid = (OriginInfoDefinition)
                    select("mods:originInfo[@eventType='distribution']", mods);

            if (oid == null) {
                oid = mods.addNewOriginInfo();
                oid.setEventType("distribution");
                signalChanges();
            }

            if (hasPublisherName) mapPublisherName(opus, oid);

        }

        return modsDocument;
    }

    private void mapPublisherName(Document opus, OriginInfoDefinition oid) {
        final String publisherName = opus.getPublisherName();

        XmlString modsPublisher = (XmlString)
                select("mods:publisher", oid);

        if (modsPublisher == null) {
            modsPublisher = oid.addNewPublisher();
            signalChanges();
        }

        if (!modsPublisher.getStringValue().equals(publisherName)) {
            modsPublisher.setStringValue(publisherName);
            signalChanges();
        }
    }
}
