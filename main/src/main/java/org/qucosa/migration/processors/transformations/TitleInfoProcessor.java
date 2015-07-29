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

import gov.loc.mods.v3.*;
import noNamespace.OpusDocument;
import noNamespace.Title;

import javax.xml.xpath.XPathExpressionException;

public class TitleInfoProcessor extends MappingProcessor {
    @Override
    public ModsDocument process(OpusDocument opusDocument, ModsDocument modsDocument) throws Exception {
        ModsDefinition modsDefinition = modsDocument.getMods();
        mapTitleElements(opusDocument, modsDefinition);
        mapTitleAlternativeElements(opusDocument, modsDefinition);
        mapTitleParentElements(opusDocument, modsDefinition);
        return modsDocument;
    }

    private void mapTitleElements(OpusDocument opusDocument, ModsDefinition modsDefinition)
            throws XPathExpressionException {

        if ((nodeExists("TitleMain", opusDocument.getOpus().getOpusDocument())
                || nodeExists("TitleSub", opusDocument.getOpus().getOpusDocument()))) {

            TitleInfoDefinition titleInfoDefinition =
                    (TitleInfoDefinition) select("mods:titleInfo", modsDefinition);

            if (titleInfoDefinition == null) {
                titleInfoDefinition = modsDefinition.addNewTitleInfo();
                signalChanges();
            }

            mapTitleSubElements(opusDocument, titleInfoDefinition);
            mapTitleMainElements(opusDocument, titleInfoDefinition);
        }
    }

    private void mapTitleAlternativeElements(OpusDocument opusDocument, ModsDefinition modsDefinition)
            throws XPathExpressionException {

        if (nodeExists("TitleAlternative", opusDocument.getOpus().getOpusDocument())) {

            TitleInfoDefinition titleInfoDefinition =
                    (TitleInfoDefinition) select("mods:titleInfo[@type='alternative']", modsDefinition);

            if (titleInfoDefinition == null) {
                titleInfoDefinition = modsDefinition.addNewTitleInfo();
                titleInfoDefinition.setType(TitleInfoDefinition.Type.ALTERNATIVE);
                signalChanges();
            }

            mapTitleAlternativeElements(opusDocument, titleInfoDefinition);
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
                signalChanges();
            }

            TitleInfoDefinition titleInfoDefinition =
                    (TitleInfoDefinition) select("mods:titleInfo", relatedItemDefinition);

            if (titleInfoDefinition == null) {
                titleInfoDefinition = relatedItemDefinition.addNewTitleInfo();
                signalChanges();
            }

            mapTitleParentElements(opusDocument, titleInfoDefinition);
        }
    }

    private void mapTitleAlternativeElements(OpusDocument opusDocument, TitleInfoDefinition titleInfoDefinition)
            throws XPathExpressionException {

        for (Title ot : opusDocument.getOpus().getOpusDocument().getTitleAlternativeArray()) {
            String query = splQuery("mods:title", languageEncoding(ot.getLanguage()), ot.getValue());
            if (!nodeExists(query, titleInfoDefinition)) {
                StringPlusLanguage mt = titleInfoDefinition.addNewTitle();
                mt.setLang(ot.getLanguage());
                mt.setStringValue(ot.getValue());
                signalChanges();
            }
        }
    }

    private void mapTitleSubElements(OpusDocument opusDocument, TitleInfoDefinition titleInfoDefinition)
            throws XPathExpressionException {

        for (Title ot : opusDocument.getOpus().getOpusDocument().getTitleSubArray()) {
            String query = splQuery("mods:subTitle", languageEncoding(ot.getLanguage()), ot.getValue());
            if (!nodeExists(query, titleInfoDefinition)) {
                StringPlusLanguage mt = titleInfoDefinition.addNewSubTitle();
                mt.setLang(ot.getLanguage());
                mt.setStringValue(ot.getValue());
                signalChanges();
            }
        }
    }

    private void mapTitleMainElements(OpusDocument opusDocument, TitleInfoDefinition titleInfoDefinition)
            throws XPathExpressionException {

        for (Title ot : opusDocument.getOpus().getOpusDocument().getTitleMainArray()) {
            String query = splQuery("mods:title", languageEncoding(ot.getLanguage()), ot.getValue());
            if (!nodeExists(query, titleInfoDefinition)) {
                StringPlusLanguage mt = titleInfoDefinition.addNewTitle();
                mt.setLang(ot.getLanguage());
                mt.setStringValue(ot.getValue());
                signalChanges();
            }
        }
    }

    private void mapTitleParentElements(OpusDocument opusDocument, TitleInfoDefinition titleInfoDefinition)
            throws XPathExpressionException {

        for (Title ot : opusDocument.getOpus().getOpusDocument().getTitleParentArray()) {
            String query = splQuery("mods:title", languageEncoding(ot.getLanguage()), ot.getValue());
            if (!nodeExists(query, titleInfoDefinition)) {
                StringPlusLanguage mt = titleInfoDefinition.addNewTitle();
                mt.setLang(ot.getLanguage());
                mt.setStringValue(ot.getValue());
                signalChanges();
            }
        }
    }

    private String splQuery(String qname, String lang, String value) {
        if (lang != null && !lang.isEmpty()) {
            return String.format("%s[@lang='%s' and text()='%s']", qname, lang, value);
        } else {
            return String.format("%s[text()='%s']", qname, value);
        }
    }

}
