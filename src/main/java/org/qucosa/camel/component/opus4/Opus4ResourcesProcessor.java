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

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.spi.Registry;

import java.util.List;

public class Opus4ResourcesProcessor implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        Registry reg = exchange.getContext().getRegistry();
        Opus4DataSource repo = (Opus4DataSource) reg.lookupByName(Opus4DataSource.DATA_SOURCE_NAME);
        if (repo == null) {
            throw new Exception("No instance of " + Opus4DataSource.DATA_SOURCE_NAME +
                    " found in context registry.");
        }

        Message msg = exchange.getIn();
        Opus4ResourceID res = (Opus4ResourceID) msg.getBody();
        List<Opus4ResourceID> resourceIDs = repo.children(res);
        msg.setBody(resourceIDs);
    }
}

