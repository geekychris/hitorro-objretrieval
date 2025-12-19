package com.hitorro.obj.core.solr.queryvisitors;

import com.hitorro.jsontypesystem.JVS;
import com.hitorro.obj.core.solr.RetrievalContext;
import com.hitorro.util.json.keys.BooleanProperty;

public class Debug implements QueryVisitor {
    public static BooleanProperty debug = new BooleanProperty("search.debug", "", false);
    public static BooleanProperty timing = new BooleanProperty("search.timing", "", true);

    @Override
    public void visit(final JVS jvsQuery, final RetrievalContext retrievalContext) {
    }

    @Override
    public void finalize(final JVS jvsQuery, final RetrievalContext retrievalContext) {
        if (debug.apply(jvsQuery)) {
            retrievalContext.getSolrQuery().setShowDebugInfo(true);
        }
        if (timing.apply(jvsQuery)) {
            retrievalContext.getSolrQuery().set("debug", "timing");
        }
    }
}
    /*
    q.setShowDebugInfo(sc.debug);
            //q.set("debug.explain.structured", sc.debug);
            q.set("debug", "timing");
     */

