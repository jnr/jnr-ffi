/*
 * Copyright (C) 2007-2010 Wayne Meissner
 *
 * This file is part of the JNR project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jnr.ffi;

import jnr.ffi.annotations.LongLong;
import jnr.ffi.annotations.In;
import jnr.ffi.annotations.Out;

import java.nio.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author wayne
 */
public class BufferTest {

    public BufferTest() {
    }
    public static interface TestLib {
        void fillByteBuffer(@Out ByteBuffer buf, byte value, int size);
        void fillByteBuffer(@Out Buffer buf, byte value, int size);
//        void fillCharBuffer(@Out CharBuffer buf, char value, int size);
        void fillShortBuffer(@Out ShortBuffer buf, short value, int size);
        void fillShortBuffer(@Out Buffer buf, short value, int size);
        void fillIntBuffer(@Out IntBuffer buf, int value, int size);
        void fillIntBuffer(@Out Buffer buf, int value, int size);
        void fillLongBuffer(@Out LongBuffer buf, @LongLong long value, int size);
        void fillLongBuffer(@Out Buffer buf, @LongLong long value, int size);
        void fillFloatBuffer(@Out FloatBuffer buf, float value, int size);
        void fillDoubleBuffer(@Out DoubleBuffer buf, double value, int size);
        void fillByteBuffer(@Out byte[] buf, byte value, int size);
//        void fillCharBuffer(@Out char[] buf, char value, int size);
        void fillShortBuffer(@Out short[] buf, short value, int size);
        void fillIntBuffer(@Out int[] buf, int value, int size);
        void fillLongBuffer(@Out @LongLong long[] buf, @LongLong long value, int size);
        void fillFloatBuffer(@Out float[] buf, float value, int size);
        void fillDoubleBuffer(@Out double[] buf, double value, int size);
        void copyByteBuffer(@Out ByteBuffer dst, @In ByteBuffer src, int size);
        void copyByteBuffer(@Out ByteBuffer dst, @In byte[] src, int size);
        void copyByteBuffer(@Out byte[] dst, @In ByteBuffer src, int size);
        void copyByteBuffer(@Out byte[] dst, @In byte[] src, int size);
        void copyShortBuffer(@Out ShortBuffer dst, @In ShortBuffer src, int size);
        void copyShortBuffer(@Out ShortBuffer dst, @In short[] src, int size);
        void copyShortBuffer(@Out short[] dst, @In ShortBuffer src, int size);
        void copyShortBuffer(@Out short[] dst, @In short[] src, int size);
        void copyIntBuffer(@Out IntBuffer dst, @In IntBuffer src, int size);
        void copyIntBuffer(@Out IntBuffer dst, @In int[] src, int size);
        void copyIntBuffer(@Out int[] dst, @In IntBuffer src, int size);
        void copyIntBuffer(@Out int[] dst, @In int[] src, int size);
    }
    static TestLib lib;
    static Runtime runtime;
    @BeforeClass
    public static void setUpClass() throws Exception {
        lib = TstUtil.loadTestLib(TestLib.class);
        runtime = Runtime.getRuntime(lib);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        lib = null;
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }
    // SMALL sized heap buffers can be done using direct buffers
    private static final int SMALL = 64;
    
    // LARGE sized heap buffers are handled via the native code
    private static final int LARGE = 2048;
   
    public void testByteBufferArgument(int size) {
        ByteBuffer buf  = ByteBuffer.allocate(size).order(runtime.byteOrder());
        final byte MAGIC = (byte)0xED;
        lib.fillByteBuffer(buf, MAGIC, buf.remaining());
        for (int i=0;i < buf.capacity();i++) {
            assertEquals("Bad value at index " + i, MAGIC, buf.get(i));
        }
    }
    @Test
    public void fillSmallByteBufferArgument() {
        testByteBufferArgument(SMALL);
    }
    @Test
    public void fillLargeByteBufferArgument() {
        testByteBufferArgument(LARGE);
    }
    @Test
    public void fillByteBufferWithOffsetArgument() {
        ByteBuffer buf  = ByteBuffer.allocate(SMALL).order(runtime.byteOrder());
        final byte MAGIC = (byte)0xED;
        buf.put((byte)0xDE);
        lib.fillByteBuffer(buf.slice(), MAGIC, SMALL - 1);
        assertEquals("Value at position 0 overwritten", (byte)0xde, buf.get(0));
        for (int i=buf.position();i < buf.capacity();i++) {
            assertEquals("Bad value at index " + i, MAGIC, buf.get(i));
        }
    }
    @Test
    public void fillByteBufferSlice() {
        final int SIZE = SMALL;
        ByteBuffer buf  = ByteBuffer.allocate(SIZE).order(runtime.byteOrder());
        final byte MAGIC = (byte)0xED;
        buf.put(0, (byte)0xDE);
        buf.put(buf.limit() - 1, (byte) 0xDE);
        ByteBuffer dup = buf.duplicate();
        dup.position(1).limit(buf.limit() - 1);
        ByteBuffer slice = dup.slice();
        lib.fillByteBuffer(slice, MAGIC, slice.capacity());
        assertEquals("Value at position 0 overwritten", (byte)0xde, buf.get(0));
        assertEquals("Value at position " + (SIZE - 1) + " overwritten", 
                (byte)0xde, buf.get(SIZE - 1));
        for (int i = 1; i < buf.capacity() - 1; i++) {
            assertEquals("Bad value at index " + i, MAGIC, buf.get(i));
        }
    }

    public void testShortBufferArgument(int size) {        
        ShortBuffer buf = ShortBuffer.allocate(size);
        final short MAGIC = (short)0xABED;
        lib.fillShortBuffer(buf, MAGIC, size);
        for (int i=0;i < buf.capacity();i++) {
            assertEquals("Bad value at index " + i, MAGIC, buf.get(i));
        }
    }
    
    @Test
    public void fillSmallShortBufferArgument() { 
        testShortBufferArgument(SMALL);
    }
    @Test
    public void fillLargeShortBufferArgument() { 
        testShortBufferArgument(LARGE);
    }
    
    @Test
    public void fillShortBufferWithOffsetArgument() {        
        ShortBuffer buf = ShortBuffer.allocate(SMALL);
        final short MAGIC = (short)0xABED;
        buf.put((short)0xDEAD);
        lib.fillShortBuffer(buf.slice(), MAGIC, SMALL - 1);
        assertEquals("Value at position 0 overwritten", (short)0xdead, buf.get(0));
        for (int i=1;i < buf.capacity();i++) {
            assertEquals("Bad value at index " + i, MAGIC, buf.get(i));
        }
    }
    @Test
    public void fillShortBufferSlice() {        
        ShortBuffer buf = ShortBuffer.allocate(SMALL);
        final short FILL = (short) 0x1234;
        final short GUARD = (short) 0xdead;
        buf.put(0, GUARD).put(buf.limit() - 1, GUARD);
        ShortBuffer dup = buf.duplicate();
        dup.position(1).limit(buf.limit() - 1);
        ShortBuffer slice = dup.slice();
        lib.fillShortBuffer(slice, FILL, slice.capacity());
        assertEquals("Value at position 0 overwritten", GUARD, buf.get(0));
        assertEquals("Value at position " + (buf.limit() - 1) + " overwritten", 
                GUARD, buf.get(buf.limit() - 1));
        for (int i = 1; i < buf.limit() - 1; i++) {
            assertEquals("Bad value at index " + i, FILL, buf.get(i));
        }
    }
    public void testIntBufferArgument(int size) {        
        IntBuffer buf = IntBuffer.allocate(size);
        final int MAGIC = 0xABEDCF23;
        lib.fillIntBuffer(buf, MAGIC, size);
        for (int i=0;i < buf.capacity();i++) {
            assertEquals("Bad value at index " + i, MAGIC, buf.get(i));
        }
    }
    @Test
    public void fillSmallIntBufferArgument() {  
        testIntBufferArgument(SMALL);
    }
    @Test
    public void fillLargeIntBufferArgument() {  
        testIntBufferArgument(SMALL);
    }
    @Test
    public void fillIntBufferWithOffsetArgument() {        
        IntBuffer buf = IntBuffer.allocate(SMALL);
        final int MAGIC = 0xABEDCF23;
        buf.put(0xdeadbeef);
        lib.fillIntBuffer(buf.slice(), MAGIC, SMALL - 1);
        assertEquals("Value at position 0 overwritten", 0xdeadbeef, buf.get(0));
        for (int i=1;i < buf.capacity();i++) {
            assertEquals("Bad value at index " + i, MAGIC, buf.get(i));
        }
    }
    @Test
    public void fillIntBufferSlice() {        
        IntBuffer buf = IntBuffer.allocate(SMALL);
        final int FILL = 0x12345678;
        final int GUARD = 0xdeadbeef;
        buf.put(0, GUARD).put(buf.limit() - 1, GUARD);
        IntBuffer dup = buf.duplicate();
        dup.position(1).limit(buf.limit() - 1);
        IntBuffer slice = dup.slice();
        lib.fillIntBuffer(slice, FILL, slice.capacity());
        assertEquals("Value at position 0 overwritten", GUARD, buf.get(0));
        assertEquals("Value at position " + (buf.limit() - 1) + " overwritten", 
                GUARD, buf.get(buf.limit() - 1));
        for (int i = 1; i < buf.limit() - 1; i++) {
            assertEquals("Bad value at index " + i, FILL, buf.get(i));
        }
    }
    @Test
    public void fillLongBufferArgument() {        
        LongBuffer buf = LongBuffer.allocate(SMALL);
        final long MAGIC = 0x1234567887654321L;
        lib.fillLongBuffer(buf, MAGIC, SMALL);
        for (int i=0;i < buf.capacity();i++) {
            assertEquals("Bad value at index " + i, MAGIC, buf.get(i));
        }
    }
    @Test
    public void fillLongBufferWithOffsetArgument() {        
        LongBuffer buf = LongBuffer.allocate(SMALL);
        final long MAGIC = 0x1234567887654321L;
        buf.put(0xdeadbeefL);
        lib.fillLongBuffer(buf.slice(), MAGIC, SMALL - 1);
        assertEquals("Value at position 0 overwritten", 0xdeadbeefL, buf.get(0));
        for (int i=1;i < buf.capacity();i++) {
            assertEquals("Bad value at index " + i, MAGIC, buf.get(i));
        }
    }
    @Test
    public void fillLongBufferSlice() {
        LongBuffer buf = LongBuffer.allocate(SMALL);
        final long GUARD = 0xdeadbeefL;
        final long FILL = 0x1234567887654321L;
        buf.put(0, GUARD).put(buf.limit() - 1, GUARD);
        LongBuffer dup = buf.duplicate();
        dup.position(1).limit(buf.limit() - 1);
        LongBuffer slice = dup.slice();
        lib.fillLongBuffer(dup.slice(), FILL, slice.capacity());
        assertEquals("Value at position 0 overwritten", GUARD, buf.get(0));
        assertEquals("Value at position " + (buf.limit() - 1) + " overwritten", 
                GUARD, buf.get(buf.limit() - 1));
        for (int i = 1; i < buf.limit() - 1; i++) {
            assertEquals("Bad value at index " + i, FILL, buf.get(i));
        }
    }
    @Test
    public void fillDirectByteBufferArgument() {
        ByteBuffer buf  = ByteBuffer.allocateDirect(SMALL).order(runtime.byteOrder());
        final byte MAGIC = (byte)0xED;
        lib.fillByteBuffer(buf, MAGIC, SMALL);
        for (int i=0;i < buf.capacity();i++) {
            assertEquals("Bad value at index " + i, MAGIC, buf.get(i));
        }
    }
    @Test
    public void fillDirectShortBufferArgument() {
        ByteBuffer buf  = ByteBuffer.allocateDirect(SMALL*2).order(runtime.byteOrder());
        ShortBuffer shortBuf = buf.asShortBuffer();
        final short MAGIC = (short)0xABED;
        lib.fillShortBuffer(shortBuf, MAGIC, SMALL);
        for (int i=0;i < shortBuf.capacity();i++) {
            assertEquals("Bad value at index " + i, MAGIC, shortBuf.get(i));
        }
    }

    @Test
    public void fillDirectShortBufferSlice() {
        ByteBuffer buf  = ByteBuffer.allocateDirect(SMALL*2).order(runtime.byteOrder());
        ShortBuffer shortBuf = buf.asShortBuffer();
        buf.position(2);
        ShortBuffer sliced = buf.slice().asShortBuffer();
        final short MAGIC = (short)0xABED;
        lib.fillShortBuffer(sliced, MAGIC, SMALL - 1);
        assertEquals("Bad value at index " + 0, 0, shortBuf.get(0));
        for (int i=1; i < shortBuf.capacity() - 1; i++) {
            assertEquals("Bad value at index " + i, MAGIC, shortBuf.get(i));
        }
    }

    @Test
    public void fillDirectShortBufferSliceAsBuffer() {
        ByteBuffer buf  = ByteBuffer.allocateDirect(SMALL*2).order(runtime.byteOrder());
        ShortBuffer shortBuf = buf.asShortBuffer();
        buf.position(2);
        Buffer sliced = buf.slice().asShortBuffer();
        final short MAGIC = (short)0xABED;
        lib.fillShortBuffer(sliced, MAGIC, SMALL - 1);
        assertEquals("Bad value at index " + 0, 0, shortBuf.get(0));
        for (int i=1; i < shortBuf.capacity() - 1; i++) {
            assertEquals("Bad value at index " + i, MAGIC, shortBuf.get(i));
        }
    }

    @Test
    public void fillDirectIntBufferArgument() {
        ByteBuffer buf  = ByteBuffer.allocateDirect(SMALL*4).order(runtime.byteOrder());
        IntBuffer intBuf = buf.asIntBuffer();
        final int MAGIC = 0xABEDCF23;
        lib.fillIntBuffer(intBuf, MAGIC, SMALL);
        for (int i=0;i < intBuf.capacity();i++) {
            assertEquals("Bad value at index " + i, MAGIC, intBuf.get(i));
        }
    }
    @Test
    public void fillDirectLongBufferArgument() {
        ByteBuffer buf  = ByteBuffer.allocateDirect(SMALL*8).order(runtime.byteOrder());
        LongBuffer longBuf = buf.asLongBuffer();
        final long MAGIC = 0x1234567887654321L;
        lib.fillLongBuffer(longBuf, MAGIC, SMALL);
        for (int i=0;i < longBuf.capacity();i++) {
            assertEquals("Bad value at index " + i, MAGIC, longBuf.get(i));
        }
    }
    @Test
    public void copySmallDirectByteBufferToArray() {
        testCopyByteBufferToArray(ByteBuffer.allocateDirect(SMALL));
    }
    @Test
    public void copyLargeDirectByteBufferToArray() {
        testCopyByteBufferToArray(ByteBuffer.allocateDirect(LARGE));
    }
    @Test
    public void copySmallHeapByteBufferToArray() {
        testCopyByteBufferToArray(ByteBuffer.allocate(SMALL));
    }
    @Test
    public void copyLargeHeapByteBufferToArray() {
        testCopyByteBufferToArray(ByteBuffer.allocate(LARGE));
    }
    public void testCopyByteBufferToArray(ByteBuffer src) {
        final int size = src.capacity();
        for (int i = 0; i < size; ++i) {
            src.put(i, (byte) i);
        }
        byte[] dst = new byte[size];
        lib.copyByteBuffer(dst, src, size);
        for (int i = 0; i < size; ++i) {
            assertEquals("Bad value at index " + i, (byte) i, dst[i]);
        }
    }
    // FIXME: re-enable when read-only buffers are supported
//    @Test
    public void copyReadOnlyHeapByteBufferToArray() {
        final int size = SMALL;
        ByteBuffer src = ByteBuffer.allocate(size);
        for (int i = 0; i < size; ++i) {
            src.put(i, (byte) i);
        }
        byte[] dst = new byte[size];
        lib.copyByteBuffer(dst, src.asReadOnlyBuffer(), size);
        for (int i = 0; i < size; ++i) {
            assertEquals("Bad value at index " + i, (byte) i, dst[i]);
        }
    }
//    @Test
//    public void pinnedByteBuffer() {
//        ByteBuffer buf = ByteBuffer.allocate(LARGE);
//        Function fillByteBuffer = NativeLibrary.getInstance("jffitest").getFunction("fillByteBuffer");
//        NativeInvoker nativeInvoker = FFINativeInvoker.getInvoker(fillByteBuffer, NativeInvoker.VOID);
//        CallBuilder cb = CallBuilder.create(nativeInvoker);
//        final byte MAGIC = (byte)0xED;
//        cb.addPinned(buf).addInt8(MAGIC).addInt32(buf.remaining()).invokeVoid();
//        for (int i=0;i < buf.capacity();i++) {
//            assertEquals("Bad value at index " + i, MAGIC, buf.get(i));
//        }
//    }
}