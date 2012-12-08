package jnr.ffi.provider.converters;

import jnr.ffi.Pointer;
import jnr.ffi.annotations.LongLong;
import jnr.ffi.mapper.ToNativeContext;
import jnr.ffi.mapper.ToNativeConverter;
import jnr.ffi.provider.MemoryManager;
import jnr.ffi.provider.ParameterFlags;

/**
 * Converts a Pointer[] array to a long[] array parameter
 */
@ToNativeConverter.NoContext
@ToNativeConverter.Cacheable
public class Pointer64ArrayParameterConverter implements ToNativeConverter<Pointer[], long[]> {
    protected final jnr.ffi.Runtime runtime;
    protected final int parameterFlags;

    public static ToNativeConverter<Pointer[], long[]> getInstance(jnr.ffi.Runtime runtime, int parameterFlags) {
        return !ParameterFlags.isOut(parameterFlags)
                ? new Pointer64ArrayParameterConverter(runtime, parameterFlags)
                : new Pointer64ArrayParameterConverter.Out(runtime, parameterFlags);
    }

    Pointer64ArrayParameterConverter(jnr.ffi.Runtime runtime, int parameterFlags) {
        this.runtime = runtime;
        this.parameterFlags = parameterFlags;
    }

    @Override
    @LongLong
    public Class<long[]> nativeType() {
        return long[].class;
    }

    @Override
    public long[] toNative(Pointer[] pointers, ToNativeContext context) {
        if (pointers == null) {
            return null;
        }
        long[] primitive = new long[pointers.length];
        if (ParameterFlags.isIn(parameterFlags)) {
            for (int i = 0; i < pointers.length; i++) {
                if (pointers[i] != null && !pointers[i].isDirect()) {
                    throw new IllegalArgumentException("invalid pointer in array at index " + i);
                }
                primitive[i] = pointers[i] != null ? pointers[i].address() : 0L;
            }
        }

        return primitive;
    }

    public static final class Out extends Pointer64ArrayParameterConverter implements ToNativeConverter.PostInvocation<Pointer[], long[]> {
        Out(jnr.ffi.Runtime runtime, int parameterFlags) {
            super(runtime, parameterFlags);
        }

        @Override
        public void postInvoke(Pointer[] pointers, long[] primitive, ToNativeContext context) {
            if (pointers != null && primitive != null) {
                MemoryManager mm = runtime.getMemoryManager();
                for (int i = 0; i < pointers.length; i++) {
                    pointers[i] = mm.newPointer(primitive[i]);
                }
            }
        }
    }

}
