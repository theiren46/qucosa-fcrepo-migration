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

import java.util.*;

import static gov.loc.mods.v3.CodeOrText.CODE;
import static gov.loc.mods.v3.NameDefinition.Type.CORPORATE;

public class InstitutionInfoProcessor extends MappingProcessor {

    final private static HashMap<String, LinkedList<String>> hierarchies = new HashMap<String, LinkedList<String>>() {{
        put("other", new LinkedList<String>() {{
            add("institution");
            add("section");
            add("section");
            add("section");
        }});
        put("university", new LinkedList<String>() {{
            add("university");
            add("faculty");
            add("institute");
            add("chair");
        }});
    }};

    @Override
    public void process(OpusDocument opusDocument, ModsDocument modsDocument, InfoDocument infoDocument) throws Exception {
        Document opus = opusDocument.getOpus().getOpusDocument();
        ModsDefinition mods = modsDocument.getMods();

        for (Organisation org : opus.getOrganisationArray()) {
            final String type = String.valueOf(org.getType());
            final String place = org.getAddress();
            final String role = marcrelatorEncoding(org.getRole());

            final Stack<String> stackOfNames = stackOfNames(org);
            final String significantName = stackOfNames.pop();

            if (significantName != null) {
                final String token = buildTokenFrom("CORP_", significantName);

                NameDefinition nd = getNameDefinition(mods, token);
                setNamePart(significantName, nd);

                RoleDefinition rd = getRoleDefinition(nd);
                setRoleTerm(role, rd);

                if (!stackOfNames.isEmpty()) {
                    Collections.reverse(stackOfNames);
                    ExtensionDefinition ed = getExtensionDefinition(mods);
                    InfoDocument id = getInfoDocument(type, place, stackOfNames, token, ed);
                    if (id != null) {
                        ed.set(id);
                    }
                }
            }

        }
    }

    private InfoDocument getInfoDocument(String type, String place, Stack<String> nameStack, String token, ExtensionDefinition ed) {
        InfoDocument id = null;
        InfoType it = (InfoType) select("slub:info", ed);
        if (it == null) {
            id = InfoDocument.Factory.newInstance();
            it = id.addNewInfo();
            signalChanges(MODS_CHANGES);
        }

        CorporationType ct = (CorporationType) select("slub:corporation[@slub:ref='" + token + "']", it);
        if (ct == null) {
            ct = it.addNewCorporation();
            ct.setRef(token);
            signalChanges(MODS_CHANGES);
        }
        if (ct.getType() == null || !ct.getType().equals(type)) {
            ct.setType(type);
            signalChanges(MODS_CHANGES);
        }
        if (ct.getPlace() == null || !ct.getPlace().equals(place)) {
            ct.setPlace(place);
            signalChanges(MODS_CHANGES);
        }

        LinkedList<String> hierarchy = hierarchies.get(type);
        Iterator<String> hi = hierarchy.listIterator();
        while (!nameStack.isEmpty() && hi.hasNext()) {
            createOrganizationType(ct, hi.next(), nameStack.pop());
            signalChanges(MODS_CHANGES);
        }

        return id;
    }

    private void createOrganizationType(CorporationType ct, String hierarchy, String name) {
        switch (hierarchy) {
            case "institution":
                ct.addInstitution(name);
                break;
            case "section":
                ct.addSection(name);
                break;
            case "university":
                ct.addUniversity(name);
                break;
            case "faculty":
                ct.addFaculty(name);
                break;
            case "institute":
                ct.addInstitute(name);
                break;
            case "chair":
                ct.addChair(name);
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

    private Stack<String> stackOfNames(Organisation org) {
        Stack<String> names = new Stack<>();
        addIfNotEmpty(org.getFirstLevelName(), names);
        addIfNotEmpty(org.getSecondLevelName(), names);
        addIfNotEmpty(org.getThirdLevelName(), names);
        addIfNotEmpty(org.getFourthLevelName(), names);
        return names;
    }

    private void addIfNotEmpty(String s, Stack<String> ss) {
        if (s != null && !s.isEmpty()) ss.push(s);
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
