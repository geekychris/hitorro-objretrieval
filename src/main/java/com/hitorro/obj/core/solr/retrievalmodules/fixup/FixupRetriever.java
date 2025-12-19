package com.hitorro.obj.core.solr.retrievalmodules.fixup;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.jsontypesystem.JVS;
import com.hitorro.obj.core.solr.RetrievalContext;
import com.hitorro.obj.core.solr.retrievalmodules.Retriever;
import com.hitorro.util.core.iterator.AbstractIterator;
import com.hitorro.util.core.iterator.Mapper;
import com.hitorro.util.json.keys.CollectionProperty;
import com.hitorro.util.json.keys.JsonProperty;

import java.util.List;
import java.util.function.Function;

public class FixupRetriever implements Retriever {
    public static CollectionProperty<String> removeKeys = CollectionProperty.getStringCollection("remove", "");

    public static CollectionProperty<String> addKeys = CollectionProperty.getStringCollection("add", "");

    public static CollectionProperty<String> enrichKeys = CollectionProperty.getStringCollection("enrichtags", "");
    public static CollectionProperty<String> removeGroupsKey = CollectionProperty.getStringCollection("removetags", "");


    public static JsonProperty fixup = new JsonProperty("fixup", "", null);

    public boolean getParticipate(JVS query, final RetrievalContext context) {
        JsonNode node = fixup.apply(query);
        return node != null;
    }

    @Override
    public AbstractIterator<JVS> appendRetriever(final AbstractIterator<JVS> iter, final JVS query, final RetrievalContext context) {
        JsonNode node = fixup.apply(query);
        Mapper<JVS, JVS> mapper = null;
        List<String> remove = removeKeys.apply(node);
        List<String> add = addKeys.apply(node);
        List<String> enrich = enrichKeys.apply(node);
        List<String> removeGroup = removeGroupsKey.apply(node);
        Function<JVS, JVS> m = RemoveAddFieldMapper.get(remove, add, enrich, removeGroup);


        return new FixupIterator(iter, m);
    }
}

//JVS2JVSRemoveMapper
