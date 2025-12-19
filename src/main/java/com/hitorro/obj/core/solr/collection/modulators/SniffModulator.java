package com.hitorro.obj.core.solr.collection.modulators;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.obj.core.SolrQueryContext;
import com.hitorro.obj.core.solr.QueryModulatorInterface;
import org.apache.solr.handler.component.ResponseBuilder;

public class SniffModulator implements QueryModulatorInterface {
    @Override
    public void modulate(final ResponseBuilder rb, final SolrQueryContext sqc) {
        int i = 1;
    }

    @Override
    public boolean init(final JsonNode node) {
        return true;
    }
}