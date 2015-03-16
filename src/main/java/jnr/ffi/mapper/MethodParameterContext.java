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
import java.util.Collection;

import static jnr.ffi.util.Annotations.sortedAnnotationCollection;

/**
 * Holds context for a method parameter from Java to native conversion.
 */
public final class MethodParameterContext implements ToNativeContext {
    private final jnr.ffi.Runtime runtime;
    private final Method method;
    private final int parameterIndex;
    private Collection<Annotation> annotations;
    private Annotation[] annotationArray;

    public MethodParameterContext(jnr.ffi.Runtime runtime, Method method, int parameterIndex) {
        this.runtime = runtime;
        this.method = method;
        this.parameterIndex = parameterIndex;
    }

    public MethodParameterContext(jnr.ffi.Runtime runtime, Method method, int parameterIndex, Annotation[] annotationArray) {
        this.runtime = runtime;
        this.method = method;
        this.parameterIndex = parameterIndex;
        this.annotationArray = annotationArray.clone();
    }

    public MethodParameterContext(jnr.ffi.Runtime runtime, Method method, int parameterIndex, Collection<Annotation> annotations) {
        this.runtime = runtime;
        this.method = method;
        this.parameterIndex = parameterIndex;
        this.annotations = sortedAnnotationCollection(annotations);
    }

    public Method getMethod() {
        return method;
    }

    public int getParameterIndex() {
        return parameterIndex;
    }

    public Collection<Annotation> getAnnotations() {
        return annotations != null ? annotations : buildAnnotationCollection();
    }

    public jnr.ffi.Runtime getRuntime() {
        return runtime;
    }

    private Collection<Annotation> buildAnnotationCollection() {
        if (annotationArray != null) {
            return annotations = sortedAnnotationCollection(annotationArray);
        } else {
            return annotations = sortedAnnotationCollection(annotationArray = method.getParameterAnnotations()[parameterIndex]);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MethodParameterContext that = (MethodParameterContext) o;

        return parameterIndex == that.parameterIndex
                && method.equals(that.method)
                && getAnnotations().equals(that.getAnnotations());
    }

    @Override
    public int hashCode() {
        int result = method.hashCode();
        result = 31 * result + parameterIndex;
        result = 31 * result + getAnnotations().hashCode();
        return result;
    }
}
