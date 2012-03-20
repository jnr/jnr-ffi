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
import com.kenai.jffi.Platform;
import jnr.ffi.*;
import jnr.ffi.Struct;
import jnr.ffi.provider.ParameterFlags;
import jnr.ffi.byref.ByReference;
import jnr.ffi.provider.AbstractArrayMemoryIO;
import jnr.ffi.provider.AbstractBufferMemoryIO;
import jnr.ffi.provider.DelegatingMemoryIO;
import jnr.ffi.provider.InvocationSession;
import jnr.ffi.util.BufferUtil;
import jnr.ffi.util.EnumMapper;

import java.nio.*;
import java.nio.charset.Charset;
import java.util.EnumSet;

/**
 * Utility methods that are used at runtime by generated code.
 */
public final class AsmRuntime {
    public static final com.kenai.jffi.MemoryIO IO = com.kenai.jffi.MemoryIO.getInstance();

    private AsmRuntime() {}

    public static final void marshal(InvocationBuffer buffer, byte[] array, int flags) {
        if (array == null) {
            buffer.putAddress(0L);
        } else {
            buffer.putArray(array, 0, array.length, flags);
        }
    }

    public static final void marshal(InvocationBuffer buffer, short[] array, int flags) {
        if (array == null) {
            buffer.putAddress(0L);
        } else {
            buffer.putArray(array, 0, array.length, flags);
        }
    }

    public static final void marshal(InvocationBuffer buffer, int[] array, int flags) {
        if (array == null) {
            buffer.putAddress(0L);
        } else {
            buffer.putArray(array, 0, array.length, flags);
        }
    }

    public static final void marshal(InvocationBuffer buffer, long[] array, int nativeArrayFlags) {
        if (array == null) {
            buffer.putAddress(0L);
        } else {
            buffer.putArray(array, 0, array.length, nativeArrayFlags);
        }
    }
    
    public static final void marshal32(InvocationBuffer buffer, InvocationSession session, 
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

    public static final void marshal(InvocationBuffer buffer, float[] array, int flags) {
        if (array == null) {
            buffer.putAddress(0L);
        } else {
            buffer.putArray(array, 0, array.length, flags);
        }
    }

    public static final void marshal(InvocationBuffer buffer, double[] array, int flags) {
        if (array == null) {
            buffer.putAddress(0L);
        } else {
            buffer.putArray(array, 0, array.length, flags);
        }
    }

    public static final void marshal(InvocationBuffer buffer, Pointer ptr, int nativeArrayFlags) {
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

    public static final void marshal(InvocationBuffer buffer, Address ptr) {
        if (ptr == null) {
            buffer.putAddress(0L);
        } else {
            buffer.putAddress(ptr.nativeAddress());
        }
    }

    public static void marshal(InvocationBuffer buffer, Buffer buf, int flags) {
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

    public static final void marshal(InvocationBuffer buffer, ByteBuffer buf, int flags) {
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

    public static final void marshal(InvocationBuffer buffer, ShortBuffer buf, int flags) {
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

    public static final void marshal(InvocationBuffer buffer, IntBuffer buf, int flags) {
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

    public static final void marshal(InvocationBuffer buffer, LongBuffer buf, int flags) {
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

    public static final void marshal(InvocationBuffer buffer, FloatBuffer buf, int flags) {
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

    public static final void marshal(InvocationBuffer buffer, DoubleBuffer buf, int flags) {
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

    public static final void marshal(InvocationBuffer buffer, CharSequence cs) {
        if (cs == null) {
            buffer.putAddress(0L);
        } else {
            ByteBuffer buf = Charset.defaultCharset().encode(CharBuffer.wrap(cs));
            buffer.putArray(buf.array(), buf.arrayOffset(), buf.remaining(), com.kenai.jffi.ArrayFlags.IN | com.kenai.jffi.ArrayFlags.NULTERMINATE);
        }
    }

    public static final void marshal(InvocationBuffer buffer, jnr.ffi.Struct parameter, int parameterFlags, int nativeArrayFlags) {
        if (parameter == null) {
            buffer.putAddress(0L);
        } else {
            jnr.ffi.Struct s = parameter;
            Pointer memory = Struct.getMemory(s, parameterFlags);
            
            if (memory instanceof AbstractArrayMemoryIO) {
                AbstractArrayMemoryIO aio = (AbstractArrayMemoryIO) memory;
                buffer.putArray(aio.array(), aio.offset(), aio.length(), nativeArrayFlags);
            
            } else if (memory.isDirect()) {
                buffer.putAddress(memory.address());
            }
        }
    }

    public static final void marshal(InvocationBuffer buffer, jnr.ffi.Struct[] parameter, int parameterFlags, int nativeArrayFlags) {
        if (parameter == null) {
            buffer.putAddress(0L);
        } else {
            jnr.ffi.Struct[] array = parameter;
            Pointer memory = Struct.getMemory(array[0], parameterFlags);
            if (!(memory instanceof DelegatingMemoryIO)) {
                throw new RuntimeException("Struct array must be backed by contiguous array");
            }
            memory = ((DelegatingMemoryIO) memory).getDelegatedMemoryIO();
            if (memory instanceof AbstractArrayMemoryIO) {
                AbstractArrayMemoryIO aio = (AbstractArrayMemoryIO) memory;
                buffer.putArray(aio.array(), aio.offset(), aio.length(), nativeArrayFlags);
            
            } else if (memory.isDirect()) {
                buffer.putAddress(memory.address());
            }
        }
    }

    public static final void marshal(InvocationBuffer buffer, InvocationSession session, ByReference parameter, int flags) {
        if (parameter == null) {
            buffer.putAddress(0L);
        } else {
            final ByReference ref = (ByReference) parameter;
            int size = ref.nativeSize(NativeRuntime.getInstance());
            final ArrayMemoryIO memory = new ArrayMemoryIO(size);
            
            if (com.kenai.jffi.ArrayFlags.isIn(flags)) {
                ref.marshal(memory, 0);
            }

            buffer.putArray(memory.array(), memory.offset(), size, flags);
            if (com.kenai.jffi.ArrayFlags.isOut(flags)) {
                session.addPostInvoke(new InvocationSession.PostInvoke() {
                    public void postInvoke() {
                        ref.unmarshal(memory, 0);
                    }
                });
            }
        }
    }

    public static final void marshal(InvocationBuffer buffer, InvocationSession session, StringBuilder parameter, int inout, int nflags) {
        if (parameter == null) {
            buffer.putAddress(0L);
        } else {
            final StringBuilder sb = parameter;
            ByteBuffer buf = ParameterFlags.isIn(inout)
                ? Charset.defaultCharset().encode(CharBuffer.wrap(parameter))
                : ByteBuffer.allocate(sb.capacity() + 1);
            if (ParameterFlags.isOut(inout) && buf.capacity() < sb.capacity() + 1) {
                ByteBuffer tmp = ByteBuffer.allocate(sb.capacity() + 1);
                tmp.put(buf);
                tmp.flip();
                buf = tmp;
            }
            buffer.putArray(buf.array(), buf.arrayOffset(), buf.capacity(), nflags);
            //
            // Copy the string back out if its an OUT parameter
            //
            if (ParameterFlags.isOut(inout)) {
                final ByteBuffer tmp = buf;
                session.addPostInvoke(new InvocationSession.PostInvoke() {

                    public void postInvoke() {
                        tmp.limit(tmp.capacity());
                        tmp.position(0);
                        sb.delete(0, sb.length()).append(BufferUtil.getCharSequence(tmp, Charset.defaultCharset()));
                    }
                });
            }
        }
    }
    
    public static final void marshal(InvocationBuffer buffer, InvocationSession session, final StringBuffer parameter, int inout, int nflags) {
        if (parameter == null) {
            buffer.putAddress(0L);
        } else {
            final StringBuffer sb = parameter;
            ByteBuffer buf = ParameterFlags.isIn(inout)
                            ? Charset.defaultCharset().encode(CharBuffer.wrap(parameter))
                            : ByteBuffer.allocate(sb.capacity() + 1);

            if (ParameterFlags.isOut(inout) && buf.capacity() < sb.capacity() + 1) {
                ByteBuffer tmp = ByteBuffer.allocate(sb.capacity() + 1);
                tmp.put(buf);
                tmp.flip();
                buf = tmp;
            }
            buffer.putArray(buf.array(), buf.arrayOffset(), buf.capacity(), nflags);
            //
            // Copy the string back out if its an OUT parameter
            //
            if (ParameterFlags.isOut(inout)) {
                final ByteBuffer tmp = buf;
                session.addPostInvoke(new InvocationSession.PostInvoke() {

                    public void postInvoke() {
                        tmp.limit(tmp.capacity());
                        tmp.position(0);
                        sb.delete(0, sb.length()).append(BufferUtil.getCharSequence(tmp, Charset.defaultCharset()));
                    }
                });
            }
        }
    }

    public static final void marshal(InvocationBuffer buffer, InvocationSession session,
            final CharSequence[] strings, final int inout, int nativeArrayFlags) {
        if (strings == null) {
            buffer.putAddress(0L);

        } else {
            final AllocatedDirectMemoryIO[] pointers = new AllocatedDirectMemoryIO[strings.length];
            Charset charset = Charset.defaultCharset();

            if (ParameterFlags.isIn(inout)) {
                for (int i = 0; i < strings.length; ++i) {
                    if (strings[i] != null) {
                        ByteBuffer buf = charset.encode(CharBuffer.wrap(strings[i]));
                        AllocatedDirectMemoryIO ptr = new AllocatedDirectMemoryIO(buf.remaining() + 1, false);
                        ptr.putZeroTerminatedByteArray(0, buf.array(), buf.arrayOffset() + buf.position(), buf.remaining());
                        pointers[i] = ptr;

                    } else {
                        pointers[i] = null;
                    }
                }
            }

            // pass it as an array of pointers
            final Pointer[] tmp = new Pointer[pointers.length];
            System.arraycopy(pointers, 0, tmp, 0, tmp.length);
            marshal(buffer, session, tmp, inout, nativeArrayFlags);

            // Reload any elements of the native array that were changed, and convert back to java strings
            // the PostInvoke also keeps the native memory alive until after the function call
            session.addPostInvoke(new InvocationSession.PostInvoke() {

                public void postInvoke() {
                    if (ParameterFlags.isOut(inout)) {
                        for (int i = 0; i < pointers.length; ++i) {
                            if (tmp[i] != null) {
                                strings[i] = tmp[i].getString(0);
                            }
                        }
                    }

                    // Dispose all the allocated memory as soon as the call is finished
                    for (int i = 0; i < pointers.length; ++i) {
                        if (pointers[i] != null) {
                            pointers[i].dispose();
                        }
                    }
                }
            });

        }
    }


    public static final void marshal(InvocationBuffer buffer, final Enum parameter) {
        if (parameter == null) {
            throw new IllegalArgumentException("enum value cannot be null");
        } else {
            buffer.putInt(EnumMapper.getInstance(parameter.getClass()).intValue(parameter));
        }
    }

    public static final void marshal(InvocationBuffer buffer, final Boolean parameter) {
        if (parameter == null) {
            throw new IllegalArgumentException("Boolean value cannot be null");
        } else {
            buffer.putInt(parameter.booleanValue() ? 1 : 0);
        }
    }

    public static final void marshal(InvocationBuffer buffer, final boolean parameter) {
        buffer.putInt(parameter ? 1 : 0);
    }


    public static final void marshal(InvocationBuffer buffer, InvocationSession session,
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

    public static final UnsatisfiedLinkError newUnsatisifiedLinkError(String msg) {
        return new UnsatisfiedLinkError(msg);
    }

    public static final HeapInvocationBuffer newHeapInvocationBuffer(Function function) {
        return new HeapInvocationBuffer(function);
    }

    public static HeapInvocationBuffer newHeapInvocationBuffer(CallContext callContext) {
        return new HeapInvocationBuffer(callContext);
    }

    public static final String stringValue(long ptr) {
        if (ptr == 0) {
            return null;
        }
        return Charset.defaultCharset().decode(ByteBuffer.wrap(IO.getZeroTerminatedByteArray(ptr))).toString();
    }

    public static final String stringValue(int ptr) {
        return stringValue((long) ptr);
    }

    public static final Pointer pointerValue(long ptr) {
        return ptr != 0 ? new DirectMemoryIO(ptr) : null;
    }

    public static final Pointer pointerValue(int ptr) {
        return ptr != 0 ? new DirectMemoryIO(ptr) : null;
    }

    public static final void useMemory(long ptr, jnr.ffi.Struct s) {
        s.useMemory(ptr != 0 ? new DirectMemoryIO(ptr) : MemoryUtil.NULL);
    }

    public static final void useMemory(int ptr, jnr.ffi.Struct s) {
        s.useMemory(ptr != 0 ? new DirectMemoryIO(ptr) : MemoryUtil.NULL);
    }

    public static final boolean isDirect(Pointer ptr) {
        return ptr == null || ptr.isDirect();
    }

    public static final int intValue(Pointer ptr) {
        return ptr != null ? (int) ptr.address() : 0;
    }

    public static final long longValue(Pointer ptr) {
        return ptr != null ? ptr.address() : 0L;
    }

    public static final boolean isDirect5(Buffer buffer) {
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

    public static final boolean isDirect(Buffer buffer) {
        return buffer == null || buffer.isDirect();
    }

    public static final boolean isDirect(ByteBuffer buffer) {
        return buffer == null || buffer.isDirect();
    }

    public static final boolean isDirect(CharBuffer buffer) {
        return buffer == null || buffer.isDirect();
    }

    public static final boolean isDirect(ShortBuffer buffer) {
        return buffer == null || buffer.isDirect();
    }

    public static final boolean isDirect(IntBuffer buffer) {
        return buffer == null || buffer.isDirect();
    }

    public static final boolean isDirect(LongBuffer buffer) {
        return buffer == null || buffer.isDirect();
    }

    public static final boolean isDirect(FloatBuffer buffer) {
        return buffer == null || buffer.isDirect();
    }

    public static final boolean isDirect(DoubleBuffer buffer) {
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
    public static final long longValue(ByteBuffer buffer) {
        return buffer != null ? MemoryIO.getInstance().getDirectBufferAddress(buffer) + buffer.position(): 0L;
    }

    public static final long longValue(ShortBuffer buffer) {
        return buffer != null ? MemoryIO.getInstance().getDirectBufferAddress(buffer) + (buffer.position() << 1): 0L;
    }

    public static final long longValue(CharBuffer buffer) {
        return buffer != null ? MemoryIO.getInstance().getDirectBufferAddress(buffer) + (buffer.position() << 1): 0L;
    }

    public static final long longValue(IntBuffer buffer) {
        return buffer != null ? MemoryIO.getInstance().getDirectBufferAddress(buffer) + (buffer.position() << 2): 0L;
    }

    public static final long longValue(LongBuffer buffer) {
        return buffer != null ? MemoryIO.getInstance().getDirectBufferAddress(buffer) + (buffer.position() << 3): 0L;
    }

    public static final long longValue(FloatBuffer buffer) {
        return buffer != null ? MemoryIO.getInstance().getDirectBufferAddress(buffer) + (buffer.position() << 2): 0L;
    }

    public static final long longValue(DoubleBuffer buffer) {
        return buffer != null ? MemoryIO.getInstance().getDirectBufferAddress(buffer) + (buffer.position() << 3): 0L;
    }

    public static final boolean isDirect(jnr.ffi.Struct s) {
        return s == null || Struct.isDirect(s);
    }

    public static final boolean isDirect(jnr.ffi.Struct s, int flags) {
        return s == null || Struct.getMemory(s, flags).isDirect();
    }

    public static final int intValue(jnr.ffi.Struct s) {
        return s != null ? (int) Struct.getMemory(s).address() : 0;
    }

    public static final long longValue(jnr.ffi.Struct s) {
        return s != null ? Struct.getMemory(s).address() : 0L;
    }

    public static final Enum enumValue(int value, Class<? extends Enum> enumClass) {
        return EnumMapper.getInstance(enumClass).valueOf(value);
    }

    public static final int intValue(Enum e) {
        return EnumMapper.getInstance(e.getClass()).intValue(e);
    }

    public static final long longValue(Enum e) {
        return EnumMapper.getInstance(e.getClass()).intValue(e);
    }

    public static PointerParameterStrategy pointerParameterStrategy(Pointer pointer) {
        if (pointer == null) {
            return NullPointerParameterStrategy.INSTANCE;

        } else if (pointer.isDirect()) {
            return pointer instanceof AbstractBufferMemoryIO
                    ? DirectBufferMemoryParameterStrategy.INSTANCE : DirectPointerParameterStrategy.INSTANCE;

        } else if (pointer.hasArray()) {
            return ArrayMemoryParameterStrategy.INSTANCE;

        } else if (pointer instanceof DelegatingMemoryIO) {
            return pointerParameterStrategy(((DelegatingMemoryIO) pointer).getDelegatedMemoryIO());

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

    public static PointerParameterStrategy structParameterStrategy(Struct s) {
        if (s == null) {
            return NullPointerParameterStrategy.INSTANCE;

        } else if (Struct.isDirect(s)) {
            return DirectStructParameterStrategy.INSTANCE;

        } else {
            return HeapStructParameterStrategy.INSTANCE;
        }
    }

    public static PointerParameterStrategy directStructParameterStrategy(Struct s) {
        if (s == null) {
            return NullPointerParameterStrategy.INSTANCE;

        } else {
            return DirectStructParameterStrategy.INSTANCE;
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

}
