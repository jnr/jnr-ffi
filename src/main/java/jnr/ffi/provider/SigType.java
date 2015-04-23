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

package jnr.ffi.provider;

import jnr.ffi.NativeType;
import jnr.ffi.mapper.SignatureType;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;

/**
 *
 */
abstract public class SigType implements SignatureType {
    private final Class javaType, convertedType;
    private final Collection<Annotation> annotations;
    private final NativeType nativeType;

    public SigType(Class javaType, NativeType nativeType, Collection<Annotation> annotations, Class convertedType) {
        this.javaType = javaType;
        this.annotations = annotations;
        this.convertedType = convertedType;
        this.nativeType = nativeType;
    }

    public final Class getDeclaredType() {
        return javaType;
    }

    public final Class effectiveJavaType() {
        return convertedType;
    }

    public final Collection<Annotation> annotations() {
        return annotations;
    }

    public final Collection<Annotation> getAnnotations() {
        return annotations;
    }

    @Override
    public Type getGenericType() {
        return getDeclaredType();
    }

    public final String toString() {
        return String.format("declared: %s, effective: %s, native: %s", getDeclaredType(), effectiveJavaType(), getNativeType());
    }

    public NativeType getNativeType() {
        return nativeType;
    }
}
