package com.hitorro.obj.core.solr.collection;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hitorro.jsontypesystem.JsonTypeSystem;
import com.hitorro.jsontypesystem.Type;
import com.hitorro.util.basefile.Name2JsonMapper;
import com.hitorro.util.core.Env;
import com.hitorro.util.core.events.cache.HashCache;
import com.hitorro.util.json.JsonInitable;
import com.hitorro.util.json.keys.JsonProperty;
import com.hitorro.util.json.keys.StringProperty;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CollectionConfig implements JsonInitable {

    public static HashCache<String, JsonNode> jsonCollectionConfig =
            new HashCache<>(0, true,
                    null, "collectionconfig",
                    new Name2JsonMapper(Env.getBinConfigBaseFile().getChild("collections"), null));

    public static HashCache<String, CollectionConfig> configCache =
            new HashCache<>(0, true,
                    null, "collections",
                    new CollectionConfigMapper());


    public static StringProperty collectionMetaTypeKey = new StringProperty("meta.type", "", "core_sysobject");

    public static StringProperty collectionMetaNameKey = new StringProperty("meta.collection", "", null);
    public static StringProperty langKey = new StringProperty("lang", "", "en");

    public static StringProperty collectionNameKey = new StringProperty("name", "", "<name_missing>");

    public static JsonProperty intents = new JsonProperty("intents", "", null);
    private final Map<String, CollectionIntent> intentMap = new HashMap();
    private JsonNode node;
    private Type type;

    public String getName() {
        return collectionNameKey.apply(node);
    }

    @Override
    public boolean init(final JsonNode node) {
        this.node = node;
        type = JsonTypeSystem.getMe().getType(collectionMetaTypeKey.apply(node));

        JsonNode n = intents.apply(node);
        if (n != null && n.isObject()) {
            ObjectNode intents = (ObjectNode) n;
            Iterator<String> keys = intents.fieldNames();
            while (keys.hasNext()) {
                String key = keys.next();
                JsonNode val = intents.get(key);
                CollectionIntent ci = new CollectionIntent();
                ci.init(val);
                intentMap.put(key, ci);
            }
        }
        return true;
    }

    public Type getType() {
        return type;
    }

    public CollectionIntent getIntent(String intentName) {
        return intentMap.get(intentName);
    }
}

