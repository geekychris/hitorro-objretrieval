package com.hitorro.obj.core.solr.queryvisitors;

import com.hitorro.jsontypesystem.JVS;
import com.hitorro.obj.core.solr.RetrievalContext;
import com.hitorro.util.core.classes.JsonNode2JsonInitable;
import com.hitorro.util.json.keys.ArrayProperty;

import java.util.ArrayList;
import java.util.List;

public class Query implements QueryVisitor {
    public static JsonNode2JsonInitable<QueryRow> queryRowKey = new JsonNode2JsonInitable(QueryRow.class, "class", QueryRow.class);
    public static ArrayProperty<QueryRow> queryRowsKey =
            new ArrayProperty<QueryRow>("search.query", "", new ArrayList(), queryRowKey);

    @Override
    public void visit(final JVS jvsQuery, final RetrievalContext retrievalContext) {

    }

    @Override
    public void finalize(final JVS jvsQuery, final RetrievalContext rc) {
        List<QueryRow> rows = queryRowsKey.apply(jvsQuery.getJsonNode());
        for (QueryRow row : rows) {
            row.addQuery(rc.getSolrQuery());
        }
    }
}
