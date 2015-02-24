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

import noNamespace.OpusDocument;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.SystemConfiguration;
import org.custommonkey.xmlunit.XMLAssert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;

public class OpusRepositoryIT {

    private Opus4ImmutableRepository opus4Repository;

    @Before
    public void setUp() throws ConfigurationException, SQLException {
        Configuration conf = new SystemConfiguration();
        opus4Repository = new Opus4ImmutableRepository();
        opus4Repository.configure(conf);
    }

    @After
    public void tearDown() {
        opus4Repository.release();
    }

    @Test
    public void retrievesOpusVersion2Document() throws Exception {
        OpusDocument doc = opus4Repository.get(OpusResourceID.create("Opus/Document/37"));
        XMLAssert.assertEquals("2.0", doc.getOpus().getVersion());
    }

    @Test
    public void retrievesDocumentWithCorrectId() throws Exception {
        OpusDocument doc = opus4Repository.get(OpusResourceID.create("Opus/Document/37"));
        final String id = doc.getOpus().getOpusDocument().getDocumentId().newCursor().getTextValue();
        XMLAssert.assertEquals("37", id);
    }

}
