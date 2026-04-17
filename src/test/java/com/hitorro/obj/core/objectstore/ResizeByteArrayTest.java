package com.hitorro.obj.core.objectstore;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for ResizeByteArray.
 */
public class ResizeByteArrayTest {

    @Test
    public void testDefaultArraySize() {
        ResizeByteArray rba = new ResizeByteArray();
        assertNotNull(rba.arr);
        assertEquals(1024, rba.arr.length);
    }

    @Test
    public void testDefaultArrayIsZeroFilled() {
        ResizeByteArray rba = new ResizeByteArray();
        for (int i = 0; i < rba.arr.length; i++) {
            assertEquals("Byte at index " + i + " should be 0", 0, rba.arr[i]);
        }
    }

    @Test
    public void testArrayIsWritable() {
        ResizeByteArray rba = new ResizeByteArray();
        rba.arr[0] = (byte) 0xFF;
        rba.arr[1023] = (byte) 0x42;

        assertEquals((byte) 0xFF, rba.arr[0]);
        assertEquals((byte) 0x42, rba.arr[1023]);
    }

    @Test
    public void testArrayCanBeReplaced() {
        ResizeByteArray rba = new ResizeByteArray();
        byte[] larger = new byte[4096];
        larger[0] = 1;

        rba.arr = larger;

        assertEquals(4096, rba.arr.length);
        assertEquals(1, rba.arr[0]);
    }

    @Test
    public void testMultipleInstances() {
        ResizeByteArray rba1 = new ResizeByteArray();
        ResizeByteArray rba2 = new ResizeByteArray();

        rba1.arr[0] = 1;
        rba2.arr[0] = 2;

        assertNotSame(rba1.arr, rba2.arr);
        assertEquals(1, rba1.arr[0]);
        assertEquals(2, rba2.arr[0]);
    }
}
