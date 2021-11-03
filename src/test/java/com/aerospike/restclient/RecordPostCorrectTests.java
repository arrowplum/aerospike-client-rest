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

import com.aerospike.client.AerospikeClient;
import com.aerospike.client.AerospikeException;
import com.aerospike.client.Key;
import com.aerospike.client.Record;
import com.aerospike.client.Value;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.msgpack.jackson.dataformat.MessagePackFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class RecordPostCorrectTests {

	private PostPerformer postPerformer;
	private MockMvc mockMVC;

	private Key testKey;
	private Key intKey;
	private Key bytesKey;
	private String testEndpoint;
	private String digestEndpoint;
	private String intEndpoint;
	private String bytesEndpoint;

	@Autowired
	private AerospikeClient client;

	@Autowired
	private WebApplicationContext wac;

	@BeforeEach
	public void setup() {
		mockMVC = MockMvcBuilders.webAppContextSetup(wac).build();
	}

	@AfterEach
	public void clean() {
		client.delete(null, testKey);
		try {
			client.delete(null, bytesKey);
		} catch (AerospikeException ignore) {
		}
		try {
			client.delete(null, intKey);
		} catch (AerospikeException ignore) {
		}
	}

	private static Stream<Arguments> getParams() {
		return Stream.of(
				Arguments.of(new JSONPostPerformer(MediaType.APPLICATION_JSON.toString(), new ObjectMapper()), true),
				Arguments.of(new MsgPackPostPerformer("application/msgpack", new ObjectMapper(new MessagePackFactory())), true),
				Arguments.of(new JSONPostPerformer(MediaType.APPLICATION_JSON.toString(), new ObjectMapper()), false),
				Arguments.of(new MsgPackPostPerformer("application/msgpack", new ObjectMapper(new MessagePackFactory())), false)
		);
	}

	@ParameterizedTest
	@MethodSource("getParams")
	void addParams(PostPerformer performer, boolean useSet) {
		if (useSet) {
			this.testEndpoint = ASTestUtils.buildEndpoint("kvs", "test", "junit", "getput");
			this.testKey = new Key("test", "junit", "getput");
			this.intKey = new Key("test", "junit", 1);
			this.bytesKey = new Key("test", "junit", new byte[] {1, 127, 127, 1});

			String urlDigest = Base64.getUrlEncoder().encodeToString(testKey.digest);
			digestEndpoint = ASTestUtils.buildEndpoint("kvs", "test", "junit", urlDigest) + "?keytype=DIGEST";

			String urlBytes = Base64.getUrlEncoder().encodeToString((byte[]) bytesKey.userKey.getObject());
			bytesEndpoint = ASTestUtils.buildEndpoint("kvs", "test", "junit", urlBytes) + "?keytype=BYTES";

			intEndpoint = ASTestUtils.buildEndpoint("kvs", "test", "junit", "1") + "?keytype=INTEGER";
		} else {
			this.testEndpoint = ASTestUtils.buildEndpoint("kvs", "test", "getput");
			this.testKey = new Key("test", null, "getput");
			this.intKey = new Key("test", null, 1);
			this.bytesKey = new Key("test", null, new byte[] {1, 127, 127, 1});

			String urlDigest = Base64.getUrlEncoder().encodeToString(testKey.digest);
			digestEndpoint = ASTestUtils.buildEndpoint("kvs", "test", urlDigest) + "?keytype=DIGEST";

			String urlBytes = Base64.getUrlEncoder().encodeToString((byte[]) bytesKey.userKey.getObject());
			bytesEndpoint = ASTestUtils.buildEndpoint("kvs", "test",  urlBytes) + "?keytype=BYTES";

			intEndpoint = ASTestUtils.buildEndpoint("kvs", "test", "1") + "?keytype=INTEGER";
		}
		this.postPerformer = performer;
	}

	@Test
	public void PutInteger() throws Exception {
		Map<String, Object> binMap = new HashMap<>();

		binMap.put("integer", 12345);

		postPerformer.perform(mockMVC, testEndpoint, binMap);

		Record record = client.get(null, this.testKey);
		assertEquals(record.bins.get("integer"), 12345L);
	}

	@Test
	public void PutString() throws Exception {
		Map<String, Object> binMap = new HashMap<>();

		binMap.put("string", "Aerospike");

		postPerformer.perform(mockMVC, testEndpoint, binMap);

		Record record = client.get(null, this.testKey);
		assertEquals(record.bins.get("string"), "Aerospike");
	}

	@Test
	public void PutDouble() throws Exception {
		Map<String, Object> binMap = new HashMap<>();

		binMap.put("double", 2.718);

		postPerformer.perform(mockMVC, testEndpoint, binMap);

		Record record = client.get(null, this.testKey);
		assertEquals(record.bins.get("double"), 2.718);
	}

	@Test
	public void PutList() throws Exception {
		Map<String, Object> binMap = new HashMap<>();

		List<?>trueList = Arrays.asList(1L, "a", 3.5);

		binMap.put("ary", trueList);

		postPerformer.perform(mockMVC, testEndpoint, binMap);

		Record record = client.get(null, this.testKey);

		assertTrue(ASTestUtils.compareCollection((List<?>) record.bins.get("ary"), trueList));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void PutMapStringKeys() throws Exception {
		Map<String, Object> binMap = new HashMap<>();

		Map<Object, Object>testMap = new HashMap<>();

		testMap.put("string", "a string");
		testMap.put("long", 2L);
		testMap.put("double", 4.5);

		binMap.put("map", testMap);

		postPerformer.perform(mockMVC, testEndpoint, binMap);

		Record record = client.get(null, this.testKey);

		assertTrue(ASTestUtils.compareMap((Map<Object,Object>) record.bins.get("map"), testMap));
	}

	@Test
	public void PutWithIntegerKey() throws Exception {
		Map<String, Object> binMap = new HashMap<>();

		binMap.put("integer", 12345);

		postPerformer.perform(mockMVC, intEndpoint, binMap);

		Record record = client.get(null, this.intKey);
		assertEquals(record.bins.get("integer"), 12345L);
	}

	@Test
	public void PutWithBytesKey() throws Exception {
		Map<String, Object> binMap = new HashMap<>();
		binMap.put("integer", 12345);

		postPerformer.perform(mockMVC, bytesEndpoint, binMap);

		Record record = client.get(null, this.bytesKey);
		assertEquals(record.bins.get("integer"), 12345L);
	}

	@Test
	public void PutWithDigestKey() throws Exception {
		Map<String, Object> binMap = new HashMap<>();

		binMap.put("integer", 12345);

		postPerformer.perform(mockMVC, digestEndpoint, binMap);

		Record record = client.get(null, this.testKey);
		assertEquals(record.bins.get("integer"), 12345L);
	}

	@Test
	public void PostByteArray() throws Exception {
		Map<String, Object> binMap = new HashMap<>();
		Map<String, String> byteArray = new HashMap<>();
		byte[] arr = new byte[]{1, 101};
		byteArray.put("type", "BYTE_ARRAY");
		byteArray.put("value", Base64.getEncoder().encodeToString(arr));

		binMap.put("byte_array", byteArray);

		postPerformer.perform(mockMVC, testEndpoint, binMap);

		Record record = client.get(null, this.testKey);
		assertArrayEquals((byte[]) record.bins.get("byte_array"), arr);
	}

	@Test
	public void PostGeoJson() throws Exception {
		Map<String, Object> binMap = new HashMap<>();
		Map<String, String> geoJson = new HashMap<>();
		String geoStr = "{\"type\": \"Point\", \"coordinates\": [-80.604333, 28.608389]}";
		geoJson.put("type", "GEO_JSON");
		geoJson.put("value", Base64.getEncoder().encodeToString(geoStr.getBytes()));

		binMap.put("geo_json", geoJson);

		postPerformer.perform(mockMVC, testEndpoint, binMap);

		Record record = client.get(null, this.testKey);
		assertEquals(((Value.GeoJSONValue) record.bins.get("geo_json")).getObject(), geoStr);
	}

	@Test
	public void PostBoolean() throws Exception {
		Map<String, Object> binMap = new HashMap<>();
		
		binMap.put("bool", true);

		postPerformer.perform(mockMVC, testEndpoint, binMap);

		Record record = client.get(null, this.testKey);
		assertEquals((long) record.bins.get("bool"), 1L);
	}
}

interface PostPerformer {
	void perform(MockMvc mockMVC, String testEndpoint, Map<String, Object> binMap) throws Exception;
}

class JSONPostPerformer implements PostPerformer {
	String mediaType;
	ObjectMapper mapper;

	public JSONPostPerformer(String mediaType, ObjectMapper mapper) {
		this.mediaType = mediaType;
		this.mapper = mapper;
	}

	@Override
	public void perform(MockMvc mockMVC, String testEndpoint, Map<String, Object>binMap)
			throws Exception {
		mockMVC.perform(post(testEndpoint).contentType(mediaType)
				.content(mapper.writeValueAsString(binMap)))
		.andExpect(status().isCreated());
	}

}

class MsgPackPostPerformer implements PostPerformer {
	String mediaType;
	ObjectMapper mapper;

	public MsgPackPostPerformer(String mediaType, ObjectMapper mapper) {
		this.mediaType = mediaType;
		this.mapper = mapper;
	}

	@Override
	public void perform(MockMvc mockMVC, String testEndpoint, Map<String, Object>binMap)
			throws Exception {
		mockMVC.perform(post(testEndpoint).contentType(mediaType)
				.content(mapper.writeValueAsBytes(binMap)))
		.andExpect(status().isCreated());
	}
}
