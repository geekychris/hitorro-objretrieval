package com.hitorro.obj.core.solr.retrievalmodules.fixup;

import com.hitorro.jsontypesystem.JVS;
import com.hitorro.util.core.iterator.AbstractIterator;

import java.util.function.Function;

public class FixupIterator extends AbstractIterator<JVS> {
    private final AbstractIterator<JVS> iterIn;
    private Function<JVS, JVS> mapper = null;

    FixupIterator(AbstractIterator<JVS> iterIn, Function<JVS, JVS> mapper) {
        this.iterIn = iterIn;
        this.mapper = mapper;
    }


    @Override
    public boolean hasNext() {
        return iterIn.hasNext();
    }

    @Override
    public JVS next() {
        JVS j = iterIn.next();
        JVS doc = j.getDoc();
        fix(doc);
        return j;
    }

    private void fix(JVS doc) {
        if (doc != null) {
            mapper.apply(doc);
        }
    }
}
