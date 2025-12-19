package com.hitorro.obj.core.solr.queryvisitors;

import com.hitorro.jsontypesystem.JVS;
import com.hitorro.obj.core.solr.RetrievalContext;
import com.hitorro.util.json.keys.IntegerProperty;

public class Rows implements QueryVisitor {
    public static IntegerProperty rows = new IntegerProperty("search.rows", "", 20);

    @Override
    public void visit(final JVS jvsQuery, final RetrievalContext retrievalContext) {
        retrievalContext.results = Math.max(retrievalContext.results, rows.apply(jvsQuery));
    }

    @Override
    public void finalize(final JVS jvsQuery, final RetrievalContext retrievalContext) {
        retrievalContext.getSolrQuery().setRows(retrievalContext.results);
    }
}