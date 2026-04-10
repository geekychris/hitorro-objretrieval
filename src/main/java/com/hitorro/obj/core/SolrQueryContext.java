package com.hitorro.obj.core;

import com.hitorro.jsontypesystem.JVS;
import com.hitorro.jsontypesystem.JsonTypeSystem;
import com.hitorro.jsontypesystem.Type;
import com.hitorro.obj.core.solr.RetrievalContextBase;
import com.hitorro.obj.core.solr.collection.CollectionConfig;
import com.hitorro.util.core.string.StringUtil;
import org.apache.solr.handler.component.ResponseBuilder;

public class SolrQueryContext {
    public static ThreadLocal<SolrQueryContext> tl = new ThreadLocal<>();
    private JVS query;
    private Type queryType;
    private CollectionConfig cc;
    private boolean valid;
    private RetrievalContextBase retrievalContext;
    private String lang;

    public static SolrQueryContext get() {
        SolrQueryContext sqc = tl.get();
        if (sqc == null) {
            sqc = new SolrQueryContext();
            tl.set(sqc);
        }
        return sqc;
    }

    public JVS getQuery() {
        return query;
    }

    public Type getType() {
        return queryType;
    }

    public RetrievalContextBase getRetrievalContext() {
        return retrievalContext;
    }

    public CollectionConfig getCollectionConfig() {
        return cc;
    }

    public void setResponse(ResponseBuilder rb) {
        String q = rb.req.getParams().get("jvs");
        if (StringUtil.nullOrEmptyString(q)) {
            query = null;
            valid = false;
        } else {
            query = JVS.read(q);
            queryType = JsonTypeSystem.getMe().getType(CollectionConfig.collectionMetaTypeKey.apply(query.getJsonNode()));
            cc = CollectionConfig.configCache.get(CollectionConfig.collectionMetaNameKey.apply(query.getJsonNode()));
            lang = CollectionConfig.langKey.apply(query.getJsonNode());
            retrievalContext = new RetrievalContextBase(cc, lang);
            valid = (cc != null && queryType != null);
        }
    }

    public boolean isValid() {
        return valid;
    }
}
