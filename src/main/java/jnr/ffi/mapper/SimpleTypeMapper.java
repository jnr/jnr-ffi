package jnr.ffi.mapper;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

final class SimpleTypeMapper implements TypeMapper {
    private final Map<Class, ToNativeConverter<?, ?>> toNativeConverters;
    private final Map<Class, FromNativeConverter<?, ?>> fromNativeConverters;

    public SimpleTypeMapper(Map<Class, ToNativeConverter<?, ?>> toNativeConverters, Map<Class, FromNativeConverter<?, ?>> fromNativeConverters) {
        this.toNativeConverters = Collections.unmodifiableMap(new IdentityHashMap<Class, ToNativeConverter<?, ?>>(toNativeConverters));
        this.fromNativeConverters = Collections.unmodifiableMap(new IdentityHashMap<Class, FromNativeConverter<?, ?>>(fromNativeConverters));
    }

    @Override
    public FromNativeConverter getFromNativeConverter(Class type) {
        return fromNativeConverters.get(type);
    }

    @Override
    public ToNativeConverter getToNativeConverter(Class type) {
        return toNativeConverters.get(type);
    }
}
