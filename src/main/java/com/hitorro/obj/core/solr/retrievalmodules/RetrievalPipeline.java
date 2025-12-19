package com.hitorro.obj.core.solr.retrievalmodules;

import com.hitorro.jsontypesystem.JVS;
import com.hitorro.obj.core.solr.RetrievalContext;
import com.hitorro.util.core.iterator.AbstractIterator;
import com.hitorro.util.core.iterator.sinks.Sink;

import java.util.List;

public class RetrievalPipeline {
    private final List<Retriever> list;

    public RetrievalPipeline(List<Retriever> list) {
        this.list = list;
    }

    public AbstractIterator<JVS> constructChain(JVS query, AbstractIterator<JVS> iter, RetrievalContext context) {
        for (Retriever ret : list) {
            iter = ret.appendRetriever(iter, query, context);
        }
        return iter;
    }

    public void applyAggregates(Sink<JVS> sink, RetrievalContext rc) {
        rc.addAggregates(sink);
    }
}

