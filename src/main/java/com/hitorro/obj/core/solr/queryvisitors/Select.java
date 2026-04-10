package com.hitorro.obj.core.solr.queryvisitors;

import com.hitorro.jsontypesystem.JVS;
import com.hitorro.obj.core.solr.RetrievalContext;
import com.hitorro.util.core.classes.JsonNode2JsonInitable;
import com.hitorro.util.json.keys.ArrayProperty;

import java.util.ArrayList;
import java.util.List;

public class Select implements QueryVisitor {
    public static JsonNode2JsonInitable<SelectRow> selectRowKey = new JsonNode2JsonInitable(SelectRow.class, "class", SelectRow.class);
    public static ArrayProperty<SelectRow> selectRowsKey =
            new ArrayProperty<SelectRow>("search.select", "", new ArrayList(), selectRowKey);

    @Override
    public void visit(final JVS jvsQuery, final RetrievalContext retrievalContext) {
        retrievalContext.addAlias("score", null);
        retrievalContext.addAlias("id.id", "id.id");
        List<SelectRow> rows = selectRowsKey.apply(jvsQuery.getJsonNode());
        retrievalContext.addAlias(rows);
    }

    @Override
    public void finalize(final JVS jvsQuery, final RetrievalContext retrievalContext) {
        retrievalContext.getSortFields("fl", retrievalContext.getAliases());
    }
}
