package jnr.ffi.mapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public final class CompositeTypeMapper implements SignatureTypeMapper {
    private final Collection<SignatureTypeMapper> signatureTypeMappers;

    public CompositeTypeMapper(SignatureTypeMapper... signatureTypeMappers) {
        this.signatureTypeMappers = Collections.unmodifiableList(Arrays.asList(signatureTypeMappers.clone()));
    }

    public CompositeTypeMapper(Collection<SignatureTypeMapper> signatureTypeMappers) {
        this.signatureTypeMappers = Collections.unmodifiableList(new ArrayList<SignatureTypeMapper>(signatureTypeMappers));
    }

    @Override
    public FromNativeType getFromNativeType(SignatureType type, FromNativeContext context) {
        for (SignatureTypeMapper m : signatureTypeMappers) {
            FromNativeType fromNativeType = m.getFromNativeType(type, context);
            if (fromNativeType != null) {
                return fromNativeType;
            }
        }

        return null;
    }

    @Override
    public ToNativeType getToNativeType(SignatureType type, ToNativeContext context) {
        for (SignatureTypeMapper m : signatureTypeMappers) {
            ToNativeType toNativeType = m.getToNativeType(type, context);
            if (toNativeType != null) {
                return toNativeType;
            }
        }

        return null;
    }
}
