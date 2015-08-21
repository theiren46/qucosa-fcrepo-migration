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
import noNamespace.Reference;
import org.apache.xmlbeans.XmlString;

import static gov.loc.mods.v3.RelatedItemDefinition.Type.ORIGINAL;

public class SourcesInfoProcessor extends MappingProcessor {
    @Override
    public void process(OpusDocument opusDocument, ModsDocument modsDocument, InfoDocument infoDocument) throws Exception {
        final Document opus = opusDocument.getOpus().getOpusDocument();
        final ModsDefinition mods = modsDocument.getMods();

        mapReferenceElements(mods, opus.getReferenceUrlArray(), "uri");
        mapReferenceElements(mods, opus.getReferenceIsbnArray(), "isbn");
        mapReferenceElements(mods, opus.getReferenceIssnArray(), "issn");
    }

    private void mapReferenceElements(ModsDefinition mods, Reference[] references, String type) {
        for (Reference r : references) {
            final String uri = r.getValue();
            final String label = r.getLabel();
            final String partnum = (r.getSortOrder() == null) ? null : r.getSortOrder().toString();

            RelatedItemDefinition rid = getRelatedItemDefinition(mods, label);
            setLabelIfdefined(label, rid);
            setIdentifierIfNotFound(uri, rid, type);
            setSortOrderIfDefined(partnum, rid);
        }
    }

    private void setSortOrderIfDefined(String partNumber, RelatedItemDefinition rid) {
        TitleInfoDefinition tid = (TitleInfoDefinition)
                select("mods:titleInfo", rid);
        if (tid == null) {
            tid = rid.addNewTitleInfo();
            signalChanges(MODS_CHANGES);
        }

        XmlString xs = (XmlString) select("mods:partNumber", tid);
        if (xs == null) {
            xs = tid.addNewPartNumber();
            xs.setStringValue(partNumber);
            signalChanges(MODS_CHANGES);
        }
    }

    private void setIdentifierIfNotFound(String uri, RelatedItemDefinition rid, final String type) {
        IdentifierDefinition id = (IdentifierDefinition)
                select("mods:identifier[@type='" + type + "' and text()='" + uri + "']", rid);
        if (id == null) {
            id = rid.addNewIdentifier();
            id.setType(type);
            id.setStringValue(uri);
            signalChanges(MODS_CHANGES);
        }
    }

    private void setLabelIfdefined(String label, RelatedItemDefinition rid) {
        if (label != null) {
            if (!label.equals(rid.getDisplayLabel())) {
                rid.setDisplayLabel(label);
                signalChanges(MODS_CHANGES);
            }
        }
    }

    private RelatedItemDefinition getRelatedItemDefinition(ModsDefinition mods, String label) {
        final String query = (label == null || label.isEmpty()) ?
                "mods:relatedItem[@type='original']" :
                "mods:relatedItem[@type='original' and @displayLabel='" + label + "']";

        RelatedItemDefinition rid = (RelatedItemDefinition)
                select(query, mods);
        if (rid == null) {
            rid = mods.addNewRelatedItem();
            rid.setType(ORIGINAL);
            signalChanges(MODS_CHANGES);
        }
        return rid;
    }
}
