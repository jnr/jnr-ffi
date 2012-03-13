package jnr.ffi.provider.jffi;

import jnr.ffi.NativeLong;
import jnr.ffi.mapper.FromNativeContext;
import jnr.ffi.mapper.FromNativeConverter;
import jnr.ffi.mapper.ToNativeContext;
import jnr.ffi.mapper.ToNativeConverter;

/**
 * Converts a NativeLong parameter to a 32 bit Integer parameter on ILP32 architectures.
 */
public class NativeLong32Converter implements ToNativeConverter<NativeLong, Integer>, FromNativeConverter<NativeLong, Integer> {
    public static final NativeLong32Converter INSTANCE = new NativeLong32Converter();

    @Override
    public NativeLong fromNative(Integer nativeValue, FromNativeContext context) {
        return new NativeLong(nativeValue.longValue());
    }

    @Override
    public Integer toNative(NativeLong value, ToNativeContext context) {
        return value.intValue();
    }

    @Override
    public Class<Integer> nativeType() {
        return Integer.class;
    }
}
