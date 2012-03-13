package jnr.ffi.provider.jffi;

import jnr.ffi.mapper.FromNativeContext;
import jnr.ffi.mapper.FromNativeConverter;
import jnr.ffi.mapper.ToNativeContext;
import jnr.ffi.mapper.ToNativeConverter;

/**
 * Converts a java Long parameter to a 32 bit Integer parameter on ILP32 architectures.
 */
class BoxedLong32Converter implements ToNativeConverter<Long, Integer>, FromNativeConverter<Long, Integer> {
    public static final BoxedLong32Converter INSTANCE = new BoxedLong32Converter();

    @Override
    public Long fromNative(Integer nativeValue, FromNativeContext context) {
        return nativeValue.longValue();
    }

    @Override
    public Integer toNative(Long value, ToNativeContext context) {
        return value.intValue();
    }

    @Override
    public Class<Integer> nativeType() {
        return Integer.class;
    }
}
