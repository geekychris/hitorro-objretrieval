package com.hitorro.obj.core.solr.retrievalmodules.pagination;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.jsontypesystem.JVS;
import com.hitorro.obj.core.solr.RetrievalContext;
import com.hitorro.obj.core.solr.retrievalmodules.Retriever;
import com.hitorro.util.core.iterator.AbstractIterator;
import com.hitorro.util.json.keys.IntegerProperty;
import com.hitorro.util.json.keys.JsonProperty;

public class PaginationRetriever implements Retriever {
    public static IntegerProperty rowsKey = new IntegerProperty("rows", "", 20);
    public static IntegerProperty pageKey = new IntegerProperty("page", "", 0);
    public static JsonProperty pagination = new JsonProperty("page", "", null);

    public boolean getParticipate(JVS query, final RetrievalContext context) {
        JsonNode node = pagination.apply(query.getJsonNode());
        return node != null;
    }

    @Override
    public AbstractIterator<JVS> appendRetriever(final AbstractIterator<JVS> iter, final JVS query, final RetrievalContext context) {
        JsonNode node = pagination.apply(query.getJsonNode());
        int rows = rowsKey.apply(node);
        int page = pageKey.apply(node);
        int skip = rows * page;
        return iter.skipNTakeM(skip, rows, true);
    }
}
