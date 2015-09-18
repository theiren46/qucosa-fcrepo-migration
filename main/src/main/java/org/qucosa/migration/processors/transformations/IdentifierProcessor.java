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
import gov.loc.mods.v3.IdentifierDefinition;
import gov.loc.mods.v3.ModsDefinition;
import gov.loc.mods.v3.ModsDocument;
import noNamespace.Document;
import noNamespace.Identifier;
import noNamespace.OpusDocument;
import org.apache.xmlbeans.XmlObject;

import javax.xml.xpath.XPathExpressionException;

public class IdentifierProcessor extends MappingProcessor {
    @Override
    public void process(OpusDocument opusDocument, ModsDocument modsDocument, InfoDocument infoDocument) throws Exception {
        final Document opus = opusDocument.getOpus().getOpusDocument();
        final ModsDefinition mods = modsDocument.getMods();

        extractOpusId(opus, mods);

        String[] ns = {"Isbn", "Urn", "Doi", "Issn", "Ppn"};
        for (String n : ns) map(n, opus, mods);
    }

    private void extractOpusId(Document opus, ModsDefinition mods) {
        String opusId = opus.getDocumentId();
        ensureIdentifierElement("opus", opusId, mods);
    }

    private void map(String type, Document opusDocument, ModsDefinition mods) throws XPathExpressionException {
        for (XmlObject xmlObject : selectAll("Identifier" + type, opusDocument)) {
            Identifier oid = (Identifier) xmlObject;
            final String oidValue = oid.getValue();
            ensureIdentifierElement(type, oidValue, mods);
        }
    }

    private void ensureIdentifierElement(String type, String id, ModsDefinition mods) {
        if (id != null && !nodeExists(
                String.format("mods:identifier[@type='%s' and text()='%s']", type.toLowerCase(), qq(id)),
                mods)) {
            IdentifierDefinition identifierDefinition = mods.addNewIdentifier();
            identifierDefinition.setType(type.toLowerCase());
            identifierDefinition.setStringValue(id);
            signalChanges(MODS_CHANGES);
        }
    }
}
