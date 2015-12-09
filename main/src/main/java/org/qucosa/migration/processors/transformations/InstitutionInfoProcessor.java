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

import de.slubDresden.CorporationType;
import de.slubDresden.InfoDocument;
import de.slubDresden.InfoType;
import gov.loc.mods.v3.*;
import noNamespace.Document;
import noNamespace.OpusDocument;
import noNamespace.Organisation;
import noNamespace.Organisation.Type;

import javax.xml.xpath.XPathExpressionException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import static gov.loc.mods.v3.CodeOrText.CODE;
import static gov.loc.mods.v3.NameDefinition.Type.CORPORATE;

public class InstitutionInfoProcessor extends MappingProcessor {

    @Override
    public void process(OpusDocument opusDocument, ModsDocument modsDocument, InfoDocument infoDocument) throws Exception {
        Document opus = opusDocument.getOpus().getOpusDocument();
        ModsDefinition mods = modsDocument.getMods();

        for (Organisation org : opus.getOrganisationArray()) {
            final Type.Enum type = org.getType();
            final String place = org.getAddress();
            final String role = marcrelatorEncoding(org.getRole());

            final ArrayList<String> nameArray = buildNameArray(org);
            final String significantName = nameArray.get(0);

            if (significantName != null) {
                nameArray.remove(0);
                final String token = buildTokenFrom("CORP_", significantName);

                NameDefinition nd = getNameDefinition(mods, token);
                setNamePart(significantName, nd);

                // FIXME Bad mapping hack to make up for inaptness of TYPO3 mapping configuration
                addMappingHack(nd, type);

                RoleDefinition rd = getRoleDefinition(nd);
                setRoleTerm(role, rd);

                if (!nameArray.isEmpty()) {
                    ExtensionDefinition ed = getExtensionDefinition(mods);
                    InfoDocument id = getInfoDocument(type, place, nameArray, token, ed);
                    if (id != null) {
                        ed.set(id);
                    }
                }
            }

        }
    }

    private InfoDocument getInfoDocument(Type.Enum type, String place, ArrayList<String> names, String token, ExtensionDefinition ed) throws Exception {
        InfoDocument id = null;
        InfoType it = (InfoType) select("slub:info", ed);
        if (it == null) {
            id = InfoDocument.Factory.newInstance();
            it = id.addNewInfo();
            signalChanges(MODS_CHANGES);
        }

        CorporationType ct = (CorporationType) select("slub:corporation[@ref='" + token + "']", it);
        if (ct == null) {
            ct = it.addNewCorporation();
            ct.setRef(token);
            signalChanges(MODS_CHANGES);
        }

        final String mappedType = (Type.OTHER.equals(type)) ? type.toString() : Type.UNIVERSITY.toString();
        if (ct.getType() == null || !ct.getType().equals(mappedType)) {
            ct.setType(mappedType);
            signalChanges(MODS_CHANGES);
        }

        if (ct.getPlace() == null || !ct.getPlace().equals(place)) {
            ct.setPlace(place);
            signalChanges(MODS_CHANGES);
        }

        final LinkedList<String> otherHierarchy = new LinkedList<String>() {{
            add("section");
            add("section");
            add("section");
        }};

        final LinkedList<String> universityHierarchy = new LinkedList<String>() {{
            add("faculty");
            add("institute");
            add("chair");
        }};

        Iterator<String> hi = (Type.OTHER.equals(type) ? otherHierarchy : universityHierarchy).listIterator();
        for (String name : names) {
            String hierarchyLevel = (hi.hasNext()) ? hi.next() : null;
            if (hierarchyLevel != null) {
                createOrganizationType(ct, hierarchyLevel, name);
                signalChanges(MODS_CHANGES);
            }
        }

        return id;
    }

    private void createOrganizationType(CorporationType ct, String hierarchy, String name) throws XPathExpressionException {
        switch (hierarchy) {
            case "institution":
                if (!nodeExists("slub:institution[text()='" + singleline(name) + "']", ct)) ct.addInstitution(name);
                break;
            case "section":
                if (!nodeExists("slub:section[text()='" + singleline(name) + "']", ct)) ct.addSection(name);
                break;
            case "university":
                if (!nodeExists("slub:university[text()='" + singleline(name) + "']", ct)) ct.addUniversity(name);
                break;
            case "faculty":
                if (!nodeExists("slub:faculty[text()='" + singleline(name) + "']", ct)) ct.addFaculty(name);
                break;
            case "institute":
                if (!nodeExists("slub:institute[text()='" + singleline(name) + "']", ct)) ct.addInstitute(name);
                break;
            case "chair":
                if (!nodeExists("slub:chair[text()='" + singleline(name) + "']", ct)) ct.addChair(name);
                break;
        }
    }

    private ExtensionDefinition getExtensionDefinition(ModsDefinition mods) {
        ExtensionDefinition ed = (ExtensionDefinition) select("mods:extension", mods);
        if (ed == null) {
            ed = mods.addNewExtension();
            signalChanges(MODS_CHANGES);
        }
        return ed;
    }

    private void setRoleTerm(String role, RoleDefinition rd) {
        RoleTermDefinition rtd = (RoleTermDefinition) select("mods:roleTerm[text()='" + role + "']", rd);
        if (rtd == null) {
            rtd = rd.addNewRoleTerm();
            rtd.setType(CODE);
            rtd.setAuthority("marcrelator");
            rtd.setAuthorityURI(LOC_GOV_VOCABULARY_RELATORS);
            rtd.setValueURI(LOC_GOV_VOCABULARY_RELATORS + "/" + role);
            rtd.setStringValue(role);
            signalChanges(MODS_CHANGES);
        }
    }

    private RoleDefinition getRoleDefinition(NameDefinition nd) {
        RoleDefinition rd = (RoleDefinition) select("mods:role", nd);
        if (rd == null) {
            rd = nd.addNewRole();
            signalChanges(MODS_CHANGES);
        }
        return rd;
    }

    private void setNamePart(String significantName, NameDefinition nd) {
        NamePartDefinition npd = (NamePartDefinition) select("mods:namePart[text()='" + significantName + "']", nd);
        if (npd == null) {
            npd = nd.addNewNamePart();
            npd.setStringValue(significantName);
            signalChanges(MODS_CHANGES);
        }
    }

    private NameDefinition getNameDefinition(ModsDefinition mods, String token) {
        NameDefinition nd = (NameDefinition) select("mods:name[@ID='" + token + "' and @type='corporate']", mods);
        if (nd == null) {
            nd = mods.addNewName();
            nd.setID(token);
            nd.setType2(CORPORATE);
            signalChanges(MODS_CHANGES);
        }
        return nd;
    }

    private void addMappingHack(NameDefinition nd, Type.Enum type) {
        String mappingHack = "";
        if (Type.OTHER.equals(type)) {
            mappingHack = "mapping-hack-other";
        } else if (Type.UNIVERSITY.equals(type)) {
            mappingHack = "mapping-hack-university";
        }
        if (!mappingHack.equals(nd.getDisplayLabel())) {
            nd.setDisplayLabel(mappingHack);
            signalChanges(MODS_CHANGES);
        }
    }

    private ArrayList<String> buildNameArray(Organisation org) {
        ArrayList<String> names = new ArrayList<>();
        addIfNotEmpty(org.getFirstLevelName(), names);
        addIfNotEmpty(org.getSecondLevelName(), names);
        addIfNotEmpty(org.getThirdLevelName(), names);
        addIfNotEmpty(org.getFourthLevelName(), names);
        return names;
    }

    private void addIfNotEmpty(String s, ArrayList<String> ss) {
        if (s != null && !s.isEmpty()) ss.add(s);
    }

    private String marcrelatorEncoding(String role) {
        switch (role) {
            case "publisher":
                return "pbl";
            case "contributor":
                return "ctb";
            default:
                return null;
        }
    }
}
