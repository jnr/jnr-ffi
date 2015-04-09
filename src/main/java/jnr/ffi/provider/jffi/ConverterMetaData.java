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

import jnr.ffi.mapper.FromNativeContext;
import jnr.ffi.mapper.FromNativeConverter;
import jnr.ffi.mapper.ToNativeContext;
import jnr.ffi.mapper.ToNativeConverter;
import jnr.ffi.util.Annotations;

import java.lang.annotation.Annotation;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.reflect.Method;
import java.util.*;

/**
 *
 */
class ConverterMetaData {
    private static volatile Reference<Map<Class, ConverterMetaData>> cacheReference;
    final Collection<Annotation> classAnnotations;
    final Collection<Annotation> toNativeMethodAnnotations, fromNativeMethodAnnotations, nativeTypeMethodAnnotations;
    final Collection<Annotation> toNativeAnnotations, fromNativeAnnotations;

    ConverterMetaData(Class converterClass, Class nativeType) {
        classAnnotations = Annotations.sortedAnnotationCollection(converterClass.getAnnotations());
        nativeTypeMethodAnnotations = getConverterMethodAnnotations(converterClass, "nativeType");
        fromNativeMethodAnnotations = getConverterMethodAnnotations(converterClass, "fromNative", nativeType, FromNativeContext.class);
        toNativeMethodAnnotations = getConverterMethodAnnotations(converterClass, "toNative", nativeType, ToNativeContext.class);;
        toNativeAnnotations = Annotations.mergeAnnotations(classAnnotations, toNativeMethodAnnotations, nativeTypeMethodAnnotations);
        fromNativeAnnotations = Annotations.mergeAnnotations(classAnnotations, fromNativeMethodAnnotations, nativeTypeMethodAnnotations);
    }

    
    private static Collection<Annotation> getToNativeMethodAnnotations(Class converterClass, Class resultClass) {
        try {
            final Method baseMethod = converterClass.getMethod("toNative", Object.class, ToNativeContext.class);
            for (Method m : converterClass.getMethods()) {
                if (!m.getName().equals("toNative")) {
                    continue;
                }
                if (!resultClass.isAssignableFrom(m.getReturnType())) {
                    continue;
                }

                Class[] methodParameterTypes = m.getParameterTypes();
                if (methodParameterTypes.length != 2 || !methodParameterTypes[1].isAssignableFrom(ToNativeContext.class)) {
                    continue;
                }

                return Annotations.mergeAnnotations(Annotations.sortedAnnotationCollection(m.getAnnotations()), Annotations.sortedAnnotationCollection(baseMethod.getAnnotations()));
            }

            return Annotations.EMPTY_ANNOTATIONS;
        } catch (SecurityException se) {
            return Annotations.EMPTY_ANNOTATIONS;
        } catch (NoSuchMethodException ignored) {
            return Annotations.EMPTY_ANNOTATIONS;
        }
    }

    @SuppressWarnings("unchecked")
    private static Collection<Annotation> getConverterMethodAnnotations(Class converterClass, String methodName, Class... parameterClasses) {
        try {
            return Annotations.sortedAnnotationCollection(converterClass.getMethod(methodName).getAnnotations());
        } catch (NoSuchMethodException ignored) {
            return Annotations.EMPTY_ANNOTATIONS;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
    
    private static ConverterMetaData getMetaData(Class converterClass, Class nativeType) {
        Map<Class, ConverterMetaData> cache = cacheReference != null ? cacheReference.get() : null;
        ConverterMetaData metaData;
        if (cache != null && (metaData = cache.get(converterClass)) != null) {
            return metaData;
        }

        return addMetaData(converterClass, nativeType);
    }

    private static synchronized ConverterMetaData addMetaData(Class converterClass, Class nativeType) {
        Map<Class, ConverterMetaData> cache = cacheReference != null ? cacheReference.get() : null;
        ConverterMetaData metaData;
        if (cache != null && (metaData = cache.get(converterClass)) != null) {
            return metaData;
        }

        Map<Class, ConverterMetaData> m = new HashMap<Class, ConverterMetaData>(cache != null ? cache : Collections.EMPTY_MAP);
        m.put(converterClass, metaData = new ConverterMetaData(converterClass, nativeType));
        cacheReference = new SoftReference<Map<Class, ConverterMetaData>>(cache = new IdentityHashMap<Class, ConverterMetaData>(m));

        return metaData;
    }

    static Collection<Annotation> getAnnotations(ToNativeConverter toNativeConverter) {
        return toNativeConverter != null
                ? getMetaData(toNativeConverter.getClass(), toNativeConverter.nativeType()).toNativeAnnotations
                : Annotations.EMPTY_ANNOTATIONS;
    }

    static Collection<Annotation> getAnnotations(FromNativeConverter fromNativeConverter) {
        return fromNativeConverter != null
                ? getMetaData(fromNativeConverter.getClass(), fromNativeConverter.nativeType()).fromNativeAnnotations
                : Annotations.EMPTY_ANNOTATIONS;
    }
}
