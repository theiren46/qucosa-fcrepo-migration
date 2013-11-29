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
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringWriter;

public class FedoraObjectBuilderTest {

	@Test
	public void buildDocument() throws IOException, SAXException {
		FedoraObjectBuilder fob = new FedoraObjectBuilder();
		fob.setPid("qucosa:4711");
		fob.setUrn("urn:de:slub-dresden:" + fob.getPid());
		fob.setLabel("An arbitrarily migrated Qucosa Document");
		fob.setOwnerId("slub");
		fob.setParentCollectionPid("qucosa:slub");
		DigitalObjectDocument d = fob.build();
		StringWriter sw = new StringWriter();
		d.save(sw);

		XMLUnit.setIgnoreWhitespace(true);
		Document ctrl = XMLUnit.buildControlDocument(new InputSource(
				getClass().getResourceAsStream("/FedoraObjectBuilderTest-fo.xml")));
		Document test = XMLUnit.buildTestDocument(sw.toString());

		XMLAssert.assertXMLEqual(ctrl, test);
	}

}
