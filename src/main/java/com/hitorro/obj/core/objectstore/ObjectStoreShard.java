package com.hitorro.obj.core.objectstore;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.jsontypesystem.JVS;
import com.hitorro.util.core.GenericKeyValue;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.iterator.AbstractIterator;
import com.hitorro.util.core.iterator.Mapper;
import com.hitorro.util.core.iterator.sinks.Sink;
import com.hitorro.util.io.FileUtil;
import com.hitorro.util.io.StoreException;
import com.hitorro.util.io.compression.DictCompressor;
import com.hitorro.util.io.largedata.compressedstreams.COutputStream;
import com.hitorro.util.json.keys.propaccess.PropaccessError;
import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.env.*;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

import static jetbrains.exodus.bindings.StringBinding.stringToEntry;
import static jetbrains.exodus.env.StoreConfig.WITHOUT_DUPLICATES;

/**
 * XXX Should replace this with RocksDB!
 */
public class ObjectStoreShard implements Sink<JVS> {
    private final File dir;
    private final Environment env;
    private final Store store;
    int compressorVersion = 1;
    private Transaction writeTransaction;

    public ObjectStoreShard(File dir) {
        this.dir = dir;
        FileUtil.ensureDirectoryExists(dir);
        env = Environments.newInstance(dir.getAbsolutePath());

        store = env.computeInTransaction(new TransactionalComputable<Store>() {
            @Override
            public Store compute(@NotNull final Transaction txn) {
                return env.openStore("MyStore", WITHOUT_DUPLICATES, txn);
            }
        });
    }

    @NotNull
    public static JVS getJvsFromBytes(final byte[] buff, int length) throws IOException {
        DictCompressor dict = new DictCompressor();
        String s = new String(dict.decompressBytes(buff, length));
        return JVS.read(s);
    }

    public Sink<GenericKeyValue<ByteIterable, ByteIterable>> getCompressedSink() {
        return new CompressedSink(this);
    }

    public Mapper<JVS, GenericKeyValue<ByteIterable, ByteIterable>> getCompressionMapper() {
        return new CompressionMapper(this);
    }

    public void write(JVS jvs) throws PropaccessError {
        Transaction trans = env.beginExclusiveTransaction();
        writeInTransaction(jvs, trans);
        trans.commit();
    }

    private void writeInTransaction(final JVS jvs, final Transaction trans) throws PropaccessError {
        GenericKeyValue<ByteIterable, ByteIterable> gkv = getBytes(jvs);
        writeInTransaction(trans, gkv);
    }

    public GenericKeyValue<ByteIterable, ByteIterable> getBytes(JVS jvs) {
        try {
            String id = jvs.getId();
            GenericKeyValue<ByteIterable, ByteIterable> gkv = new GenericKeyValue(stringToEntry(id),
                    ObjectStoreUtil.getArrayByteIterableFromJVS(jvs, compressorVersion));
            return gkv;
        } catch (IOException e) {
            return null;
        }
    }

    public void writeInTransaction(final Transaction trans, GenericKeyValue<ByteIterable, ByteIterable> gkv) {
        store.put(trans, gkv.getKey(), gkv.getValue());
    }

    public JVS get(String keyString) throws IOException {
        Transaction trans = env.beginReadonlyTransaction();
        try {
            ByteIterable key = stringToEntry(keyString);
            ByteIterable bi = store.get(trans, key);
            return getJVSFromByteIterable(bi);
        } finally {
            trans.abort();
        }
    }

    @NotNull
    private JVS getJVSFromByteIterable(final ByteIterable bi) throws IOException {
        int l = bi.getLength();
        byte[] buff = new byte[l];
        System.arraycopy(bi.getBytesUnsafe(), 0, buff, 0, l);
        return getJvsFromBytes(buff, l);
    }

    /**
     * given an iterator of JVS objects that are shells containing only ID's fetch the
     * bytes from backing storage and write them out to the provided compressed output stream.
     * the COS allows the length of the buffer to be encoded using variable length encoding to save a few bytes
     * in transport.
     *
     * @param keysIter
     * @param cos
     */
    public void getValuesAsByteStream(AbstractIterator<JVS> keysIter, COutputStream cos) {
        Transaction trans = env.beginReadonlyTransaction();
        try {
            while (keysIter.hasNext()) {
                JVS keyJvs = keysIter.next();
                String keyString = keyJvs.getId();
                ByteIterable key = stringToEntry(keyString);
                ByteIterable bi = store.get(trans, key);
                if (bi != null) {
                    cos.writeLong(bi.getLength());
                    cos.writeBytes(bi.getBytesUnsafe(), bi.getLength());
                }
            }
            cos.writeLong(0);
        } catch (PropaccessError e) {
            Log.util.error("Unable to get values from backing store %s %e", e, e);
        } catch (IOException e) {
            Log.util.error("Unable to get values from backing store %s %e", e, e);
        } finally {
            trans.abort();
        }
    }

    @Override
    public boolean init(final JsonNode node) {
        return false;
    }

    @Override
    public boolean start() {
        writeTransaction = env.beginTransaction();
        return true;
    }

    @Override
    public boolean add(final JVS o) {
        if (writeTransaction == null) {
            return false;
        }
        try {
            writeInTransaction(o, writeTransaction);
        } catch (PropaccessError propaccessError) {
            return false;
        }
        return true;
    }

    public boolean addGKV(final GenericKeyValue<ByteIterable, ByteIterable> gkv) {
        if (writeTransaction == null) {
            return false;
        }
        try {
            writeInTransaction(writeTransaction, gkv);
        } catch (PropaccessError propaccessError) {
            return false;
        }
        return true;
    }

    @Override
    public boolean stop() {
        if (writeTransaction != null) {
            writeTransaction.commit();
            writeTransaction = null;
            return true;
        }
        return false;
    }

    @Override
    public void close() throws IOException {
        stop();
    }
}

class CompressedSink implements Sink<GenericKeyValue<ByteIterable, ByteIterable>> {
    ObjectStoreShard oss;

    public CompressedSink(ObjectStoreShard oss) {
        this.oss = oss;
    }

    @Override
    public boolean init(final JsonNode node) {
        return oss.init(node);
    }

    @Override
    public boolean start() throws IOException {
        return oss.start();
    }

    @Override
    public boolean add(final GenericKeyValue<ByteIterable, ByteIterable> gkv) throws IOException, StoreException {
        return oss.addGKV(gkv);
    }

    @Override
    public boolean stop() {
        return oss.stop();
    }

    @Override
    public void close() throws IOException {
        oss.close();
    }
}

class CompressionMapper implements Mapper<JVS, GenericKeyValue<ByteIterable, ByteIterable>> {
    private final ObjectStoreShard oss;

    CompressionMapper(ObjectStoreShard oss) {
        this.oss = oss;
    }

    @Override
    public GenericKeyValue<ByteIterable, ByteIterable> apply(final JVS jvs) {
        return oss.getBytes(jvs);
    }
}