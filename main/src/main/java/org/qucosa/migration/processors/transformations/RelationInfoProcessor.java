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
import gov.loc.mods.v3.RelatedItemDefinition.Type;
import noNamespace.Document;
import noNamespace.OpusDocument;
import noNamespace.Reference;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;

import javax.xml.namespace.QName;

public class RelationInfoProcessor extends ModsRelatedItemProcessor {

    private static final String NBN_RESOLVING_URL = "http://nbn-resolving.de/";

    @Override
    public void process(OpusDocument opusDocument, ModsDocument modsDocument, InfoDocument infoDocument) throws Exception {
        final Document opus = opusDocument.getOpus().getOpusDocument();
        final ModsDefinition mods = modsDocument.getMods();

        for (Reference r : opus.getReferenceUrnArray()) {
            final String urn = r.getValue();
            final String label = r.getLabel();
            final String partnum = (r.getSortOrder() == null) ? null : r.getSortOrder();
            final Type.Enum itemType = determineItemType(r.getRelation());

            RelatedItemDefinition rid = getRelatedItemDefinition(mods, label, itemType);
            setLabelIfdefined(label, rid);
            setHrefIfDefined(urn, rid);
            setIdentifierIfNotFound(urn, rid, "urn");
            setSortOrderIfDefined(partnum, rid);
        }
    }

    private void setHrefIfDefined(String urn, RelatedItemDefinition rid) {
        if (urn != null) {
            final QName qName = new QName(NS_XLINK, "href");
            XmlObject href = rid.selectAttribute(qName);
            if (href == null
                    || href.getDomNode().getNodeValue() == null
                    || !href.getDomNode().getNodeValue().equals(urn)) {
                XmlCursor cursor = rid.newCursor();
                cursor.setAttributeText(qName, NBN_RESOLVING_URL + urn);
                cursor.dispose();
                signalChanges(MODS_CHANGES);
            }
        }
    }

    private Type.Enum determineItemType(String relation) {
        switch (relation) {
            case "series":
            case "journal":
            case "proceeding":
                return Type.SERIES;
            case "issue":
                return Type.CONSTITUENT;
            case "predecessor":
                return Type.PRECEDING;
            default:
                return Type.REFERENCES;
        }
    }

}
