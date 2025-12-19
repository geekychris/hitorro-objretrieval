package com.hitorro.obj.core.solr;

import com.hitorro.jsontypesystem.JVS;
import com.hitorro.jsontypesystem.Type;
import com.hitorro.util.core.events.cache.HashCache;

public class JVS2SolrMapper extends BaseProjectionMapper<com.hitorro.jsontypesystem.executors.IndexerAction> {

    public JVS2SolrMapper(String... tags) {
        setCache(tags);
    }

    public JVS2SolrMapper() {
        setCache(null);
    }

    @Override
    public JVS apply(final JVS jvs) {
        com.hitorro.jsontypesystem.executors.ProjectionContext pc = project(jvs);
        if (pc == null) {
            return null;
        }
        return pc.target;
    }

    @Override
    protected HashCache<Type, com.hitorro.jsontypesystem.executors.ExecutionBuilder> getCache() {
        return Type.projectionCache.get("index");
    }

    @Override
    public com.hitorro.jsontypesystem.executors.BaseProjectionFactoryMapper<com.hitorro.jsontypesystem.executors.IndexerAction> getMapper() {
        return new com.hitorro.jsontypesystem.executors.IndexExecutionBuilderMapper();
    }
}
