package com.hitorro.obj.core.objectstore;

import com.hitorro.jsontypesystem.JVS;
import com.hitorro.util.core.iterator.mappers.BaseMapper;
import jetbrains.exodus.ArrayByteIterable;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class TestCompressorMapper extends BaseMapper<JVS, JVS> {
    private final ResizeByteArray rba = new ResizeByteArray();

    @Override
    public JVS apply(final JVS jvs) {
        try {
            ArrayByteIterable abi = ObjectStoreUtil.getArrayByteIterableFromJVS(jvs, 1);
            ByteArrayInputStream bais = new ByteArrayInputStream(abi.getBytesUnsafe(), 0, abi.getLength());
            DataInputStream dis = new DataInputStream(bais);
            JVS resp = ObjectStoreUtil.getJvsFromDataInputStream(dis, rba, abi.getLength());

        } catch (IOException e) {
            return null;
        }
        return null;
    }
}