package com.hitorro.obj.core.objectstore;

import com.hitorro.jsontypesystem.JVS;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.http.HTTPIterableClient;
import com.hitorro.util.core.iterator.AbstractIterator;
import com.hitorro.util.core.iterator.mappers.BaseMapper;
import com.hitorro.util.core.iterator.sinks.BaseSink;
import com.hitorro.util.core.iterator.sinks.JsonSink;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class JVSObjectStoreClient extends HTTPIterableClient<JVS, JVS> {
    public static BaseMapper<InputStream, AbstractIterator<JVS>> is2jvs = new BaseMapper<InputStream, AbstractIterator<JVS>>() {

        @Override
        public AbstractIterator<JVS> apply(final InputStream is) {
            DataInputStream dis = new DataInputStream(is);
            return new InputStream2CompressedJVS(dis);
        }
    };

    public static BaseMapper<OutputStream, BaseSink<JVS>> os2JVSSink = new BaseMapper<OutputStream, BaseSink<JVS>>() {
        @Override
        public BaseSink<JVS> apply(OutputStream o) {
            JsonSink jsonSink = new JsonSink(o);
            return new BaseSink<JVS>() {
                @Override
                public boolean init(com.fasterxml.jackson.databind.JsonNode node) {
                    return jsonSink.init(node);
                }

                @Override
                public boolean start() {
                    return jsonSink.start();
                }

                @Override
                public boolean add(JVS jvs) {
                    return jsonSink.add(jvs.getJsonNode());
                }

                @Override
                public boolean stop() {
                    return jsonSink.stop();
                }
            };
        }
    };

    public JVSObjectStoreClient(String name, String password) {
        super(name, password, is2jvs, os2JVSSink);
    }

    public JVSObjectStoreClient() {
        super(null, null, is2jvs, os2JVSSink);
    }
}

class InputStream2CompressedJVS extends AbstractIterator<JVS> {
    private final DataInputStream dis;
    private final ResizeByteArray rba = new ResizeByteArray();
    private JVS curr;

    public InputStream2CompressedJVS(DataInputStream dis) {
        this.dis = dis;
        try {
            curr = getAux();
        } catch (IOException e) {
            Log.util.error("Unable to read JVS");
        }
    }

    @Override
    public boolean hasNext() {
        return curr != null;
    }

    @Override
    public JVS next() {
        JVS ret = curr;
        try {
            curr = getAux();
        } catch (IOException e) {
            Log.util.error("Unable to read JVS %s %e", e, e);
            //curr = null;
        }

        return ret;
    }

    private JVS getAux() throws IOException {
        long size = dis.readLong();
        if (size == 0) {
            return null;
        }
        return ObjectStoreUtil.getJvsFromDataInputStream(dis, rba, size);
    }
}