package jnr.ffi.provider.jffi;

import jnr.ffi.mapper.*;

/**
 * Adapts a {@link jnr.ffi.mapper.TypeMapper} to a SignatureTypeMapper
 */
public class SignatureTypeMapperAdapter implements SignatureTypeMapper {
    private final TypeMapper typeMapper;

    public SignatureTypeMapperAdapter(TypeMapper typeMapper) {
        this.typeMapper = typeMapper;
    }

    @Override
    public FromNativeConverter getFromNativeConverter(SignatureType type, FromNativeContext context) {
        return typeMapper.getFromNativeConverter(type.getDeclaredType());
    }

    @Override
    public ToNativeConverter getToNativeConverter(SignatureType type, ToNativeContext context) {
        return typeMapper.getToNativeConverter(type.getDeclaredType());
    }
}
