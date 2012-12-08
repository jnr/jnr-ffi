package jnr.ffi.provider.converters;

import jnr.ffi.NativeLong;

/**
 * Parameter and return type support for the old NativeLong type
 */
import jnr.ffi.mapper.*;

@ToNativeConverter.NoContext
@FromNativeConverter.NoContext
@ToNativeConverter.Cacheable
@FromNativeConverter.Cacheable
public final class NativeLongConverter extends AbstractDataConverter<NativeLong, Long> {
    public static final DataConverter INSTANCE = new NativeLongConverter();

    public Class<Long> nativeType() {
        return Long.class;
    }

    public Long toNative(NativeLong value, ToNativeContext toNativeContext) {
        return value.longValue();
    }

    public NativeLong fromNative(Long value, FromNativeContext fromNativeContext) {
        return NativeLong.valueOf(value);
    }
}
