package jnr.ffi.provider.jffi;

import jnr.ffi.NativeLong;
import jnr.ffi.Pointer;
import jnr.ffi.Struct;
import jnr.ffi.byref.ByReference;
import jnr.ffi.mapper.*;
import jnr.ffi.provider.converters.EnumConverter;
import jnr.ffi.provider.ParameterFlags;
import jnr.ffi.provider.converters.*;

import java.nio.charset.Charset;
import java.util.EnumSet;
import java.util.Set;

import static jnr.ffi.provider.jffi.AsmUtil.isDelegate;
import static jnr.ffi.provider.jffi.InvokerUtil.getNativeType;
import static jnr.ffi.provider.jffi.NumberUtil.sizeof;

final class InvokerTypeMapper implements SignatureTypeMapper {
    private final NativeClosureManager closureManager;

    public InvokerTypeMapper(NativeClosureManager closureManager) {
        this.closureManager = closureManager;
    }

    public FromNativeConverter getFromNativeConverter(SignatureType signatureType, FromNativeContext fromNativeContext) {
        FromNativeConverter converter;

        if (Enum.class.isAssignableFrom(signatureType.getDeclaredType())) {
            return EnumConverter.getInstance(signatureType.getDeclaredType().asSubclass(Enum.class));

        } else if (Struct.class.isAssignableFrom(signatureType.getDeclaredType())) {
            return StructByReferenceFromNativeConverter.newStructByReferenceConverter(signatureType.getDeclaredType().asSubclass(Struct.class),
                    ParameterFlags.parse(fromNativeContext.getAnnotations()));

        } else if (closureManager != null && isDelegate(signatureType.getDeclaredType())) {
            return ClosureFromNativeConverter.getInstance(signatureType, AsmLibraryLoader.getCurrentClassLoader(), this);

        } else if (NativeLong.class == signatureType.getDeclaredType()) {
            return NativeLongConverter.INSTANCE;

        } else if (String.class == signatureType.getDeclaredType() || CharSequence.class == signatureType.getDeclaredType()) {
            return StringResultConverter.getInstance(Charset.defaultCharset());

        } else if ((Set.class == signatureType.getDeclaredType() || EnumSet.class == signatureType.getDeclaredType()) && (converter = EnumSetConverter.getFromNativeConverter(signatureType, fromNativeContext)) != null) {
            return converter;

        } else {
            return null;
        }

    }

    public ToNativeConverter getToNativeConverter(SignatureType signatureType, ToNativeContext context) {
        Class javaType = signatureType.getDeclaredType();
        ToNativeConverter converter;

        if (Enum.class.isAssignableFrom(javaType)) {
            return EnumConverter.getInstance(javaType.asSubclass(Enum.class));

        } else if (Set.class.isAssignableFrom(javaType) && (converter = EnumSetConverter.getToNativeConverter(signatureType, context)) != null) {
            return converter;

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

        } else if (CharSequence.class.isAssignableFrom(javaType)) {
            return CharSequenceParameterConverter.getInstance(NativeRuntime.getInstance(), Charset.defaultCharset());

        } else if (Byte[].class.isAssignableFrom(javaType)) {
            return BoxedByteArrayParameterConverter.getInstance(NativeRuntime.getInstance(), ParameterFlags.parse(context.getAnnotations()));

        } else if (Short[].class.isAssignableFrom(javaType)) {
            return BoxedShortArrayParameterConverter.getInstance(NativeRuntime.getInstance(), ParameterFlags.parse(context.getAnnotations()));

        } else if (Integer[].class.isAssignableFrom(javaType)) {
            return BoxedIntegerArrayParameterConverter.getInstance(NativeRuntime.getInstance(), ParameterFlags.parse(context.getAnnotations()));

        } else if (Long[].class.isAssignableFrom(javaType)) {
            return sizeof(getNativeType(NativeRuntime.getInstance(), javaType.getComponentType(), context.getAnnotations())) == 4
                ? BoxedLong32ArrayParameterConverter.getInstance(NativeRuntime.getInstance(), ParameterFlags.parse(context.getAnnotations()))
                : BoxedLong64ArrayParameterConverter.getInstance(NativeRuntime.getInstance(), ParameterFlags.parse(context.getAnnotations()));

        } else if (NativeLong[].class.isAssignableFrom(javaType)) {
            return sizeof(getNativeType(NativeRuntime.getInstance(), javaType.getComponentType(), context.getAnnotations())) == 4
                    ? NativeLong32ArrayParameterConverter.getInstance(NativeRuntime.getInstance(), ParameterFlags.parse(context.getAnnotations()))
                    : NativeLong64ArrayParameterConverter.getInstance(NativeRuntime.getInstance(), ParameterFlags.parse(context.getAnnotations()));

        } else if (Float[].class.isAssignableFrom(javaType)) {
            return BoxedFloatArrayParameterConverter.getInstance(NativeRuntime.getInstance(), ParameterFlags.parse(context.getAnnotations()));

        } else if (Double[].class.isAssignableFrom(javaType)) {
            return BoxedDoubleArrayParameterConverter.getInstance(NativeRuntime.getInstance(), ParameterFlags.parse(context.getAnnotations()));

        } else if (Boolean[].class.isAssignableFrom(javaType)) {
            return BoxedBooleanArrayParameterConverter.getInstance(NativeRuntime.getInstance(), ParameterFlags.parse(context.getAnnotations()));

        } else if (javaType.isArray() && Pointer.class.isAssignableFrom(javaType.getComponentType())) {
            return NativeRuntime.getInstance().addressSize() == 4
                    ? Pointer32ArrayParameterConverter.getInstance(NativeRuntime.getInstance(), ParameterFlags.parse(context.getAnnotations()))
                    : Pointer64ArrayParameterConverter.getInstance(NativeRuntime.getInstance(), ParameterFlags.parse(context.getAnnotations()));

        } else if (long[].class.isAssignableFrom(javaType) && sizeof(getNativeType(NativeRuntime.getInstance(), javaType.getComponentType(), context.getAnnotations())) == 4) {
            return Long32ArrayParameterConverter.getInstance(NativeRuntime.getInstance(), ParameterFlags.parse(context.getAnnotations()));

        } else if (javaType.isArray() && Struct.class.isAssignableFrom(javaType.getComponentType())) {
            return StructArrayParameterConverter.getInstance(NativeRuntime.getInstance(), javaType.getComponentType(), ParameterFlags.parse(context.getAnnotations()));

        } else if (javaType.isArray() && String.class.isAssignableFrom(javaType.getComponentType())) {
            return StringArrayParameterConverter.getInstance(NativeRuntime.getInstance(), ParameterFlags.parse(context.getAnnotations()));

        } else {
            return null;
        }
    }
}
