package jnr.ffi.provider.jffi;

import jnr.ffi.mapper.*;

class CompositeTypeMapper implements SignatureTypeMapper {
    private final SignatureTypeMapper[] signatureTypeMappers;

    public CompositeTypeMapper(SignatureTypeMapper... signatureTypeMappers) {
        this.signatureTypeMappers = signatureTypeMappers.clone();
    }

    public FromNativeConverter getFromNativeConverter(SignatureType type, FromNativeContext context) {
        for (SignatureTypeMapper m : signatureTypeMappers) {
            FromNativeConverter converter = m.getFromNativeConverter(type, context);
            if (converter != null) {
                return converter;
            }
        }

        return null;
    }

    public ToNativeConverter getToNativeConverter(SignatureType type, ToNativeContext context) {
        for (SignatureTypeMapper m : signatureTypeMappers) {
            ToNativeConverter converter = m.getToNativeConverter(type, context);
            if (converter != null) {
                return converter;
            }
        }

        return null;
    }
}
