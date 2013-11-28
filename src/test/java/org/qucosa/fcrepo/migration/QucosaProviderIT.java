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
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.List;

public class QucosaProviderIT {

    private QucosaProvider qucosaProvider;

    @Before
    public void setUp() throws SQLException, ConfigurationException {
        Configuration conf = new SystemConfiguration();
        qucosaProvider = new QucosaProvider();
        qucosaProvider.configure(conf);
    }

    @After
    public void tearDown() {
        qucosaProvider.release();
    }

    @Test
    public void listsSubResources() throws SQLException {
        List<String> resources = qucosaProvider.getResourcesOf("DIU");
        for (String resourceId : resources) {
            System.out.println(resourceId);
        }
    }

    @Test
    public void listsResourcesByPattern() throws SQLException {
        List<String> resources = qucosaProvider.getResources("%/Document/__");
        for (String resourceId : resources) {
            System.out.println(resourceId);
        }
    }

    @Test
    public void retrievesQucosaXmlDocument() throws Exception {
        Assert.assertNotNull(qucosaProvider.getXmlDocumentResource("Opus/Document/37"));
    }

}
