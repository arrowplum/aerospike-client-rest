/*
 * Copyright 2019 Aerospike, Inc.
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
package com.aerospike.restclient;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.List;
import java.util.Map;

class JSONOperationPerformer implements OperationPerformer {

    private static final Logger logger = LogManager.getLogger(JSONOperationPerformer.class);

    private final ObjectMapper mapper;
    private final TypeReference<Map<String, Object>> recordType = new TypeReference<Map<String, Object>>() {
    };

    public JSONOperationPerformer() {
        this.mapper = new ObjectMapper();
    }

    @Override
    public Map<String, Object> performOperationsAndReturn(MockMvc mockMVC, String testEndpoint,
                                                          List<Map<String, Object>> ops) {
        try {
            String jsonString = mapper.writeValueAsString(ops);
            String response = ASTestUtils.performOperationAndReturn(mockMVC, testEndpoint, jsonString);
            return mapper.readValue(response, recordType);
        } catch (Exception e) {
            logger.error("Error performing operation", e);
        }
        return null;
    }

    @Override
    public void performOperationsAndExpect(MockMvc mockMVC, String testEndpoint, List<Map<String, Object>> ops,
                                           ResultMatcher matcher) throws Exception {
        String jsonString = mapper.writeValueAsString(ops);
        ASTestUtils.performOperationAndExpect(mockMVC, testEndpoint, jsonString, matcher);
    }

}
