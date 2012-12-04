package jnr.ffi.provider.converters;

import jnr.ffi.mapper.ToNativeContext;
import jnr.ffi.mapper.ToNativeConverter;
import jnr.ffi.provider.ParameterFlags;

/**
 * Converts a long[] array to a primitive int[] array parameter
 */
@ToNativeConverter.NoContext
public class Long32ArrayParameterConverter implements ToNativeConverter<long[], int[]> {
    private final jnr.ffi.Runtime runtime;
    private final int parameterFlags;

    public static ToNativeConverter<long[], int[]> getInstance(jnr.ffi.Runtime runtime, int parameterFlags) {
        return !ParameterFlags.isOut(parameterFlags)
                ? new Long32ArrayParameterConverter(runtime, parameterFlags)
                : new Long32ArrayParameterConverter.Out(runtime, parameterFlags);
    }

    public Long32ArrayParameterConverter(jnr.ffi.Runtime runtime, int parameterFlags) {
        this.runtime = runtime;
        this.parameterFlags = parameterFlags;
    }

    @Override
    public int[] toNative(long[] array, ToNativeContext context) {
        if (array == null) {
            return null;
        }

        int[] primitive = new int[array.length];
        if (ParameterFlags.isIn(parameterFlags)) {
            for (int i = 0; i < array.length; i++) {
                primitive[i] = (int) array[i];
            }
        }

        return primitive;
    }

    public static final class Out extends Long32ArrayParameterConverter implements PostInvocation<long[], int[]> {
        Out(jnr.ffi.Runtime runtime, int parameterFlags) {
            super(runtime, parameterFlags);
        }

        @Override
        public void postInvoke(long[] array, int[] primitive, ToNativeContext context) {
            if (array != null && primitive != null) {
                for (int i = 0; i < array.length; i++) {
                    array[i] = primitive[i];
                }
            }
        }
    }

    @Override
    public Class<int[]> nativeType() {
        return int[].class;
    }
}
