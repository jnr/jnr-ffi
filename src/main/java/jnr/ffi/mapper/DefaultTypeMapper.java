package jnr.ffi.mapper;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 */
public final class DefaultTypeMapper implements TypeMapper {
    private final Map<Class, ToNativeConverter> toNativeConverters;
    private final Map<Class, FromNativeConverter> fromNativeConverters;

    public DefaultTypeMapper() {
        toNativeConverters = new LinkedHashMap<Class, ToNativeConverter>();
        fromNativeConverters = new LinkedHashMap<Class, FromNativeConverter>();
    }

    public final void put(Class javaClass, DataConverter converter) {
        toNativeConverters.put(javaClass, converter);
        fromNativeConverters.put(javaClass, converter);
    }

    public final void put(Class javaClass, ToNativeConverter converter) {
        toNativeConverters.put(javaClass, converter);
    }

    public final void put(Class javaClass, FromNativeConverter converter) {
        fromNativeConverters.put(javaClass, converter);
    }

    public FromNativeConverter getFromNativeConverter(Class type, FromNativeContext context) {
        return fromNativeConverters.get(type);
    }

    public ToNativeConverter getToNativeConverter(Class type, ToNativeContext context) {
        return toNativeConverters.get(type);
    }
}
