package jnr.ffi.provider.converters;

import jnr.ffi.mapper.*;
import jnr.ffi.util.EnumMapper;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.EnumSet;

public final class EnumSetConverter implements DataConverter<EnumSet<? extends Enum>, Integer> {
    private final Class<? extends Enum> enumClass;
    private final EnumMapper enumMapper;
    private final EnumSet<? extends Enum> allValues;

    private EnumSetConverter(Class<? extends Enum> enumClass) {
        this.enumClass = enumClass;
        this.enumMapper = EnumMapper.getInstance(enumClass);
        this.allValues = EnumSet.allOf(enumClass);
    }

    @SuppressWarnings("unchecked")
    public static ToNativeConverter<EnumSet<? extends Enum>, Integer> getToNativeConverter(Class javaType, ToNativeContext toNativeContext) {
        if (!(toNativeContext instanceof MethodParameterContext)) {
            return null;
        }
        Method method = ((MethodParameterContext) toNativeContext).getMethod();
        Type parameterTypes = method.getGenericParameterTypes()[((MethodParameterContext) toNativeContext).getParameterIndex()];
        if (!(parameterTypes instanceof ParameterizedType)) {
            return null;
        }

        if (((ParameterizedType) parameterTypes).getActualTypeArguments().length < 1) {
            return null;
        }

        Type enumType = ((ParameterizedType) parameterTypes).getActualTypeArguments()[0];
        if (!(enumType instanceof Class)) {
            return null;
        }

        return new EnumSetConverter(((Class) enumType).asSubclass(Enum.class));
    }

    @Override
    @SuppressWarnings("unchecked")
    public EnumSet fromNative(Integer nativeValue, FromNativeContext context) {
        EnumSet enums = EnumSet.noneOf(enumClass);
        for (Enum e : allValues) {
            int enumValue = enumMapper.intValue(e);
            if ((nativeValue & enumValue) == enumValue) {
                enums.add(e);
            }
        }

        return enums;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Integer toNative(EnumSet<? extends Enum> value, ToNativeContext context) {
        int intValue = 0;
        for (Enum e : value) {
            intValue |= enumMapper.intValue(e);
        }

        return intValue;
    }

    @Override
    public Class<Integer> nativeType() {
        return Integer.class;
    }
}
