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

package com.kenai.jaffl.provider.jffi;

import com.kenai.jaffl.Address;
import com.kenai.jaffl.provider.ParameterFlags;
import com.kenai.jaffl.Pointer;
import com.kenai.jaffl.byref.ByReference;
import com.kenai.jaffl.provider.AbstractArrayMemoryIO;
import com.kenai.jaffl.provider.AbstractBufferMemoryIO;
import com.kenai.jaffl.provider.DelegatingMemoryIO;
import com.kenai.jaffl.provider.InvocationSession;
import com.kenai.jaffl.provider.StringIO;
import com.kenai.jaffl.struct.Struct;
import com.kenai.jaffl.struct.StructUtil;
import com.kenai.jaffl.util.EnumMapper;
import com.kenai.jffi.Function;
import com.kenai.jffi.HeapInvocationBuffer;
import com.kenai.jffi.InvocationBuffer;
import com.kenai.jffi.Platform;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;

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
            ByteBuffer buf = StringIO.getStringIO().toNative(cs, cs.length(), true);
            buffer.putArray(buf.array(), buf.arrayOffset(), buf.remaining(), com.kenai.jffi.ArrayFlags.IN | com.kenai.jffi.ArrayFlags.NULTERMINATE);
        }
    }

    public static final void marshal(InvocationBuffer buffer, Struct parameter, int parameterFlags, int nativeArrayFlags) {
        if (parameter == null) {
            buffer.putAddress(0L);
        } else {
            Struct s = parameter;
            Pointer memory = StructUtil.getMemory(s, parameterFlags);
            
            if (memory instanceof AbstractArrayMemoryIO) {
                AbstractArrayMemoryIO aio = (AbstractArrayMemoryIO) memory;
                buffer.putArray(aio.array(), aio.offset(), aio.length(), nativeArrayFlags);
            
            } else if (memory.isDirect()) {
                buffer.putAddress(memory.address());
            }
        }
    }

    public static final void marshal(InvocationBuffer buffer, Struct[] parameter, int parameterFlags, int nativeArrayFlags) {
        if (parameter == null) {
            buffer.putAddress(0L);
        } else {
            Struct[] array = parameter;
            Pointer memory = StructUtil.getMemory(array[0], parameterFlags);
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
            final StringIO io = StringIO.getStringIO();
            final ByteBuffer buf = io.toNative(sb, sb.capacity(), ParameterFlags.isIn(inout));
            buffer.putArray(buf.array(), buf.arrayOffset(), buf.remaining(), nflags);
            //
            // Copy the string back out if its an OUT parameter
            //
            if (ParameterFlags.isOut(inout)) {
                session.addPostInvoke(new InvocationSession.PostInvoke() {

                    public void postInvoke() {
                        sb.delete(0, sb.length()).append(io.fromNative(buf, sb.capacity()));
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
            final StringIO io = StringIO.getStringIO();
            final ByteBuffer buf = io.toNative(sb, sb.capacity(), ParameterFlags.isIn(inout));
            buffer.putArray(buf.array(), buf.arrayOffset(), buf.remaining(), nflags);
            //
            // Copy the string back out if its an OUT parameter
            //
            if (ParameterFlags.isOut(inout)) {
                session.addPostInvoke(new InvocationSession.PostInvoke() {

                    public void postInvoke() {
                        sb.delete(0, sb.length()).append(io.fromNative(buf, sb.capacity()));
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
            final StringIO io = StringIO.getStringIO();

            if (ParameterFlags.isIn(inout)) {
                for (int i = 0; i < strings.length; ++i) {
                    if (strings[i] != null) {
                        ByteBuffer buf = io.toNative(strings[i], strings[i].length(), ParameterFlags.isIn(inout));
                        AllocatedDirectMemoryIO ptr = new AllocatedDirectMemoryIO(buf.remaining(), false);
                        ptr.put(0, buf.array(), buf.arrayOffset() + buf.position(), buf.remaining());
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

    public static final String stringValue(long ptr) {
        if (ptr == 0) {
            return null;
        }
        final ByteBuffer buf = ByteBuffer.wrap(IO.getZeroTerminatedByteArray(ptr));

        return StringIO.getStringIO().fromNative(buf).toString();
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

    public static final void useMemory(long ptr, Struct s) {
        s.useMemory(ptr != 0 ? new DirectMemoryIO(ptr) : MemoryUtil.NULL);
    }

    public static final void useMemory(int ptr, Struct s) {
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


    public static final boolean isDirect(Struct s) {
        return s == null || StructUtil.isDirect(s);
    }

    public static final boolean isDirect(Struct s, int flags) {
        return s == null || StructUtil.getMemory(s, flags).isDirect();
    }

    public static final int intValue(Struct s) {
        return s != null ? (int) StructUtil.getMemory(s).address() : 0;
    }

    public static final long longValue(Struct s) {
        return s != null ? StructUtil.getMemory(s).address() : 0L;
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
}
