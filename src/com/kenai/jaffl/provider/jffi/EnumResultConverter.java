
package com.kenai.jaffl.provider.jffi;

import com.kenai.jaffl.mapper.FromNativeContext;
import com.kenai.jaffl.mapper.FromNativeConverter;
import com.kenai.jaffl.util.EnumMapper;


public class EnumResultConverter implements FromNativeConverter {
    private final EnumMapper mapper;
    private final Class enumClass;

    EnumResultConverter(Class enumClass) {
        this.mapper = EnumMapper.getInstance(enumClass.asSubclass(Enum.class));
        this.enumClass = enumClass;
    }

    public Object fromNative(Object nativeValue, FromNativeContext context) {
        return mapper.valueOf((Integer) nativeValue);
    }

    public Class nativeType() {
        return Integer.class;
    }
}
