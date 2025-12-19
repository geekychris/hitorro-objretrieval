package com.hitorro.obj.core.solr.queryvisitors;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.util.json.JsonInitable;
import com.hitorro.util.json.keys.StringProperty;

public class SortRow implements JsonInitable {
    public static final StringProperty sortKey = new StringProperty("field", "", null);
    public static final StringProperty sortDirKey = new StringProperty("dir", "", "desc");
    protected String field;
    protected String dir;

    public SortRow() {

    }

    public SortRow(String field, String direction) {
        this.field = field;
        this.dir = direction;
    }

    @Override
    public boolean init(JsonNode node) {
        field = sortKey.apply(node);
        dir = sortDirKey.apply(node);
        return false;
    }

    public boolean isAscending() {
        return "asc".equals(dir);
    }
}