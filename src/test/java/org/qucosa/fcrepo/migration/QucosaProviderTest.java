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

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.junit.*;

import java.sql.SQLException;
import java.util.List;

public class QucosaProviderTest {

    private QucosaProvider qucosaProvider;

    @Before
    public void setUp() throws SQLException, ConfigurationException {
        Configuration conf = new BaseConfiguration();
        conf.setProperty(QucosaProvider.WEBAPI_PARAM_QUCOSA_HOST, "http://www.example.com");
        conf.setProperty(QucosaProvider.WEBAPI_PARAM_QUCOSA_ROLE, "admin");
        conf.setProperty(QucosaProvider.DB_PARAM_HOST, "jdbc:h2:mem:test;" +
                "INIT=RUNSCRIPT FROM 'classpath:QucosaProviderTest-DB_SETUP.sql' CHARSET 'UTF-8'");
        conf.setProperty(QucosaProvider.DB_PARAM_USER, "test");
        conf.setProperty(QucosaProvider.DB_PARAM_PASSWORD, "test");

        qucosaProvider = new QucosaProvider();
        qucosaProvider.configure(conf);
    }

    @After
    public void tearDown() {
        qucosaProvider.release();
    }

    @Test
    public void listsSubResources() throws SQLException {
        List<String> resources = qucosaProvider.getResourcesOf("SLUB");
        Assert.assertFalse(resources.isEmpty());
        Assert.assertTrue(resources.contains("Opus/Document/10"));
        Assert.assertTrue(resources.contains("Opus/Document/20"));
    }

    @Test
    public void listsResourcesByPattern() throws SQLException {
        List<String> resources = qucosaProvider.getResources("%/Document/__");
        Assert.assertFalse(resources.isEmpty());
        Assert.assertEquals(2, resources.size());
    }

}
