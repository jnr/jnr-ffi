package jnr.ffi.provider.converters;

import jnr.ffi.*;
import jnr.ffi.mapper.ToNativeContext;
import jnr.ffi.mapper.ToNativeConverter;
import jnr.ffi.provider.InAccessibleMemoryIO;
import jnr.ffi.provider.ParameterFlags;
import jnr.ffi.provider.jffi.NativeRuntime;
import jnr.ffi.provider.jffi.TransientNativeMemory;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Converts a String[] array to a Pointer parameter
 */
@ToNativeConverter.NoContext
@ToNativeConverter.Cacheable
public class StringArrayParameterConverter implements ToNativeConverter<String[], Pointer> {
    private final jnr.ffi.Runtime runtime;
    private final int parameterFlags;

    public static ToNativeConverter<String[], Pointer> getInstance(jnr.ffi.Runtime runtime, int parameterFlags) {
        return !ParameterFlags.isOut(parameterFlags)
            ? new StringArrayParameterConverter(runtime, parameterFlags)
            : new StringArrayParameterConverter.Out(runtime, parameterFlags);
    }

    StringArrayParameterConverter(jnr.ffi.Runtime runtime, int parameterFlags) {
        this.runtime = runtime;
        this.parameterFlags = parameterFlags;
    }

    @Override
    public Pointer toNative(String[] array, ToNativeContext context) {
        if (array == null) {
            return null;
        }

        StringArray stringArray = new StringArray(runtime, array.length + 1);
        if (ParameterFlags.isIn(parameterFlags)) {
            for (int i = 0; i < array.length; i++) {
                stringArray.put(i, array[i]);
            }
        }

        return stringArray;
    }

    public static final class Out extends StringArrayParameterConverter implements PostInvocation<String[], Pointer> {
        Out(jnr.ffi.Runtime runtime, int parameterFlags) {
            super(runtime, parameterFlags);
        }

        @Override
        public void postInvoke(String[] array, Pointer primitive, ToNativeContext context) {
            if (array != null && primitive != null) {
                StringArray stringArray = (StringArray) primitive;
                for (int i = 0; i < array.length; i++) {
                    array[i] = stringArray.get(i);
                }
            }
        }
    }

    @Override
    public Class<Pointer> nativeType() {
        return Pointer.class;
    }

    private final static class StringArray extends InAccessibleMemoryIO {
        private final Pointer memory;
        private List<Pointer> stringMemory;
        private final Charset charset = Charset.defaultCharset();

        private StringArray(jnr.ffi.Runtime runtime, int capacity) {
            super(runtime);
            this.memory = Memory.allocateDirect(runtime, capacity * runtime.addressSize());
            this.stringMemory = new ArrayList<Pointer>(capacity);
        }

        String get(int idx) {
            Pointer ptr = memory.getPointer(idx * getRuntime().addressSize());
            return ptr != null ? ptr.getString(0) : null;
        }

        void put(int idx, String str) {
            if (str == null) {
                memory.putAddress(idx * getRuntime().addressSize(), 0L);
            } else {
                ByteBuffer buf = charset.encode(CharBuffer.wrap(str));
                Pointer ptr = Memory.allocateDirect(NativeRuntime.getInstance(), buf.remaining() + 4, true);
                ptr.put(0, buf.array(), 0, buf.remaining());
                stringMemory.add(idx, ptr);
                memory.putPointer(idx * getRuntime().addressSize(), ptr);
            }
        }

        @Override
        public boolean isDirect() {
            return memory.isDirect();
        }

        @Override
        public long address() {
            return memory.address();
        }

        @Override
        public long size() {
            return memory.size();
        }
    }
}
