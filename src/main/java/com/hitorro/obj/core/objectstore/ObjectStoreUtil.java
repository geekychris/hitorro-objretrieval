package com.hitorro.obj.core.objectstore;

import com.hitorro.jsontypesystem.JVS;
import com.hitorro.util.core.Env;
import com.hitorro.util.core.Log;
import com.hitorro.util.io.compression.Compressor;
import com.hitorro.util.io.compression.CompressorCache;
import com.hitorro.util.json.Json2ByteArrayMapper;
import jetbrains.exodus.ArrayByteIterable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.DataInputStream;
import java.io.IOException;

public class ObjectStoreUtil {
    @NotNull
    public static ArrayByteIterable getArrayByteIterableFromJVS(final JVS jvs, int compressorVersion) throws IOException {
        byte[] buff = Json2ByteArrayMapper.threadedMapper.get().apply(jvs.getJsonNode());

        Compressor comp = CompressorCache.get(compressorVersion);
        int bytesSize = CompressorCache.get(compressorVersion).compressBytes(buff, buff.length);
        buff = comp.getCompressionBuffer();
        byte[] ret = new byte[bytesSize];
        // must copy buffer else trash happens in write
        System.arraycopy(buff, 0, ret, 0, bytesSize);
        return byteArrayToEntry(ret, bytesSize);
    }

    public static ArrayByteIterable byteArrayToEntry(@NotNull final byte[] buff, int length) {
        return new ArrayByteIterable(buff, length);
    }

    @Nullable
    public static JVS getJvsFromDataInputStream(DataInputStream dis, ResizeByteArray rba, long size) throws IOException {
        if (rba.arr.length < size) {
            int l = (int) Env.getNextPower2Value(rba.arr.length, size);
            rba.arr = new byte[l];
        }
        int read = 0;

        while (read < size) {
            read += dis.read(rba.arr, read, (int) (size - read));
        }
        if (read < size) {
            Log.util.error("read less than expectd 124");
        }
        Compressor dict = CompressorCache.get(0);
        rba.arr = dict.decompressBytes(rba.arr);
        String s = new String(rba.arr);
        return JVS.read(s);
    }
}
