package jnr.ffi.mapper;

/**
 * Adapts a {@link jnr.ffi.mapper.TypeMapper} to a SignatureTypeMapper
 */
public class SignatureTypeMapperAdapter implements SignatureTypeMapper {
    private final TypeMapper typeMapper;

    public SignatureTypeMapperAdapter(TypeMapper typeMapper) {
        this.typeMapper = typeMapper;
    }

    @Override
    public FromNativeType getFromNativeType(SignatureType type, FromNativeContext context) {
        return FromNativeTypes.create(typeMapper.getFromNativeConverter(type.getDeclaredType()));
    }

    @Override
    public ToNativeType getToNativeType(SignatureType type, ToNativeContext context) {
        return ToNativeTypes.create(typeMapper.getToNativeConverter(type.getDeclaredType()));
    }
}
