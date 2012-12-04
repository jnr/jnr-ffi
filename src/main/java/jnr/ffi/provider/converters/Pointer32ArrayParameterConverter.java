package jnr.ffi.provider.converters;

import jnr.ffi.*;
import jnr.ffi.mapper.ToNativeContext;
import jnr.ffi.mapper.ToNativeConverter;
import jnr.ffi.provider.MemoryManager;
import jnr.ffi.provider.ParameterFlags;

/**
 * Converts a Pointer[] array to a int[] array parameter
 */
@ToNativeConverter.NoContext
public class Pointer32ArrayParameterConverter implements ToNativeConverter<Pointer[], int[]> {
    protected final jnr.ffi.Runtime runtime;
    protected final int parameterFlags;

    public static ToNativeConverter<Pointer[], int[]> getInstance(jnr.ffi.Runtime runtime, int parameterFlags) {
        return !ParameterFlags.isOut(parameterFlags)
            ? new Pointer32ArrayParameterConverter(runtime, parameterFlags)
            : new Pointer32ArrayParameterConverter.Out(runtime, parameterFlags);
    }

    Pointer32ArrayParameterConverter(jnr.ffi.Runtime runtime, int parameterFlags) {
        this.runtime = runtime;
        this.parameterFlags = parameterFlags;
    }

    @Override
    public Class<int[]> nativeType() {
        return int[].class;
    }

    @Override
    public int[] toNative(Pointer[] pointers, ToNativeContext context) {
        if (pointers == null) {
            return null;
        }
        int[] primitive = new int[pointers.length];
        if (ParameterFlags.isIn(parameterFlags)) {
            for (int i = 0; i < pointers.length; i++) {
                if (pointers[i] != null && !pointers[i].isDirect()) {
                    throw new IllegalArgumentException("invalid pointer in array at index " + i);
                }
                primitive[i] = pointers[i] != null ? (int) pointers[i].address() : 0;
            }
        }

        return primitive;
    }

    public static final class Out extends Pointer32ArrayParameterConverter implements ToNativeConverter.PostInvocation<Pointer[], int[]> {
        public Out(jnr.ffi.Runtime runtime, int parameterFlags) {
            super(runtime, parameterFlags);
        }

        @Override
        public void postInvoke(Pointer[] pointers, int[] primitive, ToNativeContext context) {
            if (pointers != null && primitive != null && ParameterFlags.isOut(parameterFlags)) {
                MemoryManager mm = runtime.getMemoryManager();
                for (int i = 0; i < pointers.length; i++) {
                    pointers[i] = mm.newPointer(primitive[i]);
                }
            }
        }
    }
}
