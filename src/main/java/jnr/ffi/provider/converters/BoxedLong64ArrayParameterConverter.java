package jnr.ffi.provider.converters;

import jnr.ffi.mapper.ToNativeContext;
import jnr.ffi.mapper.ToNativeConverter;
import jnr.ffi.provider.ParameterFlags;

/**
 * Converts a Long[] array to a primitive long[] array parameter
 */
@ToNativeConverter.NoContext
public class BoxedLong64ArrayParameterConverter implements ToNativeConverter<Long[], long[]> {
    private final jnr.ffi.Runtime runtime;
    private final int parameterFlags;

    public static ToNativeConverter<Long[], long[]> getInstance(jnr.ffi.Runtime runtime, int parameterFlags) {
        return !ParameterFlags.isOut(parameterFlags)
                ? new BoxedLong64ArrayParameterConverter(runtime, parameterFlags)
                : new BoxedLong64ArrayParameterConverter.Out(runtime, parameterFlags);
    }

    public BoxedLong64ArrayParameterConverter(jnr.ffi.Runtime runtime, int parameterFlags) {
        this.runtime = runtime;
        this.parameterFlags = parameterFlags;
    }

    @Override
    public long[] toNative(Long[] array, ToNativeContext context) {
        if (array == null) {
            return null;
        }
        long[] primitive = new long[array.length];
        if (ParameterFlags.isIn(parameterFlags)) {
            for (int i = 0; i < array.length; i++) {
                primitive[i] = array[i] != null ? array[i] : 0;
            }
        }

        return primitive;
    }

    public static final class Out extends BoxedLong64ArrayParameterConverter implements PostInvocation<Long[], long[]> {
        Out(jnr.ffi.Runtime runtime, int parameterFlags) {
            super(runtime, parameterFlags);
        }

        @Override
        public void postInvoke(Long[] array, long[] primitive, ToNativeContext context) {
            if (array != null && primitive != null) {
                for (int i = 0; i < array.length; i++) {
                    array[i] = primitive[i];
                }
            }
        }
    }

    @Override
    public Class<long[]> nativeType() {
        return long[].class;
    }
}
