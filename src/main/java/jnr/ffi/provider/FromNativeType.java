package jnr.ffi.provider;

import jnr.ffi.NativeType;
import jnr.ffi.mapper.FromNativeContext;
import jnr.ffi.mapper.FromNativeConverter;

import java.lang.annotation.Annotation;
import java.util.Collection;

/**
 *
 */
public class FromNativeType extends SigType implements jnr.ffi.mapper.FromNativeType {
    private final FromNativeConverter fromNativeConverter;
    private final FromNativeContext fromNativeContext;

    public FromNativeType(Class javaType, NativeType nativeType, Collection<Annotation> annotations,
                   FromNativeConverter fromNativeConverter, FromNativeContext fromNativeContext) {
        super(javaType, nativeType, annotations, fromNativeConverter != null ? fromNativeConverter.nativeType() : javaType);
        this.fromNativeConverter = fromNativeConverter;
        this.fromNativeContext = fromNativeContext;
    }

    @Override
    public FromNativeConverter getFromNativeConverter() {
        return fromNativeConverter;
    }

    public FromNativeContext getFromNativeContext() {
        return fromNativeContext;
    }
}
