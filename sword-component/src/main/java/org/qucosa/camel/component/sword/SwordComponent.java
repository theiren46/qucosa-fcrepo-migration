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

import org.apache.camel.Endpoint;
import org.apache.camel.impl.DefaultComponent;
import org.apache.camel.impl.ProcessorEndpoint;

import java.util.Map;

public class SwordComponent extends DefaultComponent {
    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        if (remaining.startsWith("deposit")) {
            return new ProcessorEndpoint(uri, this, new SwordDepositProcessor(DepositMode.DEPOSIT));
        }
        if (remaining.startsWith("update")) {
            return new ProcessorEndpoint(uri, this, new SwordDepositProcessor(DepositMode.UPDATE));
        }
        throw new Exception("Unknown endpoint URI:" + remaining);
    }

    public enum DepositMode {
        DEPOSIT, UPDATE
    }
}
