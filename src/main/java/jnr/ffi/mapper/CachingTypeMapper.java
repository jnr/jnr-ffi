package jnr.ffi.mapper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Caches Class -> native converter lookups.
 */
public final class CachingTypeMapper implements TypeMapper {
    private final TypeMapper mapper;
    private Map<Class, Map<Collection<Annotation>, ToNativeConverter>> toNativeConverterMap = Collections.emptyMap();
    private Map<Class, Map<Collection<Annotation>, FromNativeConverter>> fromNativeConverterMap = Collections.emptyMap();

    public CachingTypeMapper(TypeMapper mapper) {
        this.mapper = mapper;
    }

    public final FromNativeConverter getFromNativeConverter(Class klazz, FromNativeContext context) {
        // Do not cache converters for generic types
        if ((context instanceof MethodResultContext)) {
            Method method = ((MethodResultContext) context).getMethod();
            if (method.getGenericReturnType() instanceof ParameterizedType) {
                return mapper.getFromNativeConverter(klazz, context);
            }
        }

        Map<Collection<Annotation>, FromNativeConverter> map = fromNativeConverterMap.get(klazz);
        FromNativeConverter fromNativeConverter = map != null ? map.get(context.getAnnotations()) : null;
        return fromNativeConverter != null ? fromNativeConverter : lookupAndCacheFromNativeConverter(klazz, context);
    }

    public final ToNativeConverter getToNativeConverter(Class klazz, ToNativeContext context) {
        // Do not cache converters for generic types
        if ((context instanceof MethodParameterContext)) {
            Method method = ((MethodParameterContext) context).getMethod();
            Type genericParameterType = method.getGenericParameterTypes()[((MethodParameterContext) context).getParameterIndex()];
            if (genericParameterType instanceof ParameterizedType) {
                return mapper.getToNativeConverter(klazz, context);
            }
        }

        Map<Collection<Annotation>, ToNativeConverter> map = toNativeConverterMap.get(klazz);
        ToNativeConverter toNativeConverter = map != null ? map.get(context.getAnnotations()) : null;
        return toNativeConverter != null ? toNativeConverter : lookupAndCacheToNativeConverter(klazz, context);
    }

    private synchronized FromNativeConverter lookupAndCacheFromNativeConverter(Class klazz, FromNativeContext context) {
        Map<Collection<Annotation>, FromNativeConverter> converterMap = fromNativeConverterMap.get(klazz);
        FromNativeConverter fromNativeConverter = converterMap != null ? converterMap.get(context.getAnnotations()) : null;
        if (fromNativeConverter == null) {
            Map<Collection<Annotation>, FromNativeConverter> cm = new HashMap<Collection<Annotation>, FromNativeConverter>((converterMap != null ? converterMap.size() : 0) + 1, 0.5f);
            if (converterMap != null) cm.putAll(converterMap);
            cm.put(context.getAnnotations(), fromNativeConverter = mapper.getFromNativeConverter(klazz, context));

            Map<Class, Map<Collection<Annotation>, FromNativeConverter>> m
                    = new IdentityHashMap<Class, Map<Collection<Annotation>, FromNativeConverter>>(fromNativeConverterMap.size() + 1);
            m.putAll(fromNativeConverterMap);
            m.put(klazz, Collections.unmodifiableMap(cm));
            fromNativeConverterMap = Collections.unmodifiableMap(m);
        }

        return fromNativeConverter;
    }

    private synchronized ToNativeConverter lookupAndCacheToNativeConverter(Class klazz, ToNativeContext context) {
        Map<Collection<Annotation>, ToNativeConverter> converterMap = toNativeConverterMap.get(klazz);
        ToNativeConverter toNativeConverter = converterMap != null ? converterMap.get(context.getAnnotations()) : null;
        if (toNativeConverter == null) {
            Map<Collection<Annotation>, ToNativeConverter> cm = new HashMap<Collection<Annotation>, ToNativeConverter>((converterMap != null ? converterMap.size() : 0) + 1, 0.5f);
            if (converterMap != null) cm.putAll(converterMap);
            cm.put(context.getAnnotations(), toNativeConverter = mapper.getToNativeConverter(klazz, context));

            Map<Class, Map<Collection<Annotation>, ToNativeConverter>> m
                    = new IdentityHashMap<Class, Map<Collection<Annotation>, ToNativeConverter>>(toNativeConverterMap.size() + 1);
            m.putAll(toNativeConverterMap);
            m.put(klazz, Collections.unmodifiableMap(cm));
            toNativeConverterMap = Collections.unmodifiableMap(m);
        }

        return toNativeConverter;
    }
}
