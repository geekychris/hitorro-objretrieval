package com.hitorro.obj.core.solr.collection.modulators;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.jsontypesystem.JVS;
import com.hitorro.obj.core.SolrQueryContext;
import com.hitorro.obj.core.solr.QueryModulatorInterface;
import com.hitorro.obj.core.solr.retrievalmodules.solr.SolrRetriever;
import com.hitorro.util.core.map.MapUtil;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.json.keys.IntegerProperty;
import com.hitorro.util.json.keys.StringProperty;
import org.apache.solr.handler.component.ResponseBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

interface PosVisitor {
    IntegerProperty startKey = new IntegerProperty("ss", "", 0);
    IntegerProperty endKey = new IntegerProperty("se", "", 0);

    void visit(JsonNode root, Set<String> pos, String type);
}

public class AnswerTypeModulator implements QueryModulatorInterface {

    public static StringProperty typeKey = new StringProperty("type", "", "");

    public static void visit(JsonNode root, Set<String> pos, PosVisitor visitor) {
        if (root.isArray()) {
            // multiple sentences case
            for (JsonNode child : root) {
                visit(child, pos, visitor);
            }
        } else {
            String type = typeKey.apply(root).toLowerCase();
            if (pos.contains(type)) {
                visitor.visit(root, pos, type);
            }
            JsonNode c = root.get("c");
            if (c != null && c.isArray()) {
                for (JsonNode child : c) {
                    visit(child, pos, visitor);
                }
            }
        }
    }

    @Override
    public void modulate(final ResponseBuilder rb, final SolrQueryContext sqc) {
        JVS query = sqc.getQuery();
        JVS rawQuery = query.getJVSChild(SolrRetriever.queryK);

        String lang = query.getString(SolrRetriever.langKey);
        if (StringUtil.nullOrEmptyString(lang)) {
            return;
        }
        String clean = rawQuery.getString("natural.mls[%s].clean", lang);
        JsonNode answer = rawQuery.get("natural.mls[%s].segmented_answers", lang);
        JsonNode parsed = rawQuery.get("natural.mls[%s].segmented_parsed", lang);

        SpanVisitor sv = new SpanVisitor(clean);
        Set<String> pos = MapUtil.makeHashSet("vb", "np", "n", "advp");
        visit(parsed, pos, sv);
        int i = 1;
    }

    @Override
    public boolean init(final JsonNode node) {
        return true;
    }
}

class SpanVisitor implements PosVisitor {
    private final String text;
    private final Map<String, List<String>> matches = new HashMap();

    public SpanVisitor(String text) {
        this.text = text;
    }

    public void visit(JsonNode root, Set<String> pos, String type) {
        int start = startKey.apply(root);

        int end = endKey.apply(root);
        if (start < end) {
            String v = text.substring(start, end);
            MapUtil.addMapListValue(matches, type, v, true);
        }
    }
}

