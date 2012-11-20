package jnr.ffi.provider.jffi;

import jnr.ffi.NativeType;
import jnr.ffi.mapper.ToNativeConverter;

import java.lang.annotation.Annotation;

/**
 *
 */
class ParameterType extends ToNativeType {

    ParameterType(Class javaType, NativeType nativeType, Annotation[] annotations, ToNativeConverter toNativeConverter) {
        super(javaType, nativeType, annotations, toNativeConverter);
    }
}
