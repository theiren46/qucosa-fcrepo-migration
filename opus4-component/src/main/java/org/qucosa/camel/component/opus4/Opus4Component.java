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

import org.apache.camel.Endpoint;
import org.apache.camel.impl.DefaultComponent;
import org.apache.camel.impl.ProcessorEndpoint;

import java.util.Map;

public class Opus4Component extends DefaultComponent {
    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        if (remaining.startsWith("resources")) {
            return new ProcessorEndpoint(uri, this, new Opus4ResourcesProcessor());
        }
        if (remaining.startsWith("documents")) {
            return new ProcessorEndpoint(uri, this, new Opus4DocumentProcessor());
        }
        throw new Exception("Unknown endpoint URI:" + remaining);
    }

    @Override
    protected void doShutdown() throws Exception {
        ((Opus4DataSource)
                getCamelContext().getRegistry().lookupByName(Opus4DataSource.DATA_SOURCE_NAME)
        ).release();
    }
}
