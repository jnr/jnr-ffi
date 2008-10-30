
package com.kenai.jaffl.mapper;

/**
 *
 * @author wayne
 */
public interface ToNativeConverter<J, N> {
    public N toNative(J value, ToNativeContext context);
}
