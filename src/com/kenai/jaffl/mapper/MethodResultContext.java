
package com.kenai.jaffl.mapper;

import java.lang.reflect.Method;

/**
 *
 */
public class MethodResultContext implements FromNativeContext {
    private final Method method;
    public MethodResultContext(Method method) {
        this.method = method;
    }
    public Method getMethod() {
        return method;
    }
}
