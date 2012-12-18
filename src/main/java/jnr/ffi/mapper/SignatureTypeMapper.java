package jnr.ffi.mapper;

/**

*/
public interface SignatureTypeMapper {
    public FromNativeType getFromNativeType(SignatureType type, FromNativeContext context);
    public ToNativeType getToNativeType(SignatureType type, ToNativeContext context);
}
