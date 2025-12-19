package com.hitorro.obj.core.solr.queryvisitors;

import com.hitorro.jsontypesystem.JVS;
import com.hitorro.obj.core.solr.RetrievalContext;

public class Group extends Sort {
    @Override
    public void finalize(final JVS jvsQuery, final RetrievalContext rc) {
        StringBuilder sb = finalizeAux(jvsQuery, rc);
        if (sb.length() == 0) {
            return;
        }
        rc.getSolrQuery().set("group.sort", sb.toString());
        rc.getSolrQuery().set("group", "true");
    }
}
