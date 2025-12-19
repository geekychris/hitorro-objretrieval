package com.hitorro.obj.core.solr;

import com.hitorro.jsontypesystem.BaseT;
import com.hitorro.jsontypesystem.Group;
import com.hitorro.jsontypesystem.JVS;
import com.hitorro.jsontypesystem.Type;
import com.hitorro.jsontypesystem.executors.BaseProjectionFactoryMapper;
import com.hitorro.jsontypesystem.executors.ExecutionBuilder;
import com.hitorro.jsontypesystem.executors.ExecutorAction;
import com.hitorro.jsontypesystem.executors.ProjectionContext;
import com.hitorro.util.core.ArrayUtil;
import com.hitorro.util.core.ListUtil;
import com.hitorro.util.core.events.cache.HashCache;
import com.hitorro.util.core.iterator.mappers.BaseMapper;
import com.hitorro.util.core.map.MapUtil;
import com.hitorro.util.core.thread.ThreadStash;
import com.hitorro.util.json.keys.propaccess.PropaccessError;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public abstract class BaseProjectionMapper<ACTION extends ExecutorAction> extends BaseMapper<JVS, JVS> {
    protected ThreadStash<ProjectionContext> ts = new ThreadStash<ProjectionContext>() {
        @Override
        public ProjectionContext getNew() {
            return new ProjectionContext();
        }
    };

    protected HashCache<Type, ExecutionBuilder> cache;
    protected ProjectionContext pc = new ProjectionContext();
    private String[] tags;

    protected abstract HashCache<Type, ExecutionBuilder> getCache();

    protected void setCache(String... tags) {
        this.tags = tags;
        if (ArrayUtil.nullOrEmpty(tags)) {
            BaseProjectionFactoryMapper mapper = getMapper();
            String[] basic = {"basic"};
            mapper.setPredicate(mapper.getPredicate().and(new GroupTagPredicated(basic).or(new GroupTagNull())));
            cache = Type.getExecBuilderCache("e", mapper);
        } else {
            BaseProjectionFactoryMapper mapper = getMapper();
            mapper.setPredicate(mapper.getPredicate().and(new GroupTagPredicated(tags).or(new GroupTagNull())));
            cache = Type.getExecBuilderCache("e", mapper);
        }
    }

    public abstract BaseProjectionFactoryMapper<ACTION> getMapper();

    protected ProjectionContext project(final JVS jvs) {
        ProjectionContext pc = ts.get();
        pc.source = jvs;
        pc.target = new JVS();
        try {
            cache.get(jvs.getType()).getCurrentNode().project(pc);
        } catch (PropaccessError propaccessError) {
            return null;
        }
        return pc;
    }
}


class GroupTagPredicated implements Predicate<BaseT> {
    private final Set<String> tags;

    public GroupTagPredicated(String[] tagsIn) {
        tags = MapUtil.getSet(tagsIn);
    }

    @Override
    public boolean test(final BaseT b) {
        if (b instanceof Group group) {
            for (String s : group.getTags()) {
                if (tags.contains(s)) {
                    return true;
                }
            }
        }

        return false;
    }
}

class GroupTagNull implements Predicate<BaseT> {
    public GroupTagNull() {
    }

    @Override
    public boolean test(final BaseT b) {
        if (b instanceof Group group) {
            List<String> tags = group.getTags();
            return ListUtil.nullOrEmpty(tags);
        }

        return false;
    }
}

