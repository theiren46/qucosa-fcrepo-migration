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

package org.qucosa.fedora;

import com.yourmediashelf.fedora.client.FedoraClient;
import com.yourmediashelf.fedora.client.FedoraClientException;
import com.yourmediashelf.fedora.client.FedoraCredentials;
import com.yourmediashelf.fedora.client.request.*;
import com.yourmediashelf.fedora.client.response.DescribeRepositoryResponse;
import com.yourmediashelf.fedora.client.response.FedoraResponse;
import com.yourmediashelf.fedora.client.response.FindObjectsResponse;
import com.yourmediashelf.fedora.generated.access.FedoraRepository;
import fedora.fedoraSystemDef.foxml.DigitalObjectDocument;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.qucosa.migration.DestinationRepositoryProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;

public class FedoraApiProvider implements DestinationRepositoryProvider<DigitalObjectDocument> {

    public static final String FEDORA_PARAM_BASE_URL = "fedora.url";
    public static final String FEDORA_PARAM_USER = "fedora.user";
    public static final String FEDORA_PARAM_PASSWORD = "fedora.password";
    private static final Logger log = LoggerFactory.getLogger(FedoraApiProvider.class);
    private String user;
    private String password;
    private String host;
    private FedoraClient client;

    public void configure(Configuration conf) throws ConfigurationException, MalformedURLException, FedoraClientException {
        host = getConfigValueOrThrowException(conf, FEDORA_PARAM_BASE_URL);
        user = getConfigValueOrThrowException(conf, FEDORA_PARAM_USER);
        password = getConfigValueOrThrowException(conf, FEDORA_PARAM_PASSWORD);
        client = setupClientConnection();
        describeRepository();
    }

    @Override
    public void ingest(DigitalObjectDocument ingestObject) throws FedoraClientException {
        Ingest ingest = new Ingest();
        ingest.content(ingestObject.newInputStream());
        ingest.execute(client);
    }

    @Override
    public int modifyObjectState(String pid, String state) throws FedoraClientException {
        ModifyObject modifyObjectRequest = new ModifyObject(pid);
        FedoraResponse response = modifyObjectRequest
                .state(state)
                .execute(client);
        return response.getStatus();
    }

    @Override
    public boolean hasObject(String pid) throws FedoraClientException {
        FindObjectsResponse findObjectsResponse = new FindObjects().query("pid%3D" + pid).pid().execute(client);
        return (findObjectsResponse.getPids().size() > 0);
    }

    @Override
    public void purgeObject(String pid) throws FedoraClientException {
        PurgeObject purgeRequest = new PurgeObject(pid);
        purgeRequest.execute(client);
    }

    private void describeRepository() throws FedoraClientException {
        DescribeRepository describeRequest = new DescribeRepository();
        DescribeRepositoryResponse describeResponse = describeRequest.execute(client);
        FedoraRepository repoInfo = describeResponse.getRepositoryInfo();

        log.info("Repository Version : " + repoInfo.getRepositoryVersion());
        log.info("Repository Base URL: " + repoInfo.getRepositoryBaseURL());
        log.info("Repository PID     : " + repoInfo.getRepositoryPID());
        log.info("Repository User    : " + user);
    }

    private String getConfigValueOrThrowException(Configuration conf, String key) throws ConfigurationException {
        String val = conf.getString(key, null);
        if (val == null) {
            throw new ConfigurationException("No config value for " + key);
        }
        return val;
    }

    private FedoraClient setupClientConnection() throws MalformedURLException {
        FedoraCredentials credentials = new FedoraCredentials(host, user, password);
        return new FedoraClient(credentials);
    }

}
