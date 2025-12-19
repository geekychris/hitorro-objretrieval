package com.hitorro.obj.core.solr.retrievalmodules.cluster;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hitorro.jsontypesystem.JVS;
import com.hitorro.obj.core.solr.RetrievalContext;
import com.hitorro.obj.core.solr.retrievalmodules.RetrievalAggregate;
import com.hitorro.util.core.iterator.AbstractIterator;
import com.hitorro.util.json.JSONUtil;
import com.hitorro.util.json.keys.StringProperty;
import org.carrot2.core.*;

import java.util.ArrayList;
import java.util.List;

public class ClusteringIterator extends AbstractIterator<JVS> implements RetrievalAggregate {
    public static final String typeNameKey = "base:cluster";
    public static final String ScoreKey = "score";
    public static final String ClustersKey = "clusters";
    public static final String PhrasesKey = "phrases";
    public static final String IdsKey = "ids";
    public static final String IdCountKey = "count";
    private final StringProperty title;
    private final StringProperty body;
    private final StringProperty url;
    private final AbstractIterator<JVS> iterIn;
    private final String query;
    private final boolean includeIds;
    private final List<String> ids = new ArrayList();
    private final Class algorithm;
    private final String name;
    ArrayList<Document> documents = new ArrayList<Document>();
    private int counter = 0;

    public ClusteringIterator(AbstractIterator<JVS> iterIn,
                              String titleIn,
                              String bodyIn,
                              String urlIn,
                              String query,
                              boolean includeIds,
                              Class algorithm,
                              String name) {
        this.title = new StringProperty(titleIn, "", "");
        this.body = new StringProperty(bodyIn, "", "");
        this.url = new StringProperty(urlIn, "", "");
        this.query = query;
        this.iterIn = iterIn;
        this.includeIds = includeIds;
        this.algorithm = algorithm;
        this.name = name;
    }

    @Override
    public void close() throws Exception {
        iterIn.close();
    }

    @Override
    public boolean hasNext() {
        return iterIn.hasNext();
    }

    @Override
    public JVS next() {
        JVS vs = iterIn.next();
        JVS doc = vs.getDoc();
        documents.add(new Document(title.apply(doc), body.apply(doc), url.apply(doc)));
        ids.add(doc.getId());
        counter++;
        return vs;
    }

    @Override
    public void remove() {
        iterIn.remove();
    }


    public JVS getAggregate(final RetrievalContext context) {
        Controller controller = ControllerFactory.createSimple();
        ProcessingResult byTopicClusters = controller.process(documents, query,
                algorithm);
        List<Cluster> clustersByTopic = byTopicClusters.getClusters();
        ObjectNode root = JsonNodeFactory.instance.objectNode();
        root.put("type", typeNameKey);
        root.put("query", query);
        root.put("count", counter);
        ObjectNode namedCollection = JsonNodeFactory.instance.objectNode();
        root.set(name, namedCollection);
        collectCluster(clustersByTopic, namedCollection);


       /* if (true)
        {
            ProcessingResult byDomainClusters = controller.process(documents, null,
                                                                   ByUrlClusteringAlgorithm.class);
        }*/

        return new JVS(root);
    }

    private void collectCluster(List<Cluster> clustersByTopic, ObjectNode root) {
        ArrayNode clusters = JsonNodeFactory.instance.arrayNode();
        root.set(ClustersKey, clusters);
        for (Cluster c : clustersByTopic) {
            List<Document> docs = c.getAllDocuments();
            List<String> phrases = c.getPhrases();
            ObjectNode clus = JsonNodeFactory.instance.objectNode();

            clus.put(ScoreKey, c.getScore());

            ArrayNode phrasesArr = JSONUtil.toJsonStringArray(phrases);
            clus.set(PhrasesKey, phrasesArr);
            clus.put(IdCountKey, docs.size());
            ArrayNode arr = JsonNodeFactory.instance.arrayNode();
            if (includeIds) {
                for (Document d : docs) {
                    arr.add(ids.get(d.getId()));
                }
                clus.set(IdsKey, arr);
            }
            clusters.add(clus);
        }
    }
}
