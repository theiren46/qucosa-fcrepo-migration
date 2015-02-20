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

package org.qucosa.fedora;

import fedora.fedoraSystemDef.foxml.*;
import org.openarchives.oai.x20.oaiDc.DcDocument;
import org.openarchives.oai.x20.oaiDc.OaiDcType;
import org.purl.dc.elements.x11.ElementType;
import org.w3.x1999.x02.x22RdfSyntaxNs.RDFDocument;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.ParserConfigurationException;

public class FedoraObjectBuilder {

    private String pid;
    private String label;
    private String ownerId;
    private String urn;
    private Object parentCollectionPid;
    private Document qucosaXmlDocument = null;
    private String constituentPid;
    private String title;
    private String derivativeOfPid;

    public DigitalObjectDocument build() throws ParserConfigurationException {
        DigitalObjectDocument dof = DigitalObjectDocument.Factory.newInstance();
        DigitalObjectDocument.DigitalObject dobj = createDigitalObject(dof);
        addFedoraObjectProperties(dobj);
        addDCDatastream(dobj);
        addRELSEXTDatastream(dobj);
        if (qucosaXmlDocument != null) addQucosaXMLContentDatastream(dobj);
        return dof;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public void setUrn(String urn) {
        this.urn = urn;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public void setParentCollectionPid(Object parentCollectionPid) {
        this.parentCollectionPid = parentCollectionPid;
    }

    public void setQucosaXmlDocument(Document qucosaXmlDocument) {
        this.qucosaXmlDocument = qucosaXmlDocument;
    }

    public void setConstituentPid(String constituentPid) {
        this.constituentPid = constituentPid;
    }

    public void setTitle(String s) {
        this.title = s;
    }

    public void isDerivativeOfPid(String s) {
        this.derivativeOfPid = s;
    }

    private void addDCDatastream(DigitalObjectDocument.DigitalObject dobj) {
        DatastreamType ds = dobj.addNewDatastream();
        ds.setID("DC");
        ds.setCONTROLGROUP(DatastreamType.CONTROLGROUP.X);
        ds.setSTATE(StateType.A);

        DatastreamVersionType dsv = ds.addNewDatastreamVersion();
        dsv.setID("DC.0");
        dsv.setFORMATURI("http://www.openarchives.org/OAI/2.0/oai_dc/");
        dsv.setMIMETYPE("text/xml");
        dsv.setLABEL("Dublin Core Record for this object");

        XmlContentType content = dsv.addNewXmlContent();
        DcDocument dcDocument = getDcDocument();
        content.set(dcDocument);
    }

    private void addFedoraObjectProperties(DigitalObjectDocument.DigitalObject dobj) {
        ObjectPropertiesType pt = dobj.addNewObjectProperties();
        {
            PropertyType p = pt.addNewProperty();
            p.setNAME(PropertyType.NAME.INFO_FEDORA_FEDORA_SYSTEM_DEF_MODEL_STATE);
            p.setVALUE("A");
        }
        {
            PropertyType p = pt.addNewProperty();
            p.setNAME(PropertyType.NAME.INFO_FEDORA_FEDORA_SYSTEM_DEF_MODEL_LABEL);
            p.setVALUE(label);
        }
        {
            PropertyType p = pt.addNewProperty();
            p.setNAME(PropertyType.NAME.INFO_FEDORA_FEDORA_SYSTEM_DEF_MODEL_OWNER_ID);
            p.setVALUE(ownerId);
        }
    }

    private void addQucosaXMLContentDatastream(DigitalObjectDocument.DigitalObject dobj) throws ParserConfigurationException {
        DatastreamType ds = dobj.addNewDatastream();
        ds.setID("QUCOSA-XML");
        ds.setCONTROLGROUP(DatastreamType.CONTROLGROUP.X);
        ds.setSTATE(StateType.A);

        DatastreamVersionType dsv = ds.addNewDatastreamVersion();
        dsv.setID("QUCOSA-XML.0");
        dsv.setMIMETYPE("text/xml");
        dsv.setLABEL("Qucosa XML Record for this Object (Opus4-XMLv2)");

        XmlContentType content = dsv.addNewXmlContent();

        // Workaround for https://issues.apache.org/jira/browse/XMLBEANS-100
        // XMLBeans does not work with jdk1.6 as calls made to methods within XObj.java
        // return with a runtime: "java.lang.RuntimeException: DOM Level 3 Not implemented"
        Node childNode = content.getDomNode().getOwnerDocument().importNode(qucosaXmlDocument.getDocumentElement(), true);
        content.getDomNode().appendChild(childNode);
    }

    private void addRELSEXTDatastream(DigitalObjectDocument.DigitalObject dobj) {
        DatastreamType datastream = dobj.addNewDatastream();
        datastream.setID("RELS-EXT");
        datastream.setSTATE(StateType.A);
        datastream.setCONTROLGROUP(DatastreamType.CONTROLGROUP.X);

        DatastreamVersionType version = datastream.addNewDatastreamVersion();
        version.setID("RELS-EXT.0");
        version.setMIMETYPE("application/rdf+xml");
        version.setFORMATURI("info:fedora/fedora-system:FedoraRELSExt-1.0");
        version.setLABEL("RDF Statements about this object");

        XmlContentType content = version.addNewXmlContent();
        RDFDocument rdfDocument = getRDFDescription();
        content.set(rdfDocument);
    }

    private DigitalObjectDocument.DigitalObject createDigitalObject(DigitalObjectDocument dof) {
        DigitalObjectDocument.DigitalObject dobj = dof.addNewDigitalObject();
        dobj.setVERSION(DigitalObjectType.VERSION.X_1_1);
        dobj.setPID(pid);
        return dobj;
    }

    private DcDocument getDcDocument() {
        DcDocument dcDocument = DcDocument.Factory.newInstance();
        OaiDcType dc = dcDocument.addNewDc();
        ElementType t = dc.addNewTitle();
        t.setStringValue(title);
        ElementType id = dc.addNewIdentifier();
        id.setStringValue(urn);
        return dcDocument;
    }

    private RDFDocument getRDFDescription() {
        RDFDocument rdfDocument = RDFDocument.Factory.newInstance();
        RDFDocument.RDF rdf = rdfDocument.addNewRDF();
        RDFDocument.RDF.Description desc = rdf.addNewDescription();
        desc.setAbout("info:fedora/" + pid);
        if (parentCollectionPid != null) {
            Element e = desc.getDomNode().getOwnerDocument().createElementNS(
                    "info:fedora/fedora-system:def/relations-external#", "isMemberOfCollection");
            e.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "resource",
                    "info:fedora/" + parentCollectionPid);
            desc.getDomNode().appendChild(e);
        }
        if (constituentPid != null) {
            Element e = desc.getDomNode().getOwnerDocument().createElementNS(
                    "info:fedora/fedora-system:def/relations-external#", "isConstituentOf");
            e.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "resource",
                    "info:fedora/" + constituentPid);
            desc.getDomNode().appendChild(e);
        }
        if (derivativeOfPid != null) {
            Element e = desc.getDomNode().getOwnerDocument().createElementNS(
                    "info:fedora/fedora-system:def/relations-external#", "isDerivationOf");
            e.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "resource",
                    "info:fedora/" + derivativeOfPid);
            desc.getDomNode().appendChild(e);
        }
        return rdfDocument;
    }
}
