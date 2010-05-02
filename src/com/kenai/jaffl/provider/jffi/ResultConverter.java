
package com.kenai.jaffl.provider.jffi;

import com.kenai.jaffl.mapper.FromNativeContext;
import com.kenai.jaffl.mapper.FromNativeConverter;

/**
 * Proxy to hold info for parameter conversion.
 */
public final class ResultConverter implements FromNativeConverter {
    private final FromNativeConverter converter;
    private final FromNativeContext ctx;

    public ResultConverter(FromNativeConverter converter, FromNativeContext ctx) {
        this.converter = converter;
        this.ctx = ctx;
    }

    public final Object fromNative(Object nativeValue, FromNativeContext unused) {
        return converter.fromNative(nativeValue, ctx);
    }

    public final Object fromNative(Object nativeValue) {
        return converter.fromNative(nativeValue, ctx);
    }

    public final Class nativeType() {
        return converter.nativeType();
    }

}
