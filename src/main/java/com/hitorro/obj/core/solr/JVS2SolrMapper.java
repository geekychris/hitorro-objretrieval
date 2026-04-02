package com.hitorro.obj.core.solr;

import com.hitorro.jsontypesystem.JVS;
import com.hitorro.jsontypesystem.Type;
import com.hitorro.jsontypesystem.executors.BaseProjectionFactoryMapper;
import com.hitorro.jsontypesystem.executors.ExecutionBuilder;
import com.hitorro.jsontypesystem.executors.IndexExecutionBuilderMapper;
import com.hitorro.jsontypesystem.executors.IndexerAction;
import com.hitorro.jsontypesystem.executors.ProjectionContext;
import com.hitorro.util.core.events.cache.HashCache;

public class JVS2SolrMapper extends BaseProjectionMapper<IndexerAction> {

    public JVS2SolrMapper(String... tags) {
        setCache(tags);
    }

    public JVS2SolrMapper() {
        setCache(null);
    }

    @Override
    public JVS apply(final JVS jvs) {
        ProjectionContext pc = project(jvs);
        if (pc == null) {
            return null;
        }
        return pc.target;
    }

    @Override
    protected HashCache<Type, ExecutionBuilder> getCache() {
        return Type.projectionCache.get("index");
    }

    @Override
    public BaseProjectionFactoryMapper<IndexerAction> getMapper() {
        return new IndexExecutionBuilderMapper();
    }
}
