package jnr.ffi.provider.converters;

import jnr.ffi.Pointer;
import jnr.ffi.PointerWrapper;
import jnr.ffi.mapper.ToNativeContext;
import jnr.ffi.mapper.ToNativeConverter;

/**
 * @author Andrew Yefanov.
 * @since 30.08.2017.
 */
public class PointerWrapperToNativeConverter implements ToNativeConverter<PointerWrapper, Pointer> {

    public static final PointerWrapperToNativeConverter INSTANCE = new PointerWrapperToNativeConverter();

    public static ToNativeConverter<PointerWrapper, Pointer> getInstance() {
        return INSTANCE;
    }

    @Override
    public Pointer toNative(PointerWrapper value, ToNativeContext context) {
        return value == null ? null : value.pointer();
    }

    @Override
    public Class<Pointer> nativeType() {
        return Pointer.class;
    }
}