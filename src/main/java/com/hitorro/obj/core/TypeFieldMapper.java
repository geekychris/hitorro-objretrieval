package com.hitorro.obj.core;

import gnu.trove.map.hash.TLongObjectHashMap;
import com.hitorro.features.FeatureEngine;
import com.hitorro.jsontypesystem.Group;
import com.hitorro.jsontypesystem.SolrFieldType;
import com.hitorro.jsontypesystem.SolrFieldTypes;
import com.hitorro.jsontypesystem.Type;
import com.hitorro.jsontypesystem.executors.ExecutionBuilder;
import com.hitorro.jsontypesystem.executors.IndexerAction;
import com.hitorro.util.core.events.cache.HashCache;
import com.hitorro.util.json.keys.propaccess.Propaccess;

import java.util.HashMap;
import java.util.Map;

import static com.hitorro.obj.core.TypeFieldMapper.FieldType.Special;

/**
 * Context to go back and forth between mapped fields and type fields along with
 * utilities for introspection.
 */
public class TypeFieldMapper {
    private final Type type;
    private final Map<String, Propaccess> decoratedToPath = new HashMap();
    private final TLongObjectHashMap<String> toDecoratedPathMap = new TLongObjectHashMap<>();

    public TypeFieldMapper(Type type) {
        this.type = type;
        HashCache<Type, ExecutionBuilder> cache = Type.projectionCache.get("index");
        ExecutionBuilder<IndexerAction> eb = cache.get(type);
    }

    public FieldType getFieldType(Propaccess path, Type t) {
        if (path == null) {
            return null;
        }
        if (FeatureEngine.getEngine().getFeature(path.toString()) != null) {
            return FieldType.FeatureField;
        }
        int depth = type.getMaxCommonDepth(path);
        if (depth > 0) {
            return FieldType.TypeField;
        }
        return Special;
    }

    public Propaccess getAccessFromDecoratedPath(String path) {
        Propaccess result = decoratedToPath.get(path);
        if (result != null) {
            return result;
        }
        Propaccess pathIn = new Propaccess(path);
        int depth = type.getMaxCommonDepth(pathIn);
        if (depth == 0) {
            return null;
        }
        result = pathIn.copy(depth + 1);
        decoratedToPath.put(path, result);
        return result;
    }

    public String getDecoratedPathFor(Type type, Propaccess access, String lang) {
        String method = "";

        int depth = type.getMaxCommonDepth(access);
        if (depth == 0) {
            return access.toString();
        }
        if (depth < access.length()) {
            method = access.get(depth).name();
            access.setLength(depth);
        } else {
            Group group = type.getDefaultGroupFor(access, "index");
            if (group == null) {
                return access.toString();
            }
            method = group.getMethod();
        }
        return getDecoratedPathFor(access, method, lang);
    }

    public String getDecoratedPathFor(Propaccess path, String method, String lang) {
        boolean isMulti = type.isMultiValuedPath(path);
        StringBuilder sb = new StringBuilder();
        SolrFieldTypes sfts = SolrFieldTypes.solrFieldTypeCache.get();
        SolrFieldType sft = sfts.get(method);
        path.getPathSansIndex(sb);
        if (sft == null) {
            return path.toString();
        }
        sft.get(sb, lang, isMulti);
        return sb.toString();
    }

    public enum FieldType {
        TypeField, FeatureField, Special
    }
}
