/*
 * Copyright 2020 Aerospike, Inc.
 *
 * Portions may be licensed to Aerospike, Inc. under one or more contributor
 * license agreements WHICH ARE COMPATIBLE WITH THE APACHE LICENSE, VERSION 2.0.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.aerospike.restclient.service;

import com.aerospike.client.AerospikeClient;
import com.aerospike.client.Operation;
import com.aerospike.client.policy.Policy;
import com.aerospike.client.policy.WritePolicy;
import com.aerospike.client.query.PredExp;
import com.aerospike.client.query.Statement;
import com.aerospike.client.task.ExecuteTask;
import com.aerospike.restclient.domain.RestClientExecuteTask;
import com.aerospike.restclient.domain.RestClientExecuteTaskStatus;
import com.aerospike.restclient.domain.RestClientOperation;
import com.aerospike.restclient.util.AerospikeAPIConstants;
import com.aerospike.restclient.util.converters.OperationsConverter;
import com.aerospike.restclient.util.converters.PolicyValueConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AerospikeExecuteServiceV1 implements AerospikeExecuteService {

    @Autowired
    private AerospikeClient client;

    @Override
    public RestClientExecuteTask executeScan(String namespace, String set, List<RestClientOperation> opsList,
                                             WritePolicy policy, Map<String, String> requestParams) {
        Statement stmt = new Statement();
        stmt.setNamespace(namespace);
        stmt.setSetName(set);

        if (requestParams.containsKey(AerospikeAPIConstants.RECORD_BINS)) {
            String[] binNames = requestParams.get(AerospikeAPIConstants.RECORD_BINS).split(",");
            stmt.setBinNames(binNames);
        }
        if (requestParams.containsKey(AerospikeAPIConstants.RECORDS_PER_SECOND)) {
            int recordsPerSecond = PolicyValueConverter.getIntValue(requestParams.get(AerospikeAPIConstants.RECORDS_PER_SECOND));
            stmt.setRecordsPerSecond(recordsPerSecond);
        }
        if (requestParams.containsKey(AerospikeAPIConstants.PRED_EXP)) {
            PredExp[] predExps = PolicyValueConverter.getPredExp(requestParams.get(AerospikeAPIConstants.PRED_EXP));
            stmt.setPredExp(predExps);
        }

        List<Map<String, Object>> opsMapsList = opsList.stream().map(RestClientOperation::toMap).collect(Collectors.toList());
        Operation[] operations = OperationsConverter.mapListToOperationsArray(opsMapsList);

        ExecuteTask task = client.execute(policy, stmt, operations);
        RestClientExecuteTask restClientTask = new RestClientExecuteTask(task.getTaskId(), true);

        return restClientTask;
    }

    @Override
    public RestClientExecuteTaskStatus queryScanStatus(String taskId) {
        long id = PolicyValueConverter.getLongValue(taskId);

        Statement statement = new Statement();
        statement.setTaskId(id);

        ExecuteTask task = new ExecuteTask(client.getCluster(), new Policy(), statement);
        int status = task.queryStatus();

        return new RestClientExecuteTaskStatus(
                new RestClientExecuteTask(id, true),
                status
        );
    }
}
