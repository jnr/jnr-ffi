package jnr.ffi.provider;

import jnr.ffi.NativeType;
import jnr.ffi.mapper.SignatureType;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;

/**
 *
 */
abstract public class SigType implements SignatureType {
    private final Class javaType, convertedType;
    private final Collection<Annotation> annotations;
    private final NativeType nativeType;

    public SigType(Class javaType, NativeType nativeType, Collection<Annotation> annotations, Class convertedType) {
        this.javaType = javaType;
        this.annotations = annotations;
        this.convertedType = convertedType;
        this.nativeType = nativeType;
    }

    public final Class getDeclaredType() {
        return javaType;
    }

    public final Class effectiveJavaType() {
        return convertedType;
    }

    public final Collection<Annotation> annotations() {
        return annotations;
    }

    public final Collection<Annotation> getAnnotations() {
        return annotations;
    }

    @Override
    public Type getGenericType() {
        return getDeclaredType();
    }

    public final String toString() {
        return String.format("declared: %s, effective: %s, native: %s", getDeclaredType(), effectiveJavaType(), getNativeType());
    }

    public NativeType getNativeType() {
        return nativeType;
    }
}
