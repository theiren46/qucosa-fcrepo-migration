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
import gov.loc.mods.v3.RelatedItemDefinition;
import noNamespace.Document;
import noNamespace.OpusDocument;
import noNamespace.Reference;

public class SourcesInfoProcessor extends ModsRelatedItemProcessor {

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
            final String partnum = (r.getSortOrder() == null) ? null : r.getSortOrder();

            RelatedItemDefinition rid = getRelatedItemDefinition(mods, label, RelatedItemDefinition.Type.ORIGINAL);
            setLabelIfdefined(label, rid);
            setIdentifierIfNotFound(uri, rid, type);
            setSortOrderIfDefined(partnum, rid);
        }
    }

}
