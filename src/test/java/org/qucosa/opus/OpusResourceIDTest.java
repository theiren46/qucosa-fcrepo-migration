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

import org.junit.Test;

import static org.junit.Assert.*;

public class OpusResourceIDTest {

    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionOnEmptyIdentifier() {
        OpusResourceID.create("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionOnNullIdentifier() {
        OpusResourceID.create(null);
    }

    @Test
    public void returnsId() {
        assertEquals("1", OpusResourceID.create("Opus/Document/1").getIdentifier());
    }

    @Test
    public void isIdentifiedAsDocumentId() {
        assertTrue(OpusResourceID.create("Opus/Document/1").isDocumentId());
    }

    @Test
    public void resourceIdWithoutNamespaceIsValid() {
        try {
            OpusResourceID.create("SLUB");
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void serializationOfNamespacedIdentifierCanBeParsedAgain() {
        OpusResourceID opusResourceID = OpusResourceID.create("Opus/Document/1234");
        assertEquals(opusResourceID.toString(), OpusResourceID.create(opusResourceID.toString()).toString());
    }

    @Test
    public void serializationCanBeParsedAgain() {
        OpusResourceID opusResourceID = OpusResourceID.create("SLUB");
        assertEquals(opusResourceID.toString(), OpusResourceID.create(opusResourceID.toString()).toString());
    }

    @Test
    public void implementsEquals() {
        assertEquals(OpusResourceID.create("TEST"), OpusResourceID.create("TEST"));
    }

}