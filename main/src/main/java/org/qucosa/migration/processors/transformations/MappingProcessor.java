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

import de.slubDresden.InfoDocument;
import gov.loc.mods.v3.ModsDocument;
import noNamespace.OpusDocument;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;

import javax.xml.xpath.XPathExpressionException;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public abstract class MappingProcessor implements Processor {
    public static final String NS_MODS_V3 = "http://www.loc.gov/mods/v3";
    public static final String NS_SLUB = "http://slub-dresden.de/";
    public static final String NS_FOAF = "http://xmlns.com/foaf/0.1/";
    public static final String NS_RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    public static final String MODS_CHANGES = "MODS_CHANGES";
    public static final String SLUB_INFO_CHANGES = "SLUB-INFO_CHANGES";
    private static final String xpathNSDeclaration =
            "declare namespace mods='" + NS_MODS_V3 + "'; " +
                    "declare namespace slub='" + NS_SLUB + "'; " +
                    "declare namespace foaf='" + NS_FOAF + "'; ";

    private String label;
    private boolean modsChanges;
    private boolean slubChanges;

    @Override
    public void process(Exchange exchange) throws Exception {
        Map m = (Map) exchange.getIn().getBody();

        modsChanges = (boolean) exchange.getProperty(MODS_CHANGES, false);
        slubChanges = (boolean) exchange.getProperty(SLUB_INFO_CHANGES, false);

        process((OpusDocument) m.get("QUCOSA-XML"),
                (ModsDocument) m.get("MODS"),
                (InfoDocument) m.get("SLUB-INFO"));

        exchange.getIn().setBody(m);
        exchange.setProperty(MODS_CHANGES, modsChanges);
        exchange.setProperty(SLUB_INFO_CHANGES, slubChanges);
    }


    public abstract void process(
            OpusDocument opusDocument,
            ModsDocument modsDocument,
            InfoDocument infoDocument) throws Exception;

    public String getLabel() {
        if (label == null) {
            String classname = this.getClass().getSimpleName();
            if (classname.endsWith("Processor")) {
                label = classname.substring(0, classname.length() - 9).toLowerCase();
            }
        }
        return label;
    }

    public void signalChanges(String dsid) {
        if (dsid.equals(MODS_CHANGES)) {
            this.modsChanges = true;
        } else if (dsid.equals(SLUB_INFO_CHANGES)) {
            this.slubChanges = true;
        }
    }

    public Boolean hasChanges() {
        return this.modsChanges;
    }

    protected XmlObject select(String query, XmlObject xmlObject) {
        XmlCursor cursor = xmlObject.newCursor();
        cursor.selectPath(xpathNSDeclaration + query);
        XmlObject result = cursor.toNextSelection() ? cursor.getObject() : null;
        cursor.dispose();
        return result;
    }

    protected List<XmlObject> selectAll(String query, XmlObject xmlObject) {
        List<XmlObject> results = new ArrayList<>();
        XmlCursor cursor = xmlObject.newCursor();
        cursor.selectPath(xpathNSDeclaration + query);
        while (cursor.toNextSelection()) {
            results.add(cursor.getObject());
        }
        cursor.dispose();
        return results;
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
        return (select(expression, object) != null);
    }

    protected String dateEncoding(BigInteger year) {
        if (year == null) return null;

        DateFormat dateFormat = new SimpleDateFormat("yyyy");
        Calendar cal = new GregorianCalendar();
        cal.set(Calendar.YEAR, year.intValue());
        return dateFormat.format(cal.getTime());
    }

    protected String dateEncoding(noNamespace.Date date) {
        if (!(date.isSetYear() && date.isSetMonth() && date.isSetDay())) {
            return null;
        }

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        TimeZone tz;
        GregorianCalendar cal;
        if (date.getTimezone() != null) {
            tz = SimpleTimeZone.getTimeZone(date.getTimezone());
            cal = new GregorianCalendar(tz);
        } else {
            cal = new GregorianCalendar();
        }

        cal.set(Calendar.YEAR, date.getYear().intValue());
        cal.set(Calendar.MONTH, date.getMonth().intValue() - 1);
        cal.set(Calendar.DAY_OF_MONTH, date.getDay().intValue());

        return dateFormat.format(cal.getTime());
    }

    protected String qq(String s) {
        return s.replace("'", "''");
    }
}
