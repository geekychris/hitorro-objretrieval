package com.hitorro.obj.core.solr.retrievalmodules;

import com.hitorro.jsontypesystem.JVS;
import com.hitorro.obj.core.solr.RetrievalContext;

public interface RetrievalAggregate {
    JVS getAggregate(final RetrievalContext context);
}
