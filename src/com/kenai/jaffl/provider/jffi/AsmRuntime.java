
package com.kenai.jaffl.provider.jffi;

import com.kenai.jaffl.Address;
import com.kenai.jaffl.MemoryIO;
import com.kenai.jaffl.ParameterFlags;
import com.kenai.jaffl.Pointer;
import com.kenai.jaffl.byref.ByReference;
import com.kenai.jaffl.provider.AbstractArrayMemoryIO;
import com.kenai.jaffl.provider.DelegatingMemoryIO;
import com.kenai.jaffl.provider.InvocationSession;
import com.kenai.jaffl.provider.StringIO;
import com.kenai.jaffl.struct.Struct;
import com.kenai.jaffl.struct.StructUtil;
import com.kenai.jaffl.util.EnumMapper;
import com.kenai.jffi.Function;
import com.kenai.jffi.HeapInvocationBuffer;
import com.kenai.jffi.InvocationBuffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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

    public static final void marshal(InvocationBuffer buffer, long[] array, int flags) {
        if (array == null) {
            buffer.putAddress(0L);
        } else {
            buffer.putArray(array, 0, array.length, flags);
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
        } else {
            throw new IllegalArgumentException("unsupported argument type" + ptr.getClass());
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
        } else {
            buffer.putDirectBuffer(buf, buf.position(), buf.remaining());
        }
    }

    public static final void marshal(InvocationBuffer buffer, ShortBuffer buf, int flags) {
        if (buf == null) {
            buffer.putAddress(0L);
        } else if (buf.hasArray()) {
            buffer.putArray(buf.array(), buf.arrayOffset() + buf.position(), buf.remaining(), flags);
        } else {
            buffer.putDirectBuffer(buf, buf.position() << 1, buf.remaining() << 1);
        }
    }

    public static final void marshal(InvocationBuffer buffer, IntBuffer buf, int flags) {
        if (buf == null) {
            buffer.putAddress(0L);
        } else if (buf.hasArray()) {
            buffer.putArray(buf.array(), buf.arrayOffset() + buf.position(), buf.remaining(), flags);
        } else {
            buffer.putDirectBuffer(buf, buf.position() << 2, buf.remaining() << 2);
        }
    }

    public static final void marshal(InvocationBuffer buffer, LongBuffer buf, int flags) {
        if (buf == null) {
            buffer.putAddress(0L);
        } else if (buf.hasArray()) {
            buffer.putArray(buf.array(), buf.arrayOffset() + buf.position(), buf.remaining(), flags);
        } else {
            buffer.putDirectBuffer(buf, buf.position() << 3, buf.remaining() << 3);
        }
    }

    public static final void marshal(InvocationBuffer buffer, FloatBuffer buf, int flags) {
        if (buf == null) {
            buffer.putAddress(0L);
        } else if (buf.hasArray()) {
            buffer.putArray(buf.array(), buf.arrayOffset() + buf.position(), buf.remaining(), flags);
        } else {
            buffer.putDirectBuffer(buf, buf.position() << 2, buf.remaining() << 2);
        }
    }

    public static final void marshal(InvocationBuffer buffer, DoubleBuffer buf, int flags) {
        if (buf == null) {
            buffer.putAddress(0L);
        } else if (buf.hasArray()) {
            buffer.putArray(buf.array(), buf.arrayOffset() + buf.position(), buf.remaining(), flags);
        } else {
            buffer.putDirectBuffer(buf, buf.position() << 3, buf.remaining() << 3);
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
            MemoryIO io = StructUtil.getMemoryIO(s, parameterFlags);
            if (io instanceof AbstractArrayMemoryIO) {
                AbstractArrayMemoryIO aio = (AbstractArrayMemoryIO) io;
                buffer.putArray(aio.array(), aio.offset(), aio.length(), nativeArrayFlags);
            } else if (io.isDirect()) {
                buffer.putAddress(io.address());
            }
        }
    }

    public static final void marshal(InvocationBuffer buffer, Struct[] parameter, int parameterFlags, int nativeArrayFlags) {
        if (parameter == null) {
            buffer.putAddress(0L);
        } else {
            Struct[] array = parameter;
            MemoryIO io = StructUtil.getMemoryIO(array[0], parameterFlags);
            if (!(io instanceof DelegatingMemoryIO)) {
                throw new RuntimeException("Struct array must be backed by contiguous array");
            }
            io = ((DelegatingMemoryIO) io).getDelegatedMemoryIO();
            if (io instanceof AbstractArrayMemoryIO) {
                AbstractArrayMemoryIO aio = (AbstractArrayMemoryIO) io;
                buffer.putArray(aio.array(), aio.offset(), aio.length(), nativeArrayFlags);
            } else if (io.isDirect()) {
                buffer.putAddress(io.address());
            }
        }
    }

    public static final void marshal(InvocationSession session, InvocationBuffer buffer, ByReference parameter, int flags) {
        if (parameter == null) {
            buffer.putAddress(0L);
        } else {
            final ByReference ref = (ByReference) parameter;
            final ByteBuffer buf = ByteBuffer.allocate(ref.nativeSize()).order(ByteOrder.nativeOrder());
            buf.clear();
            if (com.kenai.jffi.ArrayFlags.isIn(flags)) {
                ref.marshal(buf);
            }
            buffer.putArray(buf.array(), buf.arrayOffset() + buf.position(), buf.remaining(), flags);
            if (com.kenai.jffi.ArrayFlags.isOut(flags)) {
                session.addPostInvoke(new InvocationSession.PostInvoke() {
                    public void postInvoke() {
                        ref.unmarshal(buf);
                    }
                });
            }
        }
    }

    public static final void marshal(InvocationSession session, InvocationBuffer buffer, StringBuilder parameter, int inout, int nflags) {
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
    
    public static final void marshal(InvocationSession session, InvocationBuffer buffer, final StringBuffer parameter, int inout, int nflags) {
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

    public static final void marshal(InvocationSession session, InvocationBuffer buffer,
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
            marshal(session, buffer, tmp, inout, nativeArrayFlags);

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
                }
            });

        }
    }


    public static final void marshal(InvocationBuffer buffer, final Enum parameter) {
        buffer.putInt(EnumMapper.getInstance().intValue(parameter));
    }


    public static final void marshal(InvocationSession session, InvocationBuffer buffer, 
            final Pointer[] pointers, int inout, int nativeArrayFlags) {
        if (pointers == null) {
            buffer.putAddress(0L);
        } else {
            if (Pointer.SIZE == 32) {
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
    
    public static final String returnString(long ptr) {
        if (ptr == 0) {
            return null;
        }
        final ByteBuffer buf = ByteBuffer.wrap(IO.getZeroTerminatedByteArray(ptr));

        return StringIO.getStringIO().fromNative(buf).toString();
    }

    public static final Pointer pointerValue(long ptr) {
        return ptr != 0 ? new DirectMemoryIO(ptr) : null;
    }

    public static final Pointer pointerValue(int ptr) {
        return ptr != 0 ? new DirectMemoryIO((long) ptr & 0xffffffffL) : null;
    }

    public static final MemoryIO newMemoryIO(long ptr) {
        return ptr == 0 ? null : new DirectMemoryIO(ptr);
    }

    public static final void useMemory(long ptr, Struct s) {
        s.useMemory(new DirectMemoryIO(ptr));
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
        return s == null || StructUtil.getMemoryIO(s, flags).isDirect();
    }

    public static final int intValue(Struct s) {
        return s != null ? (int) StructUtil.getMemoryIO(s).address() : 0;
    }

    public static final long longValue(Struct s) {
        return s != null ? StructUtil.getMemoryIO(s).address() : 0L;
    }
}
