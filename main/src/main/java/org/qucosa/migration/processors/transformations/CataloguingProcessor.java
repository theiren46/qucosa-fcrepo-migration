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
import noNamespace.Subject;
import noNamespace.Title;

public class CataloguingProcessor extends MappingProcessor {
    @Override
    public void process(OpusDocument opusDocument, ModsDocument modsDocument, InfoDocument infoDocument) throws Exception {
        Document opus = opusDocument.getOpus().getOpusDocument();
        ModsDefinition mods = modsDocument.getMods();

        mapTitleAbstract(opus, mods);
        mapTableOfContent(opus, mods);
        mapSubject("ddc", opus, mods);
        mapSubject("rvk", opus, mods);
        mapSubject("uncontrolled", opus, mods);
    }

    private void mapSubject(String type, Document opus, ModsDefinition mods) {
        Subject[] subjects;
        String query = "[@authority='%s' and text()='%s']";
        switch (type) {
            case "ddc":
                subjects = opus.getSubjectDdcArray();
                break;
            case "rvk":
                subjects = opus.getSubjectRvkArray();
                break;
            case "uncontrolled":
                subjects = opus.getSubjectUncontrolledArray();
                query = "[@lang='%s']";
                break;
            default:
                return;
        }

        for (Subject subject : subjects) {
            final String value = subject.getValue();
            final String lang = languageEncoding(subject.getLanguage());

            ClassificationDefinition cl;
            if (type.equals("uncontrolled")) {
                cl = (ClassificationDefinition)
                        select("mods:classification" + String.format(query, lang), mods);
            } else {
                cl = (ClassificationDefinition)
                        select("mods:classification" + String.format(query, type, qq(value)), mods);
            }

            if (cl == null) {
                cl = mods.addNewClassification();
                if (type.equals("uncontrolled")) {
                    cl.setLang(lang);
                } else {
                    cl.setAuthority(type);
                }
                signalChanges(MODS_CHANGES);
            }

            if (!cl.getStringValue().equals(value)) {
                cl.setStringValue(value);
                signalChanges(MODS_CHANGES);
            }
        }
    }

    private void mapTableOfContent(Document opus, ModsDefinition mods) {
        String opusTableOfContent = opus.getTableOfContent();
        if (opusTableOfContent != null && !opusTableOfContent.isEmpty()) {
            TableOfContentsDefinition toc = (TableOfContentsDefinition)
                    select("mods:tableOfContents", mods);

            if (toc == null) {
                toc = mods.addNewTableOfContents();
                toc.setStringValue(opusTableOfContent);
                signalChanges(MODS_CHANGES);
            }
        }
    }

    private void mapTitleAbstract(Document opus, ModsDefinition mods) {
        for (Title ot : opus.getTitleAbstractArray()) {
            final String lang = languageEncoding(ot.getLanguage());
            final String abst = ot.getValue();

            AbstractDefinition ad = (AbstractDefinition) select(
                    String.format("mods:abstract[@lang='%s' and @type='%s' and text()='%s']",
                            lang, "summary", qq(abst)), mods);

            if (ad == null) {
                ad = mods.addNewAbstract();
                ad.setLang(lang);
                ad.setType("summary");
                ad.setStringValue(abst);
                signalChanges(MODS_CHANGES);
            }
        }
    }
}
