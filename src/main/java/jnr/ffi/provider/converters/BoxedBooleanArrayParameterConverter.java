package jnr.ffi.provider.converters;

import jnr.ffi.mapper.ToNativeContext;
import jnr.ffi.mapper.ToNativeConverter;
import jnr.ffi.provider.ParameterFlags;

/**
 * Converts a Boolean[] array to a primitive boolean[] array parameter
 */
@ToNativeConverter.NoContext
public class BoxedBooleanArrayParameterConverter implements ToNativeConverter<Boolean[], boolean[]> {
    private final jnr.ffi.Runtime runtime;
    private final int parameterFlags;

    public static ToNativeConverter<Boolean[], boolean[]> getInstance(jnr.ffi.Runtime runtime, int parameterFlags) {
        return !ParameterFlags.isOut(parameterFlags)
                ? new BoxedBooleanArrayParameterConverter(runtime, parameterFlags)
                : new BoxedBooleanArrayParameterConverter.Out(runtime, parameterFlags);
    }

    public BoxedBooleanArrayParameterConverter(jnr.ffi.Runtime runtime, int parameterFlags) {
        this.runtime = runtime;
        this.parameterFlags = parameterFlags;
    }

    @Override
    public boolean[] toNative(Boolean[] array, ToNativeContext context) {
        if (array == null) {
            return null;
        }
        boolean[] primitive = new boolean[array.length];
        if (ParameterFlags.isIn(parameterFlags)) {
            for (int i = 0; i < array.length; i++) {
                primitive[i] = array[i] != null ? array[i] : false;
            }
        }

        return primitive;
    }

    public static final class Out extends BoxedBooleanArrayParameterConverter implements PostInvocation<Boolean[], boolean[]> {
        Out(jnr.ffi.Runtime runtime, int parameterFlags) {
            super(runtime, parameterFlags);
        }

        @Override
        public void postInvoke(Boolean[] array, boolean[] primitive, ToNativeContext context) {
            if (array != null && primitive != null) {
                for (int i = 0; i < array.length; i++) {
                    array[i] = primitive[i];
                }
            }
        }
    }

    @Override
    public Class<boolean[]> nativeType() {
        return boolean[].class;
    }
}
