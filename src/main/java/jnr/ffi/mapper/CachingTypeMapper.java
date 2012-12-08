package jnr.ffi.mapper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Caches Class -> native converter lookups.
 */
public final class CachingTypeMapper implements TypeMapper {
    private final TypeMapper mapper;
    private volatile Map<Signature, ToNativeConverter> toNativeConverterMap = Collections.emptyMap();
    private volatile Map<Signature, FromNativeConverter> fromNativeConverterMap = Collections.emptyMap();
    private static final DataConverter NO_DATA_CONVERTER = new InvalidDataConverter();
    private static final DataConverter DO_NOT_CACHE = new InvalidDataConverter();

    public CachingTypeMapper(TypeMapper mapper) {
        this.mapper = mapper;
    }

    private static final class Signature {
        private final Class klass;
        private final Collection<Annotation> annotations;
        private final Type genericType;

        Signature(Class klass, Collection<Annotation> annotations, Type genericType) {
            this.klass = klass;
            this.annotations = annotations;
            this.genericType = genericType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Signature signature = (Signature) o;

            return klass == signature.klass
                && genericType.equals(signature.genericType)
                && annotations.equals(signature.annotations)
                ;
        }

        @Override
        public int hashCode() {
            int result = klass.hashCode();
            result = 31 * result + annotations.hashCode();
            if (genericType != null) result = 31 * result + genericType.hashCode();
            return result;
        }
    }

    public final FromNativeConverter getFromNativeConverter(Class klazz, FromNativeContext context) {
        Type genericType = !klazz.isPrimitive() && context instanceof MethodResultContext
                ? ((MethodResultContext) context).getMethod().getGenericReturnType() : klazz;
        Signature signature = new Signature(klazz, context.getAnnotations(), genericType);
        FromNativeConverter fromNativeConverter = fromNativeConverterMap.get(signature);

        if (fromNativeConverter == DO_NOT_CACHE) {
            return mapper.getFromNativeConverter(klazz, context);

        } else if (fromNativeConverter == NO_DATA_CONVERTER) {
            return null;
        }

        return fromNativeConverter != null ? fromNativeConverter : lookupAndCacheFromNativeConverter(signature, context);
    }

    public final ToNativeConverter getToNativeConverter(Class klazz, ToNativeContext context) {
        Type genericType = klazz;
        if (!klazz.isPrimitive() && context instanceof MethodParameterContext) {
            MethodParameterContext methodParameterContext = (MethodParameterContext) context;
            genericType = methodParameterContext.getMethod().getGenericParameterTypes()[methodParameterContext.getParameterIndex()];
        }

        Signature signature = new Signature(klazz, context.getAnnotations(), genericType);
        ToNativeConverter toNativeConverter = toNativeConverterMap.get(signature);
        if (toNativeConverter == DO_NOT_CACHE) {
            return mapper.getToNativeConverter(klazz, context);

        } else if (toNativeConverter == NO_DATA_CONVERTER) {
            return null;
        }

        return toNativeConverter != null ? toNativeConverter : lookupAndCacheToNativeConverter(signature, context);
    }

    private synchronized FromNativeConverter lookupAndCacheFromNativeConverter(Signature signature, FromNativeContext context) {
        FromNativeConverter fromNativeConverter = fromNativeConverterMap.get(signature);
        if (fromNativeConverter == null) {
            fromNativeConverter = mapper.getFromNativeConverter(signature.klass, context);
            FromNativeConverter converterForCaching = fromNativeConverter;
            if (fromNativeConverter == null) {
                converterForCaching = NO_DATA_CONVERTER;

            } else if (!fromNativeConverter.getClass().isAnnotationPresent(FromNativeConverter.Cacheable.class)) {
                converterForCaching = DO_NOT_CACHE;
            }

            Map<Signature, FromNativeConverter> m = new HashMap<Signature, FromNativeConverter>(fromNativeConverterMap.size() + 1);
            m.putAll(fromNativeConverterMap);
            m.put(signature, converterForCaching);
            fromNativeConverterMap = Collections.unmodifiableMap(m);
        }

        return fromNativeConverter != NO_DATA_CONVERTER ? fromNativeConverter : null;
    }

    private synchronized ToNativeConverter lookupAndCacheToNativeConverter(Signature signature, ToNativeContext context) {
        ToNativeConverter toNativeConverter = toNativeConverterMap.get(signature);
        if (toNativeConverter == null) {
            toNativeConverter = mapper.getToNativeConverter(signature.klass, context);
            ToNativeConverter converterForCaching = toNativeConverter;
            if (toNativeConverter == null) {
                converterForCaching = NO_DATA_CONVERTER;

            } else if (!toNativeConverter.getClass().isAnnotationPresent(ToNativeConverter.Cacheable.class)) {
                converterForCaching = DO_NOT_CACHE;
            }

            Map<Signature, ToNativeConverter> m = new HashMap<Signature, ToNativeConverter>(toNativeConverterMap.size() + 1);
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
