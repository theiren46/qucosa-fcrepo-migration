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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author claussni
 * @date 20.02.15.
 */
public class QucosaDocumentID {

    private static final Pattern PATTERN = Pattern.compile("Opus/Document/(\\d+)");

    private final String resourceId;
    private final String id;

    public QucosaDocumentID(String resourceId) {
        Matcher m = PATTERN.matcher(resourceId);
        if (!m.matches()) {
            throw new IllegalArgumentException("Not a valid Qucosa document resource identifier: " + resourceId);
        }
        this.resourceId = resourceId;
        this.id = m.group(1);
    }

    @Override
    public String toString() {
        return resourceId;
    }

    public String getId() {
        return id;
    }
}
