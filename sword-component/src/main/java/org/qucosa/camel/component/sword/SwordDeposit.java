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

public class SwordDeposit {

    private String body;
    private String collection;
    private String contentType;
    private String slug;

    public SwordDeposit(String slug, String body, String contentType, String collection) {
        this.slug = slug;
        this.body = body;
        this.contentType = contentType;
        this.collection = collection;
    }

    public String getBody() {
        return body;
    }

    public String getContentType() {
        return contentType;
    }

    public String getCollection() {
        return collection;
    }

    public String getSlug() {
        return slug;
    }

    @Override
    public String toString() {
        return String.format("[%s],S:'%s',C:'%s',T:'%s',B:'%s'\n",
                super.toString(), slug, collection, contentType, body);
    }
}
