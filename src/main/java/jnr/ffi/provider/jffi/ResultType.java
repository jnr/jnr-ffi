package jnr.ffi.provider.jffi;

import jnr.ffi.NativeType;
import jnr.ffi.mapper.FromNativeContext;
import jnr.ffi.mapper.FromNativeConverter;

import java.lang.annotation.Annotation;

/**
 *
 */
class ResultType extends FromNativeType {

    ResultType(Class javaType, NativeType nativeType, Annotation[] annotations,
               FromNativeConverter fromNativeConverter, FromNativeContext fromNativeContext) {
        super(javaType, nativeType, annotations, fromNativeConverter, fromNativeContext);
    }
}
