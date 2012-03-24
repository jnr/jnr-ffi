package jnr.ffi.provider.jffi;

import jnr.ffi.NativeType;
import jnr.ffi.mapper.ToNativeConverter;

import java.lang.annotation.Annotation;

/**
 *
 */
public class ToNativeType extends SigType {
    final ToNativeConverter toNativeConverter;

    ToNativeType(Class javaType, NativeType nativeType, Annotation[] annotations, ToNativeConverter toNativeConverter) {
        super(javaType, nativeType, annotations, toNativeConverter != null ? toNativeConverter.nativeType() : javaType);
        this.toNativeConverter = toNativeConverter;
    }
}
