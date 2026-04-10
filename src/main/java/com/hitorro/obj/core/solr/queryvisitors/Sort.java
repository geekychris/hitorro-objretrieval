package com.hitorro.obj.core.solr.queryvisitors;

import com.hitorro.jsontypesystem.JVS;
import com.hitorro.obj.core.TypeFieldMapper;
import com.hitorro.obj.core.solr.RetrievalContext;
import com.hitorro.util.core.Console;
import com.hitorro.util.core.classes.JsonNode2JsonInitable;
import com.hitorro.util.core.error.ErrorCode;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.json.keys.ArrayProperty;
import com.hitorro.util.json.keys.propaccess.Propaccess;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class Sort implements QueryVisitor {
    private static final AtomicLong sortFieldCounter = new AtomicLong();
    public static JsonNode2JsonInitable<SortRow> sortRowKey = new JsonNode2JsonInitable(SortRow.class, "class", SortRow.class);
    public static ArrayProperty<SortRow> sortRowsKey =
            new ArrayProperty<SortRow>("search.sort", "", new ArrayList(), sortRowKey);

    @Override
    public void visit(final JVS jvsQuery, final RetrievalContext retrievalContext) {

    }

    @Override
    public void finalize(final JVS jvsQuery, final RetrievalContext rc) {
        StringBuilder sb = finalizeAux(jvsQuery, rc);
        if (sb.length() == 0) {
            return;
        }
        rc.getSolrQuery().set("sort", sb.toString());
    }

    @NotNull
    protected StringBuilder finalizeAux(final JVS jvsQuery, final RetrievalContext rc) {
        List<SortRow> rows = sortRowsKey.apply(jvsQuery.getJsonNode());
        StringBuilder sb = new StringBuilder();
        for (SortRow sr : rows) {
            addSortField(rc, sb, sr);
        }
        return sb;
    }

    protected void addSortField(RetrievalContext rc, StringBuilder sb, SortRow sr) {
        Console.appendIfNotEmpty(sb, ",");
        Propaccess access = new Propaccess(sr.field);
        TypeFieldMapper.FieldType ft = rc.getFieldType(access);
        String s;
        if (ft == TypeFieldMapper.FieldType.FeatureField) {
            String decorated = rc.getPathForFeature(sr.field);
            s = Fmt.S("field(%s)", decorated);
        } else {
            s = rc.getTranslatedField(sr.field);
        }

        Console.bprint(sb, "%s %s", s, sr.dir);

        if (StringUtil.nullOrEmptyString(s)) {
            rc.getErrors().add(new ErrorCode(400, "field not identified %s", sr.field));
            return;
        }
        addReturnFieldFromSort(rc, s, sr.dir);
    }

    public void addReturnFieldFromSort(RetrievalContext rc, String field, String dir) {
        long sortFieldC = sortFieldCounter.incrementAndGet();
        boolean isFunc = field.contains("(");
        char direction = Character.toLowerCase(dir.charAt(0));
        String name;
        if (isFunc) {
            name = Fmt.S("sort_%s_%s", sortFieldC, direction);
        } else {
            name = Fmt.S("sort_%s_%s", field, direction);
        }
        rc.addAlias(name, field);
    }
}
