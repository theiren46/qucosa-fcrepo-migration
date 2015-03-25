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

package org.qucosa.migration;

import gov.loc.mets.MetsDocument;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

import javax.xml.namespace.QName;

public class MetsBean implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {
        Message msg = exchange.getIn();

        MetsDocument metsDocument = MetsDocument.Factory.newInstance();
        MetsDocument.Mets metsRecord = metsDocument.addNewMets();
        addXsiSchemaLocation(metsRecord, "http://www.loc.gov/METS/ http://www.loc.gov/standards/mets/mets.xsd");

        msg.setBody(metsDocument);
    }

    private void addXsiSchemaLocation(MetsDocument.Mets mets, String schemaLocation) {
        mets.newCursor().setAttributeText(
                new QName("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation"),
                schemaLocation);
    }


}
