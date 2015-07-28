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

package org.qucosa.migration.processors.transformations;

import gov.loc.mods.v3.ModsDocument;
import noNamespace.OpusDocument;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

public abstract class MappingProcessor implements Processor {
    public static final String NS_MODS_V3 = "http://www.loc.gov/mods/v3";
    private static final XPath xPath;
    private static final XPathFactory xPathFactory;

    static {
        xPathFactory = XPathFactory.newInstance();
        xPath = xPathFactory.newXPath();
        xPath.setNamespaceContext(new NamespaceContext() {
            @Override
            public String getNamespaceURI(String prefix) {
                switch (prefix) {
                    case "mods":
                        return NS_MODS_V3;
                    default:
                        return XMLConstants.NULL_NS_URI;
                }
            }

            @Override
            public String getPrefix(String namespaceURI) {
                return XMLConstants.DEFAULT_NS_PREFIX;
            }

            @Override
            public Iterator getPrefixes(String namespaceURI) {
                return new ArrayList() {{
                    add(XMLConstants.XML_NS_PREFIX);
                }}.iterator();
            }
        });
    }

    private boolean changes;
    private String label;

    public static XPath getXPath() {
        return xPath;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        Map m = (Map) exchange.getIn().getBody();
        changes = false;
        ModsDocument result = process((OpusDocument) m.get("QUCOSA-XML"), (ModsDocument) m.get("MODS"));
        exchange.getIn().setBody(result);
        exchange.setProperty("MODS_CHANGES", changes);
    }


    public abstract ModsDocument process(OpusDocument opusDocument, ModsDocument modsDocument) throws Exception;

    public String getLabel() {
        if (label == null) {
            String classname = this.getClass().getSimpleName();
            if (classname.endsWith("Processor")) {
                label = classname.substring(0, classname.length() - 9).toLowerCase();
            }
        }
        return label;
    }

    public void signalChanges() {
        this.changes = true;
    }

    public Boolean hasChanges() {
        return this.changes;
    }

    protected XmlObject select(String elementName, XmlObject xmlObject) {
        XmlCursor cursor = xmlObject.newCursor();
        if (cursor.toChild(new QName(NS_MODS_V3, elementName))) {
            return cursor.getObject();
        }
        cursor.dispose();
        return null;
    }

    public String languageEncoding(String code) {
        if (code != null) {
            if (code.length() != 3) {
                return Locale.forLanguageTag(code).getISO3Language();
            }
        }
        return code;
    }

    protected Boolean nodeExists(String expression, XmlObject object) throws XPathExpressionException {
        final XPath xPath = getXPath();
        xPath.reset();
        return (Boolean) xPath.evaluate(expression,
                object.getDomNode().cloneNode(true), XPathConstants.BOOLEAN);
    }
}
