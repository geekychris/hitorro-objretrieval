package com.hitorro.obj.core.solr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for SolrJSONUtils.
 * Tests conversion of Solr SimpleOrderedMap to Jackson JsonNode.
 */
public class SolrJSONUtilsTest {

    @Test
    public void testSimpleMapToJsonNodeWithStringValues() {
        SimpleOrderedMap<Object> sop = new SimpleOrderedMap<>();
        sop.add("name", "test");
        sop.add("value", "hello");

        ObjectNode node = JsonNodeFactory.instance.objectNode();
        SolrJSONUtils.simpleMapToJsonNode(sop, node);

        assertEquals("test", node.get("name").asText());
        assertEquals("hello", node.get("value").asText());
    }

    @Test
    public void testSimpleMapToJsonNodeWithNumericValues() {
        SimpleOrderedMap<Object> sop = new SimpleOrderedMap<>();
        sop.add("count", 42);
        sop.add("score", 3.14);

        ObjectNode node = JsonNodeFactory.instance.objectNode();
        SolrJSONUtils.simpleMapToJsonNode(sop, node);

        assertEquals(42, node.get("count").asInt());
        assertEquals(3.14, node.get("score").asDouble(), 0.001);
    }

    @Test
    public void testSimpleMapToJsonNodeWithNestedMap() {
        SimpleOrderedMap<Object> inner = new SimpleOrderedMap<>();
        inner.add("innerKey", "innerValue");

        SimpleOrderedMap<Object> outer = new SimpleOrderedMap<>();
        outer.add("nested", inner);

        ObjectNode node = JsonNodeFactory.instance.objectNode();
        SolrJSONUtils.simpleMapToJsonNode(outer, node);

        assertTrue(node.get("nested").isObject());
        assertEquals("innerValue", node.get("nested").get("innerKey").asText());
    }

    @Test
    public void testSimpleMapToJsonNodeWithListValues() {
        List<Object> list = new ArrayList<>();
        list.add("item1");
        list.add("item2");
        list.add("item3");

        SimpleOrderedMap<Object> sop = new SimpleOrderedMap<>();
        sop.add("items", list);

        ObjectNode node = JsonNodeFactory.instance.objectNode();
        SolrJSONUtils.simpleMapToJsonNode(sop, node);

        assertTrue(node.get("items").isArray());
        assertEquals(3, node.get("items").size());
        assertEquals("item1", node.get("items").get(0).asText());
        assertEquals("item2", node.get("items").get(1).asText());
        assertEquals("item3", node.get("items").get(2).asText());
    }

    @Test
    public void testSimpleMapToJsonNodeEmpty() {
        SimpleOrderedMap<Object> sop = new SimpleOrderedMap<>();

        ObjectNode node = JsonNodeFactory.instance.objectNode();
        SolrJSONUtils.simpleMapToJsonNode(sop, node);

        assertEquals(0, node.size());
    }

    @Test
    public void testGetSolrMap2JsonWithString() {
        JsonNode result = SolrJSONUtils.getSolrMap2Json("hello");
        assertTrue(result.isTextual());
        assertEquals("hello", result.asText());
    }

    @Test
    public void testGetSolrMap2JsonWithInteger() {
        JsonNode result = SolrJSONUtils.getSolrMap2Json(42);
        assertTrue(result.isNumber());
        assertEquals(42, result.asInt());
    }

    @Test
    public void testGetSolrMap2JsonWithBoolean() {
        JsonNode result = SolrJSONUtils.getSolrMap2Json(true);
        assertTrue(result.isBoolean());
        assertTrue(result.asBoolean());
    }

    @Test
    public void testGetSolrMap2JsonWithSimpleOrderedMap() {
        SimpleOrderedMap<Object> sop = new SimpleOrderedMap<>();
        sop.add("key", "value");

        JsonNode result = SolrJSONUtils.getSolrMap2Json(sop);
        assertTrue(result.isObject());
        assertEquals("value", result.get("key").asText());
    }

    @Test
    public void testGetSolrMap2JsonWithList() {
        List<Object> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);

        JsonNode result = SolrJSONUtils.getSolrMap2Json(list);
        assertTrue(result.isArray());
        assertEquals(3, result.size());
        assertEquals(1, result.get(0).asInt());
        assertEquals(2, result.get(1).asInt());
        assertEquals(3, result.get(2).asInt());
    }

    @Test
    public void testNestedListWithMaps() {
        SimpleOrderedMap<Object> map1 = new SimpleOrderedMap<>();
        map1.add("id", 1);
        SimpleOrderedMap<Object> map2 = new SimpleOrderedMap<>();
        map2.add("id", 2);

        List<Object> list = new ArrayList<>();
        list.add(map1);
        list.add(map2);

        JsonNode result = SolrJSONUtils.getSolrMap2Json(list);
        assertTrue(result.isArray());
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).get("id").asInt());
        assertEquals(2, result.get(1).get("id").asInt());
    }

    @Test
    public void testDeeplyNestedStructure() {
        SimpleOrderedMap<Object> level3 = new SimpleOrderedMap<>();
        level3.add("deep", "value");

        SimpleOrderedMap<Object> level2 = new SimpleOrderedMap<>();
        level2.add("child", level3);

        SimpleOrderedMap<Object> level1 = new SimpleOrderedMap<>();
        level1.add("parent", level2);

        ObjectNode node = JsonNodeFactory.instance.objectNode();
        SolrJSONUtils.simpleMapToJsonNode(level1, node);

        assertEquals("value", node.get("parent").get("child").get("deep").asText());
    }
}
