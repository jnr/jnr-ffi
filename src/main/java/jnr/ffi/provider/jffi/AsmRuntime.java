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
                    ? DirectBufferMemoryParameterStrategy.get(ObjectParameterType.ComponentType.BYTE) : DirectPointerParameterStrategy.INSTANCE;

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

    public static void postInvoke(ToNativeConverter.PostInvocation postInvocation, Object j, Object n, ToNativeContext context) {
        try {
            postInvocation.postInvoke(j, n, context);
        } catch (Throwable t) {}
    }

}
