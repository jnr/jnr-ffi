
package com.kenai.jaffl.provider.jffi;

import com.kenai.jaffl.mapper.FromNativeContext;
import com.kenai.jaffl.mapper.FromNativeConverter;
import com.kenai.jaffl.util.EnumMapper;


public class EnumResultConverter implements FromNativeConverter {
    private final Class enumClass;

    EnumResultConverter(Class enumClass) {
        this.enumClass = enumClass;
    }

    @SuppressWarnings("unchecked")
    public Object fromNative(Object nativeValue, FromNativeContext context) {
        return EnumMapper.getInstance().valueOf(((Integer) nativeValue), enumClass);
    }

    public Class nativeType() {
        return Integer.class;
    }
}
