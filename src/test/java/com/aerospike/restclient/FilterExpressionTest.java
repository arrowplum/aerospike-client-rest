package com.aerospike.restclient;

import com.aerospike.client.AerospikeClient;
import com.aerospike.client.Bin;
import com.aerospike.client.Key;
import com.aerospike.client.exp.Exp;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(Parameterized.class)
@SpringBootTest
public class FilterExpressionTest {

    @ClassRule
    public static final SpringClassRule springClassRule = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    private AerospikeClient client;

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMVC;
    private final RecordDeserializer recordDeserializer;
    private final String mediaType;
    private final Key testKey;
    private final String noBinEndpoint;

    @Parameterized.Parameters
    public static Object[] mappers() {
        return new Object[][]{
                {new JSONRestRecordDeserializer(), MediaType.APPLICATION_JSON.toString(), true},
                {new MsgPackRestRecordDeserializer(), "application/msgpack", true},
                {new JSONRestRecordDeserializer(), MediaType.APPLICATION_JSON.toString(), false},
                {new MsgPackRestRecordDeserializer(), "application/msgpack", false}
        };
    }

    @Before
    public void setup() {
        mockMVC = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @After
    public void clean() {
        client.delete(null, this.testKey);
    }

    public FilterExpressionTest(RecordDeserializer deserializer, String mt, boolean useSet) {
        this.recordDeserializer = deserializer;
        this.mediaType = mt;

        if (useSet) {
            testKey = new Key("test", "junit", "getput");
            noBinEndpoint = ASTestUtils.buildEndpoint("kvs", "test", "junit", "getput");
        } else {
            testKey = new Key("test", null, "getput");
            noBinEndpoint = ASTestUtils.buildEndpoint("kvs", "test", "getput");
        }
    }

    @Test
    public void GetInteger() throws Exception {
        Map<String, Object> binMap = new HashMap<>();

        Bin intBin = new Bin("integer", 10);
        binMap.put(intBin.name, intBin.value.toInteger());

        client.put(null, testKey, intBin);

        byte[] filterBytes = Exp.build(Exp.gt(Exp.bin("integer", Exp.Type.INT), Exp.val(1))).getBytes();

        String encoded = Base64.getUrlEncoder().encodeToString(filterBytes);
        String endpoint = buildEndpoint(encoded);
        MockHttpServletResponse res = mockMVC.perform(
                get(endpoint).contentType(MediaType.APPLICATION_JSON).accept(mediaType)
        ).andExpect(status().isOk()).andReturn().getResponse();
        Map<String, Object> resObject = recordDeserializer.getReturnedBins(res);
        Assert.assertTrue(ASTestUtils.compareMapStringObj(resObject, binMap));
    }

    @Test
    public void GetNoInteger() throws Exception {
        Bin intBin = new Bin("integer", 10);
        client.put(null, testKey, intBin);

        byte[] filterBytes = Exp.build(Exp.le(Exp.bin("integer", Exp.Type.INT), Exp.val(1))).getBytes();

        String encoded = Base64.getUrlEncoder().encodeToString(filterBytes);
        String endpoint = buildEndpoint(encoded);
        mockMVC.perform(
                get(endpoint).contentType(MediaType.APPLICATION_JSON).accept(mediaType)
        ).andExpect(status().isNotFound());
    }

    @Test
    public void GetString() throws Exception {
        Map<String, Object> binMap = new HashMap<>();
        Bin intBin = new Bin("string", "aerospike");

        binMap.put("string", "aerospike");

        client.put(null, testKey, intBin);

        byte[] filterBytes = Exp.build(
                Exp.eq(Exp.bin("string", Exp.Type.STRING), Exp.val("aerospike"))
        ).getBytes();

        String encoded = Base64.getUrlEncoder().encodeToString(filterBytes);
        String endpoint = buildEndpoint(encoded);
        MockHttpServletResponse res = mockMVC.perform(
                get(endpoint).contentType(MediaType.APPLICATION_JSON).accept(mediaType)
        ).andExpect(status().isOk()).andReturn().getResponse();
        Map<String, Object> resObject = recordDeserializer.getReturnedBins(res);
        Assert.assertTrue(ASTestUtils.compareMapStringObj(resObject, binMap));
    }

    @Test
    public void GetNoString() throws Exception {
        Bin intBin = new Bin("string", "aerospike");
        client.put(null, testKey, intBin);

        byte[] filterBytes = Exp.build(
                Exp.eq(Exp.bin("string", Exp.Type.STRING), Exp.val("aero"))
        ).getBytes();

        String encoded = Base64.getUrlEncoder().encodeToString(filterBytes);
        String endpoint = buildEndpoint(encoded);
        mockMVC.perform(
                get(endpoint).contentType(MediaType.APPLICATION_JSON).accept(mediaType)
        ).andExpect(status().isNotFound());
    }

    private String buildEndpoint(String encoded) {
        return noBinEndpoint + "?filterexp=" + encoded;
    }
}
