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

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.qucosa.camel.component.opus4.Opus4ResourceID;

public class PurgeFedoraObject implements Processor {

    private final Configuration config;
    private final String fedoraUri;
    private final HttpClient httpClient;

    public PurgeFedoraObject(Configuration configuration) throws ConfigurationException {
        this.config = configuration;
        this.fedoraUri = getConfigValueOrThrowException("fedora.url");

        this.httpClient = prepareHttpClient(
                getConfigValueOrThrowException("fedora.user"),
                getConfigValueOrThrowException("fedora.password"));
    }

    private HttpClient prepareHttpClient(String user, String password) {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();

        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(user, password));

        HttpClient client = HttpClientBuilder.create()
                .setConnectionManager(connectionManager)
                .setDefaultCredentialsProvider(credentialsProvider)
                .build();
        return client;
    }


    private String getConfigValueOrThrowException(String key) throws ConfigurationException {
        String val = config.getString(key, null);
        if (val == null) {
            throw new ConfigurationException("No config value for " + key);
        }
        return val;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        Opus4ResourceID resourceID = exchange.getIn().getBody(Opus4ResourceID.class);

        HttpDelete delete = new HttpDelete(fedoraUri + "/objects/qucosa:" + resourceID.getIdentifier());
        HttpResponse response = httpClient.execute(delete);
        response.getEntity().getContent().close();

        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK
                && response.getStatusLine().getStatusCode() != HttpStatus.SC_NOT_FOUND) {
            String reason = response.getStatusLine().getReasonPhrase();
            exchange.isFailed();
            exchange.setException(new Exception(reason));
        }
    }
}
