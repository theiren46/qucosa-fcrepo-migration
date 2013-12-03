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

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.SystemConfiguration;
import org.custommonkey.xmlunit.XMLAssert;
import org.junit.*;
import org.w3c.dom.Document;

import java.sql.SQLException;

public class QucosaProviderIT {

    private QucosaProvider qucosaProvider;

    @Before
    public void setUp() throws ConfigurationException, SQLException {
        Configuration conf = new SystemConfiguration();
        qucosaProvider = new QucosaProvider();
        qucosaProvider.configure(conf);
    }

    @After
    public void tearDown() {
        qucosaProvider.release();
    }

    @Test
    public void retrievesOpusVersion2Document() throws Exception {
        Document doc = qucosaProvider.getXmlDocumentResource("Opus/Document/37");
        XMLAssert.assertXpathEvaluatesTo("2.0","/Opus/@version", doc);
    }

    @Test
    public void retrievesDocumentWithCorrectId() throws Exception {
        Document doc = qucosaProvider.getXmlDocumentResource("Opus/Document/37");
        XMLAssert.assertXpathEvaluatesTo("37","//DocumentId", doc);
    }

}
