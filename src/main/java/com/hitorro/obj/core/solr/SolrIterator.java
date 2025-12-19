package com.hitorro.obj.core.solr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hitorro.jsontypesystem.JVS;
import com.hitorro.jsontypesystem.JsonTypeSystem;
import com.hitorro.jsontypesystem.Type;
import com.hitorro.obj.core.solr.retrievalmodules.RetrievalAggregate;
import com.hitorro.util.core.iterator.AbstractIterator;
import com.hitorro.util.core.iterator.Mapper;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.json.keys.propaccess.Propaccess;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SolrIterator extends AbstractIterator<JVS> implements RetrievalAggregate {
    public static String typeNameKey = "core_summary";
    private final QueryResponse qr;
    private final Iterator<SolrDocument> iter;
    private final SolrDocument2JVSMapper mapper;

    public SolrIterator(QueryResponse qr) {
        this.qr = qr;
        SolrDocumentList s = qr.getResults();
        mapper = new SolrDocument2JVSMapper(qr);
        iter = s.iterator();
    }

    @Override
    public boolean hasNext() {
        return iter.hasNext();
    }

    @Override
    public JVS next() {
        return mapper.apply(iter.next());
    }

    @Override
    public JVS getAggregate(final RetrievalContext context) {
        ObjectNode root = JsonNodeFactory.instance.objectNode();
        root.put("type", typeNameKey);
        ObjectNode col = JsonNodeFactory.instance.objectNode();
        col.put("maxscore", qr.getResults().getMaxScore());
        col.put("numberfound", qr.getResults().getNumFound());
        col.put("start", qr.getResults().getStart());
        col.put("query_time", qr.getQTime());
        col.put("elapsed_time", qr.getElapsedTime());
        if (qr.getDebugMap() != null) {
            JsonNode timing = SolrJSONUtils.getSolrMap2Json(qr.getDebugMap().get("timing"));
            if (timing != null) {
                col.set("detailed_timing", timing);
            }
        }
        root.set(context.getCollectionName(), col);


        return new JVS(root);
    }
}

class SolrDocument2JVSMapper implements Mapper<SolrDocument, JVS> {
    private final Type resultType;
    private final Map<String, Propaccess> map = new HashMap();
    private final QueryResponse qr;
    private JsonNode explainMap;

    SolrDocument2JVSMapper(QueryResponse qr) {
        resultType = JsonTypeSystem.getMe().getType("result");
        this.qr = qr;
        if (qr.getDebugMap() != null) {
            explainMap = SolrJSONUtils.getSolrMap2Json(qr.getDebugMap().get("explain"));
        }
    }

    @Override
    public JVS apply(final SolrDocument d) {
        // this is where we pull out all the solr generated fields and place them back into the result row.
        // logic has to handle field names that should map back to regular object fields.
        JVS jvs = new JVS();
        for (Map.Entry<String, Object> e : d.entrySet()) {
            String k = e.getKey();
            Propaccess access = translate(k);
            jvs.set(access, e.getValue());

        }
        jvs.setType(resultType);
        setExplainInfo(jvs);
        return jvs;
    }

    private void setExplainInfo(final JVS jvs) {
        if (explainMap != null) {
            String id = jvs.getId();
            JsonNode v = explainMap.get(id);
            if (v != null) {
                jvs.set("_debug", v.textValue());
            }
        }
    }

    private Propaccess translate(String f) {
        Propaccess v = map.get(f);
        if (v != null) {
            return v;
        }
        v = translateAux(f);
        map.put(f, v);
        return v;
    }

    private Propaccess translateAux(String f) {
        if (f.equals("id.id")) {
            return new Propaccess(f);
        }
        return new Propaccess(Fmt.S("select.%s", f));
    }


}
