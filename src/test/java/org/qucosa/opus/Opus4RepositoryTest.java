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

package org.qucosa.opus;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.List;

public class Opus4RepositoryTest {

    private Opus4Repository qucosaProvider;

    @Before
    public void setUp() throws SQLException, ConfigurationException {
        Configuration conf = new BaseConfiguration();
        conf.setProperty(Opus4Repository.WEBAPI_PARAM_QUCOSA_HOST, "http://www.example.com");
        conf.setProperty(Opus4Repository.WEBAPI_PARAM_QUCOSA_ROLE, "admin");
        conf.setProperty(Opus4Repository.DB_PARAM_HOST, "jdbc:h2:mem:test;" +
                "INIT=RUNSCRIPT FROM 'classpath:QucosaProviderTest-DB_SETUP.sql' CHARSET 'UTF-8'");
        conf.setProperty(Opus4Repository.DB_PARAM_USER, "test");
        conf.setProperty(Opus4Repository.DB_PARAM_PASSWORD, "test");

        qucosaProvider = new Opus4Repository();
        qucosaProvider.configure(conf);
    }

    @After
    public void tearDown() {
        qucosaProvider.release();
    }

    @Test
    public void listsSubResources() throws SQLException {
        List<OpusResourceID> resources = qucosaProvider.children(OpusResourceID.create("SLUB"));
        Assert.assertFalse(resources.isEmpty());
        Assert.assertTrue(resources.contains(OpusResourceID.create("Opus/Document/10")));
        Assert.assertTrue(resources.contains(OpusResourceID.create("Opus/Document/20")));
    }

    @Test
    public void listsResourcesByPattern() throws SQLException {
        List<OpusResourceID> resources = qucosaProvider.find("%/Document/__");
        Assert.assertFalse(resources.isEmpty());
        Assert.assertEquals(2, resources.size());
    }

    @Test
    public void getsQucosaIdByURN() throws Exception {
        OpusResourceID opusResourceID = qucosaProvider.resolve("urn:nbn:de:bsz:14-qucosa-32825");
        Assert.assertEquals("3282", opusResourceID.getIdentifier());
    }

}
