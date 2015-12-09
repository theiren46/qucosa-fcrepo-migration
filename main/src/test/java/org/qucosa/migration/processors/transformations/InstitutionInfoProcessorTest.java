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

import noNamespace.Organisation;
import org.custommonkey.xmlunit.XMLAssert;
import org.junit.Test;
import org.w3c.dom.Document;

public class InstitutionInfoProcessorTest extends ProcessorTestBase {

    final private MappingProcessor processor = new InstitutionInfoProcessor();

    @Test
    public void extractsRole() throws Exception {
        createOrganisation(Organisation.Type.OTHER,
                "Chemnitz", "publisher", "TU Chemnitz", "Rektorat", "Abteilung Foo", "Gruppe Baz");

        runProcessor(processor);

        Document xml = modsDocument.getMods().getDomNode().getOwnerDocument();
        XMLAssert.assertXpathExists("//mods:name/mods:role/mods:roleTerm[text()='pbl']", xml);
    }

    @Test
    public void corporationReferencesModsName() throws Exception {
        createOrganisation(Organisation.Type.OTHER,
                "Chemnitz", "publisher", "TU Chemnitz", "Rektorat", "Abteilung Foo", "Gruppe Baz");

        runProcessor(processor);

        Document xml = modsDocument.getMods().getDomNode().getOwnerDocument();
        XMLAssert.assertXpathExists("//mods:extension/slub:info/slub:corporation[@ref=//mods:name/@ID]", xml);
    }

    @Test
    public void No_new_corporation_reference_if_one_already_exists() throws Exception {
        createOrganisation(Organisation.Type.OTHER,
                "Chemnitz", "publisher", "TU Chemnitz", "Rektorat", "Abteilung Foo", "Gruppe Baz");

        runProcessor(processor);
        runProcessor(processor);

        Document xml = modsDocument.getMods().getDomNode().getOwnerDocument();
        XMLAssert.assertXpathNotExists("//mods:extension/slub:info/slub:corporation[@ref=//mods:name/@ID][2]", xml);
    }

    @Test
    public void Recognizes_type_other() throws Exception {
        createOrganisation(Organisation.Type.OTHER,
                "Chemnitz", "publisher", "TU Chemnitz", "Rektorat", "Abteilung Foo", "Gruppe Baz");

        runProcessor(processor);

        Document xml = modsDocument.getMods().getDomNode().getOwnerDocument();
        XMLAssert.assertXpathExists("//mods:extension/slub:info/slub:corporation[@type='other']", xml);
    }

    @Test
    public void Recognizes_type_university() throws Exception {
        createOrganisation(Organisation.Type.UNIVERSITY,
                "Chemnitz", "publisher", "TU Chemnitz", "Rektorat", "Abteilung Foo", "Gruppe Baz");

        runProcessor(processor);

        Document xml = modsDocument.getMods().getDomNode().getOwnerDocument();
        XMLAssert.assertXpathExists("//mods:extension/slub:info/slub:corporation[@type='university']", xml);
    }

    @Test
    public void Recognizes_type_chair_and_maps_to_university() throws Exception {
        createOrganisation(Organisation.Type.CHAIR,
                "Chemnitz", "publisher", "TU Chemnitz", "Rektorat", "Abteilung Foo", "Gruppe Baz");

        runProcessor(processor);

        Document xml = modsDocument.getMods().getDomNode().getOwnerDocument();
        XMLAssert.assertXpathExists("//mods:extension/slub:info/slub:corporation[@type='university']", xml);
    }

    @Test
    public void Recognizes_type_faculty_and_maps_to_university() throws Exception {
        createOrganisation(Organisation.Type.FACULTY,
                "Chemnitz", "publisher", "TU Chemnitz", "Rektorat", "Abteilung Foo", "Gruppe Baz");

        runProcessor(processor);

        Document xml = modsDocument.getMods().getDomNode().getOwnerDocument();
        XMLAssert.assertXpathExists("//mods:extension/slub:info/slub:corporation[@type='university']", xml);
    }

    @Test
    public void Recognizes_type_institute_and_maps_to_university() throws Exception {
        createOrganisation(Organisation.Type.INSTITUTE,
                "Chemnitz", "publisher", "TU Chemnitz", "Rektorat", "Abteilung Foo", "Gruppe Baz");

        runProcessor(processor);

        Document xml = modsDocument.getMods().getDomNode().getOwnerDocument();
        XMLAssert.assertXpathExists("//mods:extension/slub:info/slub:corporation[@type='university']", xml);
    }

    @Test
    public void extractsPlace() throws Exception {
        createOrganisation(Organisation.Type.OTHER,
                "Chemnitz", "publisher", "TU Chemnitz", "Rektorat", "Abteilung Foo", "Gruppe Baz");

        runProcessor(processor);

        Document xml = modsDocument.getMods().getDomNode().getOwnerDocument();
        XMLAssert.assertXpathExists("//mods:extension/slub:info/slub:corporation[@place='Chemnitz']", xml);
    }

    @Test
    public void extractsUnitsForTypeOther() throws Exception {
        createOrganisation(Organisation.Type.OTHER,
                "Chemnitz", "publisher", "TU Chemnitz", "Rektorat", "Abteilung Foo", "Gruppe Baz");

        runProcessor(processor);

        Document xml = modsDocument.getMods().getDomNode().getOwnerDocument();
        XMLAssert.assertXpathExists("//mods:name/mods:namePart[text()='TU Chemnitz']", xml);
        XMLAssert.assertXpathExists("//mods:extension/slub:info/slub:corporation[slub:section='Rektorat']", xml);
        XMLAssert.assertXpathExists("//mods:extension/slub:info/slub:corporation[slub:section='Abteilung Foo']", xml);
        XMLAssert.assertXpathExists("//mods:extension/slub:info/slub:corporation[slub:section='Gruppe Baz']", xml);
    }

    @Test
    public void extractsUnitsForTypeUniversity() throws Exception {
        createOrganisation(Organisation.Type.UNIVERSITY,
                "Dresden", "publisher", "Technische Universität Dresden", "Fakultät Informatik",
                "Institut für Systemarchitektur", "Professur für Datenbanken");

        runProcessor(processor);

        Document xml = modsDocument.getMods().getDomNode().getOwnerDocument();
        XMLAssert.assertXpathExists("//mods:name/mods:namePart[text()='Technische Universität Dresden']", xml);
        XMLAssert.assertXpathExists("//mods:extension/slub:info/slub:corporation[slub:faculty='Fakultät Informatik']", xml);
        XMLAssert.assertXpathExists("//mods:extension/slub:info/slub:corporation[slub:institute='Institut für Systemarchitektur']", xml);
        XMLAssert.assertXpathExists("//mods:extension/slub:info/slub:corporation[slub:chair='Professur für Datenbanken']", xml);
    }

    @Test
    public void extractsUnitsForTypeChair() throws Exception {
        createOrganisation(Organisation.Type.CHAIR,
                "Dresden", "publisher", "Technische Universität Dresden", "Fakultät Informatik",
                "Institut für Systemarchitektur", "Professur für Datenbanken");

        runProcessor(processor);

        Document xml = modsDocument.getMods().getDomNode().getOwnerDocument();
        XMLAssert.assertXpathExists("//mods:name/mods:namePart[text()='Technische Universität Dresden']", xml);
        XMLAssert.assertXpathExists("//mods:extension/slub:info/slub:corporation[slub:faculty='Fakultät Informatik']", xml);
        XMLAssert.assertXpathExists("//mods:extension/slub:info/slub:corporation[slub:institute='Institut für Systemarchitektur']", xml);
        XMLAssert.assertXpathExists("//mods:extension/slub:info/slub:corporation[slub:chair='Professur für Datenbanken']", xml);
    }

    @Test
    public void extractsUnitsForTypeFaculty() throws Exception {
        createOrganisation(Organisation.Type.FACULTY,
                "Dresden", "publisher", "Technische Universität Dresden", "Fakultät Informatik", null, null);

        runProcessor(processor);

        Document xml = modsDocument.getMods().getDomNode().getOwnerDocument();
        XMLAssert.assertXpathExists("//mods:name/mods:namePart[text()='Technische Universität Dresden']", xml);
        XMLAssert.assertXpathExists("//mods:extension/slub:info/slub:corporation[slub:faculty='Fakultät Informatik']", xml);
    }

    @Test
    public void extractsUnitsForTypeInstitute() throws Exception {
        createOrganisation(Organisation.Type.INSTITUTE,
                "Dresden", "publisher", "Technische Universität Dresden", "Fakultät Informatik",
                "Institut für Systemarchitektur", null);

        runProcessor(processor);

        Document xml = modsDocument.getMods().getDomNode().getOwnerDocument();

        XMLAssert.assertXpathExists("//mods:name/mods:namePart[text()='Technische Universität Dresden']", xml);
        XMLAssert.assertXpathExists("//mods:extension/slub:info/slub:corporation[slub:faculty='Fakultät Informatik']", xml);
        XMLAssert.assertXpathExists("//mods:extension/slub:info/slub:corporation[slub:institute='Institut für Systemarchitektur']", xml);
    }

    @Test
    public void adds_mapping_hack_to_distinguish_other_corporations() throws Exception {
        createOrganisation(Organisation.Type.OTHER,
                "Chemnitz", "publisher", "TU Chemnitz", "Rektorat", "Abteilung Foo", "Gruppe Baz");

        runProcessor(processor);

        Document xml = modsDocument.getMods().getDomNode().getOwnerDocument();
        XMLAssert.assertXpathExists("//mods:name[@type='corporate' and @displayLabel='mapping-hack-other']/mods:namePart[text()='TU Chemnitz']", xml);
    }

    @Test
    public void adds_mapping_hack_to_distinguish_universities() throws Exception {
        createOrganisation(Organisation.Type.UNIVERSITY,
                "Chemnitz", "publisher", "TU Chemnitz", "Rektorat", "Abteilung Foo", "Gruppe Baz");

        runProcessor(processor);

        Document xml = modsDocument.getMods().getDomNode().getOwnerDocument();
        XMLAssert.assertXpathExists("//mods:name[@type='corporate' and @displayLabel='mapping-hack-university']/mods:namePart[text()='TU Chemnitz']", xml);
    }

    private void createOrganisation(
            Organisation.Type.Enum type, String address, String publisher, String firstLevelName,
            String secondLevelName, String thirdLevelName, String fourthLevelName) {
        Organisation org = opusDocument.getOpus().getOpusDocument().addNewOrganisation();
        org.setType(type);
        org.setAddress(address);
        org.setRole(publisher);
        org.setFirstLevelName(firstLevelName);
        org.setSecondLevelName(secondLevelName);
        org.setThirdLevelName(thirdLevelName);
        org.setFourthLevelName(fourthLevelName);

        // not mapped...
        org.setTudFisKeyFaculty("0");
        org.setTudFisKeyChair("0");
        org.setFreeSubmission(false);
    }

}
