package jnr.ffi.provider.jffi;

import jnr.ffi.NativeType;

import java.lang.annotation.Annotation;

import static jnr.ffi.provider.jffi.NumberUtil.sizeof;

/**
 *
 */
abstract class SigType {
    private final Class javaType, convertedType;
    final NativeType nativeType;
    final Annotation[] annotations;

    SigType(Class javaType, NativeType nativeType, Annotation[] annotations, Class convertedType) {
        this.javaType = javaType;
        this.annotations = annotations.clone();
        this.convertedType = convertedType;
        this.nativeType = nativeType;
    }

    final Class getDeclaredType() {
        return javaType;
    }

    final Class effectiveJavaType() {
        return convertedType;
    }

    final int size() {
        return sizeof(nativeType);
    }
}
