package jnr.ffi.mapper;

/**

*/
public interface SignatureTypeMapper {
    public FromNativeConverter getFromNativeConverter(SignatureType type, FromNativeContext context);
    public ToNativeConverter getToNativeConverter(SignatureType type, ToNativeContext context);
}
