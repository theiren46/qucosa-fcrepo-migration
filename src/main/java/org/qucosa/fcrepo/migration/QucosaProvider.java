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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.net.URI;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QucosaProvider {

	private static final Logger log = LoggerFactory.getLogger(QucosaProvider.class);

	public static final String WEBAPI_DOCUMENT_RESOURCE_PATH = "/document";
	public static final String WEBAPI_PARAM_QUCOSA_HOST = "qucosa.host";
	public static final String WEBAPI_PARAM_QUCOSA_ROLE = "qucosa.role";
	public static final String DB_PARAM_HOST = "qucosa.db.url";
	public static final String DB_PARAM_USER = "qucosa.db.user";
	public static final String DB_PARAM_PASSWORD = "qucosa.db.passwd";
	private final HttpClient httpClient = new DefaultHttpClient();
	private String host;
	private String role;
	private String dburl;
	private String user;
	private String password;
	private Connection connection;

	public void configure(Configuration conf) throws ConfigurationException, SQLException {
		host = getConfigValueOrThrowException(conf, WEBAPI_PARAM_QUCOSA_HOST);
		role = getConfigValueOrThrowException(conf, WEBAPI_PARAM_QUCOSA_ROLE);
		dburl = getConfigValueOrThrowException(conf, DB_PARAM_HOST);
		user = getConfigValueOrThrowException(conf, DB_PARAM_USER);
		password = getConfigValueOrThrowException(conf, DB_PARAM_PASSWORD);
		connection = connectDb();
	}

	private Connection connectDb() throws SQLException {
		return DriverManager.getConnection(dburl, user, password);
	}

	private String getConfigValueOrThrowException(Configuration conf, String key) throws ConfigurationException {
		String val = conf.getString(key, null);
		if (val == null) {
			throw new ConfigurationException("No config value for " + key);
		}
		return val;
	}

	public Document getXmlDocumentResource(String resourceId) throws Exception {
		Pattern p = Pattern.compile("Opus/Document/(\\d+)");
		Matcher m = p.matcher(resourceId);
		if (!m.matches()) {
			throw new IllegalArgumentException("Not a valid Qucosa document resource identifier: " + resourceId);
		}

		String id = m.group(1);
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

	public void release() {
		try {
			if (connection != null) {
				connection.close();
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

	public List<String> getResources(String pattern) throws SQLException {
		ArrayList<String> names = new ArrayList<>();
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		try {
			stmt = connection.prepareStatement("select name from resources where name like ?");
			stmt.setString(1, pattern);
			resultSet = stmt.executeQuery();
			while (resultSet.next()) names.add(resultSet.getString("name"));
		} finally {
			if (resultSet != null) resultSet.close();
			if (stmt != null) stmt.close();
		}
		return names;
	}

	public List<String> getResourcesOf(String parentResourceName) throws SQLException {
		ArrayList<String> names = new ArrayList<>();
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		try {
			stmt = connection.prepareStatement("select r1.name as name from resources r1, resources r2 where r1.parent_id=r2.id and r2.name=?");
			stmt.setString(1, parentResourceName);
			resultSet = stmt.executeQuery();
			while (resultSet.next()) names.add(resultSet.getString("name"));
		} finally {
			if (resultSet != null) resultSet.close();
			if (stmt != null) stmt.close();
		}
		return names;
	}
}
