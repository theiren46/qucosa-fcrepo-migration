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

import de.slubDresden.*;
import gov.loc.mods.v3.ModsDocument;
import noNamespace.OpusDocument;
import org.apache.xmlbeans.XmlObject;

import java.util.ArrayList;

import static de.slubDresden.YesNo.YES;

public class RightsProcessor extends MappingProcessor {

    @Override
    public void process(OpusDocument opusDocument, ModsDocument modsDocument, InfoDocument infoDocument) throws Exception {
        mapVgWortopenKey(opusDocument, infoDocument);
        mapFileAttachments(opusDocument, infoDocument);
    }

    private void mapFileAttachments(OpusDocument opusDocument, InfoDocument infoDocument) {
        RightsType rights = infoDocument.getInfo().getRights();
        if (rights == null) {
            rights = infoDocument.getInfo().addNewRights();
            signalChanges(SLUB_INFO_CHANGES);
        }

        final ArrayList<String> existingAttachmentRefs = new ArrayList<>();
        for (XmlObject o : selectAll("slub:attachment", rights)) {
            final AttachmentType eat = (AttachmentType) o;
            existingAttachmentRefs.add(eat.getRef());
        }

        final ArrayList<String> processedAttachmentRefs = new ArrayList<>();

        int i = 0;
        for (noNamespace.File opusFile : opusDocument.getOpus().getOpusDocument().getFileArray()) {
            final String ref = "ATT-" + i;
            final YesNo.Enum hasArchivalValue = yesNoBooleanMapping(opusFile.getOaiExport());
            final YesNo.Enum isDownloadable = yesNoBooleanMapping(opusFile.getFrontdoorVisible());
            final YesNo.Enum isRedistributable = YES;

            final String query = String.format("slub:attachment[@ref='%s']", ref);
            AttachmentType at = (AttachmentType) select(query, rights);
            if (at == null) {
                at = rights.addNewAttachment();
                signalChanges(SLUB_INFO_CHANGES);
            }

            processedAttachmentRefs.add(ref);

            if (at.getRef() == null || !at.getRef().equals(ref)) {
                at.setRef(ref);
                signalChanges(SLUB_INFO_CHANGES);
            }
            if (at.getHasArchivalValue() == null || !at.getHasArchivalValue().equals(hasArchivalValue)) {
                at.setHasArchivalValue(hasArchivalValue);
                signalChanges(SLUB_INFO_CHANGES);
            }
            if (at.getIsDownloadable() == null || !at.getIsDownloadable().equals(isDownloadable)) {
                at.setIsDownloadable(isDownloadable);
                signalChanges(SLUB_INFO_CHANGES);
            }
            if (at.getIsRedistributable() == null || !at.getIsRedistributable().equals(isRedistributable)) {
                at.setIsRedistributable(isRedistributable);
                signalChanges(SLUB_INFO_CHANGES);
            }

            i++;
        }

        existingAttachmentRefs.removeAll(processedAttachmentRefs);
        for (int j = 0; j < rights.getAttachmentArray().length; j++) {
            if (existingAttachmentRefs.contains(rights.getAttachmentArray(j).getRef())) {
                rights.removeAttachment(j);
            }
        }
    }

    private void mapVgWortopenKey(OpusDocument opusDocument, InfoDocument infoDocument) {
        final String vgwortOpenKey = opusDocument.getOpus().getOpusDocument().getVgWortOpenKey();
        if (vgwortOpenKey != null && !vgwortOpenKey.isEmpty()) {
            final String encodedVgWortOpenKey = vgwortEncoding(vgwortOpenKey);
            InfoType info = infoDocument.getInfo();

            if (info.getVgwortOpenKey() == null
                    || !info.getVgwortOpenKey().equals(encodedVgWortOpenKey)) {
                info.setVgwortOpenKey(encodedVgWortOpenKey);
                signalChanges(SLUB_INFO_CHANGES);
            }
        }
    }

    private String vgwortEncoding(String vgWortOpenKey) {
        if (vgWortOpenKey.startsWith("http")) {
            return vgWortOpenKey.substring(
                    vgWortOpenKey.lastIndexOf('/') + 1,
                    vgWortOpenKey.length());
        } else {
            return vgWortOpenKey;
        }
    }
}
