package com.hitorro.obj.core.solr.collection;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.util.core.iterator.Mapper;

public class CollectionConfigMapper implements Mapper<String, CollectionConfig> {
    public CollectionConfig apply(String s) {
        JsonNode node = CollectionConfig.jsonCollectionConfig.get(s.toLowerCase());
        if (node == null) {
            return null;
        }
        return JsonNode2CollectionConfigMapper.mapper.apply(node);

    }
}
