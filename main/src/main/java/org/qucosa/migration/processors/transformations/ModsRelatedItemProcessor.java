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

import gov.loc.mods.v3.IdentifierDefinition;
import gov.loc.mods.v3.ModsDefinition;
import gov.loc.mods.v3.RelatedItemDefinition;
import gov.loc.mods.v3.RelatedItemDefinition.Type;
import gov.loc.mods.v3.TitleInfoDefinition;
import org.apache.xmlbeans.XmlString;

public abstract class ModsRelatedItemProcessor extends MappingProcessor {

    protected void setSortOrderIfDefined(String partNumber, RelatedItemDefinition rid) {
        if (partNumber != null) {
            TitleInfoDefinition tid = (TitleInfoDefinition)
                    select("mods:titleInfo", rid);
            if (tid == null) {
                tid = rid.addNewTitleInfo();
                signalChanges(SourcesInfoProcessor.MODS_CHANGES);
            }

            XmlString xs = (XmlString) select("mods:partNumber", tid);
            if (xs == null) {
                xs = tid.addNewPartNumber();
                xs.setStringValue(partNumber);
                signalChanges(SourcesInfoProcessor.MODS_CHANGES);
            }
        }
    }

    protected void setIdentifierIfNotFound(String uri, RelatedItemDefinition rid, final String type) {
        IdentifierDefinition id = (IdentifierDefinition)
                select("mods:identifier[@type='" + type + "' and text()='" + uri + "']", rid);
        if (id == null) {
            id = rid.addNewIdentifier();
            id.setType(type);
            id.setStringValue(uri);
            signalChanges(SourcesInfoProcessor.MODS_CHANGES);
        }
    }

    protected void setLabelIfdefined(String label, RelatedItemDefinition rid) {
        if (label != null) {
            if (!label.equals(rid.getDisplayLabel())) {
                rid.setDisplayLabel(label);
                signalChanges(SourcesInfoProcessor.MODS_CHANGES);
            }
        }
    }

    protected RelatedItemDefinition getRelatedItemDefinition(ModsDefinition mods, String label, Type.Enum type) {
        final String query = (label == null || label.isEmpty()) ?
                "mods:relatedItem[@type='" + type + "']" :
                "mods:relatedItem[@type='" + type + "' and @displayLabel='" + singleline(label) + "']";

        RelatedItemDefinition rid = (RelatedItemDefinition)
                select(query, mods);
        if (rid == null) {
            rid = mods.addNewRelatedItem();
            rid.setType(type);
            signalChanges(SourcesInfoProcessor.MODS_CHANGES);
        }
        return rid;
    }

}
