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

package org.qucosa.camel;

import noNamespace.OpusDocument;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.xmlbeans.XmlOptions;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

/**
 * @author claussni
 * @date 23.03.15.
 */
public class OpusXmlBeansProcessor implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        Message msg = exchange.getIn();
        OpusDocument doc = msg.getBody(OpusDocument.class);
        if (doc != null) {
            dumpToExchange(exchange, doc);
        } else {
            throw new Exception("Cannot parse " + msg.getMessageId());
        }
    }

    private void dumpToExchange(Exchange exchange, OpusDocument doc) throws IOException {
        Writer w = new StringWriter();
        doc.save(w, new XmlOptions().setSavePrettyPrint());
        w.flush();
        exchange.getOut().setBody(w.toString());
    }
}
