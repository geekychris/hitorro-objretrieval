package com.hitorro.obj.core.solr;

import com.hitorro.jsontypesystem.JVS;
import com.hitorro.obj.core.TypeFieldMapper;
import com.hitorro.obj.core.solr.collection.CollectionConfig;
import com.hitorro.obj.core.solr.queryvisitors.SelectRow;
import com.hitorro.obj.core.solr.retrievalmodules.RetrievalAggregate;
import com.hitorro.util.core.Console;
import com.hitorro.util.core.iterator.sinks.Sink;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.json.keys.propaccess.Propaccess;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;

import java.util.ArrayList;
import java.util.List;

public class RetrievalContext extends RetrievalContextBase {
    private final SolrQuery query;
    private final List<RetrievalAggregate> aggregates = new ArrayList();
    public int results;
    public QueryResponse qr;
    private String collectionName;

    public RetrievalContext(CollectionConfig cc, String lang, SolrQuery query) {
        super(cc, lang);
        this.query = query;
    }

    public void addAggregate(RetrievalAggregate agg) {
        aggregates.add(agg);
    }

    public void addAggregates(Sink<JVS> sink) {
        for (RetrievalAggregate agg : aggregates) {
            JVS j = agg.getAggregate(this);
            if (j != null) {
                sink.accept(j);
            }
        }
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String name) {
        this.collectionName = name;
    }

    public SolrQuery getSolrQuery() {
        return query;
    }

    public void getSortFields(String solrArg, List<SelectRow> aliasList) {
        StringBuilder sb = new StringBuilder();

        for (SelectRow row : aliasList) {
            Console.appendIfNotEmpty(sb, ",");
            String s = row.getField();
            s = getTranslatedField(s);
            if (StringUtil.nullOrEmptyString(row.getAlias())) {
                Console.bprint(sb, "%s", s);
            } else {
                Console.bprint(sb, "%s:%s", row.getAlias(), s);
            }
        }
        query.set(solrArg, sb.toString());
    }

    public TypeFieldMapper.FieldType getFieldType(Propaccess access) {
        return getMapper().getFieldType(access, type);
    }


}
