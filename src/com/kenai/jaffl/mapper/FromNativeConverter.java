
package com.kenai.jaffl.mapper;

/**
 * Converts data from a native type to a java type
 */
public interface FromNativeConverter<J, N> {
    public J fromNative(N nativeValue, FromNativeContext context);
    public Class<N> nativeType();
}
