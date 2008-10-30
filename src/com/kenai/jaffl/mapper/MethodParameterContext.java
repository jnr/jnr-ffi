
package com.kenai.jaffl.mapper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Holds context for a method parameter java->native conversion.
 */
public final class MethodParameterContext implements ToNativeContext {
    private final Method method;
    private final int parameterIndex;
    private final Annotation[] annotations;
    public MethodParameterContext(Method method, int parameterIndex) {
        this.method = method;
        this.parameterIndex = parameterIndex;
        this.annotations = method.getParameterAnnotations()[parameterIndex];
    }
    public Method getMethod() {
        return method;
    }
    public int getParameterIndex() {
        return parameterIndex;
    }
}
