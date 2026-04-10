package com.hitorro.obj.core.solr.retrievalmodules.object;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.jsontypesystem.JVS;
import com.hitorro.obj.core.solr.RetrievalContext;
import com.hitorro.obj.core.solr.retrievalmodules.Retriever;
import com.hitorro.util.core.iterator.AbstractIterator;
import com.hitorro.util.json.keys.IntegerProperty;
import com.hitorro.util.json.keys.JsonProperty;


public class ObjectRetriever implements Retriever {
    public static IntegerProperty batchSizeKey = new IntegerProperty("batchsize", "batchsize", 20);
    public static JsonProperty objectRetrieve = new JsonProperty("object", "", null);

    public boolean getParticipate(JVS query, final RetrievalContext context) {
        JsonNode node = objectRetrieve.apply(query.getJsonNode());
        return node != null;
    }

    @Override
    public AbstractIterator<JVS> appendRetriever(final AbstractIterator<JVS> iter, final JVS query, final RetrievalContext context) {
        JsonNode node = objectRetrieve.apply(query.getJsonNode());
        int batchSize = batchSizeKey.apply(node);
        ObjectRetrieverFillBufferHandler handler = new ObjectRetrieverFillBufferHandler(context.getCollectionName());
        return iter.fillIterator(batchSize, handler);
    }
}

