/*
 * Copyright (C) 2008-2010 Wayne Meissner
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

package jnr.ffi.provider.jffi;

import com.kenai.jffi.*;
import jnr.ffi.Address;
import jnr.ffi.Pointer;
import jnr.ffi.mapper.ToNativeContext;
import jnr.ffi.mapper.ToNativeConverter;
import jnr.ffi.provider.*;

import java.nio.*;
import java.nio.charset.Charset;

/**
 * Utility methods that are used at runtime by generated code.
 */
public final class AsmRuntime {
    public static final com.kenai.jffi.MemoryIO IO = com.kenai.jffi.MemoryIO.getInstance();

    private AsmRuntime() {}

    public static void marshal(HeapInvocationBuffer buffer, byte[] array, int flags) {
        if (array == null) {
            buffer.putAddress(0L);
        } else {
            buffer.putArray(array, 0, array.length, flags);
        }
    }

    public static void marshal(HeapInvocationBuffer buffer, short[] array, int flags) {
        if (array == null) {
            buffer.putAddress(0L);
        } else {
            buffer.putArray(array, 0, array.length, flags);
        }
    }

    public static void marshal(HeapInvocationBuffer buffer, int[] array, int flags) {
        if (array == null) {
            buffer.putAddress(0L);
        } else {
            buffer.putArray(array, 0, array.length, flags);
        }
    }

    public static void marshal(HeapInvocationBuffer buffer, long[] array, int nativeArrayFlags) {
        if (array == null) {
            buffer.putAddress(0L);
        } else {
            buffer.putArray(array, 0, array.length, nativeArrayFlags);
        }
    }
    
    public static void marshal32(HeapInvocationBuffer buffer, InvocationSession session,
            final long[] array, int nativeArrayFlags) {
        
        if (array == null) {
            buffer.putAddress(0L);
        } else {
            // Need to convert to int[], copy+convert, then reload after the call
            final int[] nativeArray = new int[array.length];
            if (com.kenai.jffi.ArrayFlags.isIn(nativeArrayFlags)) {
                for (int i = 0; i < array.length; ++i) {
                    nativeArray[i] = (int) array[i];
                }
            }

            buffer.putArray(nativeArray, 0, nativeArray.length, nativeArrayFlags);
            
            if (com.kenai.jffi.ArrayFlags.isOut(nativeArrayFlags)) {
                session.addPostInvoke(new InvocationSession.PostInvoke() {
                    public void postInvoke() {
                        for (int i = 0; i < nativeArray.length; ++i) {
                            array[i] = nativeArray[i];
                        }
                    }
                });
            }
            
        }
    }

    public static void marshal(HeapInvocationBuffer buffer, float[] array, int flags) {
        if (array == null) {
            buffer.putAddress(0L);
        } else {
            buffer.putArray(array, 0, array.length, flags);
        }
    }

    public static void marshal(HeapInvocationBuffer buffer, double[] array, int flags) {
        if (array == null) {
            buffer.putAddress(0L);
        } else {
            buffer.putArray(array, 0, array.length, flags);
        }
    }

    public static void marshal(HeapInvocationBuffer buffer, Pointer ptr, int nativeArrayFlags) {
        if (ptr == null) {
            buffer.putAddress(0L);
        
        } else if (ptr.isDirect()) {
            buffer.putAddress(ptr.address());

        } else if (ptr instanceof AbstractArrayMemoryIO) {
            AbstractArrayMemoryIO aio = (AbstractArrayMemoryIO) ptr;
            buffer.putArray(aio.array(), aio.offset(), aio.length(), nativeArrayFlags);
        
        } else if (ptr instanceof AbstractBufferMemoryIO) {
            AbstractBufferMemoryIO bio = (AbstractBufferMemoryIO) ptr;
            marshal(buffer, bio.getByteBuffer(), nativeArrayFlags);
            
        } else {
            throw new IllegalArgumentException("unsupported argument type " + ptr.getClass());
        }
    }

    public static void marshal(HeapInvocationBuffer buffer, Buffer buf, int flags) {
        if (buf == null) {
            buffer.putAddress(0L);

        } else if (buf instanceof ByteBuffer) {
            marshal(buffer, (ByteBuffer) buf, flags);

        } else if (buf instanceof ShortBuffer) {
            marshal(buffer, (ShortBuffer) buf, flags);

        } else if (buf instanceof IntBuffer) {
            marshal(buffer, (IntBuffer) buf, flags);

        } else if (buf instanceof LongBuffer) {
            marshal(buffer, (LongBuffer) buf, flags);

        } else if (buf instanceof FloatBuffer) {
            marshal(buffer, (FloatBuffer) buf, flags);

        } else if (buf instanceof DoubleBuffer) {
            marshal(buffer, (DoubleBuffer) buf, flags);

        } else {
            throw new IllegalArgumentException("cannot marshal unknown Buffer type: " + buf.getClass());
        }
    }

    public static void marshal(HeapInvocationBuffer buffer, ByteBuffer buf, int flags) {
        if (buf == null) {
            buffer.putAddress(0L);

        } else if (buf.hasArray()) {
            buffer.putArray(buf.array(), buf.arrayOffset() + buf.position(), buf.remaining(), flags);
        
        } else if (buf.isDirect()) {
            buffer.putDirectBuffer(buf, buf.position(), buf.remaining());
        
        } else {
            throw new IllegalArgumentException("cannot marshal non-direct, non-array ByteBuffer");
        }
    }

    public static void marshal(HeapInvocationBuffer buffer, ShortBuffer buf, int flags) {
        if (buf == null) {
            buffer.putAddress(0L);

        } else if (buf.hasArray()) {
            buffer.putArray(buf.array(), buf.arrayOffset() + buf.position(), buf.remaining(), flags);

        } else if (buf.isDirect()) {
            buffer.putDirectBuffer(buf, buf.position() << 1, buf.remaining() << 1);
        
        } else {
            throw new IllegalArgumentException("cannot marshal non-direct, non-array ShortBuffer");
        }
    }

    public static void marshal(HeapInvocationBuffer buffer, IntBuffer buf, int flags) {
        if (buf == null) {
            buffer.putAddress(0L);

        } else if (buf.hasArray()) {
            buffer.putArray(buf.array(), buf.arrayOffset() + buf.position(), buf.remaining(), flags);

        } else if (buf.isDirect()) {
            buffer.putDirectBuffer(buf, buf.position() << 2, buf.remaining() << 2);

        } else {
            throw new IllegalArgumentException("cannot marshal non-direct, non-array IntBuffer");
        }
    }

    public static void marshal(HeapInvocationBuffer buffer, LongBuffer buf, int flags) {
        if (buf == null) {
            buffer.putAddress(0L);

        } else if (buf.hasArray()) {
            buffer.putArray(buf.array(), buf.arrayOffset() + buf.position(), buf.remaining(), flags);
        
        } else if (buf.isDirect()) {
            buffer.putDirectBuffer(buf, buf.position() << 3, buf.remaining() << 3);
        
        } else {
            throw new IllegalArgumentException("cannot marshal non-direct, non-array LongBuffer");
        }
    }

    public static void marshal(HeapInvocationBuffer buffer, FloatBuffer buf, int flags) {
        if (buf == null) {
            buffer.putAddress(0L);

        } else if (buf.hasArray()) {
            buffer.putArray(buf.array(), buf.arrayOffset() + buf.position(), buf.remaining(), flags);
        
        } else if (buf.isDirect()) {
            buffer.putDirectBuffer(buf, buf.position() << 2, buf.remaining() << 2);
        
        } else {
            throw new IllegalArgumentException("cannot marshal non-direct, non-array FloatBuffer");
        }
    }

    public static void marshal(HeapInvocationBuffer buffer, DoubleBuffer buf, int flags) {
        if (buf == null) {
            buffer.putAddress(0L);

        } else if (buf.hasArray()) {
            buffer.putArray(buf.array(), buf.arrayOffset() + buf.position(), buf.remaining(), flags);
        
        } else if (buf.isDirect()) {
            buffer.putDirectBuffer(buf, buf.position() << 3, buf.remaining() << 3);
        
        } else {
            throw new IllegalArgumentException("cannot marshal non-direct, non-array DoubleBuffer");
        }
    }

    public static void marshal(HeapInvocationBuffer buffer, CharSequence cs) {
        if (cs == null) {
            buffer.putAddress(0L);
        } else {
            ByteBuffer buf = Charset.defaultCharset().encode(CharBuffer.wrap(cs));
            buffer.putArray(buf.array(), buf.arrayOffset(), buf.remaining(), com.kenai.jffi.ArrayFlags.IN | com.kenai.jffi.ArrayFlags.NULTERMINATE);
        }
    }

    public static void marshal(HeapInvocationBuffer buffer, InvocationSession session,
            final CharSequence[] strings, final int inout, int nativeArrayFlags) {
        if (strings == null) {
            buffer.putAddress(0L);

        } else {
            final Pointer[] pointers = new Pointer[strings.length];
            Charset charset = Charset.defaultCharset();

            if (ParameterFlags.isIn(inout)) {
                for (int i = 0; i < strings.length; ++i) {
                    if (strings[i] != null) {
                        ByteBuffer buf = charset.encode(CharBuffer.wrap(strings[i]));
                        DirectMemoryIO ptr = TransientNativeMemory.allocate(NativeRuntime.getInstance(), buf.remaining() + 1, 1, false);
                        ptr.putZeroTerminatedByteArray(0, buf.array(), buf.arrayOffset() + buf.position(), buf.remaining());
                        pointers[i] = ptr;

                    } else {
                        pointers[i] = null;
                    }
                }
            }

            marshal(buffer, session, pointers, inout, nativeArrayFlags);

            // Reload any elements of the native array that were changed, and convert back to java strings
            // the PostInvoke also keeps the native memory alive until after the function call
            session.keepAlive(pointers);
            if (ParameterFlags.isOut(inout)) {
                session.addPostInvoke(new InvocationSession.PostInvoke() {
                    public void postInvoke() {
                        for (int i = 0; i < pointers.length; ++i) {
                            if (pointers[i] != null) {
                                strings[i] = pointers[i].getString(0);
                            }
                        }
                    }
                });
            }
        }
    }

    public static void marshal(HeapInvocationBuffer buffer, final Boolean parameter) {
        if (parameter == null) {
            throw new IllegalArgumentException("Boolean value cannot be null");
        } else {
            buffer.putInt(parameter.booleanValue() ? 1 : 0);
        }
    }

    public static void marshal(HeapInvocationBuffer buffer, final boolean parameter) {
        buffer.putInt(parameter ? 1 : 0);
    }


    public static void marshal(HeapInvocationBuffer buffer, InvocationSession session,
            final Pointer[] pointers, int inout, int nativeArrayFlags) {
        if (pointers == null) {
            buffer.putAddress(0L);
        } else {
            if (Platform.getPlatform().addressSize() == 32) {
                final int[] raw = new int[pointers.length + 1];
                for (int i = 0; i < pointers.length; ++i) {
                    if (pointers[i] != null && !pointers[i].isDirect()) {
                        throw new IllegalArgumentException("invalid pointer in array at index " + i);
                    }
                    raw[i] = pointers[i] != null ? (int) pointers[i].address() : 0;
                }
                buffer.putArray(raw, 0, raw.length, nativeArrayFlags);

                if (ParameterFlags.isOut(inout)) {
                    session.addPostInvoke(new InvocationSession.PostInvoke() {

                        public void postInvoke() {
                            for (int i = 0; i < pointers.length; ++i) {
                                pointers[i] = MemoryUtil.newPointer(raw[i]);
                            }
                        }
                    });
                }
            } else {
                final long[] raw = new long[pointers.length + 1];
                for (int i = 0; i < pointers.length; ++i) {
                    if (pointers[i] != null && !pointers[i].isDirect()) {
                        throw new IllegalArgumentException("invalid pointer in array at index " + i);
                    }
                    raw[i] = pointers[i] != null ? pointers[i].address() : 0;
                }

                buffer.putArray(raw, 0, raw.length, nativeArrayFlags);

                if (ParameterFlags.isOut(inout)) {
                    session.addPostInvoke(new InvocationSession.PostInvoke() {

                        public void postInvoke() {
                            for (int i = 0; i < pointers.length; ++i) {
                                pointers[i] = MemoryUtil.newPointer(raw[i]);
                            }
                        }
                    });
                }
            }
        }
    }

    public static UnsatisfiedLinkError newUnsatisifiedLinkError(String msg) {
        return new UnsatisfiedLinkError(msg);
    }

    public static HeapInvocationBuffer newHeapInvocationBuffer(Function function) {
        return new HeapInvocationBuffer(function);
    }

    public static HeapInvocationBuffer newHeapInvocationBuffer(CallContext callContext) {
        return new HeapInvocationBuffer(callContext);
    }

    public static HeapInvocationBuffer newHeapInvocationBuffer(CallContext callContext, int objCount) {
        return new HeapInvocationBuffer(callContext, objCount);
    }

    public static String stringValue(long ptr) {
        if (ptr == 0) {
            return null;
        }
        return Charset.defaultCharset().decode(ByteBuffer.wrap(IO.getZeroTerminatedByteArray(ptr))).toString();
    }

    public static String stringValue(int ptr) {
        return stringValue((long) ptr);
    }

    public static Pointer pointerValue(long ptr) {
        return ptr != 0 ? new DirectMemoryIO(ptr) : null;
    }

    public static Pointer pointerValue(int ptr) {
        return ptr != 0 ? new DirectMemoryIO(ptr) : null;
    }

    public static boolean isDirect(Pointer ptr) {
        return ptr == null || ptr.isDirect();
    }

    public static int intValue(Pointer ptr) {
        return ptr != null ? (int) ptr.address() : 0;
    }

    public static long longValue(Pointer ptr) {
        return ptr != null ? ptr.address() : 0L;
    }

    public static boolean isDirect5(Buffer buffer) {
        if (buffer == null) {
            return true;

        } else if (buffer instanceof ByteBuffer) {
            return ((ByteBuffer) buffer).isDirect();

        } else if (buffer instanceof ShortBuffer) {
            return ((ShortBuffer) buffer).isDirect();

        } else if (buffer instanceof CharBuffer) {
            return ((CharBuffer) buffer).isDirect();

        } else if (buffer instanceof IntBuffer) {
            return ((IntBuffer) buffer).isDirect();

        } else if (buffer instanceof LongBuffer) {
            return ((LongBuffer) buffer).isDirect();

        } else if (buffer instanceof FloatBuffer) {
            return ((FloatBuffer) buffer).isDirect();

        } else if (buffer instanceof DoubleBuffer) {
            return ((DoubleBuffer) buffer).isDirect();

        } else {
            throw new UnsupportedOperationException("unsupported java.nio.Buffer subclass " + buffer.getClass());
        }
    }

    public static boolean isDirect(Buffer buffer) {
        return buffer == null || buffer.isDirect();
    }

    public static boolean isDirect(ByteBuffer buffer) {
        return buffer == null || buffer.isDirect();
    }

    public static boolean isDirect(CharBuffer buffer) {
        return buffer == null || buffer.isDirect();
    }

    public static boolean isDirect(ShortBuffer buffer) {
        return buffer == null || buffer.isDirect();
    }

    public static boolean isDirect(IntBuffer buffer) {
        return buffer == null || buffer.isDirect();
    }

    public static boolean isDirect(LongBuffer buffer) {
        return buffer == null || buffer.isDirect();
    }

    public static boolean isDirect(FloatBuffer buffer) {
        return buffer == null || buffer.isDirect();
    }

    public static boolean isDirect(DoubleBuffer buffer) {
        return buffer == null || buffer.isDirect();
    }

    public static long longValue(Buffer buffer) {

        if (buffer == null) {
            return 0;
        }
        final long address = MemoryIO.getInstance().getDirectBufferAddress(buffer);
        long pos = buffer.position();

        if (buffer instanceof ByteBuffer) {
            // no adjustment needed

        } else if (buffer instanceof ShortBuffer || buffer instanceof CharBuffer) {
            pos <<= 1;

        } else if (buffer instanceof IntBuffer || buffer instanceof FloatBuffer) {
            pos <<= 2;

        } else if (buffer instanceof LongBuffer || buffer instanceof DoubleBuffer) {
            pos <<= 3;

        } else {
            throw new UnsupportedOperationException("unsupported java.nio.Buffer subclass " + buffer.getClass());
        }

        return address + pos;
    }
    public static long longValue(ByteBuffer buffer) {
        return buffer != null ? MemoryIO.getInstance().getDirectBufferAddress(buffer) + buffer.position(): 0L;
    }

    public static long longValue(ShortBuffer buffer) {
        return buffer != null ? MemoryIO.getInstance().getDirectBufferAddress(buffer) + (buffer.position() << 1): 0L;
    }

    public static long longValue(CharBuffer buffer) {
        return buffer != null ? MemoryIO.getInstance().getDirectBufferAddress(buffer) + (buffer.position() << 1): 0L;
    }

    public static long longValue(IntBuffer buffer) {
        return buffer != null ? MemoryIO.getInstance().getDirectBufferAddress(buffer) + (buffer.position() << 2): 0L;
    }

    public static long longValue(LongBuffer buffer) {
        return buffer != null ? MemoryIO.getInstance().getDirectBufferAddress(buffer) + (buffer.position() << 3): 0L;
    }

    public static long longValue(FloatBuffer buffer) {
        return buffer != null ? MemoryIO.getInstance().getDirectBufferAddress(buffer) + (buffer.position() << 2): 0L;
    }

    public static long longValue(DoubleBuffer buffer) {
        return buffer != null ? MemoryIO.getInstance().getDirectBufferAddress(buffer) + (buffer.position() << 3): 0L;
    }

    public static long longValue(Address ptr) {
        return ptr != null ? ptr.longValue() : 0L;
    }

    public static int intValue(Address ptr) {
        return ptr != null ? ptr.intValue() : 0;
    }

    public static PointerParameterStrategy pointerParameterStrategy(Pointer pointer) {
        if (pointer instanceof DirectMemoryIO) {
            return DirectMemoryParameterStrategy.INSTANCE;

        } else {
            return otherPointerParameterStrategy(pointer);
        }
    }

    private static PointerParameterStrategy otherPointerParameterStrategy(Pointer pointer) {
        if (pointer == null) {
            return NullPointerParameterStrategy.INSTANCE;

        } else if (pointer.isDirect()) {
            return pointer instanceof AbstractBufferMemoryIO
                    ? DirectBufferMemoryParameterStrategy.INSTANCE : DirectPointerParameterStrategy.INSTANCE;

        } else if (pointer.hasArray()) {
            return ArrayMemoryParameterStrategy.INSTANCE;

        } else {
            throw new RuntimeException("cannot convert " + pointer.getClass() + " to native");
        }
    }

    public static PointerParameterStrategy pointerParameterStrategy(CharSequence s) {
        if (s == null) {
            return NullPointerParameterStrategy.INSTANCE;

        } else {
            return new StringParameterStrategy(s);
        }
    }

    public static PointerParameterStrategy bufferParameterStrategy(Buffer buffer, ObjectParameterType.ComponentType componentType) {
        if (buffer == null) {
            return NullPointerParameterStrategy.INSTANCE;

        } else if (buffer.isDirect()) {
            return DirectBufferParameterStrategy.get(componentType);

        } else if (buffer.hasArray()) {
            return HeapBufferParameterStrategy.get(componentType);

        } else {
            throw new IllegalArgumentException("cannot marshal non-direct, non-array Buffer");
        }
    }

    public static PointerParameterStrategy pointerParameterStrategy(Buffer buffer) {
        if (buffer instanceof ByteBuffer) {
            return bufferParameterStrategy(buffer, ObjectParameterType.BYTE);

        } else if (buffer instanceof ShortBuffer) {
            return bufferParameterStrategy(buffer, ObjectParameterType.SHORT);

        } else if (buffer instanceof CharBuffer) {
            return bufferParameterStrategy(buffer, ObjectParameterType.CHAR);

        } else if (buffer instanceof IntBuffer) {
            return bufferParameterStrategy(buffer, ObjectParameterType.INT);

        } else if (buffer instanceof LongBuffer) {
            return bufferParameterStrategy(buffer, ObjectParameterType.LONG);

        } else if (buffer instanceof FloatBuffer) {
            return bufferParameterStrategy(buffer, ObjectParameterType.FLOAT);

        } else if (buffer instanceof DoubleBuffer) {
            return bufferParameterStrategy(buffer, ObjectParameterType.DOUBLE);

        } else if (buffer == null) {
                return NullPointerParameterStrategy.INSTANCE;

        } else {
            throw new IllegalArgumentException("unsupported java.nio.Buffer subclass: " + buffer.getClass());
        }
    }
    public static PointerParameterStrategy pointerParameterStrategy(ByteBuffer buffer) {
        return bufferParameterStrategy(buffer, ObjectParameterType.BYTE);
    }

    public static PointerParameterStrategy pointerParameterStrategy(ShortBuffer buffer) {
        return bufferParameterStrategy(buffer, ObjectParameterType.SHORT);
    }

    public static PointerParameterStrategy pointerParameterStrategy(CharBuffer buffer) {
        return bufferParameterStrategy(buffer, ObjectParameterType.CHAR);
    }

    public static PointerParameterStrategy pointerParameterStrategy(IntBuffer buffer) {
        return bufferParameterStrategy(buffer, ObjectParameterType.INT);
    }

    public static PointerParameterStrategy pointerParameterStrategy(LongBuffer buffer) {
        return bufferParameterStrategy(buffer, ObjectParameterType.LONG);
    }

    public static PointerParameterStrategy pointerParameterStrategy(FloatBuffer buffer) {
        return bufferParameterStrategy(buffer, ObjectParameterType.FLOAT);
    }

    public static PointerParameterStrategy pointerParameterStrategy(DoubleBuffer buffer) {
        return bufferParameterStrategy(buffer, ObjectParameterType.DOUBLE);
    }

    public static PointerParameterStrategy pointerParameterStrategy(byte[] array) {
        return array != null ? PrimitiveArrayParameterStrategy.BYTE : NullPointerParameterStrategy.INSTANCE;
    }

    public static PointerParameterStrategy pointerParameterStrategy(short[] array) {
        return array != null ? PrimitiveArrayParameterStrategy.SHORT : NullPointerParameterStrategy.INSTANCE;
    }

    public static PointerParameterStrategy pointerParameterStrategy(char[] array) {
        return array != null ? PrimitiveArrayParameterStrategy.CHAR : NullPointerParameterStrategy.INSTANCE;
    }

    public static PointerParameterStrategy pointerParameterStrategy(int[] array) {
        return array != null ? PrimitiveArrayParameterStrategy.INT : NullPointerParameterStrategy.INSTANCE;
    }

    public static PointerParameterStrategy pointerParameterStrategy(long[] array) {
        return array != null ? PrimitiveArrayParameterStrategy.LONG : NullPointerParameterStrategy.INSTANCE;
    }

    public static PointerParameterStrategy pointerParameterStrategy(float[] array) {
        return array != null ? PrimitiveArrayParameterStrategy.FLOAT : NullPointerParameterStrategy.INSTANCE;
    }

    public static PointerParameterStrategy pointerParameterStrategy(double[] array) {
        return array != null ? PrimitiveArrayParameterStrategy.DOUBLE : NullPointerParameterStrategy.INSTANCE;
    }

    public static PointerParameterStrategy pointerParameterStrategy(boolean [] array) {
        return array != null ? PrimitiveArrayParameterStrategy.BOOLEAN : NullPointerParameterStrategy.INSTANCE;
    }

    public static void checkAllStrategiesAreHeap(ObjectParameterStrategy s1) {
        if (s1.isDirect()) {
            throw new RuntimeException("pointer 1 is direct");
        }
    }

    public static void checkAllStrategiesAreHeap(ObjectParameterStrategy s1, ObjectParameterStrategy s2) {
        if (s1.isDirect()) {
            throw new RuntimeException("pointer 1 is direct");
        }
        if (s2.isDirect()) {
            throw new RuntimeException("pointer 2 is direct");
        }
    }

    public static void checkAllStrategiesAreHeap(ObjectParameterStrategy s1, ObjectParameterStrategy s2, ObjectParameterStrategy s3) {
        if (s1.isDirect()) {
            throw new RuntimeException("pointer 1 is direct");
        }
        if (s2.isDirect()) {
            throw new RuntimeException("pointer 2 is direct");
        }
        if (s3.isDirect()) {
            throw new RuntimeException("pointer 3 is direct");
        }
    }

    public static void postInvoke(ToNativeConverter.PostInvocation postInvocation, Object j, Object n, ToNativeContext context) {
        try {
            postInvocation.postInvoke(j, n, context);
        } catch (Throwable t) {}
    }

}
