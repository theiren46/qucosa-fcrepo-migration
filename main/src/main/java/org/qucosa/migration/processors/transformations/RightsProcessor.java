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
import de.slubDresden.InfoType;
import gov.loc.mods.v3.ModsDocument;
import noNamespace.OpusDocument;

public class RightsProcessor extends MappingProcessor {

    @Override
    public void process(OpusDocument opusDocument, ModsDocument modsDocument, InfoDocument infoDocument) throws Exception {
        final String vgwortOpenKey = vgwortEncoding(opusDocument.getOpus().getOpusDocument().getVgWortOpenKey());
        if (vgwortOpenKey != null && !vgwortOpenKey.isEmpty()) {
            InfoType info = infoDocument.getInfo();
            if (info.getVgwortOpenKey() == null
                    || !info.getVgwortOpenKey().equals(vgwortOpenKey)) {
                info.setVgwortOpenKey(vgwortOpenKey);
                signalChanges("SLUB-INFO");
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
