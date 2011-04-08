package jnr.ffi.provider.jffi;

import com.kenai.jffi.CallingConvention;

import java.lang.annotation.Annotation;

/**
 *
 */
class Signature {
    final Class resultType;
    final Class[] parameterTypes;
    final Annotation[] resultAnnotations;
    final Annotation[][] parameterAnnotations;
    CallingConvention callingConvention;
    boolean ignoreError;

    public Signature(Class resultType, Class[] parameterTypes, Annotation[] resultAnnotations,
            Annotation[][] parameterAnnotations, CallingConvention callingConvention, boolean ignoreError) {
        this.resultType = resultType;
        this.parameterTypes = parameterTypes;
        this.resultAnnotations = resultAnnotations;
        this.parameterAnnotations = parameterAnnotations;
        this.callingConvention = callingConvention;
        this.ignoreError = ignoreError;
    }
}
