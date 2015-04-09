/*
 * Copyright (C) 2012 Wayne Meissner
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
import java.lang.reflect.Type;
import java.util.Collection;

import static jnr.ffi.util.Annotations.sortedAnnotationCollection;

/**
*
*/
public final class DefaultSignatureType implements SignatureType {
    private final Class declaredClass;
    private final Collection<Annotation> annotations;
    private final Type genericType;

    public DefaultSignatureType(Class declaredClass, Collection<Annotation> annotations, Type genericType) {
        this.declaredClass = declaredClass;
        this.annotations = sortedAnnotationCollection(annotations);
        this.genericType = genericType;
    }

    public Class getDeclaredType() {
        return declaredClass;
    }

    public Collection<Annotation> getAnnotations() {
        return annotations;
    }

    public Type getGenericType() {
        return genericType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DefaultSignatureType signature = (DefaultSignatureType) o;

        return declaredClass == signature.declaredClass
                && genericType.equals(signature.genericType)
                && annotations.equals(signature.annotations)
                ;
    }

    @Override
    public int hashCode() {
        int result = declaredClass.hashCode();
        result = 31 * result + annotations.hashCode();
        if (genericType != null) result = 31 * result + genericType.hashCode();
        return result;
    }

    public static DefaultSignatureType create(Class type, FromNativeContext context) {
        Type genericType = !type.isPrimitive() && context instanceof MethodResultContext
                ? ((MethodResultContext) context).getMethod().getGenericReturnType() : type;
        return new DefaultSignatureType(type, context.getAnnotations(), genericType);
    }

    public static DefaultSignatureType create(Class type, ToNativeContext context) {
        Type genericType = type;
        if (!type.isPrimitive() && context instanceof MethodParameterContext) {
            MethodParameterContext methodParameterContext = (MethodParameterContext) context;
            genericType = methodParameterContext.getMethod().getGenericParameterTypes()[methodParameterContext.getParameterIndex()];
        }

        return new DefaultSignatureType(type, context.getAnnotations(), genericType);
    }
}
