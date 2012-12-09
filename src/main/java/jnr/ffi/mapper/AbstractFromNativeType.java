package jnr.ffi.mapper;

/**
 *
 */
abstract public class AbstractFromNativeType implements FromNativeType {
    private final FromNativeConverter converter;

    AbstractFromNativeType(FromNativeConverter converter) {
        this.converter = converter;
    }

    @Override
    public FromNativeConverter getFromNativeConverter() {
        return converter;
    }

}
