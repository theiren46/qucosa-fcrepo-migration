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

package org.qucosa.camel.component;

import noNamespace.OpusDocument;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.spi.Registry;

/**
 * @author claussni
 * @date 24.03.15.
 */
public class QucosaDocumentProcessor implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        Registry reg = exchange.getContext().getRegistry();
        Message msg = exchange.getIn();

        Opus4Repository repo = (Opus4Repository) reg.lookupByName("qucosaDataSource");
        if (repo == null) {
            throw new Exception("No instance of 'qucosaDataSource' found in context registry.");
        }

        OpusResourceID res = (OpusResourceID) msg.getBody();

        if (res.isDocumentId()) {
            OpusDocument doc = repo.get(res);
            msg.setBody(doc);
        }
    }
}
