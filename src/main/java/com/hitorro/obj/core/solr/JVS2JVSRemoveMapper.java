package com.hitorro.obj.core.solr;

import com.hitorro.jsontypesystem.JVS;
import com.hitorro.jsontypesystem.Type;
import com.hitorro.jsontypesystem.executors.ExecutionBuilder;
import com.hitorro.jsontypesystem.executors.ProjectionContext;
import com.hitorro.jsontypesystem.executors.RemoveAction;
import com.hitorro.jsontypesystem.executors.RemoveExecutionBuilderMapper;
import com.hitorro.util.core.events.cache.HashCache;

public class JVS2JVSRemoveMapper extends BaseProjectionMapper<RemoveAction> {

    public JVS2JVSRemoveMapper(String... tags) {
        setCache(tags);
    }

    public JVS2JVSRemoveMapper() {
        setCache(null);
    }

    public RemoveExecutionBuilderMapper getMapper() {
        return new RemoveExecutionBuilderMapper();
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
        return Type.projectionCache.get("remove");
    }
}