
package com.kenai.jaffl.mapper;

/**
 *
 * @author wayne
 */
public interface TypeMapper {
    public FromNativeConverter getFromNativeConverter(Class type);
    public ToNativeConverter getToNativeConverter(Class type);
}
