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

    public static Pointer pointerValue(long ptr, jnr.ffi.Runtime runtime) {
        return ptr != 0 ? new DirectMemoryIO(runtime, ptr) : null;
    }

    public static Pointer pointerValue(int ptr, jnr.ffi.Runtime runtime) {
        return ptr != 0 ? new DirectMemoryIO(runtime, ptr) : null;
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

    public static long longValue(Address ptr) {
        return ptr != null ? ptr.longValue() : 0L;
    }

    public static int intValue(Address ptr) {
        return ptr != null ? ptr.intValue() : 0;
    }

    public static long longValue(Buffer ptr) {
        return ptr != null && ptr.isDirect() ? MemoryIO.getInstance().getDirectBufferAddress(ptr) : 0L;
    }

    public static int intValue(Buffer ptr) {
        return ptr != null && ptr.isDirect()  ? (int) MemoryIO.getInstance().getDirectBufferAddress(ptr) : 0;
    }

    public static ParameterStrategy nullParameterStrategy() {
        return NullObjectParameterStrategy.NULL;
    }

    public static PointerParameterStrategy directPointerParameterStrategy() {
        return PointerParameterStrategy.DIRECT;
    }

    public static PointerParameterStrategy pointerParameterStrategy(Pointer pointer) {
        if (pointer == null || pointer.isDirect()) {
            return PointerParameterStrategy.DIRECT;

        } else {
            return otherPointerParameterStrategy(pointer);
        }
    }

    private static PointerParameterStrategy otherPointerParameterStrategy(Pointer pointer) {
        if (pointer.hasArray()) {
            return PointerParameterStrategy.HEAP;

        } else {
            throw new RuntimeException("cannot convert " + pointer.getClass() + " to native");
        }
    }

    public static BufferParameterStrategy bufferParameterStrategy(Buffer buffer, ObjectParameterType.ComponentType componentType) {
        if (buffer == null || buffer.isDirect()) {
            return BufferParameterStrategy.direct(componentType);

        } else if (buffer.hasArray()) {
            return BufferParameterStrategy.heap(componentType);

        } else {
            throw new IllegalArgumentException("cannot marshal non-direct, non-array Buffer");
        }
    }

    public static BufferParameterStrategy pointerParameterStrategy(Buffer buffer) {
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
                return BufferParameterStrategy.direct(ObjectParameterType.BYTE);

        } else {
            throw new IllegalArgumentException("unsupported java.nio.Buffer subclass: " + buffer.getClass());
        }
    }
    public static BufferParameterStrategy pointerParameterStrategy(ByteBuffer buffer) {
        return bufferParameterStrategy(buffer, ObjectParameterType.BYTE);
    }

    public static BufferParameterStrategy pointerParameterStrategy(ShortBuffer buffer) {
        return bufferParameterStrategy(buffer, ObjectParameterType.SHORT);
    }

    public static BufferParameterStrategy pointerParameterStrategy(CharBuffer buffer) {
        return bufferParameterStrategy(buffer, ObjectParameterType.CHAR);
    }

    public static BufferParameterStrategy pointerParameterStrategy(IntBuffer buffer) {
        return bufferParameterStrategy(buffer, ObjectParameterType.INT);
    }

    public static BufferParameterStrategy pointerParameterStrategy(LongBuffer buffer) {
        return bufferParameterStrategy(buffer, ObjectParameterType.LONG);
    }

    public static BufferParameterStrategy pointerParameterStrategy(FloatBuffer buffer) {
        return bufferParameterStrategy(buffer, ObjectParameterType.FLOAT);
    }

    public static BufferParameterStrategy pointerParameterStrategy(DoubleBuffer buffer) {
        return bufferParameterStrategy(buffer, ObjectParameterType.DOUBLE);
    }

    public static ParameterStrategy pointerParameterStrategy(byte[] array) {
        return array != null ? PrimitiveArrayParameterStrategy.BYTE : NullObjectParameterStrategy.NULL;
    }

    public static ParameterStrategy pointerParameterStrategy(short[] array) {
        return array != null ? PrimitiveArrayParameterStrategy.SHORT : NullObjectParameterStrategy.NULL;
    }

    public static ParameterStrategy pointerParameterStrategy(char[] array) {
        return array != null ? PrimitiveArrayParameterStrategy.CHAR : NullObjectParameterStrategy.NULL;
    }

    public static ParameterStrategy pointerParameterStrategy(int[] array) {
        return array != null ? PrimitiveArrayParameterStrategy.INT : NullObjectParameterStrategy.NULL;
    }

    public static ParameterStrategy pointerParameterStrategy(long[] array) {
        return array != null ? PrimitiveArrayParameterStrategy.LONG : NullObjectParameterStrategy.NULL;
    }

    public static ParameterStrategy pointerParameterStrategy(float[] array) {
        return array != null ? PrimitiveArrayParameterStrategy.FLOAT : NullObjectParameterStrategy.NULL;
    }

    public static ParameterStrategy pointerParameterStrategy(double[] array) {
        return array != null ? PrimitiveArrayParameterStrategy.DOUBLE : NullObjectParameterStrategy.NULL;
    }

    public static ParameterStrategy pointerParameterStrategy(boolean[] array) {
        return array != null ? PrimitiveArrayParameterStrategy.BOOLEAN : NullObjectParameterStrategy.NULL;
    }

    public static void postInvoke(ToNativeConverter.PostInvocation postInvocation, Object j, Object n, ToNativeContext context) {
        try {
            postInvocation.postInvoke(j, n, context);
        } catch (Throwable t) {}
    }

}
