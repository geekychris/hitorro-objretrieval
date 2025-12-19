package com.hitorro.obj.core.solr;

import com.hitorro.obj.core.SolrQueryContext;
import com.hitorro.util.json.JsonInitable;
import org.apache.solr.handler.component.ResponseBuilder;

public interface QueryModulatorInterface extends JsonInitable {
    void modulate(ResponseBuilder rb, SolrQueryContext sqc);
}
