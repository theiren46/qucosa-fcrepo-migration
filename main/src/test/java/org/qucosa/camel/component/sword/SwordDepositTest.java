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

package org.qucosa.camel.component.sword;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class SwordDepositTest {

    @Test
    public void toString_returns_all_properties() {
        final String slug = "test:1234";
        final String body = "BODY TEXT!";
        final String contentType = "text/plain";
        final String collection = "collection:test";
        SwordDeposit swordDeposit = new SwordDeposit(slug, body, contentType, collection);

        final String s = swordDeposit.toString();
        System.out.println(s);

        assertTrue("Should contain SLUG", s.contains(slug));
        assertTrue("Should contain BODY", s.contains(body));
        assertTrue("Should contain CONTENT-TYPE", s.contains(contentType));
        assertTrue("Should contain COLLECTION", s.contains(collection));
    }

}
