/*
 * Copyright (C) 2013 SLUB Dresden
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.qucosa.fcrepo.migration;

import fedora.fedoraSystemDef.foxml.DigitalObjectDocument;
import org.custommonkey.xmlunit.SimpleNamespaceContext;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.exceptions.XpathException;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class FedoraObjectBuilderTest {

    static {
        Map<String, String> prefixMap = new HashMap<>();
        prefixMap.put("fox", "info:fedora/fedora-system:def/foxml#");
        prefixMap.put("rel", "info:fedora/fedora-system:def/relations-external#");
        prefixMap.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        XMLUnit.setXpathNamespaceContext(new SimpleNamespaceContext(prefixMap));
    }

    @Test
    public void buildDocument() throws IOException, SAXException, ParserConfigurationException {
        FedoraObjectBuilder fob = new FedoraObjectBuilder();
        fob.setPid("qucosa:4711");
        fob.setUrn("urn:de:slub-dresden:" + fob.getPid());
        fob.setLabel("An arbitrarily migrated Qucosa Document");
        fob.setTitle("The Title of an arbitrarily migrated Qucosa Document");
        fob.setOwnerId("slub");
        fob.setParentCollectionPid("qucosa:slub");

        XMLUnit.setIgnoreWhitespace(true);
        Document ctrl = XMLUnit.buildControlDocument(new InputSource(
                getClass().getResourceAsStream("/FedoraObjectBuilderTest-fo.xml")));
        Document test = XMLUnit.buildTestDocument(serialize(fob));

        XMLAssert.assertXMLEqual(ctrl, test);
    }

    @Test
    public void addsQucosaDatastreamAndVersion() throws XpathException, IOException, SAXException, ParserConfigurationException {
        FedoraObjectBuilder fob = new FedoraObjectBuilder();
        fob.setQucosaXmlDocument(XMLUnit.buildTestDocument("<Opus/>"));
        Document testDocument = XMLUnit.buildTestDocument(serialize(fob));

        XMLAssert.assertXpathExists("/fox:digitalObject/fox:datastream[@ID='QUCOSA-XML']/fox:datastreamVersion[@ID='QUCOSA-XML.0']", testDocument);
    }

    @Test
    public void addsQucosaDatastreamContent() throws IOException, SAXException, ParserConfigurationException, XpathException {
        FedoraObjectBuilder fob = new FedoraObjectBuilder();
        fob.setQucosaXmlDocument(XMLUnit.buildTestDocument("<Opus version=\"2.0\"><Opus_Document/></Opus>"));
        Document testDocument = XMLUnit.buildTestDocument(serialize(fob));

        XMLAssert.assertXpathExists("//Opus_Document", testDocument);
    }

    @Test
    public void addsMemberOfCollectionRelation() throws Exception {
        String parentCollectionPid = "qucosa:qucosa";
        FedoraObjectBuilder fob = new FedoraObjectBuilder();
        fob.setQucosaXmlDocument(XMLUnit.buildTestDocument("<Opus version=\"2.0\"><Opus_Document/></Opus>"));
        fob.setParentCollectionPid(parentCollectionPid);
        Document testDocument = XMLUnit.buildTestDocument(serialize(fob));

        XMLAssert.assertXpathEvaluatesTo("info:fedora/" + parentCollectionPid, "//rel:isMemberOfCollection/@rdf:resource", testDocument);
    }

    @Test
    public void addsIsPartOfRelation() throws Exception {
        FedoraObjectBuilder fob = new FedoraObjectBuilder();
        fob.setConstituentPid("qucosa:4711");
        Document testDocument = XMLUnit.buildTestDocument(serialize(fob));

        XMLAssert.assertXpathEvaluatesTo("info:fedora/qucosa:4711", "//rel:isConstituentOf/@rdf:resource", testDocument);
    }

    private String serialize(FedoraObjectBuilder fob) throws ParserConfigurationException, IOException {
        DigitalObjectDocument d = fob.build();
        StringWriter sw = new StringWriter();
        d.save(sw);
        return sw.toString();
    }

}
