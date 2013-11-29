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

import fedora.fedoraSystemDef.foxml.*;
import org.openarchives.oai.x20.oaiDc.DcDocument;
import org.openarchives.oai.x20.oaiDc.OaiDcType;
import org.purl.dc.elements.x11.ElementType;
import org.w3.x1999.x02.x22RdfSyntaxNs.RDFDocument;
import org.w3c.dom.Element;

public class FedoraObjectBuilder {

	private String pid;
	private String label;
	private String ownerId;
	private String urn;
	private Object parentCollectionPid;

	public DigitalObjectDocument build() {
		DigitalObjectDocument dof = DigitalObjectDocument.Factory.newInstance();
		DigitalObjectDocument.DigitalObject dobj = createDigitalObject(dof);
		addFedoraObjectProperties(dobj);
		addDCDatastream(dobj);
		addRELSEXTDatastream(dobj);
		return dof;
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
		RDFDocument rdfDocument = getRDFCollectionDescription();
		content.set(rdfDocument);
	}

	private RDFDocument getRDFCollectionDescription() {
		RDFDocument rdfDocument = RDFDocument.Factory.newInstance();
		RDFDocument.RDF rdf = rdfDocument.addNewRDF();
		RDFDocument.RDF.Description desc = rdf.addNewDescription();
		desc.setAbout("info:fedora/" + pid);
		Element e = desc.getDomNode().getOwnerDocument().createElementNS(
				"info:fedora/fedora-system:def/relations-external#", "isMemberOfCollection");
		e.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "resource",
				"info:fedora/" + parentCollectionPid);
		desc.getDomNode().appendChild(e);
		return rdfDocument;
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

	private DcDocument getDcDocument() {
		DcDocument dcDocument = DcDocument.Factory.newInstance();
		OaiDcType dc = dcDocument.addNewDc();
		ElementType title = dc.addNewTitle();
		title.setStringValue("Dublin-Core Record for this Object");
		ElementType id = dc.addNewIdentifier();
		id.setStringValue(urn);
		return dcDocument;
	}

	private DigitalObjectDocument.DigitalObject createDigitalObject(DigitalObjectDocument dof) {
		DigitalObjectDocument.DigitalObject dobj = dof.addNewDigitalObject();
		dobj.setVERSION(DigitalObjectType.VERSION.X_1_1);
		dobj.setPID(pid);
		return dobj;
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
}