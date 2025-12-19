package com.hitorro.obj.core.solr.queryvisitors;

import com.hitorro.jsontypesystem.JVS;
import com.hitorro.obj.core.solr.RetrievalContext;

public class DateFacets extends RangeFacets {
    @Override
    public void finalizeAux(final JVS jvsQuery, final RetrievalContext rc, String field, String limit) {
        finalizeAux(jvsQuery, rc, "date", field, limit);
    }

    public FacetType getType() {
        return FacetType.Range;
    }
}
