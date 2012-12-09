package jnr.ffi.mapper;

/**
 *
 */
abstract public class AbstractSignatureTypeMapper implements SignatureTypeMapper {

    @Override
    public FromNativeType getFromNativeType(jnr.ffi.Runtime runtime, SignatureType type, FromNativeContext context) {
        return null;
    }

    @Override
    public ToNativeType getToNativeType(jnr.ffi.Runtime runtime, SignatureType type, ToNativeContext context) {
        return null;
    }
}
