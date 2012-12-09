package jnr.ffi.mapper;

import java.util.*;

/**
 * Caches Class -> native converter lookups.
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
    public FromNativeType getFromNativeType(jnr.ffi.Runtime runtime, SignatureType type, FromNativeContext context) {
        FromNativeType fromNativeType = fromNativeTypeMap.get(type);

        if (fromNativeType == UNCACHEABLE_TYPE) {
            return mapper.getFromNativeType(runtime, type, context);

        } else if (fromNativeType == NO_TYPE) {
            return null;
        }

        return fromNativeType != null ? fromNativeType : lookupAndCacheFromNativeType(runtime, type, context);
    }

    @Override
    public ToNativeType getToNativeType(jnr.ffi.Runtime runtime, SignatureType type, ToNativeContext context) {
        ToNativeType toNativeType = toNativeTypeMap.get(type);
        if (toNativeType == UNCACHEABLE_TYPE) {
            return mapper.getToNativeType(runtime, type, context);

        } else if (toNativeType == NO_TYPE) {
            return null;
        }

        return toNativeType != null ? toNativeType : lookupAndCacheToNativeType(runtime, type, context);
    }


    private synchronized FromNativeType lookupAndCacheFromNativeType(jnr.ffi.Runtime runtime, SignatureType signature, FromNativeContext context) {
        FromNativeType fromNativeType = fromNativeTypeMap.get(signature);
        if (fromNativeType == null) {
            fromNativeType = mapper.getFromNativeType(runtime, signature, context);
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

    private synchronized ToNativeType lookupAndCacheToNativeType(jnr.ffi.Runtime runtime, SignatureType signature, ToNativeContext context) {
        ToNativeType toNativeType = toNativeTypeMap.get(signature);
        if (toNativeType == null) {
            toNativeType = mapper.getToNativeType(runtime, signature, context);
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
