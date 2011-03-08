/*
 * Copyright (C) 2008-2010 Wayne Meissner
 *
 * This file is part of the JNR project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jnr.ffi.mapper;

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
