/*
 * Copyright (C) 2013 SLUB Dresden
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.qucosa.fcrepo.migration;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.SystemConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

import static java.lang.System.exit;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            Configuration conf = getConfiguration();

            QucosaProvider qucosaProvider = new QucosaProvider();
            qucosaProvider.configure(conf);

            Document out = qucosaProvider.getXmlDocumentRecord("37");
            printXml(out);

        } catch (Exception e) {
            log.error(e.getMessage());
            exit(1);
        }
    }

    private static void printXml(Document doc) throws TransformerException {
        StringWriter sw = new StringWriter();
        Result out = new StreamResult(sw);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.transform(new DOMSource(doc), out);
        System.out.println(sw.toString());
    }

    private static Configuration getConfiguration() throws ConfigurationException {
        return new SystemConfiguration();
    }

}
