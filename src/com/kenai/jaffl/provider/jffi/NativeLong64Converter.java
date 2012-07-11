package com.kenai.jaffl.provider.jffi;

import com.kenai.jaffl.NativeLong;
import com.kenai.jaffl.mapper.FromNativeContext;
import com.kenai.jaffl.mapper.FromNativeConverter;
import com.kenai.jaffl.mapper.ToNativeContext;
import com.kenai.jaffl.mapper.ToNativeConverter;

public class NativeLong64Converter implements ToNativeConverter<NativeLong, Long>, FromNativeConverter<NativeLong, Long> {
    public static final NativeLong64Converter INSTANCE = new NativeLong64Converter();

    public NativeLong fromNative(Long nativeValue, FromNativeContext context) {
        return NativeLong.valueOf(nativeValue);
    }

    public Long toNative(NativeLong value, ToNativeContext context) {
        return value.longValue();
    }

    public Class<Long> nativeType() {
        return Long.class;
    }
}
