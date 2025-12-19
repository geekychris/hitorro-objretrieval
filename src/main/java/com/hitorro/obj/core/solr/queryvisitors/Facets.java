package com.hitorro.obj.core.solr.queryvisitors;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.jsontypesystem.JVS;
import com.hitorro.obj.core.solr.RetrievalContext;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.json.keys.BooleanProperty;
import com.hitorro.util.json.keys.JsonProperty;
import com.hitorro.util.json.keys.StringProperty;
import org.apache.solr.client.solrj.SolrQuery;

import java.util.LinkedList;
import java.util.List;

public class Facets implements QueryVisitor {
    private static final BooleanProperty isDate = new BooleanProperty("isdate", "", false);
    private static final BooleanProperty isRange = new BooleanProperty("isrange", "", false);
    private static final StringProperty sortKey = new StringProperty("sort", "", "count");
    public static StringProperty fieldProperty = new StringProperty("field", "", null);
    public static StringProperty limitProperty = new StringProperty("limit", "", null);
    public static JsonProperty facetsKey = new JsonProperty("search.facets", "facets", null);

    protected static Facets getFacet(JsonNode node) {
        Facets ret;
        if (isDate.apply(node)) {
            ret = new DateFacets();
        } else {
            if (isRange.apply(node)) {
                ret = new RangeFacets();
            } else {
                ret = new Facets();
            }
        }
        return ret;
    }

    @Override
    public void visit(final JVS jvsQuery, final RetrievalContext retrievalContext) {

    }

    public FacetType getType() {
        return FacetType.Simple;
    }

    protected void addQuery(RetrievalContext q, String msg, String value, String... args) {
        q.getSolrQuery().set(Fmt.S(msg, args), value);
    }

    @Override
    public void finalize(final JVS jvsQuery, final RetrievalContext rc) {
        JsonNode facets = facetsKey.apply(jvsQuery);
        if (facets == null) {
            return;
        }
        Fields fields = new Fields();
        rc.getSolrQuery().set("facet", "on");
        //rc.getSolrQuery().set("facet.method", "enum");
        if (facets.isArray()) {
            for (JsonNode facet : facets) {
                if (!getFacetAux(jvsQuery, rc, facet, fields)) {
                    return;
                }
            }
        } else {
            getFacetAux(jvsQuery, rc, facets, fields);
        }
        fields.addQuery(rc.getSolrQuery());
    }

    protected boolean getFacetAux(final JVS jvsQuery, final RetrievalContext rc, final JsonNode facet, Fields fields) {
        Facets f = getFacet(facet);
        if (f == null) {
            return true;
        }
        String field = fieldProperty.apply(facet);
        String limit = limitProperty.apply(facet);
        String sortValue = sortKey.apply(facet);

        String translatedField = rc.getTranslatedField(field);
        fields.add(translatedField, f.getType());
        if (!StringUtil.nullOrEmptyString(limit)) {
            addQuery(rc, "f.%s.facet.limit", limit, translatedField);
        }
        if (!StringUtil.nullOrEmptyOrBlankString(sortValue)) {
            addQuery(rc, "f.%s.facet.sort", sortValue, translatedField);
        }
        f.finalizeAux(jvsQuery, rc, field, limit);

        return true;
    }

    public void finalizeAux(JVS jvsQuery, RetrievalContext rc, String field, String limit) {

    }

    public enum FacetType {
        Simple, Date, Range
    }
}

class Fields {
    List<String> dateFieldNames = new LinkedList();
    List<String> simpleFieldNames = new LinkedList();
    List<String> rangeFieldNames = new LinkedList();

    public void add(String name, Facets.FacetType type) {
        switch (type) {
            case Simple:
                simpleFieldNames.add(name);
                return;
            case Date:
                dateFieldNames.add(name);
                return;
            case Range:
                rangeFieldNames.add(name);

        }
    }

    public void addQuery(SolrQuery query) {
        if (!dateFieldNames.isEmpty()) {
            query.set("facet.date", dateFieldNames.toArray(new String[dateFieldNames.size()]));
        }
        if (!rangeFieldNames.isEmpty()) {
            query.set("facet.range", rangeFieldNames.toArray(new String[rangeFieldNames.size()]));
        }
        if (!simpleFieldNames.isEmpty()) {
            query.set("facet.field", simpleFieldNames.toArray(new String[simpleFieldNames.size()]));
        }

    }
}