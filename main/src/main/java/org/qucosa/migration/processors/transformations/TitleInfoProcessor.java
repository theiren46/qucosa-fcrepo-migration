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

import gov.loc.mods.v3.ModsDocument;
import gov.loc.mods.v3.StringPlusLanguage;
import gov.loc.mods.v3.TitleInfoDefinition;
import noNamespace.OpusDocument;
import noNamespace.Title;

public class TitleInfoProcessor extends MappingProcessor {
    @Override
    public ModsDocument process(OpusDocument opusDocument, ModsDocument modsDocument) {
        TitleInfoDefinition titleInfo = modsDocument.getMods().addNewTitleInfo();

        // map TitleMain elements
        for (Title ot : opusDocument.getOpus().getOpusDocument().getTitleMainArray()) {
            StringPlusLanguage mt = titleInfo.addNewTitle();
            mt.setLang(ot.getLanguage());
            mt.setStringValue(ot.getValue());
        }

        // map TitleSub elements
        for (Title ot : opusDocument.getOpus().getOpusDocument().getTitleSubArray()) {
            StringPlusLanguage mt = titleInfo.addNewSubTitle();
            mt.setLang(ot.getLanguage());
            mt.setStringValue(ot.getValue());
        }

        return modsDocument;
    }
}
