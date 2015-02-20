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

package org.qucosa.migration;

import com.yourmediashelf.fedora.client.FedoraClientException;
import fedora.fedoraSystemDef.foxml.DigitalObjectDocument;
import fedora.fedoraSystemDef.foxml.StateType;
import org.qucosa.fedora.FedoraObjectBuilder;
import org.qucosa.opus.SourceRepositoryOpus4Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.List;

/**
 * @author claussni
 * @date 20.02.15.
 */
public class RepositoryMigrator {

    private static final Logger log = LoggerFactory.getLogger(RepositoryMigrator.class);

    private static final XPathFactory xPathFactory;
    private static final XPath xPath;

    static {
        xPathFactory = XPathFactory.newInstance();
        xPath = xPathFactory.newXPath();
    }


    public void migrateQucosaDocuments(SourceRepositoryOpus4Provider qucosaProvider, DestinationRepositoryProvider destinationRepositoryProvider, List<String> resourceNames, boolean purgeWhenPresent) throws Exception {
        log.info("Migrating " + resourceNames.size() + " objects.");
        for (String resourceName : resourceNames) {
            Document qucosaDoc = qucosaProvider.getXmlDocumentResource(resourceName);
            String pid = "qucosa:" + resourceName.substring(resourceName.lastIndexOf("/") + 1);
            if (destinationRepositoryProvider.hasObject(pid)) {
                if (purgeWhenPresent) {
                    log.trace(pid + " exists. Purging...");
                    destinationRepositoryProvider.purgeObject(pid);
                } else {
                    log.info(pid + " exists. Skipping.");
                    continue;
                }
            }
            try {
                doIngest(destinationRepositoryProvider, qucosaProvider, qucosaDoc, pid);
            } catch (Exception ex) {
                log.error("Ingesting " + pid + " failed: " + ex.getMessage());
            }
        }
    }

    private void doIngest(DestinationRepositoryProvider destinationRepositoryProvider, SourceRepositoryProvider sourceRepositoryProvider, Document qucosaDoc, String pid) throws Exception {
        FedoraObjectBuilder fob = new FedoraObjectBuilder();
        fob.setPid(pid);
        fob.setLabel(ats(
                xp("/Opus/Opus_Document/PersonAuthor[1]/LastName", qucosaDoc),
                xp("/Opus/Opus_Document/PersonAuthor[1]/FirstName", qucosaDoc),
                xp("/Opus/Opus_Document/TitleMain[1]/Value", qucosaDoc)));
        fob.setTitle(xp("/Opus/Opus_Document/TitleMain[1]/Value", qucosaDoc));
        fob.setOwnerId("qucosa");
        fob.setUrn(xp("/Opus/Opus_Document/IdentifierUrn[1]/Value", qucosaDoc));
        fob.setParentCollectionPid("qucosa:qucosa");
        fob.setConstituentPid(determineConstituentPid(qucosaDoc, sourceRepositoryProvider));
        fob.isDerivativeOfPid(determinePredecessorPid(qucosaDoc, sourceRepositoryProvider));
        fob.setQucosaXmlDocument(qucosaDoc);
        DigitalObjectDocument ingestObject = fob.build();
        try {
            destinationRepositoryProvider.ingest(ingestObject);

            // Object state can only be set after ingesting
            StateType.Enum objstate = mapServerstate(xp("/Opus/Opus_Document/ServerState", qucosaDoc));
            int returnVal = destinationRepositoryProvider.modifyObjectState(pid, objstate.toString());
            if (returnVal == 200) {
                log.debug("Set object state of {} to {}", pid, objstate);
            }

            log.info("Ingested {}", pid);
        } catch (FedoraClientException fe) {
            log.error("Error ingesting {}: {}", pid, fe.getMessage());
        }
    }

    private StateType.Enum mapServerstate(String serverState) {
        switch (serverState) {
            case "published":
                return StateType.A;
            case "deleted":
                return StateType.D;
            case "unpublished":
            default:
                return StateType.I;
        }
    }

    private String determinePredecessorPid(Document qucosaDoc, SourceRepositoryProvider sourceRepositoryProvider) throws Exception {
        String predecessorPid = null;
        String referenceUrn = xp("/Opus/Opus_Document/ReferenceUrn[1][Relation='predecessor']/Value", qucosaDoc);
        if (referenceUrn != null && !referenceUrn.isEmpty()) {
            predecessorPid = sourceRepositoryProvider.getQucosaIdByURN(referenceUrn);
            if (predecessorPid != null) {
                predecessorPid = "qucosa:" + predecessorPid;
            }
        }
        return predecessorPid;
    }

    private String determineConstituentPid(Document qucosaDoc, SourceRepositoryProvider sourceRepositoryProvider) throws Exception {
        String referencePid = null;
        String referenceUrn = xp("/Opus/Opus_Document/ReferenceUrn[1][" +
                "Relation='journal' or " +
                "Relation='issue' or " +
                "Relation='proceeding' or " +
                "Relation='series' or " +
                "Relation='book']/Value", qucosaDoc);
        if (referenceUrn != null && !referenceUrn.isEmpty()) {
            referencePid = sourceRepositoryProvider.getQucosaIdByURN(referenceUrn);
            if (referencePid != null) {
                referencePid = "qucosa:" + referencePid;
            }
        }
        return referencePid;
    }

    private String xp(String xpath, Document doc) throws XPathExpressionException {
        XPathExpression expr = xPath.compile(xpath);
        return expr.evaluate(doc);
    }

    private String ats(String lastName, String firstName, String title) {
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


}
