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
import org.apache.xmlbeans.XmlString;

import javax.xml.xpath.XPathExpressionException;

import static gov.loc.mods.v3.CodeOrText.CODE;
import static gov.loc.mods.v3.DateDefinition.Encoding.ISO_8601;
import static gov.loc.mods.v3.LanguageTermDefinition.Authority.ISO_639_2_B;

public class PublicationInfoProcessor extends MappingProcessor {
    @Override
    public void process(OpusDocument opusDocument, ModsDocument modsDocument, InfoDocument infoDocument) throws Exception {
        final Document opus = opusDocument.getOpus().getOpusDocument();
        final ModsDefinition mods = modsDocument.getMods();

        mapLanguageElement(opus, mods);
        mapOriginInfoElements(opus, mods);
    }

    private void mapOriginInfoElements(Document opus, ModsDefinition mods) throws XPathExpressionException {
        final Boolean hasCompletedDate = nodeExists("CompletedDate", opus);
        final Boolean hasCompletedYear = nodeExists("CompletedYear", opus);
        final Boolean hasDateAccepted = nodeExists("DateAccepted", opus);
        final Boolean hasEdition = nodeExists("Edition", opus);
        if (hasCompletedDate || hasCompletedYear || hasDateAccepted || hasEdition) {

            OriginInfoDefinition oid = (OriginInfoDefinition)
                    select("mods:originInfo[@eventType='publication']", mods);

            if (oid == null) {
                oid = mods.addNewOriginInfo();
                oid.setEventType("publication");
                signalChanges("MODS");
            }

            if (hasCompletedDate) mapCompletedDate(opus, oid);
            if (hasCompletedYear) mapCompletedYear(opus, oid);
            if (hasDateAccepted) mapDateAccepted(opus, oid);
            if (hasEdition) mapEdition(opus, oid);

        }
    }

    private void mapDateAccepted(Document opus, OriginInfoDefinition oid) {
        final String mappedDateEncoding = dateEncoding(opus.getDateAccepted());

        DateOtherDefinition dateOther = (DateOtherDefinition)
                select(String.format("mods:dateOther[@encoding='%s' and @type='%s']",
                        "iso8601", "defense"), oid);

        if (dateOther == null) {
            dateOther = oid.addNewDateOther();
            dateOther.setEncoding(ISO_8601);
            dateOther.setType("defense");
            signalChanges("MODS");
        }

        if (!dateOther.getStringValue().equals(mappedDateEncoding)) {
            dateOther.setStringValue(mappedDateEncoding);
            signalChanges("MODS");
        }
    }

    private void mapEdition(Document opus, OriginInfoDefinition oid) {
        String opusEdition = opus.getEdition();

        XmlString edition = (XmlString)
                select("mods:edition", oid);

        if (edition == null) {
            edition = oid.addNewEdition();
            signalChanges("MODS");
        }

        if (!edition.getStringValue().equals(opusEdition)) {
            edition.setStringValue(opusEdition);
            signalChanges("MODS");
        }
    }

    private void mapCompletedYear(Document opus, OriginInfoDefinition oid) {
        final String mappedCompletedYear = dateEncoding(opus.getCompletedYear());

        DateDefinition dateIssuedDefinition = (DateDefinition)
                select("mods:dateIssued[@encoding='iso8601']", oid);

        if (dateIssuedDefinition == null) {
            dateIssuedDefinition = oid.addNewDateIssued();
            dateIssuedDefinition.setEncoding(ISO_8601);
            signalChanges("MODS");
        }

        if (!dateIssuedDefinition.getStringValue().equals(mappedCompletedYear)) {
            dateIssuedDefinition.setStringValue(mappedCompletedYear);
            signalChanges("MODS");
        }
    }

    private void mapCompletedDate(Document opus, OriginInfoDefinition oid) {
        final String mappedDateEncoding = dateEncoding(opus.getCompletedDate());

        DateOtherDefinition dateOther = (DateOtherDefinition)
                select(String.format("mods:dateOther[@encoding='%s' and @type='%s']",
                        "iso8601", "submission"), oid);

        if (dateOther == null) {
            dateOther = oid.addNewDateOther();
            dateOther.setEncoding(ISO_8601);
            dateOther.setType("submission");
            signalChanges("MODS");
        }

        if (!dateOther.getStringValue().equals(mappedDateEncoding)) {
            dateOther.setStringValue(mappedDateEncoding);
            signalChanges("MODS");
        }
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
                signalChanges("MODS");
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
                signalChanges("MODS");
            }
        }
    }
}
