/*
 * Copyright (C) 2013 Wayne Meissner
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

import jnr.ffi.mapper.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class AnnotationTypeMapper extends AbstractSignatureTypeMapper implements SignatureTypeMapper {
    @Override
    public FromNativeType getFromNativeType(SignatureType type, FromNativeContext context) {
        Method fromNativeMethod = findMethodWithAnnotation(type, FromNativeConverter.FromNative.class);
        if (fromNativeMethod == null) {
            return null;
        }
        
        if (!Modifier.isStatic(fromNativeMethod.getModifiers())) {
            throw new IllegalArgumentException(fromNativeMethod.getDeclaringClass().getName() + "." 
                    + fromNativeMethod.getName() + " should be declared static");
        }
        
        return FromNativeTypes.create(new ReflectionFromNativeConverter(fromNativeMethod, 
                fromNativeMethod.getAnnotation(FromNativeConverter.FromNative.class).nativeType()));
    }

    @Override
    public ToNativeType getToNativeType(SignatureType type, ToNativeContext context) {
        Method toNativeMethod = findMethodWithAnnotation(type, ToNativeConverter.ToNative.class);
        if (toNativeMethod == null) {
            return null;
        }

        if (!Modifier.isStatic(toNativeMethod.getModifiers())) {
            throw new IllegalArgumentException(toNativeMethod.getDeclaringClass().getName() + "." 
                    + toNativeMethod.getName() + " should be declared static");
        }

        return ToNativeTypes.create(new ReflectionToNativeConverter(toNativeMethod,
                toNativeMethod.getAnnotation(ToNativeConverter.ToNative.class).nativeType()));
    }
    
    private static Method findMethodWithAnnotation(SignatureType type, Class<? extends Annotation> annotationClass) {
        for (Class klass = type.getDeclaredType(); klass != null && klass != Object.class; klass = klass.getSuperclass()) {
            for (Method m : klass.getDeclaredMethods()) {
                if (m.isAnnotationPresent(annotationClass)) {
                    return m;
                }
            }
        }
        
        return null;
    }

    
    public abstract class AbstractReflectionConverter {
        protected final Method method;
        protected final Class nativeType;

        public AbstractReflectionConverter(Method method, Class nativeType) {
            this.method = method;
            this.nativeType = nativeType;
        }

        protected final Object invoke(Object value, Object context) {
            try {
                return method.invoke(method.getDeclaringClass(), value, context);

            } catch (IllegalAccessException iae) {
                throw new RuntimeException(iae);
            } catch (InvocationTargetException ite) {
                throw new RuntimeException(ite);
            }
        }

        public final Class<Object> nativeType() {
            return nativeType;
        }
    }

    @FromNativeConverter.Cacheable
    public final class ReflectionFromNativeConverter extends AbstractReflectionConverter implements FromNativeConverter<Object, Object> {
        public ReflectionFromNativeConverter(Method method, Class nativeType) {
            super(method, nativeType);
        }

        @Override
        public Object fromNative(Object nativeValue, FromNativeContext context) {
            return invoke(nativeValue, context);
        }
    }

    @ToNativeConverter.Cacheable
    public final class ReflectionToNativeConverter extends AbstractReflectionConverter implements ToNativeConverter<Object, Object> {
        public ReflectionToNativeConverter(Method method, Class nativeType) {
            super(method, nativeType);
        }

        @Override
        public Object toNative(Object nativeValue, ToNativeContext context) {
            return invoke(nativeValue, context);
        }
    }
}
