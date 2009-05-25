
package com.kenai.jaffl.mapper;

public interface ToNativeConverter<J, N> {
    public N toNative(J value, ToNativeContext context);
    public Class<N> nativeType();
}
