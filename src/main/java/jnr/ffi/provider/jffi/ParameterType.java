package jnr.ffi.provider.jffi;

import jnr.ffi.mapper.ToNativeConverter;

import java.lang.annotation.Annotation;

/**
 *
 */
class ParameterType extends SigType {
    final ToNativeConverter toNativeConverter;

    ParameterType(Class javaType, com.kenai.jffi.Type jffiType, Annotation[] annotations, ToNativeConverter toNativeConverter) {
        super(javaType, jffiType, annotations);
        this.toNativeConverter = toNativeConverter;
    }
}
