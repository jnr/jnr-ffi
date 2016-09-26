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
import jnr.ffi.annotations.Delegate;
import jnr.ffi.mapper.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Collection;

import static jnr.ffi.util.Annotations.sortedAnnotationCollection;

/**
 *
 */
final class ClosureUtil {
    private ClosureUtil() {
    }

    static jnr.ffi.provider.ToNativeType getResultType(jnr.ffi.Runtime runtime, Method m, SignatureTypeMapper typeMapper) {
        Collection<Annotation> annotations = sortedAnnotationCollection(m.getAnnotations());
        ToNativeContext context = new SimpleNativeContext(runtime, annotations);
        SignatureType signatureType = DefaultSignatureType.create(m.getReturnType(), context);
        jnr.ffi.mapper.ToNativeType toNativeType = typeMapper.getToNativeType(signatureType, context);
        ToNativeConverter converter = toNativeType != null ? toNativeType.getToNativeConverter() : null;
        Class javaClass = converter != null ? converter.nativeType() : m.getReturnType();
        NativeType nativeType = Types.getType(runtime, javaClass, annotations).getNativeType();
        return new jnr.ffi.provider.ToNativeType(m.getReturnType(), nativeType, annotations, converter, context);
    }

    static jnr.ffi.provider.FromNativeType getParameterType(jnr.ffi.Runtime runtime, Method m, int idx, SignatureTypeMapper typeMapper) {
        Collection<Annotation> annotations = sortedAnnotationCollection(m.getParameterAnnotations()[idx]);
        Class declaredJavaClass = m.getParameterTypes()[idx];
        FromNativeContext context = new SimpleNativeContext(runtime, annotations);
        SignatureType signatureType = new DefaultSignatureType(declaredJavaClass, context.getAnnotations(), m.getGenericParameterTypes()[idx]);
        jnr.ffi.mapper.FromNativeType fromNativeType = typeMapper.getFromNativeType(signatureType, context);
        FromNativeConverter converter = fromNativeType != null ? fromNativeType.getFromNativeConverter() : null;
        Class javaClass = converter != null ? converter.nativeType() : declaredJavaClass;
        NativeType nativeType = Types.getType(runtime, javaClass, annotations).getNativeType();
        return new jnr.ffi.provider.FromNativeType(declaredJavaClass, nativeType, annotations, converter, context);
    }


    static Method getDelegateMethod(Class closureClass) {
        Method callMethod = null;
        for (Method m : closureClass.getMethods()) {
            if (m.isAnnotationPresent(Delegate.class) && Modifier.isPublic(m.getModifiers())
                    && !Modifier.isStatic(m.getModifiers())) {
                callMethod = m;
                break;
            }
        }
        if (callMethod == null) {
            throw new NoSuchMethodError("no public non-static delegate method defined in " + closureClass.getName());
        }

        return callMethod;
    }
}
