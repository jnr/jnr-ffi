package jnr.ffi.provider.converters;

import jnr.ffi.mapper.ToNativeContext;
import jnr.ffi.mapper.ToNativeConverter;
import jnr.ffi.provider.ParameterFlags;

/**
 * Converts a Short[] array to a primitive short[] array parameter
 */
@ToNativeConverter.NoContext
public class BoxedShortArrayParameterConverter implements ToNativeConverter<Short[], short[]> {
    private final jnr.ffi.Runtime runtime;
    private final int parameterFlags;

    public static ToNativeConverter<Short[], short[]> getInstance(jnr.ffi.Runtime runtime, int parameterFlags) {
        return !ParameterFlags.isOut(parameterFlags)
                ? new BoxedShortArrayParameterConverter(runtime, parameterFlags)
                : new BoxedShortArrayParameterConverter.Out(runtime, parameterFlags);
    }

    public BoxedShortArrayParameterConverter(jnr.ffi.Runtime runtime, int parameterFlags) {
        this.runtime = runtime;
        this.parameterFlags = parameterFlags;
    }

    @Override
    public short[] toNative(Short[] array, ToNativeContext context) {
        if (array == null) {
            return null;
        }
        short[] primitive = new short[array.length];
        if (ParameterFlags.isIn(parameterFlags)) {
            for (int i = 0; i < array.length; i++) {
                primitive[i] = array[i] != null ? array[i] : 0;
            }
        }

        return primitive;
    }

    public static final class Out extends BoxedShortArrayParameterConverter implements PostInvocation<Short[], short[]> {
        Out(jnr.ffi.Runtime runtime, int parameterFlags) {
            super(runtime, parameterFlags);
        }

        @Override
        public void postInvoke(Short[] array, short[] primitive, ToNativeContext context) {
            if (array != null && primitive != null) {
                for (int i = 0; i < array.length; i++) {
                    array[i] = primitive[i];
                }
            }
        }
    }

    @Override
    public Class<short[]> nativeType() {
        return short[].class;
    }
}
