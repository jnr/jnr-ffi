
package com.kenai.jaffl.provider.jffi;

import com.kenai.jaffl.MemoryIO;
import com.kenai.jaffl.Pointer;
import com.kenai.jaffl.provider.AbstractArrayMemoryIO;
import com.kenai.jaffl.provider.StringIO;
import com.kenai.jaffl.struct.Struct;
import com.kenai.jaffl.struct.StructUtil;
import com.kenai.jffi.InvocationBuffer;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;

public class MarshalUtil {
    public static final com.kenai.jffi.MemoryIO IO = com.kenai.jffi.MemoryIO.getInstance();

    private MarshalUtil() {}

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

    public static final void marshal(InvocationBuffer buffer, Pointer ptr, int flags) {
        if (ptr == null) {
            buffer.putAddress(0L);
        } else if (ptr instanceof JFFIPointer) {
            buffer.putAddress(((JFFIPointer) ptr).address);
        } else {
            throw new IllegalArgumentException("unsupported argument type" + ptr.getClass());
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

    public static final void marshal(InvocationBuffer buffer, Struct parameter, int flags, int nflags) {
        if (parameter == null) {
            buffer.putAddress(0L);
        } else {
            Struct s = (Struct) parameter;
            MemoryIO io = StructUtil.getMemoryIO(s, flags);
            if (io instanceof AbstractArrayMemoryIO) {
                AbstractArrayMemoryIO aio = (AbstractArrayMemoryIO) io;
                buffer.putArray(aio.array(), aio.offset(), aio.length(), nflags);
            } else if (io.isDirect()) {
                buffer.putAddress(io.getAddress());
            }
        }
    }

    public static final String returnString(long ptr) {
        if (ptr == 0) {
            return null;
        }
        final ByteBuffer buf = ByteBuffer.wrap(IO.getZeroTerminatedByteArray(ptr));

        return StringIO.getStringIO().fromNative(buf).toString();
    }
}
