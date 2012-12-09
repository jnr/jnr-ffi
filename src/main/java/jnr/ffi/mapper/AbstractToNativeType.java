package jnr.ffi.mapper;

/**
 *
 */
abstract public class AbstractToNativeType implements ToNativeType {
    private final ToNativeConverter converter;

    AbstractToNativeType(ToNativeConverter converter) {
        this.converter = converter;
    }

    @Override
    public ToNativeConverter getToNativeConverter() {
        return converter;
    }

}
