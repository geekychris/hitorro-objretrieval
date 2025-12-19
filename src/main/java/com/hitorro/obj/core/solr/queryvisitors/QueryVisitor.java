package com.hitorro.obj.core.solr.queryvisitors;

import com.hitorro.jsontypesystem.JVS;
import com.hitorro.obj.core.solr.RetrievalContext;

public interface QueryVisitor {
    QueryVisitor[] visitors = {new Query(), new Debug(), new Facets(), new Filter(), new Group(), new Select(), new Sort(), new Rows()};

    void visit(JVS jvsQuery, final RetrievalContext retrievalContext);

    void finalize(JVS jvsQuery, RetrievalContext retrievalContext);
}


