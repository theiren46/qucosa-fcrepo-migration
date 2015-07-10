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

package org.qucosa.migration.processors;

import gov.loc.mets.FileType;
import gov.loc.mets.FileType.FLocat;
import gov.loc.mets.MdSecType;
import gov.loc.mets.MdSecType.MdWrap;
import gov.loc.mets.MetsDocument;
import gov.loc.mets.MetsDocument.Mets;
import gov.loc.mets.MetsType.FileSec.FileGrp;
import gov.loc.mods.v3.ModsDefinition;
import gov.loc.mods.v3.ModsDocument;
import gov.loc.mods.v3.TitleInfoDefinition;
import noNamespace.File;
import noNamespace.Hash;
import noNamespace.OpusDocument;
import noNamespace.Title;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;

import static gov.loc.mets.FileType.FLocat.LOCTYPE.URL;
import static gov.loc.mets.MdSecType.MdWrap.MDTYPE;
import static gov.loc.mets.MetsType.FileSec;

public class MetsGenerator implements Processor {

    public static final String METS_SCHEMA_LOCATION = "http://www.loc.gov/METS/ http://www.loc.gov/standards/mets/mets.xsd";
    public static final String MODS_SCHEMA_LOCATION = "http://www.loc.gov/mods/v3 http://www.loc.gov/standards/mods/v3/mods-3-6.xsd";
    private static final Logger log = LoggerFactory.getLogger(MetsGenerator.class);
    private static final XmlOptions xmlOptions;

    static {
        xmlOptions = new XmlOptions();
        xmlOptions.setSavePrettyPrint();
        xmlOptions.setSaveAggressiveNamespaces();
        xmlOptions.setSaveSuggestedPrefixes(new HashMap() {{
            put("http://www.loc.gov/METS/", "mets");
            put("http://www.loc.gov/mods/v3", "mods");
        }});
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        Message msg = exchange.getIn();

        MetsDocument metsDocument = MetsDocument.Factory.newInstance();
        Mets metsRecord = metsDocument.addNewMets();
        addXsiSchemaLocation(metsRecord, METS_SCHEMA_LOCATION);
        addXsiSchemaLocation(metsRecord, MODS_SCHEMA_LOCATION);

        OpusDocument opusDocument = msg.getBody(OpusDocument.class);
        embedQucosaXml(metsRecord, opusDocument);
        generateBasicMods(metsRecord, opusDocument);

        URL fileUrl = new URL(msg.getHeader("Qucosa-File-Url").toString());
        attachUploadFileSections(metsRecord, opusDocument, fileUrl);

        if (log.isDebugEnabled()) {
            log.debug("\n" + metsDocument.xmlText(xmlOptions));
        }

        msg.setBody(metsDocument);
    }

    private void attachUploadFileSections(Mets metsRecord, OpusDocument opusDocument, java.net.URL baseFileUrl)
            throws Exception {
        FileSec fileSec = metsRecord.addNewFileSec();
        FileGrp fileGrp = fileSec.addNewFileGrp();

        fileGrp.setUSE("ORIGINAL");

        int i = 0;
        for (noNamespace.File opusFile : opusDocument.getOpus().getOpusDocument().getFileArray()) {
            FileType metsFile = fileGrp.addNewFile();
            metsFile.setID("ATT-" + i);
            i++;
            metsFile.setMIMETYPE(opusFile.getMimeType());

            Hash bestHash = selectBestHash(opusFile);
            if (bestHash != null) {
                metsFile.setCHECKSUM(bestHash.getValue());
                metsFile.setCHECKSUMTYPE(
                        FileType.CHECKSUMTYPE.Enum.forString(
                                translateQucosaHashType(bestHash.getType())));
            }

            FLocat fLocat = metsFile.addNewFLocat();
            URI href = buildProperlyEscapedURI(opusDocument, baseFileUrl, opusFile);
            fLocat.setLOCTYPE(URL);
            fLocat.setHref(href.toASCIIString());
            fLocat.setTitle(opusFile.getLabel());

        }

    }

    private String translateQucosaHashType(String qucosaHashType) throws Exception {
        switch (qucosaHashType) {
            case "md5":
                return "MD5";
            case "sha512":
                return "SHA-512";
            default:
                throw new Exception("Unknown hash type: " + qucosaHashType);
        }
    }

    private Hash selectBestHash(File opusFile) {
        Hash bestHash = null;
        for (Hash h : opusFile.getHashValueArray()) {
            if (bestHash == null) {
                bestHash = h;
            } else {
                if (h.getType().equals("sha512")) {
                    bestHash = h;
                }
            }
        }
        return bestHash;
    }

    private URI buildProperlyEscapedURI(OpusDocument opusDocument, URL baseFileUrl, File opusFile)
            throws MalformedURLException, URISyntaxException {
        URL url = new URL(baseFileUrl.toExternalForm()
                + "/"
                + extractOpusDocumentId(opusDocument)
                + "/" + opusFile.getPathName());
        return new URI(
                url.getProtocol(),
                url.getUserInfo(),
                url.getHost(),
                url.getPort(),
                url.getPath(),
                url.getQuery(),
                url.getRef());
    }

    private String extractOpusDocumentId(OpusDocument opusDocument) {
        return opusDocument.getOpus().getOpusDocument().getDocumentId().getDomNode().getFirstChild().getNodeValue();
    }

    private void generateBasicMods(Mets metsDocument, OpusDocument opusDocument) {
        MdSecType dmdSection = metsDocument.addNewDmdSec();
        dmdSection.setID("MODS_XML");
        MdWrap mdWrap = dmdSection.addNewMdWrap();
        mdWrap.setMDTYPE(MDTYPE.MODS);
        mdWrap.setMIMETYPE("application/mods+xml");

        final ModsDocument modsDocument = ModsDocument.Factory.newInstance();
        final ModsDefinition modsRecord = modsDocument.addNewMods();

        String lang;
        String title;

        final Title[] titles = opusDocument.getOpus().getOpusDocument().getTitleMainArray();
        if (titles.length > 0) {
            lang = titles[0].getLanguage();
            title = titles[0].getValue();
        } else {
            lang = "";
            title = "";
        }

        final TitleInfoDefinition titleInfo = modsRecord.addNewTitleInfo();
        titleInfo.setLang(lang);
        titleInfo.setUsage(org.apache.xmlbeans.XmlString.Factory.newValue("primary"));
        titleInfo.addNewTitle().setStringValue(title);

        mdWrap.addNewXmlData().set(modsDocument);
    }

    private void embedQucosaXml(Mets metsRecord, OpusDocument opusDocument) {
        MdSecType dmdSection = metsRecord.addNewDmdSec();
        dmdSection.setID("QUCOSA_XML");
        MdWrap mdWrap = dmdSection.addNewMdWrap();
        mdWrap.setMDTYPE(MDTYPE.OTHER);
        mdWrap.setOTHERMDTYPE("QUCOSA-XML");
        mdWrap.setMIMETYPE("application/xml");
        mdWrap.addNewXmlData().set(opusDocument);
    }

    private void addXsiSchemaLocation(XmlObject xml, String schemaLocation) {
        xml.newCursor().setAttributeText(
                new QName("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation"),
                schemaLocation);
    }

}
