package com.aerospike.restclient;

import com.aerospike.client.AerospikeClient;
import com.aerospike.client.Bin;
import com.aerospike.client.Key;
import com.aerospike.restclient.util.AerospikeOperation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.*;
import java.util.stream.Stream;

import static com.aerospike.restclient.util.AerospikeAPIConstants.OPERATION_FIELD;
import static com.aerospike.restclient.util.AerospikeAPIConstants.OPERATION_VALUES_FIELD;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class OperateBitTestsCorrect {

    private MockMvc mockMVC;

    @Autowired
    private AerospikeClient client;

    @Autowired
    private WebApplicationContext wac;

    byte[] byteArray = new byte[]{12, 5, 110, 47};
    private Key testKey;
    private String testEndpoint;

    @BeforeEach
    public void setup() {
        mockMVC = MockMvcBuilders.webAppContextSetup(wac).build();
        Bin bitBin = new Bin("bit", byteArray);
        client.put(null, testKey, bitBin);
    }

    @AfterEach
    public void clean() {
        client.delete(null, testKey);
    }

    private OperationPerformer opPerformer;

    private static Stream<Arguments> getParams() {
        return Stream.of(
                Arguments.of(new JSONOperationPerformer(), true, new MsgPackOperationPerformer(), true),
                Arguments.of(new JSONOperationPerformer(), false, new MsgPackOperationPerformer(), false)
        );
    }

    /* Set up the correct msgpack/json performer for this set of runs. Also decided whether to use the endpoint with a set or without */
    @ParameterizedTest
    @MethodSource("getParams")
    void addParams(OperationPerformer performer, boolean useSet) {
        this.opPerformer = performer;
        if (useSet) {
            testKey = new Key("test", "junit", "bitop");
            testEndpoint = ASTestUtils.buildEndpoint("operate", "test", "junit", "bitop");
        } else {
            testKey = new Key("test", null, "bitop");
            testEndpoint = ASTestUtils.buildEndpoint("operate", "test", "bitop");
        }
    }

    @Test
    public void testBitResize() {
        List<Map<String, Object>> opList = new ArrayList<>();
        Map<String, Object> opMap = new HashMap<>();
        Map<String, Object> opValues = new HashMap<>();
        opValues.put("bin", "bit");
        opValues.put("byteSize", 8);
        opValues.put("resizeFlags", 0);
        opMap.put(OPERATION_FIELD, AerospikeOperation.BIT_RESIZE);
        opMap.put(OPERATION_VALUES_FIELD, opValues);
        opList.add(opMap);

        opPerformer.performOperationsAndReturn(mockMVC, testEndpoint, opList);

        byte[] realByteArray = (byte[]) client.get(null, testKey).bins.get("bit");

        assertArrayEquals(new byte[]{12, 5, 110, 47, 0, 0, 0, 0}, realByteArray);
    }

    @Test
    public void testBitInsert() {
        List<Map<String, Object>> opList = new ArrayList<>();
        Map<String, Object> opMap = new HashMap<>();
        Map<String, Object> opValues = new HashMap<>();
        opValues.put("bin", "bit");
        opValues.put("byteOffset", 1);
        String value = Base64.getEncoder().encodeToString(new byte[]{11});
        opValues.put("value", value);
        opMap.put(OPERATION_FIELD, AerospikeOperation.BIT_INSERT);
        opMap.put(OPERATION_VALUES_FIELD, opValues);
        opList.add(opMap);

        opPerformer.performOperationsAndReturn(mockMVC, testEndpoint, opList);

        byte[] realByteArray = (byte[]) client.get(null, testKey).bins.get("bit");

        assertArrayEquals(new byte[]{12, 11, 5, 110, 47}, realByteArray);
    }

    @Test
    public void testBitRemove() {
        List<Map<String, Object>> opList = new ArrayList<>();
        Map<String, Object> opMap = new HashMap<>();
        Map<String, Object> opValues = new HashMap<>();
        opValues.put("bin", "bit");
        opValues.put("byteOffset", 1);
        opValues.put("byteSize", 2);
        opMap.put(OPERATION_FIELD, AerospikeOperation.BIT_REMOVE);
        opMap.put(OPERATION_VALUES_FIELD, opValues);
        opList.add(opMap);

        opPerformer.performOperationsAndReturn(mockMVC, testEndpoint, opList);

        byte[] realByteArray = (byte[]) client.get(null, testKey).bins.get("bit");

        assertArrayEquals(new byte[]{12, 47}, realByteArray);
    }

    @Test
    public void testBitSet() {
        List<Map<String, Object>> opList = new ArrayList<>();
        Map<String, Object> opMap = new HashMap<>();
        Map<String, Object> opValues = new HashMap<>();
        opValues.put("bin", "bit");
        opValues.put("bitOffset", 16);
        String value = Base64.getEncoder().encodeToString(new byte[]{127});
        opValues.put("value", value);
        opValues.put("bitSize", 4);
        opMap.put(OPERATION_FIELD, AerospikeOperation.BIT_SET);
        opMap.put(OPERATION_VALUES_FIELD, opValues);
        opList.add(opMap);

        opPerformer.performOperationsAndReturn(mockMVC, testEndpoint, opList);

        byte[] realByteArray = (byte[]) client.get(null, testKey).bins.get("bit");

        assertArrayEquals(new byte[]{12, 5, 126, 47}, realByteArray);
    }

    @Test
    public void testBitOr() {
        List<Map<String, Object>> opList = new ArrayList<>();
        Map<String, Object> opMap = new HashMap<>();
        Map<String, Object> opValues = new HashMap<>();
        opValues.put("bin", "bit");
        opValues.put("bitOffset", 16);
        String value = Base64.getEncoder().encodeToString(new byte[]{57});
        opValues.put("value", value);
        opValues.put("bitSize", 8);
        opMap.put(OPERATION_FIELD, AerospikeOperation.BIT_OR);
        opMap.put(OPERATION_VALUES_FIELD, opValues);
        opList.add(opMap);

        opPerformer.performOperationsAndReturn(mockMVC, testEndpoint, opList);

        byte[] realByteArray = (byte[]) client.get(null, testKey).bins.get("bit");

        assertArrayEquals(new byte[]{12, 5, 127, 47}, realByteArray);
    }

    @Test
    public void testBitXor() {
        List<Map<String, Object>> opList = new ArrayList<>();
        Map<String, Object> opMap = new HashMap<>();
        Map<String, Object> opValues = new HashMap<>();
        opValues.put("bin", "bit");
        opValues.put("bitOffset", 16);
        String value = Base64.getEncoder().encodeToString(new byte[]{57});
        opValues.put("value", value);
        opValues.put("bitSize", 8);
        opMap.put(OPERATION_FIELD, AerospikeOperation.BIT_XOR);
        opMap.put(OPERATION_VALUES_FIELD, opValues);
        opList.add(opMap);

        opPerformer.performOperationsAndReturn(mockMVC, testEndpoint, opList);

        byte[] realByteArray = (byte[]) client.get(null, testKey).bins.get("bit");

        assertArrayEquals(new byte[]{12, 5, 87, 47}, realByteArray);
    }

    @Test
    public void testBitAnd() {
        List<Map<String, Object>> opList = new ArrayList<>();
        Map<String, Object> opMap = new HashMap<>();
        Map<String, Object> opValues = new HashMap<>();
        opValues.put("bin", "bit");
        opValues.put("bitOffset", 16);
        String value = Base64.getEncoder().encodeToString(new byte[]{57});
        opValues.put("value", value);
        opValues.put("bitSize", 8);
        opMap.put(OPERATION_FIELD, AerospikeOperation.BIT_AND);
        opMap.put(OPERATION_VALUES_FIELD, opValues);
        opList.add(opMap);

        opPerformer.performOperationsAndReturn(mockMVC, testEndpoint, opList);

        byte[] realByteArray = (byte[]) client.get(null, testKey).bins.get("bit");

        assertArrayEquals(new byte[]{12, 5, 40, 47}, realByteArray);
    }

    @Test
    public void testBitNot() {
        List<Map<String, Object>> opList = new ArrayList<>();
        Map<String, Object> opMap = new HashMap<>();
        Map<String, Object> opValues = new HashMap<>();
        opValues.put("bin", "bit");
        opValues.put("bitOffset", 16);
        opValues.put("bitSize", 16);
        opMap.put(OPERATION_FIELD, AerospikeOperation.BIT_NOT);
        opMap.put(OPERATION_VALUES_FIELD, opValues);
        opList.add(opMap);

        opPerformer.performOperationsAndReturn(mockMVC, testEndpoint, opList);

        byte[] realByteArray = (byte[]) client.get(null, testKey).bins.get("bit");

        assertArrayEquals(new byte[]{12, 5, -111, -48}, realByteArray);
    }

    @Test
    public void testBitLshift() {
        List<Map<String, Object>> opList = new ArrayList<>();
        Map<String, Object> opMap = new HashMap<>();
        Map<String, Object> opValues = new HashMap<>();
        opValues.put("bin", "bit");
        opValues.put("bitOffset", 24);
        opValues.put("bitSize", 8);
        opValues.put("shift", 3);
        opMap.put(OPERATION_FIELD, AerospikeOperation.BIT_LSHIFT);
        opMap.put(OPERATION_VALUES_FIELD, opValues);
        opList.add(opMap);

        opPerformer.performOperationsAndReturn(mockMVC, testEndpoint, opList);

        byte[] realByteArray = (byte[]) client.get(null, testKey).bins.get("bit");

        assertArrayEquals(new byte[]{12, 5, 110, 120}, realByteArray);
    }

    @Test
    public void testBitRshift() {
        List<Map<String, Object>> opList = new ArrayList<>();
        Map<String, Object> opMap = new HashMap<>();
        Map<String, Object> opValues = new HashMap<>();
        opValues.put("bin", "bit");
        opValues.put("bitOffset", 24);
        opValues.put("bitSize", 8);
        opValues.put("shift", 3);
        opMap.put(OPERATION_FIELD, AerospikeOperation.BIT_RSHIFT);
        opMap.put(OPERATION_VALUES_FIELD, opValues);
        opList.add(opMap);

        opPerformer.performOperationsAndReturn(mockMVC, testEndpoint, opList);

        byte[] realByteArray = (byte[]) client.get(null, testKey).bins.get("bit");

        assertArrayEquals(new byte[]{12, 5, 110, 5}, realByteArray);
    }

    @Test
    public void testBitAdd() {
        List<Map<String, Object>> opList = new ArrayList<>();
        Map<String, Object> opMap = new HashMap<>();
        Map<String, Object> opValues = new HashMap<>();
        opValues.put("bin", "bit");
        opValues.put("bitOffset", 24);
        opValues.put("bitSize", 8);
        opValues.put("value", 3);
        opMap.put(OPERATION_FIELD, AerospikeOperation.BIT_ADD);
        opMap.put(OPERATION_VALUES_FIELD, opValues);
        opList.add(opMap);

        opPerformer.performOperationsAndReturn(mockMVC, testEndpoint, opList);

        byte[] realByteArray = (byte[]) client.get(null, testKey).bins.get("bit");

        assertArrayEquals(new byte[]{12, 5, 110, 50}, realByteArray);
    }

    @Test
    public void testBitSubtract() {
        List<Map<String, Object>> opList = new ArrayList<>();
        Map<String, Object> opMap = new HashMap<>();
        Map<String, Object> opValues = new HashMap<>();
        opValues.put("bin", "bit");
        opValues.put("bitOffset", 24);
        opValues.put("bitSize", 8);
        opValues.put("value", 3);
        opMap.put(OPERATION_FIELD, AerospikeOperation.BIT_SUBTRACT);
        opMap.put(OPERATION_VALUES_FIELD, opValues);
        opList.add(opMap);

        opPerformer.performOperationsAndReturn(mockMVC, testEndpoint, opList);

        byte[] realByteArray = (byte[]) client.get(null, testKey).bins.get("bit");

        assertArrayEquals(new byte[]{12, 5, 110, 44}, realByteArray);
    }

    @Test
    public void testBitSetInt() {
        List<Map<String, Object>> opList = new ArrayList<>();
        Map<String, Object> opMap = new HashMap<>();
        Map<String, Object> opValues = new HashMap<>();
        opValues.put("bin", "bit");
        opValues.put("bitOffset", 24);
        opValues.put("bitSize", 8);
        opValues.put("value", 3);
        opMap.put(OPERATION_FIELD, AerospikeOperation.BIT_SET_INT);
        opMap.put(OPERATION_VALUES_FIELD, opValues);
        opList.add(opMap);

        opPerformer.performOperationsAndReturn(mockMVC, testEndpoint, opList);

        byte[] realByteArray = (byte[]) client.get(null, testKey).bins.get("bit");

        assertArrayEquals(new byte[]{12, 5, 110, 3}, realByteArray);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testBitGet() {
        List<Map<String, Object>> opList = new ArrayList<>();
        Map<String, Object> opMap = new HashMap<>();
        Map<String, Object> opValues = new HashMap<>();
        opValues.put("bin", "bit");
        opValues.put("bitOffset", 24);
        opValues.put("bitSize", 8);
        opMap.put(OPERATION_FIELD, AerospikeOperation.BIT_GET);
        opMap.put(OPERATION_VALUES_FIELD, opValues);
        opList.add(opMap);

        Map<String, Object> res = opPerformer.performOperationsAndReturn(mockMVC, testEndpoint, opList);
        byte[] expected;
        try {
            expected = Base64.getDecoder().decode(((Map<String, String>) res.get("bins")).get("bit").getBytes());
        } catch (ClassCastException e) {
            expected = (byte[]) ((Map<String, Object>) res.get("bins")).get("bit");
        }

        assertArrayEquals(new byte[]{47}, expected);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testBitCount() {
        List<Map<String, Object>> opList = new ArrayList<>();
        Map<String, Object> opMap = new HashMap<>();
        Map<String, Object> opValues = new HashMap<>();
        opValues.put("bin", "bit");
        opValues.put("bitOffset", 20);
        opValues.put("bitSize", 4);
        opMap.put(OPERATION_FIELD, AerospikeOperation.BIT_COUNT);
        opMap.put(OPERATION_VALUES_FIELD, opValues);
        opList.add(opMap);

        Map<String, Object> res = opPerformer.performOperationsAndReturn(mockMVC, testEndpoint, opList);
        int expected = ((Map<String, Integer>) res.get("bins")).get("bit");

        assertEquals(3, expected);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testBitLscan() {
        List<Map<String, Object>> opList = new ArrayList<>();
        Map<String, Object> opMap = new HashMap<>();
        Map<String, Object> opValues = new HashMap<>();
        opValues.put("bin", "bit");
        opValues.put("bitOffset", 16);
        opValues.put("bitSize", 8);
        opValues.put("value", true);
        opMap.put(OPERATION_FIELD, AerospikeOperation.BIT_LSCAN);
        opMap.put(OPERATION_VALUES_FIELD, opValues);
        opList.add(opMap);

        Map<String, Object> res = opPerformer.performOperationsAndReturn(mockMVC, testEndpoint, opList);
        int expected = ((Map<String, Integer>) res.get("bins")).get("bit");

        assertEquals(1, expected);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testBitRscan() {
        List<Map<String, Object>> opList = new ArrayList<>();
        Map<String, Object> opMap = new HashMap<>();
        Map<String, Object> opValues = new HashMap<>();
        opValues.put("bin", "bit");
        opValues.put("bitOffset", 16);
        opValues.put("bitSize", 8);
        opMap.put(OPERATION_FIELD, AerospikeOperation.BIT_RSCAN);
        opMap.put(OPERATION_VALUES_FIELD, opValues);
        opList.add(opMap);

        Map<String, Object> res = opPerformer.performOperationsAndReturn(mockMVC, testEndpoint, opList);
        int expected = ((Map<String, Integer>) res.get("bins")).get("bit");

        assertEquals(7, expected);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testBitGetInt() {
        List<Map<String, Object>> opList = new ArrayList<>();
        Map<String, Object> opMap = new HashMap<>();
        Map<String, Object> opValues = new HashMap<>();
        opValues.put("bin", "bit");
        opValues.put("bitOffset", 12);
        opValues.put("bitSize", 16);
        opMap.put(OPERATION_FIELD, AerospikeOperation.BIT_GET_INT);
        opMap.put(OPERATION_VALUES_FIELD, opValues);
        opList.add(opMap);

        Map<String, Object> res = opPerformer.performOperationsAndReturn(mockMVC, testEndpoint, opList);
        int expected = ((Map<String, Integer>) res.get("bins")).get("bit");

        assertEquals(22242, expected);
    }
}
