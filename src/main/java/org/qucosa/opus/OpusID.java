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

public class OpusID {

    public static final String NS_OPUS_DOCUMENT = "Opus/Document";
    private static final Pattern PATTERN = Pattern.compile(NS_OPUS_DOCUMENT + "/(\\d+)");

    private final String id;

    private OpusID(String id) {
        this.id = id;
    }

    public static OpusID parse(String resourceId) {
        Matcher m = PATTERN.matcher(resourceId);
        if (!m.matches()) {
            throw new IllegalArgumentException("Not a valid Qucosa document resource identifier: " + resourceId);
        }
        return new OpusID(m.group(1));
    }

    @Override
    public String toString() {
        return NS_OPUS_DOCUMENT + "/" + id;
    }

    public String getId() {
        return id;
    }
}
