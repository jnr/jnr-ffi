package jnr.ffi.provider.jffi;

import com.kenai.jffi.ArrayFlags;
import com.kenai.jffi.ObjectParameterInfo;
import jnr.ffi.mapper.ToNativeConverter;
import jnr.ffi.provider.ParameterFlags;

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

    Class effectiveJavaType() {
        return toNativeConverter != null ? toNativeConverter.nativeType() : javaType;
    }
}
