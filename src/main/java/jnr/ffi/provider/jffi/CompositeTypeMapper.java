package jnr.ffi.provider.jffi;

import jnr.ffi.mapper.*;

class CompositeTypeMapper implements TypeMapper {
    private final TypeMapper[] mappers;

    public CompositeTypeMapper(TypeMapper... mappers) {
        this.mappers = mappers.clone();
    }

    public FromNativeConverter getFromNativeConverter(Class type, FromNativeContext context) {
        for (TypeMapper m : mappers) {
            FromNativeConverter converter = m.getFromNativeConverter(type, context);
            if (converter != null) {
                return converter;
            }
        }

        return null;
    }

    public ToNativeConverter getToNativeConverter(Class type, ToNativeContext context) {
        for (TypeMapper m : mappers) {
            ToNativeConverter converter = m.getToNativeConverter(type, context);
            if (converter != null) {
                return converter;
            }
        }

        return null;
    }
}
