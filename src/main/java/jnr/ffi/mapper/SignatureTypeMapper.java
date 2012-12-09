package jnr.ffi.mapper;

/**

*/
public interface SignatureTypeMapper {
    public FromNativeType getFromNativeType(jnr.ffi.Runtime runtime, SignatureType type, FromNativeContext context);
    public ToNativeType getToNativeType(jnr.ffi.Runtime runtime, SignatureType type, ToNativeContext context);
}
