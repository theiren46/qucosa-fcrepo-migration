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

import gov.loc.mods.v3.NameDefinition;
import gov.loc.mods.v3.NamePartDefinition;
import noNamespace.Date;
import noNamespace.Person;
import org.custommonkey.xmlunit.XMLAssert;
import org.junit.Test;
import org.w3c.dom.Document;

import java.math.BigInteger;

import static gov.loc.mods.v3.NameDefinition.Type.PERSONAL;
import static gov.loc.mods.v3.NamePartDefinition.Type.*;

public class PersonInfoProcessorTest extends ProcessorTestBase {

    final private MappingProcessor processor = new PersonInfoProcessor();

    @Test
    public void extractsSubmitter() throws Exception {
        Person submitter = opusDocument.getOpus().getOpusDocument().addNewPersonSubmitter();
        submitter.setPhone("+49 815 4711");
        submitter.setEmail("m.musterfrau@example.com");
        submitter.setFirstName("Maxi");
        submitter.setLastName("Musterfrau");

        runProcessor(processor);

        XMLAssert.assertXpathExists(
                "//slub:submitter/foaf:Person[" +
                        "foaf:name='Maxi Musterfrau' and " +
                        "foaf:phone='+49 815 4711' and " +
                        "foaf:mbox='m.musterfrau@example.com']",
                infoDocument.getInfo().getDomNode().getOwnerDocument());
    }

    @Test
    public void extractsAuthorNameparts() throws Exception {
        createPerson("Prof. Dr.", "m", "+49(0)1234567890", "mustermann@musteruni.de",
                "Hans", "Mustermann", "author", 1965, 11, 5);

        runProcessor(processor);

        Document xml = modsDocument.getMods().getDomNode().getOwnerDocument();
        XMLAssert.assertXpathExists("//mods:name[@type='personal']", xml);
        XMLAssert.assertXpathExists("//mods:name/mods:namePart[@type='given' and text()='Hans']", xml);
        XMLAssert.assertXpathExists("//mods:name/mods:namePart[@type='family' and text()='Mustermann']", xml);
        XMLAssert.assertXpathExists("//mods:name/mods:namePart[@type='termsOfAddress' and text()='Prof. Dr.']", xml);
        XMLAssert.assertXpathExists("//mods:name/mods:namePart[@type='date' and text()='1965-11-05']", xml);
    }

    @Test
    public void extractsAdvisorNameparts() throws Exception {
        createPerson("Prof. Dr.", "m", "+49(0)1234567890", "mustermann@musteruni.de",
                "Hans", "Mustermann", "advisor", 1965, 11, 5);

        runProcessor(processor);

        Document xml = modsDocument.getMods().getDomNode().getOwnerDocument();
        XMLAssert.assertXpathExists("//mods:name[@type='personal']", xml);
        XMLAssert.assertXpathExists("//mods:name/mods:namePart[@type='given' and text()='Hans']", xml);
        XMLAssert.assertXpathExists("//mods:name/mods:namePart[@type='family' and text()='Mustermann']", xml);
        XMLAssert.assertXpathExists("//mods:name/mods:namePart[@type='termsOfAddress' and text()='Prof. Dr.']", xml);
        XMLAssert.assertXpathExists("//mods:name/mods:namePart[@type='date' and text()='1965-11-05']", xml);
    }

    @Test
    public void updateAuthorNameparts() throws Exception {
        createPerson("Prof. Dr.", "m", "+49(0)1234567890", "mustermann@musteruni.de",
                "Hans", "Mustermann", "author", 1965, 11, 5);

        {
            NameDefinition nd = modsDocument.getMods().addNewName();
            nd.setType2(PERSONAL);
            {
                NamePartDefinition np = nd.addNewNamePart();
                np.setType(GIVEN);
                np.setStringValue("Hans");
            }
            {
                NamePartDefinition np = nd.addNewNamePart();
                np.setType(FAMILY);
                np.setStringValue("Mustermann");
            }
            {
                NamePartDefinition np = nd.addNewNamePart();
                np.setType(DATE);
                np.setStringValue("1965-11-05");
            }
        }
        {
            NameDefinition nd = modsDocument.getMods().addNewName();
            nd.setType2(PERSONAL);
            NamePartDefinition np = nd.addNewNamePart();
            np.setType(FAMILY);
            np.setStringValue("Schneider");
        }

        runProcessor(processor);

        Document xml = modsDocument.getMods().getDomNode().getOwnerDocument();
        XMLAssert.assertXpathExists("//mods:name[@type='personal'" +
                " and mods:namePart[@type='family' and text()='Mustermann']" +
                " and mods:namePart[@type='termsOfAddress' and text()='Prof. Dr.']]", xml);
    }

    @Test
    public void extractsRole() throws Exception {
        createPerson("Prof. Dr.", "m", "+49(0)1234567890", "mustermann@musteruni.de",
                "Hans", "Mustermann", "author", 1965, 11, 5);

        runProcessor(processor);

        Document xml = modsDocument.getMods().getDomNode().getOwnerDocument();
        XMLAssert.assertXpathExists("//mods:name[@type='personal']" +
                "/mods:role/mods:roleTerm[" +
                "@type='code' and @authority='marcrelator'" +
                " and @authorityURI='http://id.loc.gov/vocabulary/relators'" +
                " and @valueURI='http://id.loc.gov/vocabulary/relators/aut'" +
                " and text()='aut']", xml);
    }

    private void createPerson(String academicTitle, String gender, String phone, String email, String firstName,
                              String lastName, String role, int yearOfBirth, int monthOfBirth, int dayOfBirth) {

        Person person;
        switch (role) {
            case "author":
                person = opusDocument.getOpus().getOpusDocument().addNewPersonAuthor();
                break;
            case "advisor":
                person = opusDocument.getOpus().getOpusDocument().addNewPersonAdvisor();
                break;
            default:
                person = opusDocument.getOpus().getOpusDocument().addNewPersonOther();
        }

        person.setAcademicTitle(academicTitle);
        {
            Date date = person.addNewDateOfBirth();
            date.setYear(BigInteger.valueOf(yearOfBirth));
            date.setMonth(BigInteger.valueOf(monthOfBirth));
            date.setDay(BigInteger.valueOf(dayOfBirth));
        }
        person.setGender(gender);
        person.setPhone(phone);
        person.setEmail(email);
        person.setFirstName(firstName);
        person.setLastName(lastName);
        person.setRole(role);
    }

}