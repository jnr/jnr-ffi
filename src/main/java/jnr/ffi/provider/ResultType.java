package jnr.ffi.provider;

import jnr.ffi.NativeType;
import jnr.ffi.mapper.FromNativeContext;
import jnr.ffi.mapper.FromNativeConverter;

import java.lang.annotation.Annotation;
import java.util.Collection;

/**
 *
 */
public class ResultType extends FromNativeType {

    public ResultType(Class javaType, NativeType nativeType, Collection<Annotation> annotations,
               FromNativeConverter fromNativeConverter, FromNativeContext fromNativeContext) {
        super(javaType, nativeType, annotations, fromNativeConverter, fromNativeContext);
    }
}
