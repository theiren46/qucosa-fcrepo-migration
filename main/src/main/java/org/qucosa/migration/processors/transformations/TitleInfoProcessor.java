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
import gov.loc.mods.v3.TitleInfoDefinition.Type;
import noNamespace.OpusDocument;
import noNamespace.Title;
import org.apache.xmlbeans.XmlString;

import javax.xml.xpath.XPathExpressionException;

public class TitleInfoProcessor extends MappingProcessor {
    @Override
    public void process(OpusDocument opusDocument, ModsDocument modsDocument, InfoDocument infoDocument) throws Exception {
        ModsDefinition modsDefinition = modsDocument.getMods();
        mapTitleMainElements(opusDocument, modsDefinition);
        mapTitleSubElements(opusDocument, modsDefinition);
        mapTitleAlternativeElements(opusDocument, modsDefinition);
        mapTitleParentElements(opusDocument, modsDefinition);
    }

    private TitleInfoDefinition ensureTitleInfoElement(ModsDefinition modsDefinition, String lang) {
        TitleInfoDefinition titleInfoDefinition =
                (TitleInfoDefinition) select("mods:titleInfo[@lang='" + lang + "']", modsDefinition);

        if (titleInfoDefinition == null) {
            titleInfoDefinition = modsDefinition.addNewTitleInfo();
            titleInfoDefinition.setLang(lang);
            signalChanges(MODS_CHANGES);
        }

        return titleInfoDefinition;
    }

    private TitleInfoDefinition ensureTitleInfoElement(RelatedItemDefinition relatedItemDefinition, String lang) {
        TitleInfoDefinition titleInfoDefinition =
                (TitleInfoDefinition) select("mods:titleInfo[@lang='" + lang + "']", relatedItemDefinition);

        if (titleInfoDefinition == null) {
            titleInfoDefinition = relatedItemDefinition.addNewTitleInfo();
            titleInfoDefinition.setLang(lang);
            signalChanges(MODS_CHANGES);
        }

        return titleInfoDefinition;
    }

    private TitleInfoDefinition ensureTitleInfoElement(ModsDefinition modsDefinition, String lang, Type.Enum type) {
        TitleInfoDefinition titleInfoDefinition =
                (TitleInfoDefinition) select("mods:titleInfo[@lang='" + lang + "' and @type='" + type + "']",
                        modsDefinition);

        if (titleInfoDefinition == null) {
            titleInfoDefinition = modsDefinition.addNewTitleInfo();
            titleInfoDefinition.setType(type);
            titleInfoDefinition.setLang(lang);
            signalChanges(MODS_CHANGES);
        }

        return titleInfoDefinition;
    }

    private void mapTitleAlternativeElements(OpusDocument opusDocument, ModsDefinition modsDefinition)
            throws XPathExpressionException {

        for (Title ot : opusDocument.getOpus().getOpusDocument().getTitleAlternativeArray()) {
            final String encLang = languageEncoding(ot.getLanguage());

            TitleInfoDefinition tid = ensureTitleInfoElement(modsDefinition, encLang, Type.ALTERNATIVE);

            if (!nodeExists("mods:title[text()='" + singleline(ot.getValue()) + "']", tid)) {
                StringPlusLanguage mt = tid.addNewTitle();
                mt.setStringValue(ot.getValue());
                signalChanges(MODS_CHANGES);
            }
        }
    }

    private void mapTitleParentElements(OpusDocument opusDocument, ModsDefinition modsDefinition)
            throws XPathExpressionException {

        if (nodeExists("TitleParent", opusDocument.getOpus().getOpusDocument())) {

            RelatedItemDefinition relatedItemDefinition =
                    (RelatedItemDefinition) select("mods:relatedItem[@type='series']", modsDefinition);

            if (relatedItemDefinition == null) {
                relatedItemDefinition = modsDefinition.addNewRelatedItem();
                relatedItemDefinition.setType(RelatedItemDefinition.Type.SERIES);
                signalChanges(MODS_CHANGES);
            }

            for (Title ot : opusDocument.getOpus().getOpusDocument().getTitleParentArray()) {
                final String encLang = languageEncoding(ot.getLanguage());

                TitleInfoDefinition tid = ensureTitleInfoElement(relatedItemDefinition, encLang);

                if (!nodeExists("mods:title[text()='" + singleline(ot.getValue()) + "']", tid)) {
                    StringPlusLanguage mt = tid.addNewTitle();
                    mt.setStringValue(ot.getValue());
                    signalChanges(MODS_CHANGES);
                }
            }
        }
    }

    private void mapTitleSubElements(OpusDocument opusDocument, ModsDefinition modsDefinition)
            throws XPathExpressionException {

        for (Title ot : opusDocument.getOpus().getOpusDocument().getTitleSubArray()) {
            final String encLang = languageEncoding(ot.getLanguage());

            TitleInfoDefinition tid = ensureTitleInfoElement(modsDefinition, encLang);

            if (!nodeExists("mods:subTitle[text()='" + singleline(ot.getValue()) + "']", tid)) {
                StringPlusLanguage mt = tid.addNewSubTitle();
                mt.setStringValue(ot.getValue());
                signalChanges(MODS_CHANGES);
            }
        }
    }

    private void mapTitleMainElements(OpusDocument opusDocument, ModsDefinition modsDefinition)
            throws XPathExpressionException {

        for (Title ot : opusDocument.getOpus().getOpusDocument().getTitleMainArray()) {
            String encLang = languageEncoding(ot.getLanguage());

            TitleInfoDefinition tid = ensureTitleInfoElement(modsDefinition, encLang);
            tid.setUsage(XmlString.Factory.newValue("primary"));

            if (!nodeExists("mods:title[text()='" + singleline(ot.getValue()) + "']", tid)) {
                StringPlusLanguage mt = tid.addNewTitle();
                mt.setStringValue(ot.getValue());
                signalChanges(MODS_CHANGES);
            }
        }
    }

}
