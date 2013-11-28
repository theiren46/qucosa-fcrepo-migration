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
import org.qucosa.fcrepo.foxml.DigitalObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import static java.lang.System.exit;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        QucosaProvider qucosaProvider = new QucosaProvider();
        try {
            Configuration conf = getConfiguration();
            qucosaProvider.configure(conf);

            DigitalObject fdo = new DigitalObject();
            printXml(fdo);

        } catch (Exception e) {
            log.error(e.getMessage());
            exit(1);
        } finally {
            qucosaProvider.release();
        }
    }

    private static void printXml(DigitalObject fdo) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(DigitalObject.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "info:fedora/fedora-system:def/foxml# " +
                "http://www.fedora.info/definitions/1/0/foxml1-1.xsd");
        marshaller.marshal(fdo, System.out);
    }

    private static Configuration getConfiguration() throws ConfigurationException {
        return new SystemConfiguration();
    }

}
