package com.hitorro.obj.core.solr.collection;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.obj.core.SolrQueryContext;
import com.hitorro.obj.core.solr.QueryModulatorInterface;
import com.hitorro.util.json.JsonInitable;
import com.hitorro.util.json.keys.JsonInitableProperty;
import org.apache.solr.handler.component.ResponseBuilder;

import java.util.ArrayList;
import java.util.List;

public class CollectionIntent implements JsonInitable {
    private final List<QueryModulatorInterface> modulators = new ArrayList();
    public JsonInitableProperty<QueryModulatorInterface> modulatorKey =
            new JsonInitableProperty<QueryModulatorInterface>("", "", null, QueryModulatorInterface.class, null);

    public void modulate(ResponseBuilder rb, SolrQueryContext sqc) {
        for (QueryModulatorInterface modulator : modulators) {
            modulator.modulate(rb, sqc);
        }
    }

    @Override
    public boolean init(final JsonNode node) {
        if (node.isArray()) {
            for (JsonNode row : node) {
                QueryModulatorInterface i = modulatorKey.apply(row);
                if (i != null) {
                    modulators.add(i);
                }
            }
        } else {
            QueryModulatorInterface i = modulatorKey.apply(node);
            if (i != null) {
                modulators.add(i);
            }
        }
        return true;
    }

    public List<QueryModulatorInterface> get() {
        return modulators;
    }
}
