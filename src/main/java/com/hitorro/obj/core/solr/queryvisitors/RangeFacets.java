package com.hitorro.obj.core.solr.queryvisitors;

import com.hitorro.jsontypesystem.JVS;
import com.hitorro.obj.core.solr.RetrievalContext;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.json.keys.StringProperty;

public class RangeFacets extends Facets {
    public static StringProperty gapProperty = new StringProperty("gap", "gap", null);
    public static StringProperty startProperty = new StringProperty("start", "start", null);
    public static StringProperty stopProperty = new StringProperty("end", "end", null);

    @Override
    public void finalizeAux(final JVS jvsQuery, final RetrievalContext rc, String field, String limit) {
        finalizeAux(jvsQuery, rc, "range", field, limit);
    }

    public void finalizeAux(final JVS jvsQuery, final RetrievalContext rc, String type, String field, String limit) {
        String gap = gapProperty.apply(jvsQuery.getJsonNode());
        String start = startProperty.apply(jvsQuery.getJsonNode());
        String stop = stopProperty.apply(jvsQuery.getJsonNode());
        String translatedField = rc.getTranslatedField(field);
        addQuery(rc, "f.%s.facet.%s.gap", gap, translatedField, type);
        addQuery(rc, "f.%s.facet.%s.start", start, translatedField, type);
        addQuery(rc, "f.%s.facet.%s.end", stop, translatedField, type);
        if (!StringUtil.nullOrEmptyString(limit)) {
            addQuery(rc, "f.%s.facet.%s.limit", limit, translatedField, type);
        }
    }
}
