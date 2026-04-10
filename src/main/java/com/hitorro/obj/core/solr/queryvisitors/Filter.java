package com.hitorro.obj.core.solr.queryvisitors;

import com.hitorro.jsontypesystem.JVS;
import com.hitorro.obj.core.solr.RetrievalContext;
import com.hitorro.util.json.keys.ArrayProperty;
import com.hitorro.util.json.keys.StringProperty;

import java.util.ArrayList;
import java.util.List;

public class Filter implements QueryVisitor {
    public static StringProperty fqKey = new StringProperty("", "", null);
    public static ArrayProperty<String> fqsKey =
            new ArrayProperty<String>("search.filter", "", new ArrayList(), fqKey);

    @Override
    public void visit(final JVS jvsQuery, final RetrievalContext retrievalContext) {

    }

    @Override
    public void finalize(final JVS jvsQuery, final RetrievalContext retrievalContext) {
        List<String> queries = fqsKey.apply(jvsQuery.getJsonNode());
        for (String q : queries) {
            retrievalContext.getSolrQuery().add("fq", q);
        }
    }
}
