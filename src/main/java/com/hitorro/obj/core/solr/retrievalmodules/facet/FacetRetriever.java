package com.hitorro.obj.core.solr.retrievalmodules.facet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hitorro.jsontypesystem.JVS;
import com.hitorro.obj.core.solr.RetrievalContext;
import com.hitorro.obj.core.solr.retrievalmodules.RetrievalAggregate;
import com.hitorro.obj.core.solr.retrievalmodules.Retriever;
import com.hitorro.util.core.ListUtil;
import com.hitorro.util.core.iterator.AbstractIterator;
import com.hitorro.util.json.keys.JsonProperty;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.RangeFacet;

import java.util.List;

public class FacetRetriever implements Retriever {
    public static final String name = "f";
    public static String key = "facet";
    public static JsonProperty facetRetrieve = new JsonProperty("search.facets", "", null);

    public boolean aggregates() {
        return true;
    }

    public boolean getParticipate(JVS query, final RetrievalContext context) {
        JsonNode node = facetRetrieve.apply(query);
        return node != null;
    }

    @Override
    public AbstractIterator<JVS> appendRetriever(final AbstractIterator<JVS> iter,
                                                 final JVS query,
                                                 final RetrievalContext context) {
        context.addAggregate(new FacetAggregate());
        return iter;
    }
}

class FacetAggregate implements RetrievalAggregate {
    public static final String ANY_VALUE = "ANY_VALUE";
    public static final String name = "facet";

    public static boolean removeAny(String name) {
        return name.equals(ANY_VALUE);
    }

    @Override
    public JVS getAggregate(final RetrievalContext context) {
        ObjectNode on = JsonNodeFactory.instance.objectNode();
        on.put("type", "core_facets");
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        String collName = context.getCollectionName();
        on.set(collName, node);
        node.set("facets", consumeQuery(context));

        return new JVS(on);
    }

    public JsonNode consumeQuery(RetrievalContext qr) {
        if (qr.qr == null) {
            return null;
        }

        ObjectNode root = JsonNodeFactory.instance.objectNode();
        addFacetSets(root, qr, qr.qr.getLimitingFacets());
        addFacetSets(root, qr, qr.qr.getFacetFields());
        addFacetSets(root, qr, qr.qr.getFacetDates());
        addRangeFacetSets(root, qr, qr.qr.getFacetRanges());
        return root;
    }

    private void addFacetSets(ObjectNode node, RetrievalContext qr, List<FacetField> fields) {
        if (ListUtil.nullOrEmpty(fields)) {
            return;
        }
        for (FacetField ff : fields) {
            String name = ff.getName();
            String revName = name;
            if (revName != null) {
                name = revName;
            }
            ObjectNode n = JsonNodeFactory.instance.objectNode();
            node.set(name, n);
            List<FacetField.Count> counts = ff.getValues();
            if (counts == null) {
                continue;
            }
            for (FacetField.Count c : counts) {
                long cnt = c.getCount();
                String rowName = c.getName();
                if (removeAny(rowName)) {
                    continue;
                }
                n.put(rowName, cnt);
            }
        }
    }

    private void addRangeFacetSets(ObjectNode node, RetrievalContext qr, List<RangeFacet> fields) {
        if (ListUtil.nullOrEmpty(fields)) {
            return;
        }
        for (RangeFacet ff : fields) {
            String name = ff.getName();

            String revName = name;
            if (revName != null) {
                name = revName;
            }
            ObjectNode n = JsonNodeFactory.instance.objectNode();
            node.set(name, n);
            List<RangeFacet.Count> counts = ff.getCounts();
            if (counts == null) {
                continue;
            }
            for (RangeFacet.Count c : counts) {
                long cnt = c.getCount();
                String value = c.getValue();

                n.put(value, cnt);
            }
        }
    }
}
