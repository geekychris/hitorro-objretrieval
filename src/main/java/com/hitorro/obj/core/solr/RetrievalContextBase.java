package com.hitorro.obj.core.solr;

import com.hitorro.features.DataType;
import com.hitorro.features.Feature;
import com.hitorro.features.FeatureEngine;
import com.hitorro.features.FeatureKeyType;
import com.hitorro.jsontypesystem.Type;
import com.hitorro.obj.core.TypeFieldMapper;
import com.hitorro.obj.core.solr.collection.CollectionConfig;
import com.hitorro.obj.core.solr.queryvisitors.SelectRow;
import com.hitorro.util.core.error.ErrorCape;
import com.hitorro.util.core.error.Errors;
import com.hitorro.util.core.events.cache.HashCache;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.json.keys.propaccess.Propaccess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RetrievalContextBase implements ErrorCape {
    protected static Map<Type, TypeFieldMapper> mappers = new HashMap();
    protected List<SelectRow> aliases = new ArrayList();
    protected Errors errors = new Errors();

    protected String lang;
    protected Type type;
    protected CollectionConfig cc;
    protected HashCache<String, Propaccess> propCache = new HashCache<>((t) -> {
        return new Propaccess(t);
    });

    public RetrievalContextBase(CollectionConfig cc, String lang) {
        this.cc = cc;

        this.type = cc.getType();
        this.lang = lang;
    }

    public String getMappedField(String field) {
        if (field.endsWith("_s") || field.endsWith("_m")) {
            return field;
        }
        Propaccess a = propCache.get(field);
        return getMapper().getDecoratedPathFor(type, a, lang);
    }

    public String getLang() {
        return lang;
    }


    public void addAlias(String name, String alias) {
        aliases.add(new SelectRow(name, alias));
    }

    public void addAlias(List<SelectRow> rows) {
        aliases.addAll(rows);
    }

    public List<SelectRow> getAliases() {
        return aliases;
    }


    public TypeFieldMapper getMapper() {
        TypeFieldMapper tfm = mappers.get(type);
        if (tfm == null) {
            tfm = new TypeFieldMapper(type);
            mappers.put(type, tfm);
        }
        return tfm;
    }


    public String getPathForFeature(String field) {
        Feature feature = FeatureEngine.getEngine().getFeature(field);
        DataType dt = feature.getDataType();
        FeatureKeyType fkt = feature.getKeyType();

        //XXX we need to change this to encode what we want the feature runtime todo on the SOLR side.
        return feature.getName();
    }

    public String getTranslatedField(String s) {
        if (StringUtil.nullOrEmptyString(s)) {
            return s;
        }
        TypeFieldMapper tfm = getMapper();
        Propaccess access = new Propaccess(s);
        TypeFieldMapper.FieldType ft = tfm.getFieldType(access, type);
        switch (ft) {
            case FeatureField:
                s = Fmt.S("field(%s)", s);
                break;
            case TypeField:
                s = tfm.getDecoratedPathFor(type, access, lang);
                break;
            case Special:
                // do nothing.
                break;
        }
        return s;
    }

    @Override
    public Errors getErrors() {
        return errors;
    }

}
