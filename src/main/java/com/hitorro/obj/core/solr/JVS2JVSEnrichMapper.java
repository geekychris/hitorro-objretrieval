package com.hitorro.obj.core.solr;

import com.hitorro.jsontypesystem.JVS;
import com.hitorro.jsontypesystem.Type;
import com.hitorro.util.core.events.cache.HashCache;

public class JVS2JVSEnrichMapper extends BaseProjectionMapper<com.hitorro.jsontypesystem.executors.EnrichAction> {

    public JVS2JVSEnrichMapper(String... tags) {
        setCache(tags);
    }

    public JVS2JVSEnrichMapper() {
        setCache(null);
    }

    public com.hitorro.jsontypesystem.executors.BaseProjectionFactoryMapper<com.hitorro.jsontypesystem.executors.EnrichAction> getMapper() {
        return new com.hitorro.jsontypesystem.executors.EnrichExecutionBuilderMapper();
    }

    @Override
    public JVS apply(final JVS jvs) {
        com.hitorro.jsontypesystem.executors.ProjectionContext pc = project(jvs);
        if (pc == null) {
            return null;
        }
        return pc.source;
    }


    @Override
    protected HashCache<Type, com.hitorro.jsontypesystem.executors.ExecutionBuilder> getCache() {
        return Type.projectionCache.get("enrich");
    }
}
