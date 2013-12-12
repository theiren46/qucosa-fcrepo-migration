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

import com.yourmediashelf.fedora.client.FedoraClientException;
import fedora.fedoraSystemDef.foxml.DigitalObjectDocument;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.SystemConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.exit;

public class Main {

    private static final XPathFactory xPathFactory;
    private static final XPath xPath;

    static {
        xPathFactory = XPathFactory.newInstance();
        xPath = xPathFactory.newXPath();
    }

    private static final Logger log = LoggerFactory.getLogger(Main.class);
    private static final boolean PURGE_WHEN_PRESENT = true;

    public static void main(String[] args) {
        QucosaProvider qucosaProvider = new QucosaProvider();
        FedoraProvider fedoraProvider = new FedoraProvider();
        try {
            Configuration conf = getConfiguration();

            qucosaProvider.configure(conf);
            fedoraProvider.configure(conf);

            //List<String> resourceNames = qucosaProvider.getResources("Opus/Document/%");
            int[] ids = {9375};
            List<String> resourceNames = new ArrayList<>();
            for (int id : ids) resourceNames.add("Opus/Document/" + id);

            log.info("Ingesting " + resourceNames.size() + " objects.");

            for (String resourceName : resourceNames) {
                Document qucosaDoc = qucosaProvider.getXmlDocumentResource(resourceName);
                String pid = "qucosa:" + resourceName.substring(resourceName.lastIndexOf("/") + 1);

                if (fedoraProvider.hasObject(pid)) {
                    if (PURGE_WHEN_PRESENT) {
                        log.info(pid + " exists. Purging...");
                        fedoraProvider.purgeObject(pid);
                    } else {
                        log.info(pid + " exists. Skipping.");
                        continue;
                    }
                }

                doIngest(fedoraProvider, qucosaDoc, pid);

            }
        } catch (Exception e) {
            log.error(e.getMessage());
            exit(1);
        } finally {
            qucosaProvider.release();
        }
    }

    private static void doIngest(FedoraProvider fedoraProvider, Document qucosaDoc, String pid) throws XPathExpressionException, ParserConfigurationException {
        FedoraObjectBuilder fob = new FedoraObjectBuilder();
        fob.setPid(pid);
        fob.setLabel(ats(
                xp("/Opus/Opus_Document/PersonAuthor[1]/LastName", qucosaDoc),
                xp("/Opus/Opus_Document/PersonAuthor[1]/FirstName", qucosaDoc),
                xp("/Opus/Opus_Document/TitleMain[1]/Value", qucosaDoc)));
        fob.setOwnerId("qucosa");
        fob.setUrn(xp("/Opus/Opus_Document/IdentifierUrn[1]/Value", qucosaDoc));
        fob.setParentCollectionPid("qucosa:qucosa");
        fob.setQucosaXmlDocument(qucosaDoc);
        DigitalObjectDocument ingestObject = fob.build();
        try {
            fedoraProvider.ingest(ingestObject);
            log.info("Ingested " + pid);
        } catch (FedoraClientException fe) {
            log.error("Error ingesting " + pid);
        }
    }

    private static String xp(String xpath, Document doc) throws XPathExpressionException {
        XPathExpression expr = xPath.compile(xpath);
        return expr.evaluate(doc);
    }

    private static String ats(String lastName, String firstName, String title) {
        StringBuilder sb = new StringBuilder();
        if ((lastName != null) && (!lastName.isEmpty())) {
            sb.append(lastName);
        }
        if ((firstName != null) && (!firstName.isEmpty())) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(firstName);
        }
        if (sb.length() > 0) sb.append(": ");
        sb.append(title);

        if (sb.length() > 250) {
            sb.setLength(240);
            sb.append("[...]");
        }
        return sb.toString();
    }

    private static Configuration getConfiguration() throws ConfigurationException {
        return new SystemConfiguration();
    }

}
