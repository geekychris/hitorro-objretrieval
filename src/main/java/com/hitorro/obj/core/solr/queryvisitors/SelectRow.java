package com.hitorro.obj.core.solr.queryvisitors;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.util.core.ArrayUtil;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.json.JsonInitable;
import com.hitorro.util.json.keys.StringProperty;

import java.util.concurrent.atomic.AtomicInteger;

public class SelectRow implements JsonInitable {
    private static final AtomicInteger intCounter = new AtomicInteger();
    public static StringProperty FieldKey = new StringProperty("field", "", null);
    public static StringProperty AliasKey = new StringProperty("alias", "alias", null);
    protected String field;
    protected String alias;

    public SelectRow(String field, String alias) {
        this.field = field;
        this.alias = alias;
    }

    public SelectRow() {

    }

    @Override
    public boolean init(JsonNode node) {
        addSelectAux(node);
        return true;
    }

    public String getAlias() {
        return alias;
    }

    public String getField() {
        return field;
    }

    private void addSelectAux(JsonNode node) {
        if (node.isObject()) {
            field = FieldKey.apply(node);
            alias = AliasKey.apply(node);
            if (StringUtil.nullOrEmptyString(field)) {
            }
        } else if (node.isTextual()) {
            String txt = node.textValue();
            if (txt.contains("as")) {

                // dumb ass check for something that looks like a function
                String[] parts = StringUtil.splitByFirstToken(txt, " as ");
                if (ArrayUtil.notNullAndHasNElements(parts, 2)) {
                    alias = parts[1];
                    field = parts[0];
                    return;
                }
                alias = Fmt.S("field%s", intCounter.incrementAndGet());
                field = txt;
            }
        }
    }
}
