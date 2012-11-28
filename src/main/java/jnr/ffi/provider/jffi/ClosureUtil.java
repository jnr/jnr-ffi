/*
 * Copyright (C) 2011 Wayne Meissner
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
package jnr.ffi.provider.jffi;

import jnr.ffi.NativeType;
import jnr.ffi.mapper.FromNativeConverter;
import jnr.ffi.mapper.ToNativeConverter;
import jnr.ffi.mapper.TypeMapper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;

import static jnr.ffi.provider.jffi.InvokerUtil.annotationCollection;

/**
 *
 */
final class ClosureUtil {
    private ClosureUtil() {
    }

    static ToNativeType getResultType(NativeRuntime runtime, Method m, TypeMapper typeMapper) {
        Collection<Annotation> annotations = annotationCollection(m.getAnnotations());
        ToNativeConverter converter = typeMapper.getToNativeConverter(m.getReturnType(), new SimpleNativeContext(annotations));
        Class javaClass = converter != null ? converter.nativeType() : m.getReturnType();
        NativeType nativeType = InvokerUtil.getNativeType(runtime, javaClass, annotations);
        return new ToNativeType(m.getReturnType(), nativeType, annotations, converter, null);
    }

    static FromNativeType getParameterType(NativeRuntime runtime, Method m, int idx, TypeMapper typeMapper) {
        Collection<Annotation> annotations = annotationCollection(m.getParameterAnnotations()[idx]);
        Class declaredJavaClass = m.getParameterTypes()[idx];
        FromNativeConverter converter = typeMapper.getFromNativeConverter(declaredJavaClass, new SimpleNativeContext(annotations));
        Class javaClass = converter != null ? converter.nativeType() : declaredJavaClass;
        NativeType nativeType = InvokerUtil.getNativeType(runtime, javaClass, annotations);
        return new FromNativeType(declaredJavaClass, nativeType, annotations, converter, null);
    }
}
