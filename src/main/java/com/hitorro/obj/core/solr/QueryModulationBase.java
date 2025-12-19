package com.hitorro.obj.core.solr;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.obj.core.SolrQueryContext;
import com.hitorro.obj.core.solr.collection.CollectionIntent;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.json.keys.StringProperty;
import org.apache.solr.handler.component.ResponseBuilder;

public class QueryModulationBase implements QueryModulatorInterface {
    public static StringProperty intentKey = new StringProperty("search.intent", "", null);

    public static QueryModulationBase me = new QueryModulationBase();


    public void modulate(ResponseBuilder rb, SolrQueryContext sqc) {
        if (sqc.getQuery() == null) {
            return;
        }
        String intent = sqc.getQuery().getString(intentKey);
        if (StringUtil.nullOrEmptyString(intent)) {
            return;
        }
        CollectionIntent qmi = sqc.getCollectionConfig().getIntent(intent);
        if (qmi != null) {
            qmi.modulate(rb, sqc);
        }
    }

    @Override
    public boolean init(final JsonNode node) {
        return true;
    }
}
