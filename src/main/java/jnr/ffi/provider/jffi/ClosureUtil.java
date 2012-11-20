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
import jnr.ffi.util.EnumMapper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import static jnr.ffi.provider.jffi.InvokerUtil.jffiType;

/**
 *
 */
final class ClosureUtil {
    private ClosureUtil() {
    }

    static ToNativeType getResultType(NativeRuntime runtime, Method m, TypeMapper typeMapper) {
        Annotation[] annotations = m.getAnnotations();
        ToNativeConverter converter = getToNativeConverter(m.getReturnType(), annotations, typeMapper);
        Class javaClass = converter != null ? converter.nativeType() : m.getReturnType();
        NativeType nativeType = InvokerUtil.getNativeType(runtime, javaClass, annotations);
        return new ToNativeType(m.getReturnType(), nativeType, annotations, converter, null);
    }

    static FromNativeType getParameterType(NativeRuntime runtime, Method m, int idx, TypeMapper typeMapper) {
        Annotation[] annotations = m.getParameterAnnotations()[idx];
        Class declaredJavaClass = m.getParameterTypes()[idx];
        FromNativeConverter converter = getFromNativeConverter(declaredJavaClass, annotations, typeMapper);
        Class javaClass = converter != null ? converter.nativeType() : declaredJavaClass;
        NativeType nativeType = InvokerUtil.getNativeType(runtime, javaClass, annotations);
        return new FromNativeType(declaredJavaClass, nativeType, annotations, converter, null);
    }

    static com.kenai.jffi.Type getNativeResultType(NativeRuntime runtime, Method m, TypeMapper typeMapper) {
        ToNativeConverter converter = getToNativeConverter(m.getReturnType(), m.getAnnotations(), typeMapper);
        Class javaClass = converter != null ? converter.nativeType() : m.getReturnType();
        return jffiType(InvokerUtil.getNativeType(runtime, m.getReturnType(), m.getAnnotations()));
    }

    static com.kenai.jffi.Type getNativeParameterType(NativeRuntime runtime, Method m, int idx, TypeMapper typeMapper) {
        FromNativeConverter converter = getFromNativeConverter(m.getParameterTypes()[idx], m.getParameterAnnotations()[idx], typeMapper);
        Class javaClass = converter != null ? converter.nativeType() : m.getReturnType();
        return jffiType(InvokerUtil.getNativeType(runtime, m.getParameterTypes()[idx], m.getParameterAnnotations()[idx]));
    }

    @SuppressWarnings("unchecked")
    static FromNativeConverter getFromNativeConverter(Class javaClass, Annotation[] annotations, TypeMapper typeMapper) {
        FromNativeConverter conv = typeMapper.getFromNativeConverter(javaClass);
        if (conv != null) {
            return conv;

        } else if (Enum.class.isAssignableFrom(javaClass)) {
            return EnumMapper.getInstance(javaClass.asSubclass(Enum.class));

        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    static ToNativeConverter getToNativeConverter(Class javaClass, Annotation[] annotations, TypeMapper typeMapper) {
        ToNativeConverter conv = typeMapper.getToNativeConverter(javaClass);
        if (conv != null) {
            return conv;

        } else if (Enum.class.isAssignableFrom(javaClass)) {
            return EnumMapper.getInstance(javaClass.asSubclass(Enum.class));

        } else {
            return null;
        }
    }

}
