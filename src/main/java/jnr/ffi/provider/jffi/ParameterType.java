package jnr.ffi.provider.jffi;

import jnr.ffi.NativeType;
import jnr.ffi.mapper.ToNativeConverter;

import java.lang.annotation.Annotation;

/**
 *
 */
class ParameterType extends SigType {
    final ToNativeConverter toNativeConverter;

    ParameterType(Class javaType, NativeType nativeType, Annotation[] annotations, ToNativeConverter toNativeConverter) {
        super(javaType, nativeType, annotations, toNativeConverter != null ? toNativeConverter.nativeType() : javaType);
        this.toNativeConverter = toNativeConverter;
    }
}
