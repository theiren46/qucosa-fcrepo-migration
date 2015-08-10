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

import com.xmlns.foaf.x01.PersonDocument;
import de.slubDresden.InfoDocument;
import de.slubDresden.InfoType;
import de.slubDresden.SubmitterType;
import gov.loc.mods.v3.*;
import noNamespace.Document;
import noNamespace.OpusDocument;
import noNamespace.Person;

import static gov.loc.mods.v3.CodeOrText.CODE;
import static gov.loc.mods.v3.NameDefinition.Type.PERSONAL;
import static gov.loc.mods.v3.NamePartDefinition.Type.*;

public class PersonInfoProcessor extends MappingProcessor {

    private static final String LOC_GOV_VOCABULARY_RELATORS = "http://id.loc.gov/vocabulary/relators";

    @Override
    public void process(OpusDocument opusDocument, ModsDocument modsDocument, InfoDocument infoDocument) throws Exception {
        Document opus = opusDocument.getOpus().getOpusDocument();
        ModsDefinition mods = modsDocument.getMods();
        InfoType info = infoDocument.getInfo();

        mapPersons(mods, opus.getPersonAuthorArray());
        mapPersons(mods, opus.getPersonAdvisorArray());
        mapPersons(mods, opus.getPersonContributorArray());
        mapPersons(mods, opus.getPersonEditorArray());
        mapPersons(mods, opus.getPersonRefereeArray());
        mapPersonSubmitter(opus, info);
    }

    private void mapPersons(ModsDefinition mods, Person[] persons) {
        for (Person author : persons) {
            final String given = author.getFirstName();
            final String family = author.getLastName();
            final String termsOfAddress = author.getAcademicTitle();
            final String date = dateEncoding(author.getDateOfBirth());
            final String marcRoleTerm = marcrelatorEncoding(author.getRole());

            StringBuilder sb = new StringBuilder();
            sb.append("mods:name[");
            sb.append("@type='personal'");
            if (given != null && !given.isEmpty()) {
                sb.append(" and mods:namePart[@type='given' and text()='" + given + "']");
            }
            if (family != null && !family.isEmpty()) {
                sb.append(" and mods:namePart[@type='family' and text()='" + family + "']");
            }
            if (date != null) {
                sb.append(" and mods:namePart[@type='date' and text()='" + date + "']");
            }
            sb.append(']');

            NameDefinition nd = (NameDefinition)
                    select(sb.toString(), mods);

            if (nd == null) {
                nd = mods.addNewName();
                nd.setType2(PERSONAL);
                signalChanges(MODS_CHANGES);
            }

            if (given != null && !given.isEmpty()) {
                checkOrSetNamePart(GIVEN, given, nd);
            }
            if (family != null && !family.isEmpty()) {
                checkOrSetNamePart(FAMILY, family, nd);
            }
            if (termsOfAddress != null && !termsOfAddress.isEmpty()) {
                checkOrSetNamePart(TERMS_OF_ADDRESS, termsOfAddress, nd);
            }
            if (date != null) {
                checkOrSetNamePart(DATE, date, nd);
            }
            if (marcRoleTerm != null) {
                RoleDefinition rd = (RoleDefinition) select("mods:role", nd);
                if (rd == null) {
                    rd = nd.addNewRole();
                    signalChanges(MODS_CHANGES);
                }
                RoleTermDefinition rtd = (RoleTermDefinition)
                        select(String.format("mods:roleTerm[@type='%s'" +
                                        " and authority='%s'" +
                                        " and authorityURI='%s'" +
                                        " and valueURI='%s'" +
                                        " and text()='%s']",
                                "code", "marcrelator",
                                LOC_GOV_VOCABULARY_RELATORS,
                                LOC_GOV_VOCABULARY_RELATORS + "/" + marcRoleTerm, marcRoleTerm), rd);

                if (rtd == null) {
                    rtd = rd.addNewRoleTerm();
                    rtd.setType(CODE);
                    rtd.setAuthority("marcrelator");
                    rtd.setAuthorityURI(LOC_GOV_VOCABULARY_RELATORS);
                    rtd.setValueURI(LOC_GOV_VOCABULARY_RELATORS + "/" + marcRoleTerm);
                    rtd.setStringValue(marcRoleTerm);
                    signalChanges(MODS_CHANGES);
                }
            }

        }
    }

    private void checkOrSetNamePart(NamePartDefinition.Type.Enum type, String value, NameDefinition nd) {
        NamePartDefinition np = (NamePartDefinition)
                select("mods:namePart[@type='" + type + "' and text()='" + value + "']", nd);

        if (np == null) {
            np = nd.addNewNamePart();
            np.setType(type);
            np.setStringValue(value);
            signalChanges(MODS_CHANGES);
        }
    }

    private String marcrelatorEncoding(String role) {
        switch (role) {
            case "advisor":
                return "ths";
            case "author":
                return "aut";
            case "contributor":
                return "ctb";
            case "editor":
                return "pbl";
            case "referee":
                return "rev";
            default:
                return null;
        }
    }

    private void mapPersonSubmitter(Document opus, InfoType info) {
        for (Person submitter : opus.getPersonSubmitterArray()) {
            final String name = combineName(submitter.getFirstName(), submitter.getLastName());
            final String phone = submitter.getPhone();
            final String mbox = submitter.getEmail();

            SubmitterType st = (SubmitterType)
                    select("slub:submitter[foaf:Person/foaf:name='" + name + "']", info);

            if (st == null) {
                st = info.addNewSubmitter();
                PersonDocument.Person foafPerson = st.addNewPerson();
                foafPerson.setName(name);
                if (phone != null && !phone.isEmpty()) foafPerson.setPhone(phone);
                if (mbox != null && !mbox.isEmpty()) foafPerson.setMbox(mbox);
                signalChanges(SLUB_INFO_CHANGES);
            }
        }
    }

    private String combineName(String firstName, String lastName) {
        StringBuilder sb = new StringBuilder();
        if (firstName != null && !firstName.isEmpty()) {
            sb.append(firstName).append(' ');
        }
        sb.append(lastName);
        return sb.toString();
    }
}
