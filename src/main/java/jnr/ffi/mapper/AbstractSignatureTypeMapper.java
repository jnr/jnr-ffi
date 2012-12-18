package jnr.ffi.mapper;

/**
 *
 */
abstract public class AbstractSignatureTypeMapper implements SignatureTypeMapper {

    @Override
    public FromNativeType getFromNativeType(SignatureType type, FromNativeContext context) {
        return null;
    }

    @Override
    public ToNativeType getToNativeType(SignatureType type, ToNativeContext context) {
        return null;
    }
}
