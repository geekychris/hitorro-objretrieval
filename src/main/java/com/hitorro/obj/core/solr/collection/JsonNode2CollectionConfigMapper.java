package com.hitorro.obj.core.solr.collection;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.util.core.iterator.Mapper;

public
class JsonNode2CollectionConfigMapper implements Mapper<JsonNode, CollectionConfig> {
    static final JsonNode2CollectionConfigMapper mapper = new JsonNode2CollectionConfigMapper();

    public JsonNode2CollectionConfigMapper() {
    }

    @Override
    public CollectionConfig apply(JsonNode n) {
        CollectionConfig t = new CollectionConfig();
        t.init(n);
        return t;
    }
}