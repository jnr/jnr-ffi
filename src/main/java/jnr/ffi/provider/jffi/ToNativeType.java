package jnr.ffi.provider.jffi;

import jnr.ffi.NativeType;
import jnr.ffi.mapper.FromNativeConverter;
import jnr.ffi.mapper.ToNativeContext;
import jnr.ffi.mapper.ToNativeConverter;

import java.lang.annotation.Annotation;
import java.util.Collection;

/**
 *
 */
public class ToNativeType extends SigType implements jnr.ffi.mapper.ToNativeType {
    final ToNativeConverter toNativeConverter;
    final ToNativeContext toNativeContext;

    ToNativeType(Class javaType, NativeType nativeType, Collection<Annotation> annotations,
                 ToNativeConverter toNativeConverter, ToNativeContext toNativeContext) {
        super(javaType, nativeType, annotations, toNativeConverter != null ? toNativeConverter.nativeType() : javaType);
        this.toNativeConverter = toNativeConverter;
        this.toNativeContext = toNativeContext;
    }

    @Override
    public ToNativeConverter getToNativeConverter() {
        return toNativeConverter;
    }
}
