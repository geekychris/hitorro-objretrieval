package com.hitorro.obj.core.solr.retrievalmodules.cluster;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.jsontypesystem.JVS;
import com.hitorro.obj.core.solr.RetrievalContext;
import com.hitorro.obj.core.solr.retrievalmodules.Retriever;
import com.hitorro.util.core.iterator.AbstractIterator;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.json.keys.BooleanProperty;
import com.hitorro.util.json.keys.IntegerProperty;
import com.hitorro.util.json.keys.JsonProperty;
import com.hitorro.util.json.keys.StringProperty;
import org.carrot2.clustering.kmeans.BisectingKMeansClusteringAlgorithm;
import org.carrot2.clustering.lingo.LingoClusteringAlgorithm;


public class ClusteringRetreiver implements Retriever {
    public static IntegerProperty maxResultsKey = new IntegerProperty("rows", "maximum results", 100);
    public static StringProperty queryKey = new StringProperty("query", "query", "");
    public static StringProperty algorithmKey = new StringProperty("algorithm", "algorithm", "lingo");
    public static StringProperty titleKey = new StringProperty("title", "field", "title.mls[en].clean");
    public static StringProperty bodyKey = new StringProperty("body", "field", "body.mls[en].clean");
    public static BooleanProperty includeIdKey = new BooleanProperty("ids", "ids", true);
    public static final String name = "cluster";
    public static final String key = "clust";
    public static final String typeNameKey = "access:cluster";
    public static JsonProperty clusterRetrieve = new JsonProperty("cluster", "", null);
    private final ClusteringIterator iter = null;
    public String url = "url";
    private int maxResults;
    private String query;
    private boolean includeIds;
    private Class algorithm;
    private String title;
    private String body;
    private String collectionName;

    public String getName() {
        return name;
    }

    public boolean aggregates() {
        return true;
    }

    public boolean getParticipate(JVS query, final RetrievalContext context) {
        JsonNode node = clusterRetrieve.apply(query.getJsonNode());
        if (node != null) {
            maxResults = maxResultsKey.apply(node);
            context.results = Math.max(maxResultsKey.apply(node), context.results);
            return true;
        }
        return false;
    }

    @Override
    public AbstractIterator<JVS> appendRetriever(final AbstractIterator<JVS> iter,
                                                 final JVS query,
                                                 final RetrievalContext context) {

        JsonNode jn = clusterRetrieve.apply(query.getJsonNode());
        if (jn != null) {
            maxResults = maxResultsKey.apply(jn);
            context.results = Math.max(maxResultsKey.apply(jn), context.results);

            title = titleKey.apply(jn);
            body = bodyKey.apply(jn);

            includeIds = includeIdKey.apply(jn);
            String algo = algorithmKey.apply(jn);
            if ("bisect".equals(algo)) {
                this.algorithm = BisectingKMeansClusteringAlgorithm.class;
            } else {
                this.algorithm = LingoClusteringAlgorithm.class;
            }


            collectionName = context.getCollectionName();
            if (StringUtil.nullOrEmptyOrBlankString(collectionName)) {
                collectionName = "default";
            }
            String q = queryKey.apply(jn);
            ClusteringIterator ci = new ClusteringIterator(iter, title, body, url, q, includeIds, algorithm, collectionName);
            context.addAggregate(ci);
            return ci;
        }
        return iter;
    }
}
