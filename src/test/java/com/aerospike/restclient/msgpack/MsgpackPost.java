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
package com.aerospike.restclient.msgpack;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.jackson.dataformat.MessagePackExtensionType;
import org.msgpack.jackson.dataformat.MessagePackFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.aerospike.client.AerospikeClient;
import com.aerospike.client.Bin;
import com.aerospike.client.Key;
import com.aerospike.client.Record;
import com.aerospike.client.Value.GeoJSONValue;
import com.aerospike.client.command.ParticleType;
import com.aerospike.restclient.ASTestUtils;
import com.aerospike.restclient.util.deserializers.MsgPackBinParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class MsgpackPost {

	private MockMvc mockMVC;

	@Autowired
	private AerospikeClient client;

	@Autowired
	private WebApplicationContext wac;

	private final String mtString = "application/msgpack";
	private ObjectMapper mapper;
	private final Key testKey = new Key("test", "msgpack", "post");

	private final String testEndpoint = ASTestUtils.buildEndpoint("kvs", "test", "msgpack", "post");
	private final TypeReference<Map<String, Object>> sOMapType = new TypeReference<Map<String, Object>>() {};

	@BeforeEach
	public void setup() {
		mapper = new ObjectMapper(new MessagePackFactory());
		mockMVC = MockMvcBuilders.webAppContextSetup(wac).build();
	}

	@AfterEach
	public void clean() {
		client.delete(null, testKey);
	}

	@Test
	public void testStoringByteArray() throws Exception {
		Map<String, byte[]>byteBins = new HashMap<>();
		byte[] testBytes = new byte[] {1, 127, 127, 1};
		byteBins.put("byte", testBytes);

		mockMVC.perform(post(testEndpoint).contentType(mtString)
				.content(mapper.writeValueAsBytes(byteBins)))
		.andExpect(status().isCreated());


		Record rec = client.get(null, testKey);
		byte[] realBytes = (byte[]) rec.bins.get("byte");
		assertArrayEquals(testBytes, realBytes);
	}

	@Test
	public void testStoringByteArray2() throws Exception {
		byte[] testBytes = {1, 2, 3};
		MessageBufferPacker packer = new MessagePack.PackerConfig().newBufferPacker();

		packer.packMapHeader(1);

		packer.packString("my_bytes");

		packer.packBinaryHeader(3);
		packer.writePayload(testBytes);

		byte[] payload = packer.toByteArray();

		mockMVC.perform(post(testEndpoint).contentType(mtString)
				.content(payload))
		.andExpect(status().isCreated());


		Record rec = client.get(null, testKey);
		byte[] realBytes = (byte[]) rec.bins.get("my_bytes");
		assertArrayEquals(testBytes, realBytes);
	}

	@Test
	public void testReturningByteArray() throws Exception {
		byte[] testBytes = new byte[] {1, 127, 127, 1};
		client.put(null, testKey, new Bin("byte", testBytes));


		byte[] resContent = mockMVC.perform(get(testEndpoint).accept(mtString))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray();

		Map<String, Object>retRec = mapper.readValue(resContent, sOMapType);
		@SuppressWarnings("unchecked")
		Map<String, Object>bins = (Map<String, Object>) retRec.get("bins");

		byte[] realBytes = (byte[]) bins.get("byte");

		assertArrayEquals(testBytes, realBytes);
	}

	@Test
	public void testStoringGeoJson() throws Exception {
		String geoString = "{\"coordinates\": [-122.0, 37.5], \"type\": \"Point\"}";
		MessageBufferPacker packer = new MessagePack.PackerConfig().newBufferPacker();
		packer.packMapHeader(1);
		packer.packString("geo");
		packer.packExtensionTypeHeader((byte) 23, geoString.length());
		packer.addPayload(geoString.getBytes("UTF-8"));

		mockMVC.perform(post(testEndpoint).contentType(mtString)
				.content(packer.toByteArray()))
		.andExpect(status().isCreated());

		Record rec = client.get(null, testKey);
		GeoJSONValue geoV = (GeoJSONValue) rec.bins.get("geo");
		assertEquals(ParticleType.GEOJSON, geoV.getType());
		assertEquals(geoString, geoV.toString());
	}

	@Test
	public void testGettingGeoJson() throws Exception {
		String geoString = "{\"coordinates\": [-122.0, 37.5], \"type\": \"Point\"}";
		GeoJSONValue geoV = new GeoJSONValue(geoString);
		client.put(null, testKey, new Bin("geo", geoV));

		byte[] resContent = mockMVC.perform(get(testEndpoint).accept(mtString))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray();

		Map<String, Object>retRec = mapper.readValue(resContent, sOMapType);

		@SuppressWarnings("unchecked")
		Map<String, Object>bins = (Map<String, Object>) retRec.get("bins");
		MessagePackExtensionType eType = (MessagePackExtensionType) bins.get("geo");

		assertEquals(eType.getType(), ParticleType.GEOJSON);
		assertEquals(new String(eType.getData(), "UTF-8"), geoString);
	}

	@Test
	public void testStoringMapWithIntKeys() throws Exception {
		MessageBufferPacker packer = new MessagePack.PackerConfig().newBufferPacker();
		// Store a map {map={1L=2L}}
		packer.packMapHeader(1);
		packer.packString("map");
		packer.packMapHeader(1);
		packer.packLong(1);
		packer.packLong(2);

		mockMVC.perform(post(testEndpoint).contentType(mtString)
				.content(packer.toByteArray()))
		.andExpect(status().isCreated());

		Record rec = client.get(null, testKey);
		@SuppressWarnings("unchecked")
		Map<Long, Long> retMap = (Map<Long, Long>) rec.bins.get("map");

		assertEquals(retMap.size(), 1);
		assertEquals(retMap.get(1L), (Long) 2L);
	}

	@Test
	public void testGettingMapWithIntKey() throws Exception {
		Map<Long, Long> iMap = new HashMap<>();
		iMap.put(1L, 2L);

		client.put(null, testKey, new Bin("map", iMap));

		byte[] resContent = mockMVC.perform(get(testEndpoint).accept(mtString))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray();

		// This helper lets us parse out a Map<String, Object>
		// Since that's the format of a returned record, this works.
		// We end up using Rec["bins"]
		MsgPackBinParser bParser = new MsgPackBinParser(new ByteArrayInputStream(resContent));
		Map<String, Object>retRec = bParser.parseBins();
		@SuppressWarnings("unchecked")
		Map<String, Object>bins = (Map<String, Object>) retRec.get("bins");
		@SuppressWarnings("unchecked")
		Map<Object, Object>lMap = (Map<Object, Object>) bins.get("map");

		assertEquals(lMap.size(), 1);
		assertEquals(lMap.get(1L), 2L);
	}

	/* Write a request with empty msgpack data */
	@Test
	public void testInvalidMsgpack() throws Exception {

		mockMVC.perform(post(testEndpoint).contentType(mtString)
				.content(new byte[] {}))
		.andExpect(status().isBadRequest());
	}

	/* Write a request with an incomplete msgpack map*/
	@Test
	public void testInvalidMsgpackBins() throws Exception {
		MessageBufferPacker packer = new MessagePack.PackerConfig().newBufferPacker();
		// Store a map {map={1L=2L}}
		packer.packMapHeader(1);
		packer.packString("map");

		mockMVC.perform(post(testEndpoint).contentType(mtString)
				.content(new byte[] {}))
		.andExpect(status().isBadRequest());
	}

	@Test
	public void testNonMapMsgPack() throws Exception {
		MessageBufferPacker packer = new MessagePack.PackerConfig().newBufferPacker();
		// Store a map {map={1L=2L}}
		packer.packArrayHeader(2);
		packer.packString("map");
		packer.packString("value");

		mockMVC.perform(post(testEndpoint).contentType(mtString)
				.content(new byte[] {}))
		.andExpect(status().isBadRequest());
	}
}