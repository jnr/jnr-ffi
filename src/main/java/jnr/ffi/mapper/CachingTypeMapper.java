package jnr.ffi.mapper;

import java.util.*;

/**
 * Caches Class -> native converter lookups.
 */
public final class CachingTypeMapper implements SignatureTypeMapper {
    private final SignatureTypeMapper mapper;
    private volatile Map<SignatureType, ToNativeConverter> toNativeConverterMap = Collections.emptyMap();
    private volatile Map<SignatureType, FromNativeConverter> fromNativeConverterMap = Collections.emptyMap();
    private static final DataConverter NO_DATA_CONVERTER = new InvalidDataConverter();
    private static final DataConverter DO_NOT_CACHE = new InvalidDataConverter();

    public CachingTypeMapper(SignatureTypeMapper mapper) {
        this.mapper = mapper;
    }

    public final FromNativeConverter getFromNativeConverter(SignatureType type, FromNativeContext context) {
        FromNativeConverter fromNativeConverter = fromNativeConverterMap.get(type);

        if (fromNativeConverter == DO_NOT_CACHE) {
            return mapper.getFromNativeConverter(type, context);

        } else if (fromNativeConverter == NO_DATA_CONVERTER) {
            return null;
        }

        return fromNativeConverter != null ? fromNativeConverter : lookupAndCacheFromNativeConverter(type, context);
    }

    public final ToNativeConverter getToNativeConverter(SignatureType type, ToNativeContext context) {
        ToNativeConverter toNativeConverter = toNativeConverterMap.get(type);
        if (toNativeConverter == DO_NOT_CACHE) {
            return mapper.getToNativeConverter(type, context);

        } else if (toNativeConverter == NO_DATA_CONVERTER) {
            return null;
        }

        return toNativeConverter != null ? toNativeConverter : lookupAndCacheToNativeConverter(type, context);
    }

    private synchronized FromNativeConverter lookupAndCacheFromNativeConverter(SignatureType signature, FromNativeContext context) {
        FromNativeConverter fromNativeConverter = fromNativeConverterMap.get(signature);
        if (fromNativeConverter == null) {
            fromNativeConverter = mapper.getFromNativeConverter(signature, context);
            FromNativeConverter converterForCaching = fromNativeConverter;
            if (fromNativeConverter == null) {
                converterForCaching = NO_DATA_CONVERTER;

            } else if (!fromNativeConverter.getClass().isAnnotationPresent(FromNativeConverter.Cacheable.class)) {
                converterForCaching = DO_NOT_CACHE;
            }

            Map<SignatureType, FromNativeConverter> m = new HashMap<SignatureType, FromNativeConverter>(fromNativeConverterMap.size() + 1);
            m.putAll(fromNativeConverterMap);
            m.put(signature, converterForCaching);
            fromNativeConverterMap = Collections.unmodifiableMap(m);
        }

        return fromNativeConverter != NO_DATA_CONVERTER ? fromNativeConverter : null;
    }

    private synchronized ToNativeConverter lookupAndCacheToNativeConverter(SignatureType signature, ToNativeContext context) {
        ToNativeConverter toNativeConverter = toNativeConverterMap.get(signature);
        if (toNativeConverter == null) {
            toNativeConverter = mapper.getToNativeConverter(signature, context);
            ToNativeConverter converterForCaching = toNativeConverter;
            if (toNativeConverter == null) {
                converterForCaching = NO_DATA_CONVERTER;

            } else if (!toNativeConverter.getClass().isAnnotationPresent(ToNativeConverter.Cacheable.class)) {
                converterForCaching = DO_NOT_CACHE;
            }

            Map<SignatureType, ToNativeConverter> m = new HashMap<SignatureType, ToNativeConverter>(toNativeConverterMap.size() + 1);
            m.putAll(toNativeConverterMap);
            m.put(signature, converterForCaching);
            toNativeConverterMap = Collections.unmodifiableMap(m);
        }


        return toNativeConverter != NO_DATA_CONVERTER ? toNativeConverter : null;
    }

    private static final class InvalidDataConverter implements DataConverter {


        @Override
        public Object fromNative(Object nativeValue, FromNativeContext context) {
            throw new RuntimeException("should not be called");
        }

        @Override
        public Object toNative(Object value, ToNativeContext context) {
            throw new RuntimeException("should not be called");
        }

        @Override
        public Class nativeType() {
            throw new RuntimeException("should not be called");
        }
    }
}
