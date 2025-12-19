package com.hitorro.obj.core.solr;

import com.hitorro.obj.core.SolrService;

import java.util.HashSet;
import java.util.Set;

public class SolrInfo {
    private Set<String> names = new HashSet();

    public SolrInfo(SolrService adm) throws Exception {
        names = new HashSet(adm.getCoreNames());
    }

    public boolean hasCore(String name) {
        return names.contains(name);
    }
}

