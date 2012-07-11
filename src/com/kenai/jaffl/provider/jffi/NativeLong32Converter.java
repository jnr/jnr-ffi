package com.kenai.jaffl.provider.jffi;

import com.kenai.jaffl.NativeLong;
import com.kenai.jaffl.mapper.FromNativeContext;
import com.kenai.jaffl.mapper.FromNativeConverter;
import com.kenai.jaffl.mapper.ToNativeContext;
import com.kenai.jaffl.mapper.ToNativeConverter;

public class NativeLong32Converter implements ToNativeConverter<NativeLong, Integer>, FromNativeConverter<NativeLong, Integer> {
    public static final NativeLong32Converter INSTANCE = new NativeLong32Converter();

    public NativeLong fromNative(Integer nativeValue, FromNativeContext context) {
        return NativeLong.valueOf(nativeValue);
    }

    public Integer toNative(NativeLong value, ToNativeContext context) {
        return value.intValue();
    }

    public Class<Integer> nativeType() {
        return Integer.class;
    }
}
