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

package org.qucosa.camel.component.sword;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.commons.io.IOUtils.toInputStream;

public class SwordConnection {

    public static final String DATA_SOURCE_NAME = "swordConnection";
    private static final Logger log = LoggerFactory.getLogger(SwordConnection.class);
    private HttpClient httpClient;
    private String password;
    private String url;
    private String user;

    public void configure(Configuration conf) throws ConfigurationException {
        url = getConfigValueOrThrowException(conf, "sword.url");
        user = getConfigValueOrThrowException(conf, "sword.user");
        password = getConfigValueOrThrowException(conf, "sword.password");
        httpClient = prepareHttpClient();
    }

    private HttpClient prepareHttpClient() {
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

    private String getConfigValueOrThrowException(Configuration conf, String key) throws ConfigurationException {
        String val = conf.getString(key, null);
        if (val == null) {
            throw new ConfigurationException("No config value for " + key);
        }
        return val;
    }

    public HttpResponse deposit(SwordDeposit deposit, Boolean noop, String slugHeader, String onBehalfOfHeader) throws Exception {
        URIBuilder uriBuilder = new URIBuilder(url + "/" + deposit.getCollection());
        HttpPost httpPost = new HttpPost(uriBuilder.build());
        httpPost.setHeader("X-No-Op", String.valueOf(noop));
        httpPost.setHeader("Content-Type", deposit.getContentType());

        if (onBehalfOfHeader != null && !onBehalfOfHeader.isEmpty()) {
            httpPost.setHeader("X-On-Behalf-Of", onBehalfOfHeader);
        }

        if (slugHeader != null && !slugHeader.isEmpty()) {
            httpPost.setHeader("Slug", slugHeader);
        }

        BasicHttpEntity httpEntity = new BasicHttpEntity();
        httpEntity.setContent(toInputStream(deposit.getBody()));
        httpEntity.setContentType(deposit.getContentType());
        httpPost.setEntity(new BufferedHttpEntity(httpEntity));

        HttpResponse response = httpClient.execute(httpPost);
        EntityUtils.consume(response.getEntity());

        if (log.isDebugEnabled()) {
            if (noop) {
                log.debug("SWORD parameter 'X-No-Op' is '{}'", noop);
                log.debug("SWORD parameter 'X-On-Behalf-Of' is '{}'", onBehalfOfHeader);
                log.debug("Slug header is '{}'", slugHeader);
                log.debug("Content type is '{}'", deposit.getContentType());
                log.debug("Posting to SWORD collection '{}'", deposit.getCollection());
            }
            log.debug(response.toString());
        }

        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_CREATED) {
            String reason = response.getStatusLine().getReasonPhrase();
            throw new Exception(reason);
        }

        return response;
    }

    public HttpResponse update(String pid, SwordDeposit deposit, Boolean noop, String onBehalfOfHeader) throws Exception {
        URIBuilder uriBuilder = new URIBuilder(url + "/" + deposit.getCollection() + "/" + pid);
        HttpPut httpPut = new HttpPut(uriBuilder.build());
        httpPut.setHeader("X-No-Op", String.valueOf(noop));
        httpPut.setHeader("Content-Type", deposit.getContentType());

        if (onBehalfOfHeader != null && !onBehalfOfHeader.isEmpty()) {
            httpPut.setHeader("X-On-Behalf-Of", onBehalfOfHeader);
        }

        BasicHttpEntity httpEntity = new BasicHttpEntity();
        httpEntity.setContent(toInputStream(deposit.getBody()));
        httpEntity.setContentType(deposit.getContentType());
        httpPut.setEntity(new BufferedHttpEntity(httpEntity));

        HttpResponse response = httpClient.execute(httpPut);
        EntityUtils.consume(response.getEntity());

        if (log.isDebugEnabled()) {
            if (noop) {
                log.debug("SWORD parameter 'X-No-Op' is '{}'", noop);
                log.debug("SWORD parameter 'X-On-Behalf-Of' is '{}'", onBehalfOfHeader);
                log.debug("Content type is '{}'", deposit.getContentType());
                log.debug("Posting to SWORD collection '{}'", deposit.getCollection());
            }
            log.debug(response.toString());
        }

        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            String reason = response.getStatusLine().getReasonPhrase();
            throw new Exception(reason);
        }

        return response;
    }
}
