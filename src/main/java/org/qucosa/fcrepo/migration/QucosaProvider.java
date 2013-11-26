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
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.net.URI;

public class QucosaProvider {

    public static final String WEBAPI_DOCUMENT_RESOURCE_PATH = "/document";
    public static final String WEBAPI_PARAM_QUCOSA_HOST = "qucosa.host";
    public static final String WEBAPI_PARAM_QUCOSA_ROLE = "qucosa.role";
    private final HttpClient httpClient = new DefaultHttpClient();
    private String host;
    private String role;

    public void configure(Configuration conf) throws ConfigurationException {
        host = getConfigValueOrThrowException(conf, WEBAPI_PARAM_QUCOSA_HOST);
        role = getConfigValueOrThrowException(conf, WEBAPI_PARAM_QUCOSA_ROLE);
    }

    private String getConfigValueOrThrowException(Configuration conf, String key) throws ConfigurationException {
        String val = conf.getString(key, null);
        if (val == null) {
            throw new ConfigurationException("No config value for " + key);
        }
        return val;
    }

    public Document getXmlDocumentRecord(String id) throws Exception {
        URI uri = new URI(host + WEBAPI_DOCUMENT_RESOURCE_PATH + "/" + id);
        HttpGet request = new HttpGet(uri);

        request.setParams(new BasicHttpParams().setParameter("role", role));

        HttpResponse response = httpClient.execute(request);

        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(response.getEntity().getContent());
        } else {
            throw new Exception(response.getStatusLine().getReasonPhrase());
        }
    }

}
