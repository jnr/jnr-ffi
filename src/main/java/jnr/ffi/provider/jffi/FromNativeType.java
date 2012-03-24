package jnr.ffi.provider.jffi;

import jnr.ffi.NativeType;
import jnr.ffi.mapper.FromNativeConverter;

import java.lang.annotation.Annotation;

/**
 *
 */
class FromNativeType extends SigType {
    final FromNativeConverter fromNativeConverter;

    FromNativeType(Class javaType, NativeType nativeType, Annotation[] annotations, FromNativeConverter fromNativeConverter) {
        super(javaType, nativeType, annotations, fromNativeConverter != null ? fromNativeConverter.nativeType() : javaType);
        this.fromNativeConverter = fromNativeConverter;
    }
}
