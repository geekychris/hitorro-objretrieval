package com.hitorro.obj.core.solr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hitorro.util.json.JSONUtil;
import org.apache.solr.common.util.SimpleOrderedMap;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SolrJSONUtils {
    public static void simpleMapToJsonNode(SimpleOrderedMap sop, ObjectNode node) {
        Iterator<Map.Entry> iterE = sop.iterator();
        while (iterE.hasNext()) {
            Map.Entry e = iterE.next();
            Object key = e.getKey();
            Object val = e.getValue();
            node.set(key.toString(), getSolrMap2Json(val));
        }
    }

    public static JsonNode getSolrMap2Json(Object val) {
        if (val instanceof SimpleOrderedMap) {
            ObjectNode child = JsonNodeFactory.instance.objectNode();
            simpleMapToJsonNode((SimpleOrderedMap) val, child);
            return child;
        } else if (val instanceof List) {
            ArrayNode vals = JsonNodeFactory.instance.arrayNode();
            List<Object> l = (List) val;
            for (Object elem : l) {
                vals.add(getSolrMap2Json(elem));
            }
            return vals;
        }
        return JSONUtil.ensureJsonNode(val);
    }
}
