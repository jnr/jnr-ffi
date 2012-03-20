package jnr.ffi.provider.jffi;

import jnr.ffi.NativeType;

import java.lang.annotation.Annotation;

/**
 *
 */
abstract class SigType {
    final Class javaType, convertedType;
    final com.kenai.jffi.Type jffiType;
    final Annotation[] annotations;

    SigType(Class javaType, com.kenai.jffi.Type jffiType, Annotation[] annotations) {
        this.javaType = javaType;
        this.convertedType = javaType;
        this.jffiType = jffiType;
        this.annotations = annotations.clone();
    }

    SigType(Class javaType, com.kenai.jffi.Type jffiType, Annotation[] annotations, Class convertedType) {
        this.javaType = javaType;
        this.jffiType = jffiType;
        this.annotations = annotations.clone();
        this.convertedType = convertedType;
    }

    final Class getDeclaredType() {
        return javaType;
    }

    final Class effectiveJavaType() {
        return convertedType;
    }
}
