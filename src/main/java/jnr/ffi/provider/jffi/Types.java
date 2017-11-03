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

import jnr.ffi.Address;
import jnr.ffi.NativeType;
import jnr.ffi.Pointer;
import jnr.ffi.Type;
import jnr.ffi.annotations.TypeDefinition;

import java.lang.annotation.Annotation;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.nio.Buffer;
import java.util.*;


/**
 *
 */
class Types {
    private static Reference<Map<Class, Map<Collection<Annotation>, Type>>> typeCacheReference;

    static Type getType(jnr.ffi.Runtime runtime, Class javaType, Collection<Annotation> annotations) {
        Map<Class, Map<Collection<Annotation>, Type>> cache = typeCacheReference != null ? typeCacheReference.get() : null;
        Map<Collection<Annotation>, Type> aliasCache = cache != null ? cache.get(javaType) : null;
        Type type = aliasCache != null ? aliasCache.get(annotations) : null;
        
        return type != null ? type : lookupAndCacheType(runtime, javaType, annotations);
    }
    
    @SuppressWarnings("unchecked")
    private static synchronized Type lookupAndCacheType(jnr.ffi.Runtime runtime, Class javaType, Collection<Annotation> annotations) {
        Map<Class, Map<Collection<Annotation>, Type>> cache = typeCacheReference != null ? typeCacheReference.get() : null;
        Map<Collection<Annotation>, Type> aliasCache = cache != null ? cache.get(javaType) : null;
        Type type = aliasCache != null ? aliasCache.get(annotations) : null;
        if (type != null) {
            return type;
        }
        cache = new HashMap<Class, Map<Collection<Annotation>, Type>>(cache != null ? cache : Collections.EMPTY_MAP);
        
        aliasCache = new HashMap<Collection<Annotation>, Type>(aliasCache != null ? aliasCache : Collections.EMPTY_MAP);
        aliasCache.put(annotations, type = lookupType(runtime, javaType, annotations));
        cache.put(javaType, Collections.unmodifiableMap(aliasCache));

        typeCacheReference = new SoftReference<Map<Class, Map<Collection<Annotation>, Type>>>(Collections.unmodifiableMap(new IdentityHashMap<Class, Map<Collection<Annotation>, Type>>(cache)));
        
        return type;
    }
    
    private static Type lookupAliasedType(jnr.ffi.Runtime runtime, Collection<Annotation> annotations) {
        for (Annotation a : annotations) {
            TypeDefinition typedef = a.annotationType().getAnnotation(TypeDefinition.class);
            if (typedef != null) {
                return runtime.findType(typedef.alias());
            }
        }

        return null;
    }

    static Type lookupType(jnr.ffi.Runtime runtime, Class type, Collection<Annotation> annotations) {
        Type aliasedType = type.isArray() ? null : lookupAliasedType(runtime, annotations);
        if (aliasedType != null) {
            return aliasedType;

        } else if (Void.class.isAssignableFrom(type) || void.class == type) {
            return runtime.findType(NativeType.VOID);

        } else if (Boolean.class.isAssignableFrom(type) || boolean.class == type) {
            return runtime.findType(NativeType.SINT);

        } else if (Byte.class.isAssignableFrom(type) || byte.class == type) {
            return runtime.findType(NativeType.SCHAR);

        } else if (Short.class.isAssignableFrom(type) || short.class == type) {
            return runtime.findType(NativeType.SSHORT);

        } else if (Character.class.isAssignableFrom(type) || char.class == type) {
            return runtime.findType(NativeType.USHORT);

        } else if (Integer.class.isAssignableFrom(type) || int.class == type) {
            return runtime.findType(NativeType.SINT);

        } else if (Long.class.isAssignableFrom(type) || long.class == type) {
            return runtime.findType(NativeType.SLONG);

        } else if (Float.class.isAssignableFrom(type) || float.class == type) {
            return runtime.findType(NativeType.FLOAT);

        } else if (Double.class.isAssignableFrom(type) || double.class == type) {
            return runtime.findType(NativeType.DOUBLE);

        } else if (Pointer.class.isAssignableFrom(type)) {
            return runtime.findType(NativeType.ADDRESS);

        } else if (Address.class.isAssignableFrom(type)) {
            return runtime.findType(NativeType.ADDRESS);

        } else if (Buffer.class.isAssignableFrom(type)) {
            return runtime.findType(NativeType.ADDRESS);

        } else if (CharSequence.class.isAssignableFrom(type)) {
            return runtime.findType(NativeType.ADDRESS);

        } else if (type.isArray()) {
            return runtime.findType(NativeType.ADDRESS);

        } else {
            throw new IllegalArgumentException("unsupported type: " + type);
        }
    }

}
