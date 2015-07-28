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

import gov.loc.mods.v3.ModsDefinition;
import gov.loc.mods.v3.ModsDocument;
import gov.loc.mods.v3.StringPlusLanguage;
import gov.loc.mods.v3.TitleInfoDefinition;
import noNamespace.OpusDocument;
import noNamespace.Title;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

public class TitleInfoProcessor extends MappingProcessor {
    @Override
    public ModsDocument process(OpusDocument opusDocument, ModsDocument modsDocument) throws Exception {
        ModsDefinition modsDefinition = modsDocument.getMods();
        mapTitleElements(opusDocument, modsDefinition);
        return modsDocument;
    }

    private void mapTitleElements(OpusDocument opusDocument, ModsDefinition modsDefinition) throws XPathExpressionException, XmlException {
        TitleInfoDefinition titleInfoDefinition =
                (TitleInfoDefinition) selectOrCreate("titleInfo", modsDefinition);
        mapTitleSubElements(opusDocument, titleInfoDefinition);
        mapTitleMainElements(opusDocument, titleInfoDefinition);
    }

    private void mapTitleSubElements(OpusDocument opusDocument, TitleInfoDefinition titleInfoDefinition) throws XPathExpressionException {
        for (Title ot : opusDocument.getOpus().getOpusDocument().getTitleSubArray()) {
            Boolean exists = nodeExists(
                    "mods:subTitle[@lang='" + ot.getLanguage() + "' and text()='" + ot.getValue() + "']",
                    titleInfoDefinition);
            if (!exists) {
                StringPlusLanguage mt = titleInfoDefinition.addNewSubTitle();
                mt.setLang(ot.getLanguage());
                mt.setStringValue(ot.getValue());
            }
        }
    }

    private void mapTitleMainElements(OpusDocument opusDocument, TitleInfoDefinition titleInfoDefinition) throws XPathExpressionException {
        for (Title ot : opusDocument.getOpus().getOpusDocument().getTitleMainArray()) {
            Boolean exists = nodeExists(
                    "mods:title[@lang='" + ot.getLanguage() + "' and text()='" + ot.getValue() + "']",
                    titleInfoDefinition);
            if (!exists) {
                StringPlusLanguage mt = titleInfoDefinition.addNewTitle();
                mt.setLang(ot.getLanguage());
                mt.setStringValue(ot.getValue());
                signalChanges();
            }
        }
    }

    private Boolean nodeExists(String expression, XmlObject object) throws XPathExpressionException {
        final XPath xPath = getXPath();
        xPath.reset();
        return (Boolean) xPath.evaluate(expression,
                object.getDomNode().cloneNode(true), XPathConstants.BOOLEAN);
    }
}
