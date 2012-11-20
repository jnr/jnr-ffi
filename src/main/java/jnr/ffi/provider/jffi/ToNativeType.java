package jnr.ffi.provider.jffi;

import jnr.ffi.NativeType;
import jnr.ffi.mapper.ToNativeContext;
import jnr.ffi.mapper.ToNativeConverter;

import java.lang.annotation.Annotation;

/**
 *
 */
public class ToNativeType extends SigType {
    final ToNativeConverter toNativeConverter;
    final ToNativeContext toNativeContext;

    ToNativeType(Class javaType, NativeType nativeType, Annotation[] annotations,
                 ToNativeConverter toNativeConverter, ToNativeContext toNativeContext) {
        super(javaType, nativeType, annotations, toNativeConverter != null ? toNativeConverter.nativeType() : javaType);
        this.toNativeConverter = toNativeConverter;
        this.toNativeContext = toNativeContext;
    }
}
