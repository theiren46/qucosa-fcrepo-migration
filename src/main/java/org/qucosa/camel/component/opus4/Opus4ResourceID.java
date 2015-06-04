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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Opus4ResourceID {

    public static final String NS_OPUS_DOCUMENT = "Opus/Document";
    private static final Pattern PATTERN = Pattern.compile(NS_OPUS_DOCUMENT + "/(\\d+)");
    private final String identifier;
    private final String namespace;

    private Opus4ResourceID(String namespace, String id) {
        this.namespace = namespace;
        this.identifier = id;
    }

    public static Opus4ResourceID create(String resourceId) {
        if (resourceId == null || resourceId.isEmpty()) {
            throw new IllegalArgumentException("Not a valid Opus resource identifier: " + resourceId);
        }

        String ns, id;
        if (resourceId.startsWith(NS_OPUS_DOCUMENT)) {
            Matcher m = PATTERN.matcher(resourceId);
            if (!m.matches()) {
                throw new IllegalArgumentException("Not a valid Opus document resource identifier: " + resourceId);
            }
            ns = NS_OPUS_DOCUMENT;
            id = m.group(1);
        } else {
            ns = "";
            id = resourceId;
        }

        return new Opus4ResourceID(ns, id);
    }

    @Override
    public String toString() {
        return (namespace.isEmpty()) ? identifier : NS_OPUS_DOCUMENT + "/" + identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    public boolean isDocumentId() {
        return (!namespace.isEmpty());
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Opus4ResourceID) && (toString().equals(obj.toString()));
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}
