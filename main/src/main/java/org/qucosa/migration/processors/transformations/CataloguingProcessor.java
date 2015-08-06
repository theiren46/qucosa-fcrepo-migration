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
import gov.loc.mods.v3.AbstractDefinition;
import gov.loc.mods.v3.ModsDefinition;
import gov.loc.mods.v3.ModsDocument;
import noNamespace.Document;
import noNamespace.OpusDocument;
import noNamespace.Title;

public class CataloguingProcessor extends MappingProcessor {
    @Override
    public void process(OpusDocument opusDocument, ModsDocument modsDocument, InfoDocument infoDocument) throws Exception {
        Document opus = opusDocument.getOpus().getOpusDocument();
        ModsDefinition mods = modsDocument.getMods();

        for (Title ot : opus.getTitleAbstractArray()) {
            final String lang = languageEncoding(ot.getLanguage());
            final String abst = ot.getValue();

            AbstractDefinition ad = (AbstractDefinition) select(
                    String.format("mods:abstract[@lang='%s' and @type='%s' and text()='%s']",
                            lang, "content", abst), mods);

            if (ad == null) {
                ad = mods.addNewAbstract();
                ad.setLang(lang);
                ad.setType("content");
                ad.setStringValue(abst);
                signalChanges("MODS");
            }
        }
    }
}
