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

import java.util.*;

/**
 * Caches native converter lookups by class.
 */
public final class CachingTypeMapper extends AbstractSignatureTypeMapper implements SignatureTypeMapper {
    private final SignatureTypeMapper mapper;
    private volatile Map<SignatureType, ToNativeType> toNativeTypeMap = Collections.emptyMap();
    private volatile Map<SignatureType, FromNativeType> fromNativeTypeMap = Collections.emptyMap();
    private static final InvalidType UNCACHEABLE_TYPE = new InvalidType();
    private static final InvalidType NO_TYPE = new InvalidType();

    public CachingTypeMapper(SignatureTypeMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public FromNativeType getFromNativeType(SignatureType type, FromNativeContext context) {
        FromNativeType fromNativeType = fromNativeTypeMap.get(type);

        if (fromNativeType == UNCACHEABLE_TYPE) {
            return mapper.getFromNativeType(type, context);

        } else if (fromNativeType == NO_TYPE) {
            return null;
        }

        return fromNativeType != null ? fromNativeType : lookupAndCacheFromNativeType(type, context);
    }

    @Override
    public ToNativeType getToNativeType(SignatureType type, ToNativeContext context) {
        ToNativeType toNativeType = toNativeTypeMap.get(type);
        if (toNativeType == UNCACHEABLE_TYPE) {
            return mapper.getToNativeType(type, context);

        } else if (toNativeType == NO_TYPE) {
            return null;
        }

        return toNativeType != null ? toNativeType : lookupAndCacheToNativeType(type, context);
    }


    private synchronized FromNativeType lookupAndCacheFromNativeType(SignatureType signature, FromNativeContext context) {
        FromNativeType fromNativeType = fromNativeTypeMap.get(signature);
        if (fromNativeType == null) {
            fromNativeType = mapper.getFromNativeType(signature, context);
            FromNativeType typeForCaching = fromNativeType;
            if (fromNativeType == null) {
                typeForCaching = NO_TYPE;

            } else if (!fromNativeType.getClass().isAnnotationPresent(FromNativeType.Cacheable.class)) {
                typeForCaching = UNCACHEABLE_TYPE;
            }

            Map<SignatureType, FromNativeType> m = new HashMap<SignatureType, FromNativeType>(fromNativeTypeMap.size() + 1);
            m.putAll(fromNativeTypeMap);
            m.put(signature, typeForCaching);
            fromNativeTypeMap = Collections.unmodifiableMap(m);
        }

        return fromNativeType != NO_TYPE ? fromNativeType : null;
    }

    private synchronized ToNativeType lookupAndCacheToNativeType(SignatureType signature, ToNativeContext context) {
        ToNativeType toNativeType = toNativeTypeMap.get(signature);
        if (toNativeType == null) {
            toNativeType = mapper.getToNativeType(signature, context);
            ToNativeType typeForCaching = toNativeType;
            if (toNativeType == null) {
                typeForCaching = NO_TYPE;

            } else if (!toNativeType.getClass().isAnnotationPresent(ToNativeType.Cacheable.class)) {
                typeForCaching = UNCACHEABLE_TYPE;
            }

            Map<SignatureType, ToNativeType> m = new HashMap<SignatureType, ToNativeType>(toNativeTypeMap.size() + 1);
            m.putAll(toNativeTypeMap);
            m.put(signature, typeForCaching);
            toNativeTypeMap = Collections.unmodifiableMap(m);
        }

        return toNativeType != NO_TYPE ? toNativeType : null;
    }

    private static final class InvalidType implements ToNativeType, FromNativeType {
        @Override
        public FromNativeConverter getFromNativeConverter() {
            return null;
        }

        @Override
        public ToNativeConverter getToNativeConverter() {
            return null;
        }
    }

}
