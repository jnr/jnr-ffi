package jnr.ffi.provider.converters;

import jnr.ffi.mapper.ToNativeContext;
import jnr.ffi.mapper.ToNativeConverter;
import jnr.ffi.provider.ParameterFlags;

/**
 * Converts a Long[] array to a primitive int[] array parameter
 */
@ToNativeConverter.NoContext
public class BoxedLong32ArrayParameterConverter implements ToNativeConverter<Long[], int[]> {
    private final jnr.ffi.Runtime runtime;
    private final int parameterFlags;

    public static ToNativeConverter<Long[], int[]> getInstance(jnr.ffi.Runtime runtime, int parameterFlags) {
        return !ParameterFlags.isOut(parameterFlags)
                ? new BoxedLong32ArrayParameterConverter(runtime, parameterFlags)
                : new BoxedLong32ArrayParameterConverter.Out(runtime, parameterFlags);
    }

    public BoxedLong32ArrayParameterConverter(jnr.ffi.Runtime runtime, int parameterFlags) {
        this.runtime = runtime;
        this.parameterFlags = parameterFlags;
    }

    @Override
    public int[] toNative(Long[] array, ToNativeContext context) {
        if (array == null) {
            return null;
        }
        int[] primitive = new int[array.length];
        if (ParameterFlags.isIn(parameterFlags)) {
            for (int i = 0; i < array.length; i++) {
                primitive[i] = array[i] != null ? array[i].intValue() : 0;
            }
        }

        return primitive;
    }

    public static final class Out extends BoxedLong32ArrayParameterConverter implements PostInvocation<Long[], int[]> {
        Out(jnr.ffi.Runtime runtime, int parameterFlags) {
            super(runtime, parameterFlags);
        }

        @Override
        public void postInvoke(Long[] array, int[] primitive, ToNativeContext context) {
            if (array != null && primitive != null) {
                for (int i = 0; i < array.length; i++) {
                    array[i] = (long) primitive[i];
                }
            }
        }
    }

    @Override
    public Class<int[]> nativeType() {
        return int[].class;
    }
}
