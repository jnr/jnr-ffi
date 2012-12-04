package jnr.ffi.provider.jffi;

import jnr.ffi.NativeLong;
import jnr.ffi.Pointer;
import jnr.ffi.Struct;
import jnr.ffi.byref.ByReference;
import jnr.ffi.mapper.*;
import jnr.ffi.provider.EnumConverter;
import jnr.ffi.provider.ParameterFlags;
import jnr.ffi.provider.converters.ByteArrayParameterConverter;
import jnr.ffi.provider.converters.Pointer32ArrayParameterConverter;
import jnr.ffi.provider.converters.Pointer64ArrayParameterConverter;
import jnr.ffi.provider.converters.StructArrayParameterConverter;

import java.util.EnumSet;

import static jnr.ffi.provider.jffi.AsmUtil.isDelegate;

final class InvokerTypeMapper implements TypeMapper {
    private final NativeClosureManager closureManager;

    public InvokerTypeMapper(NativeClosureManager closureManager) {
        this.closureManager = closureManager;
    }

    public FromNativeConverter getFromNativeConverter(Class javaType, FromNativeContext fromNativeContext) {
        if (Enum.class.isAssignableFrom(javaType)) {
            return EnumConverter.getInstance(javaType.asSubclass(Enum.class));

        } else if (Struct.class.isAssignableFrom(javaType)) {
            return StructByReferenceFromNativeConverter.newStructByReferenceConverter(javaType.asSubclass(Struct.class),
                    ParameterFlags.parse(fromNativeContext.getAnnotations()));

        } else if (closureManager != null && isDelegate(javaType)) {
            final Class type = javaType;
            return new FromNativeConverter() {
                public Object fromNative(Object nativeValue, FromNativeContext context) {
                    throw new UnsupportedOperationException("cannot convert to " + type);
                }

                public Class nativeType() {
                    return Pointer.class;
                }
            };

        } else if (NativeLong.class.isAssignableFrom(javaType)) {
            return NativeLongConverter.INSTANCE;

        } else {
            return null;
        }

    }

    public ToNativeConverter getToNativeConverter(Class javaType, ToNativeContext context) {
        if (Enum.class.isAssignableFrom(javaType)) {
            return EnumConverter.getInstance(javaType.asSubclass(Enum.class));

        } else if (EnumSet.class.isAssignableFrom(javaType)) {
            return EnumSetConverter.getToNativeConverter(javaType, context);

        } else if (isDelegate(javaType)) {
            return closureManager.newClosureSite(javaType);

        } else if (ByReference.class.isAssignableFrom(javaType)) {
            return new ByReferenceParameterConverter(ParameterFlags.parse(context.getAnnotations()));

        } else if (Struct.class.isAssignableFrom(javaType)) {
            return new StructByReferenceToNativeConverter(ParameterFlags.parse(context.getAnnotations()));

        } else if (NativeLong.class.isAssignableFrom(javaType)) {
            return NativeLongConverter.INSTANCE;

        } else if (StringBuilder.class.isAssignableFrom(javaType)) {
            return StringBuilderParameterConverter.getInstance(NativeRuntime.getInstance(), ParameterFlags.parse(context.getAnnotations()));

        } else if (StringBuffer.class.isAssignableFrom(javaType)) {
            return StringBufferParameterConverter.getInstance(NativeRuntime.getInstance(), ParameterFlags.parse(context.getAnnotations()));

        } else if (Byte[].class.isAssignableFrom(javaType)) {
            return ByteArrayParameterConverter.getInstance(NativeRuntime.getInstance(), ParameterFlags.parse(context.getAnnotations()));

        } else if (javaType.isArray() && Pointer.class.isAssignableFrom(javaType.getComponentType())) {
            return NativeRuntime.getInstance().addressSize() == 4
                    ? Pointer32ArrayParameterConverter.getInstance(NativeRuntime.getInstance(), ParameterFlags.parse(context.getAnnotations()))
                    : Pointer64ArrayParameterConverter.getInstance(NativeRuntime.getInstance(), ParameterFlags.parse(context.getAnnotations()));

        } else if (javaType.isArray() && Struct.class.isAssignableFrom(javaType.getComponentType())) {
            return StructArrayParameterConverter.getInstance(NativeRuntime.getInstance(), javaType.getComponentType(), ParameterFlags.parse(context.getAnnotations()));
        } else {
            return null;
        }
    }
}
