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
import gov.loc.mods.v3.*;
import noNamespace.Document;
import noNamespace.OpusDocument;
import org.apache.xmlbeans.XmlString;

import static gov.loc.mods.v3.CodeOrText.TEXT;
import static gov.loc.mods.v3.DateDefinition.Encoding.ISO_8601;

public class DistributionInfoProcessor extends MappingProcessor {
    @Override
    public void process(OpusDocument opusDocument, ModsDocument modsDocument, InfoDocument infoDocument) throws Exception {
        final Document opus = opusDocument.getOpus().getOpusDocument();
        final ModsDefinition mods = modsDocument.getMods();

        final Boolean hasPublisherName = nodeExists("PublisherName[text()!='']", opus);
        final Boolean hasPublisherPlace = nodeExists("PublisherPlace[text()!='']", opus);
        final Boolean hasServerDatePublished = nodeExists("ServerDatePublished", opus);

        if (hasPublisherName || hasPublisherPlace || hasServerDatePublished) {
            OriginInfoDefinition oid = (OriginInfoDefinition)
                    select("mods:originInfo[@eventType='distribution']", mods);

            if (oid == null) {
                oid = mods.addNewOriginInfo();
                oid.setEventType("distribution");
                signalChanges(MODS_CHANGES);
            }

            if (hasPublisherName) mapPublisherName(opus, oid);
            if (hasPublisherPlace) mapPublisherPlace(opus, oid);
            if (hasServerDatePublished) mapServerDatePublished(opus, oid);

        }
    }

    private void mapServerDatePublished(Document opus, OriginInfoDefinition oid) {
        final String mappedDateEncoding = dateEncoding(opus.getServerDatePublished());

        DateDefinition dateIssued = (DateDefinition)
                select(String.format("mods:dateIssued[@encoding='%s' and @keyDate='%s']",
                        "iso8601", "yes"), oid);

        if (dateIssued == null) {
            dateIssued = oid.addNewDateIssued();
            dateIssued.setEncoding(ISO_8601);
            dateIssued.setKeyDate(XmlString.Factory.newValue("yes"));
            signalChanges(MODS_CHANGES);
        }

        if (!dateIssued.getStringValue().equals(mappedDateEncoding)) {
            dateIssued.setStringValue(mappedDateEncoding);
            signalChanges(MODS_CHANGES);
        }
    }

    private void mapPublisherPlace(Document opus, OriginInfoDefinition oid) {
        final String publisherPlace = opus.getPublisherPlace();

        PlaceDefinition pd = (PlaceDefinition)
                select("mods:place", oid);

        if (pd == null) {
            pd = oid.addNewPlace();
            signalChanges(MODS_CHANGES);
        }

        PlaceTermDefinition ptd = (PlaceTermDefinition)
                select("mods:placeTerm[@type='text' and text()='" + singleline(publisherPlace) + "']", pd);

        if (ptd == null) {
            ptd = pd.addNewPlaceTerm();
            ptd.setType(TEXT);
            ptd.setStringValue(publisherPlace);
            signalChanges(MODS_CHANGES);
        }
    }

    private void mapPublisherName(Document opus, OriginInfoDefinition oid) {
        final String publisherName = opus.getPublisherName();

        XmlString modsPublisher = (XmlString)
                select("mods:publisher", oid);

        if (modsPublisher == null) {
            modsPublisher = oid.addNewPublisher();
            signalChanges(MODS_CHANGES);
        }

        if (!modsPublisher.getStringValue().equals(publisherName)) {
            modsPublisher.setStringValue(publisherName);
            signalChanges(MODS_CHANGES);
        }
    }
}
