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

import gov.loc.mods.v3.LanguageDefinition;
import gov.loc.mods.v3.LanguageTermDefinition;
import gov.loc.mods.v3.ModsDefinition;
import gov.loc.mods.v3.ModsDocument;
import noNamespace.Document;
import noNamespace.OpusDocument;

import javax.xml.xpath.XPathExpressionException;

import static gov.loc.mods.v3.CodeOrText.CODE;
import static gov.loc.mods.v3.LanguageTermDefinition.Authority.ISO_639_2_B;

public class PublicationInfoProcessor extends MappingProcessor {
    @Override
    public ModsDocument process(OpusDocument opusDocument, ModsDocument modsDocument) throws Exception {
        final Document opus = opusDocument.getOpus().getOpusDocument();
        final ModsDefinition mods = modsDocument.getMods();

        mapLanguageElement(opus, mods);

        return modsDocument;
    }

    private void mapLanguageElement(Document opus, ModsDefinition mods) throws XPathExpressionException {
        String languageText = null;
        if (opus.getLanguageArray().length > 0) {
            languageText = opus.getLanguageArray(0);
        }

        if (languageText != null) {
            languageText = languageEncoding(languageText);

            LanguageDefinition ld = (LanguageDefinition)
                    select("mods:language[@usage='primary']", mods);
            if (ld == null) {
                ld = mods.addNewLanguage();
                ld.addNewUsage().setStringValue("primary");
                signalChanges();
            }

            if (!nodeExists(
                    String.format("mods:languageTerm[@authority='%s' and @type='%s' and text()='%s']",
                            "iso639-2b", "code", languageText),
                    ld
            )) {
                LanguageTermDefinition lngtd = ld.addNewLanguageTerm();
                lngtd.setAuthority(ISO_639_2_B);
                lngtd.setType(CODE);
                lngtd.setStringValue(languageText);
                signalChanges();
            }
        }
    }
}
