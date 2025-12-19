package com.hitorro.obj.core.solr.queryvisitors;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.util.json.JsonInitable;
import com.hitorro.util.json.keys.BooleanProperty;
import com.hitorro.util.json.keys.StringProperty;
import org.apache.solr.client.solrj.SolrQuery;

public class QueryRow implements JsonInitable {
    private static final BooleanProperty mustProperty = new BooleanProperty("must", "", true);
    public static StringProperty queryProperty = new StringProperty("query", "", null);
    private String query;
    private boolean must;

    @Override
    public boolean init(final JsonNode node) {
        query = queryProperty.apply(node);
        must = mustProperty.apply(node);
        return true;
    }

    public void addQuery(SolrQuery sq) {
        if (must) {
            sq.add("qm", query);
        } else {
            sq.add("q", query);
        }
    }

    public String getQuery() {
        return query;
    }

    public boolean must() {
        return must;
    }

}