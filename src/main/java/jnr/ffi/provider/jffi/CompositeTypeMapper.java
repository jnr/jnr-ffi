package jnr.ffi.provider.jffi;

import jnr.ffi.mapper.*;
import jnr.ffi.mapper.FromNativeType;
import jnr.ffi.mapper.ToNativeType;

class CompositeTypeMapper implements SignatureTypeMapper {
    private final SignatureTypeMapper[] signatureTypeMappers;

    public CompositeTypeMapper(SignatureTypeMapper... signatureTypeMappers) {
        this.signatureTypeMappers = signatureTypeMappers.clone();
    }

    @Override
    public FromNativeType getFromNativeType(jnr.ffi.Runtime runtime, SignatureType type, FromNativeContext context) {
        for (SignatureTypeMapper m : signatureTypeMappers) {
            FromNativeType fromNativeType = m.getFromNativeType(runtime, type, context);
            if (fromNativeType != null) {
                return fromNativeType;
            }
        }

        return null;
    }

    @Override
    public ToNativeType getToNativeType(jnr.ffi.Runtime runtime, SignatureType type, ToNativeContext context) {
        for (SignatureTypeMapper m : signatureTypeMappers) {
            ToNativeType toNativeType = m.getToNativeType(runtime, type, context);
            if (toNativeType != null) {
                return toNativeType;
            }
        }

        return null;
    }
}
