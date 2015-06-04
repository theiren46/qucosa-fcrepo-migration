package org.qucosa.camel.component.sword;

import gov.loc.mets.MetsDocument;

public class SwordDeposit {

    private String body;
    private MetsDocument metsDocument;

    public SwordDeposit(MetsDocument metsDocument) {
        this.metsDocument = metsDocument;
    }

    public String getBody() {
        if (body == null) {
            body = metsDocument.xmlText();
        }
        return body;
    }

    public String getContentType() {
        return "application/vnd.qucosa.mets+xml";
    }

    public String getCollection() {
        return "qucosa:all";
    }

}
