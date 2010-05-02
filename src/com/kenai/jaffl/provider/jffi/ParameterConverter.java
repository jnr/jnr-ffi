

package com.kenai.jaffl.provider.jffi;

import com.kenai.jaffl.mapper.ToNativeContext;
import com.kenai.jaffl.mapper.ToNativeConverter;

/**
 * Proxy to hold information for parameter conversion to native types.
 *
 * <p>
 * This wraps a {@link ToNativeConverter}, but with a simplified interface to
 * ease code generation.
 */
public final class ParameterConverter implements ToNativeConverter{
    private final ToNativeConverter converter;
    private final ToNativeContext ctx;

    public ParameterConverter(ToNativeConverter converter, ToNativeContext ctx) {
        this.converter = converter;
        this.ctx = ctx;
    }

    public final Object toNative(Object value, ToNativeContext unused) {
        return converter.toNative(value, ctx);
    }

    public final Object toNative(Object value) {
        return converter.toNative(value, ctx);
    }

    public final Class nativeType() {
        return converter.nativeType();
    }
}
