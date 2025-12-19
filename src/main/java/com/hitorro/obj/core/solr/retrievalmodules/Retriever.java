package com.hitorro.obj.core.solr.retrievalmodules;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.jsontypesystem.JVS;
import com.hitorro.obj.core.solr.RetrievalContext;
import com.hitorro.util.core.iterator.AbstractIterator;
import com.hitorro.util.json.JsonInitable;

public interface Retriever extends JsonInitable {
    default boolean init(JsonNode node) {
        return true;
    }

    default boolean aggregates() {
        return false;
    }

    boolean getParticipate(JVS query, final RetrievalContext context);

    AbstractIterator<JVS> appendRetriever(AbstractIterator<JVS> iter, JVS query, RetrievalContext context);
}
