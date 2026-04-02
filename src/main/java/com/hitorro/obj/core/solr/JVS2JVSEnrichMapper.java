package com.hitorro.obj.core.solr;

import com.hitorro.jsontypesystem.JVS;
import com.hitorro.jsontypesystem.Type;
import com.hitorro.jsontypesystem.executors.BaseProjectionFactoryMapper;
import com.hitorro.jsontypesystem.executors.EnrichAction;
import com.hitorro.jsontypesystem.executors.EnrichExecutionBuilderMapper;
import com.hitorro.jsontypesystem.executors.ExecutionBuilder;
import com.hitorro.jsontypesystem.executors.ProjectionContext;
import com.hitorro.util.core.events.cache.HashCache;

public class JVS2JVSEnrichMapper extends BaseProjectionMapper<EnrichAction> {

    public JVS2JVSEnrichMapper(String... tags) {
        setCache(tags);
    }

    public JVS2JVSEnrichMapper() {
        setCache(null);
    }

    public BaseProjectionFactoryMapper<EnrichAction> getMapper() {
        return new EnrichExecutionBuilderMapper();
    }

    @Override
    public JVS apply(final JVS jvs) {
        ProjectionContext pc = project(jvs);
        if (pc == null) {
            return null;
        }
        return pc.source;
    }


    @Override
    protected HashCache<Type, ExecutionBuilder> getCache() {
        return Type.projectionCache.get("enrich");
    }
}
