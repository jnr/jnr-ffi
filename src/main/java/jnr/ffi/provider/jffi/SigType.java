package jnr.ffi.provider.jffi;

import jnr.ffi.NativeType;

import java.lang.annotation.Annotation;

/**
 *
 */
abstract class SigType {
    final Class javaType;
    final com.kenai.jffi.Type jffiType;
    final Annotation[] annotations;

    SigType(Class javaType, com.kenai.jffi.Type jffiType, Annotation[] annotations) {
        this.javaType = javaType;
        this.jffiType = jffiType;
        this.annotations = annotations.clone();
    }
}
