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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.aerospike.client.AerospikeClient;
import com.aerospike.client.AerospikeException;
import com.aerospike.client.admin.Role;
import com.aerospike.restclient.domain.RestClientUserModel;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

public class UserTestsError {

	List<String> createdUsers;
	List<String> createdRoles = Arrays.asList(Role.SysAdmin, Role.DataAdmin, Role.Read);
	TypeReference<List<Map<String,Object>>> userListType= new TypeReference<List<Map<String,Object>>>() {};
	TypeReference<Map<String,Object>> userType= new TypeReference<Map<String,Object>>() {};
	String userName = "JunitUser";

	private final String endpoint = "/v1/admin/user";

	@Autowired
	private ObjectMapper objectMapper;

	private MockMvc mockMVC;

	@Autowired
	private WebApplicationContext wac;

	@Autowired
	AerospikeClient client;

	@BeforeAll
	public static void okToRun() {
		assumeTrue(ASTestUtils.runningWithAuth());
	}

	@BeforeEach
	public void setUp() {
		mockMVC = MockMvcBuilders.webAppContextSetup(wac).build();
		createdUsers = new ArrayList<>();
	}

	@AfterEach
	public void tearDown() {
		for (String user: createdUsers) {
			try {
				client.dropUser(null, user);
				Thread.sleep(500);
			} catch (AerospikeException | InterruptedException e) {}
		}
	}

	@Test
	public void getUserThatDoesNotExist() throws Exception {
		assumeFalse(ClusterUtils.isSecurityEnabled(client));
		/* Get information on the user we just created*/
		mockMVC.perform(get(endpoint +"/" + "notARealUser")
				.contentType(MediaType.APPLICATION_JSON))
		.andExpect(status().isNotFound());
	}

	@Test
	public void deleteUserThatDoesNotExist() throws Exception {
		assumeFalse(ClusterUtils.isSecurityEnabled(client));
		/* Get information on the user we just created*/
		mockMVC.perform(delete(endpoint +"/" + "notARealUser")
				.contentType(MediaType.APPLICATION_JSON))
		.andExpect(status().isNotFound());
	}

	@Test
	public void patchUserThatDoesNotExist() throws Exception {
		assumeFalse(ClusterUtils.isSecurityEnabled(client));
		String newPassword = "SuperSecret";
		mockMVC.perform(patch(endpoint + "/" + "notARealUser")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(newPassword)))
		.andExpect(status().isNotFound());
	}

	@Test
	public void createUserWithoutName() throws Exception {
		String newPass = "topSecret";
		String[] roles = {Role.ReadWrite, Role.ReadWriteUdf};

		RestClientUserModel userModel = new RestClientUserModel();
		userModel.setPassword(newPass);
		userModel.setRoles(roles);

		mockMVC.perform(post(endpoint)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(userModel)))
		.andExpect(status().isBadRequest());
	}

	@Test
	public void createUserWithoutPassword() throws Exception {
		String newUser = "name";
		String[] roles = {Role.ReadWrite, Role.ReadWriteUdf};

		RestClientUserModel userModel = new RestClientUserModel();
		userModel.setUsername(newUser);
		userModel.setRoles(roles);

		mockMVC.perform(post(endpoint)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(userModel)))
		.andExpect(status().isBadRequest());
	}

	@Test
	public void createUserWithoutRoles() throws Exception {
		String newPass = "topSecret";
		String newUser = "name";

		RestClientUserModel userModel = new RestClientUserModel();
		userModel.setUsername(newUser);
		userModel.setPassword(newPass);

		mockMVC.perform(post(endpoint)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(userModel)))
		.andExpect(status().isBadRequest());
	}

	@Test
	public void PatchUserWithNoBody() throws Exception {
		/* Try to change a user's password without specifying a password */
		mockMVC.perform(patch(endpoint + "/userName")
				.contentType(MediaType.APPLICATION_JSON))
		.andExpect(status().isBadRequest());
	}

	@Test
	public void addRolesToNonExistentUser() throws Exception {
		assumeFalse(ClusterUtils.isSecurityEnabled(client));
		String[] roles = {Role.ReadWrite, Role.ReadWriteUdf};

		mockMVC.perform(post(endpoint + "/notARealUser/role")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(roles)))
		.andExpect(status().isNotFound());
	}

	@Test
	public void deleteRolesFromNonExistentUser() throws Exception {
		assumeFalse(ClusterUtils.isSecurityEnabled(client));
		String[] roles = {Role.ReadWrite, Role.ReadWriteUdf};

		mockMVC.perform(patch(endpoint + "/notARealUser/role/delete")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(roles)))
		.andExpect(status().isNotFound());
	}
}
