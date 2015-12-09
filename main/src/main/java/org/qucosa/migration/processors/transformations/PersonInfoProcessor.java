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

import javax.xml.namespace.QName;

import static gov.loc.mods.v3.CodeOrText.CODE;
import static gov.loc.mods.v3.NameDefinition.Type.PERSONAL;
import static gov.loc.mods.v3.NamePartDefinition.Type.*;

public class PersonInfoProcessor extends MappingProcessor {

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
        mapPersons(mods, opus.getPersonOtherArray());
        mapPersons(mods, opus.getPersonTranslatorArray());
        mapPersonSubmitter(opus, info);
    }

    private void mapPersons(ModsDefinition mods, Person[] persons) {
        for (Person person : persons) {
            final String given = person.getFirstName();
            final String family = person.getLastName();
            final String termsOfAddress = person.getAcademicTitle();
            final String date = dateEncoding(person.getDateOfBirth());
            final String marcRoleTerm = marcrelatorEncoding(person.getRole());

            NameDefinition nd = findOrCreateNameDefinition(mods, given, family, date);
            setNameParts(given, family, termsOfAddress, date, nd);
            setNodeIdForReferencing(given, family, termsOfAddress, nd);
            setRole(marcRoleTerm, nd);
            setExtension(person, nd, mods);
        }
    }


    private void setExtension(Person person, NameDefinition nd, ModsDefinition mods) {
        ExtensionDefinition ext = (ExtensionDefinition)
                select("mods:extension", mods);
        final String phone = person.getPhone();
        final String mbox = person.getEmail();
        final String gender = genderMapping(person.getGender());

        if (phone == null || mbox == null || gender == null) {
            return;
        }

        if (ext == null) {
            ext = mods.addNewExtension();
            signalChanges(MODS_CHANGES);
        }

        PersonDocument.Person foafPerson = (PersonDocument.Person)
                select("foaf:Person[@ID='" + nd.getID() + "']", mods);

        boolean _importPd = false;
        PersonDocument pd = PersonDocument.Factory.newInstance();

        if (foafPerson == null) {
            foafPerson = pd.addNewPerson();
            foafPerson.newCursor().setAttributeText(
                    new QName(NS_RDF, "about"), nd.getID());
            _importPd = true;
            signalChanges(MODS_CHANGES);
        }

        if (!phone.isEmpty()) {
            if (foafPerson.getPhone() == null
                    || !foafPerson.getPhone().equals(phone)) {
                foafPerson.setPhone(phone);
                signalChanges(MODS_CHANGES);
            }
        }

        if (!mbox.isEmpty()) {
            if (foafPerson.getMbox() == null
                    || !foafPerson.getMbox().equals(mbox)) {
                foafPerson.setMbox(mbox);
                signalChanges(MODS_CHANGES);
            }
        }

        if (!gender.isEmpty()) {
            if (foafPerson.getGender() == null
                    || !foafPerson.getGender().equals(gender)) {
                foafPerson.setGender(gender);
                signalChanges(MODS_CHANGES);
            }
        }

        if (_importPd) {
            ext.set(pd);
        }
    }

    private String genderMapping(String gender) {
        switch (gender) {
            case "m":
                return "male";
            case "f":
                return "female";
            default:
                return null;
        }
    }

    private void setRole(String marcRoleTerm, NameDefinition nd) {
        if (marcRoleTerm != null) {
            RoleDefinition rd = (RoleDefinition) select("mods:role", nd);
            if (rd == null) {
                rd = nd.addNewRole();
                signalChanges(MODS_CHANGES);
            }

            RoleTermDefinition rtd = (RoleTermDefinition)
                    select(String.format("mods:roleTerm[@type='%s'" +
                                    " and @authority='%s'" +
                                    " and @authorityURI='%s'" +
                                    " and @valueURI='%s'" +
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

    private void setNameParts(String given, String family, String termsOfAddress, String date, NameDefinition nd) {
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
    }

    private void setNodeIdForReferencing(String given, String family, String termsOfAddress, NameDefinition nd) {
        String ndid = nd.getID();
        if (ndid == null || ndid.isEmpty()) {
            String token = buildTokenFrom("PERS_", given, family, termsOfAddress);
            nd.setID(token);
            signalChanges(MODS_CHANGES);
        }
    }

    private NameDefinition findOrCreateNameDefinition(ModsDefinition mods, String given, String family, String date) {
        StringBuilder sb = new StringBuilder();
        sb.append("mods:name[");
        sb.append("@type='personal'");
        if (given != null && !given.isEmpty()) {
            sb.append(" and mods:namePart[@type='given' and text()='" + singleline(given) + "']");
        }
        if (family != null && !family.isEmpty()) {
            sb.append(" and mods:namePart[@type='family' and text()='" + singleline(family) + "']");
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
        return nd;
    }

    private void checkOrSetNamePart(NamePartDefinition.Type.Enum type, String value, NameDefinition nd) {
        NamePartDefinition np = (NamePartDefinition)
                select("mods:namePart[@type='" + type + "' and text()='" + singleline(value) + "']", nd);

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
            case "other":
                return "oth";
            case "translator":
                return "trl";
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
                    select("slub:submitter[foaf:Person/foaf:name='" + singleline(name) + "']", info);

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
