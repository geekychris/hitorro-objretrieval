package com.hitorro.obj.core.solr.retrievalmodules.solr;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.jsontypesystem.JVS;
import com.hitorro.obj.core.solr.RetrievalContext;
import com.hitorro.obj.core.solr.SolrIterator;
import com.hitorro.obj.core.solr.collection.CollectionConfig;
import com.hitorro.obj.core.solr.queryvisitors.QueryVisitor;
import com.hitorro.obj.core.solr.retrievalmodules.Retriever;
import com.hitorro.util.core.iterator.AbstractIterator;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.json.keys.JsonProperty;
import com.hitorro.util.json.keys.StringProperty;
import com.hitorro.util.json.keys.propaccess.Propaccess;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;

import java.io.IOException;

public class SolrRetriever implements Retriever {
    public static Propaccess naturalK = new Propaccess("natural.mls");
    public static Propaccess queryK = new Propaccess("query");
    public static StringProperty langKey = new StringProperty("lang", "", "en");

    public static StringProperty naturalKey = new StringProperty("search.natural", "", null);

    public static JsonProperty searchKey = new JsonProperty("search", "", null);

    @Override
    public boolean getParticipate(final JVS query, final RetrievalContext context) {
        JsonNode node = searchKey.apply(query);
        if (node == null) {
            return false;
        }
        for (QueryVisitor qv : QueryVisitor.visitors) {
            qv.visit(query, context);
        }
        return true;
    }

    @Override
    public AbstractIterator<JVS> appendRetriever(final AbstractIterator<JVS> iter, final JVS query, final RetrievalContext context) {
        for (QueryVisitor qv : QueryVisitor.visitors) {
            qv.finalize(query, context);
        }

        String collection = context.getCollectionName();
        query.setIfNotNull(CollectionConfig.collectionMetaNameKey, collection);

        JVS qJvs = new JVS();
        qJvs.setType("core_query");
        String lang = langKey.apply(query);
        String naturalQ = naturalKey.apply(query);
        if (!StringUtil.nullOrEmptyString(lang) && !StringUtil.nullOrEmptyString(naturalQ)) {
            qJvs.addLangTextTemporaryReLook(naturalK, naturalQ, lang);
        }
        query.setJVSChild(queryK, qJvs);

        String qString = query.getStringRepresentation();
        context.getSolrQuery().add("jvs", qString);

        try {
            SolrQuery sq = context.getSolrQuery();
            context.qr = getClient().query(collection, sq);
            SolrIterator si = new SolrIterator(context.qr);
            context.addAggregate(si);
            return si;
        } catch (SolrServerException | IOException e) {
        }

        return null;
    }


    private HttpSolrClient getClient() {
        final String solrUrl = "http://localhost:8072/solr";
        return new HttpSolrClient.Builder(solrUrl)
                .withConnectionTimeout(10000)
                .withSocketTimeout(60000)
                .build();
    }
}
