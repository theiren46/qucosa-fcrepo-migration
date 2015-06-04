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

package org.qucosa.camel.component.opus4;

import noNamespace.OpusDocument;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Opus4DataSource {

    public static final String WEBAPI_DOCUMENT_RESOURCE_PATH = "/document";
    public static final String WEBAPI_PARAM_QUCOSA_HOST = "qucosa.host";
    public static final String DB_PARAM_HOST = "qucosa.db.url";
    public static final String DB_PARAM_USER = "qucosa.db.user";
    public static final String DB_PARAM_PASSWORD = "qucosa.db.passwd";
    public static final String DATA_SOURCE_NAME = "opus4DataSource";
    private static final Logger log = LoggerFactory.getLogger(Opus4DataSource.class);
    private Connection connection;
    private String dburl;
    private String host;
    private HttpClient httpClient;
    private String password;
    private String user;

    public void configure(Configuration conf) throws ConfigurationException, SQLException {
        host = getConfigValueOrThrowException(conf, WEBAPI_PARAM_QUCOSA_HOST);
        dburl = getConfigValueOrThrowException(conf, DB_PARAM_HOST);
        user = getConfigValueOrThrowException(conf, DB_PARAM_USER);
        password = getConfigValueOrThrowException(conf, DB_PARAM_PASSWORD);
        connection = connectDb();
        httpClient = prepareHttpClient();
    }

    private HttpClient prepareHttpClient() {
        PoolingHttpClientConnectionManager mgr = new PoolingHttpClientConnectionManager();
        mgr.setMaxTotal(200);
        mgr.setDefaultMaxPerRoute(100);
        HttpClient client = HttpClients.createMinimal(mgr);
        return client;
    }

    public OpusDocument get(Opus4ResourceID qid) throws Exception {
        URIBuilder uriBuilder = new URIBuilder(host + WEBAPI_DOCUMENT_RESOURCE_PATH + "/" + qid.getIdentifier());
        HttpGet request = new HttpGet(uriBuilder.build());

        HttpResponse response = httpClient.execute(request);

        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            return OpusDocument.Factory.parse(response.getEntity().getContent());
        } else {
            String reason = response.getStatusLine().getReasonPhrase();
            EntityUtils.consume(response.getEntity());
            throw new Exception(reason);
        }
    }

    public Opus4ResourceID resolve(String pattern) throws SQLException {
        Opus4ResourceID opus4ResourceID = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try {
            stmt = connection.prepareStatement("select document_id from document_identifiers where type='urn' and value=?");
            stmt.setString(1, pattern);
            resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                opus4ResourceID = Opus4ResourceID.create(Opus4ResourceID.NS_OPUS_DOCUMENT + "/" + resultSet.getString(1));
            }
        } finally {
            if (resultSet != null) resultSet.close();
            if (stmt != null) stmt.close();
        }
        return opus4ResourceID;
    }

    public List<Opus4ResourceID> children(Opus4ResourceID opus4ResourceID) throws SQLException {
        ArrayList<Opus4ResourceID> opus4ResourceIDs = new ArrayList<>();
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try {
            stmt = connection.prepareStatement(
                    "select r1.name as name" +
                            " from resources r1, resources r2" +
                            " where r1.parent_id=r2.id and r2.name=?");
            stmt.setString(1, opus4ResourceID.toString());
            resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                opus4ResourceIDs.add(Opus4ResourceID.create(resultSet.getString("name")));
            }
        } finally {
            if (resultSet != null) resultSet.close();
            if (stmt != null) stmt.close();
        }
        return opus4ResourceIDs;
    }

    public List<Opus4ResourceID> find(String pattern) throws SQLException {
        ArrayList<Opus4ResourceID> names = new ArrayList<>();
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try {
            stmt = connection.prepareStatement("select name from resources where name like ?");
            stmt.setString(1, pattern);
            resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                names.add(Opus4ResourceID.create(resultSet.getString("name")));
            }
        } finally {
            if (resultSet != null) resultSet.close();
            if (stmt != null) stmt.close();
        }
        return names;
    }

    public void release() {
        try {
            if (connection != null) {
                connection.close();
                log.debug("Closed database connection to " + dburl);
            }
        } catch (SQLException e) {
            log.warn("Failed to close database connection: " + e.getMessage());
        }
    }

    @Override
    protected void finalize() throws Throwable {
        release();
        super.finalize();
    }

    private Connection connectDb() throws SQLException {
        final Connection connection = DriverManager.getConnection(dburl, user, password);
        log.debug("Established JDBC connection to " + dburl);
        return connection;
    }

    private String getConfigValueOrThrowException(Configuration conf, String key) throws ConfigurationException {
        String val = conf.getString(key, null);
        if (val == null) {
            throw new ConfigurationException("No config value for " + key);
        }
        return val;
    }

}
