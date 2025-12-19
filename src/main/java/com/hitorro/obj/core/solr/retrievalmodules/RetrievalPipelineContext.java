package com.hitorro.obj.core.solr.retrievalmodules;

import com.hitorro.jsontypesystem.JVS;
import com.hitorro.obj.core.solr.RetrievalContext;
import com.hitorro.util.core.iterator.CollectionIterator;
import com.hitorro.util.json.keys.CollectionProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class RetrievalPipelineContext {
    private static final RetrievalPipelineContext me = new RetrievalPipelineContext();
    CollectionProperty<Retriever> retrieversKey = CollectionProperty.getJsonInitableCollection("front", Retriever.class, "");
    private List<Retriever> retrievers = new ArrayList();

    protected RetrievalPipelineContext() {
        retrievers = retrieversKey.apply();
    }

    public static RetrievalPipelineContext me() {
        return me;
    }

    public RetrievalPipeline get(JVS query, RetrievalContext rc) {
        List<Retriever> participate = new CollectionIterator(retrievers).filter(
                new RetrieverPredicate(query, rc)).toCollection(new ArrayList<>());
        return new RetrievalPipeline(participate);
    }
}

class RetrieverPredicate implements Predicate<Retriever> {
    private final JVS query;
    private final RetrievalContext rc;

    public RetrieverPredicate(JVS query, RetrievalContext rc) {
        this.query = query;
        this.rc = rc;
    }

    @Override
    public boolean test(final Retriever retriever) {
        return retriever.getParticipate(query, rc);
    }


}
