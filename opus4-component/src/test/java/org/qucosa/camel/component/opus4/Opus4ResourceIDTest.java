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

package org.qucosa.camel.component.opus4;

import org.junit.Test;

import static org.junit.Assert.*;

public class Opus4ResourceIDTest {

    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionOnEmptyIdentifier() {
        Opus4ResourceID.create("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionOnNullIdentifier() {
        Opus4ResourceID.create(null);
    }

    @Test
    public void returnsId() {
        assertEquals("1", Opus4ResourceID.create("Opus/Document/1").getIdentifier());
    }

    @Test
    public void isIdentifiedAsDocumentId() {
        assertTrue(Opus4ResourceID.create("Opus/Document/1").isDocumentId());
    }

    @Test
    public void resourceIdWithoutNamespaceIsValid() {
        try {
            Opus4ResourceID.create("SLUB");
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void serializationOfNamespacedIdentifierCanBeParsedAgain() {
        Opus4ResourceID opus4ResourceID = Opus4ResourceID.create("Opus/Document/1234");
        assertEquals(opus4ResourceID.toString(), Opus4ResourceID.create(opus4ResourceID.toString()).toString());
    }

    @Test
    public void serializationCanBeParsedAgain() {
        Opus4ResourceID opus4ResourceID = Opus4ResourceID.create("SLUB");
        assertEquals(opus4ResourceID.toString(), Opus4ResourceID.create(opus4ResourceID.toString()).toString());
    }

    @Test
    public void implementsEquals() {
        assertEquals(Opus4ResourceID.create("TEST"), Opus4ResourceID.create("TEST"));
    }

}