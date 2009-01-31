
package com.kenai.jaffl.mapper;

public interface TypeMapper {
    public FromNativeConverter getFromNativeConverter(Class type);
    public ToNativeConverter getToNativeConverter(Class type);
}
