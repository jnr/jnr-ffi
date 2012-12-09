package jnr.ffi.provider.jffi;

import jnr.ffi.*;
import jnr.ffi.byref.ByReference;
import jnr.ffi.mapper.*;
import jnr.ffi.mapper.FromNativeType;
import jnr.ffi.mapper.ToNativeType;
import jnr.ffi.provider.converters.EnumConverter;
import jnr.ffi.provider.ParameterFlags;
import jnr.ffi.provider.converters.*;

import java.nio.charset.Charset;
import java.util.EnumSet;
import java.util.Set;

import static jnr.ffi.provider.jffi.AsmUtil.isDelegate;
import static jnr.ffi.provider.jffi.InvokerUtil.getNativeType;
import static jnr.ffi.provider.jffi.NumberUtil.sizeof;

final class InvokerTypeMapper extends AbstractSignatureTypeMapper implements SignatureTypeMapper {
    private final NativeClosureManager closureManager;
    private final AsmClassLoader classLoader;
    private final jnr.ffi.Runtime runtime;

    public InvokerTypeMapper(jnr.ffi.Runtime runtime, NativeClosureManager closureManager, AsmClassLoader classLoader) {
        this.runtime = runtime;
        this.closureManager = closureManager;
        this.classLoader = classLoader;
    }

    public FromNativeConverter getFromNativeConverter(SignatureType signatureType, FromNativeContext fromNativeContext) {
        FromNativeConverter converter;

        if (Enum.class.isAssignableFrom(signatureType.getDeclaredType())) {
            return EnumConverter.getInstance(signatureType.getDeclaredType().asSubclass(Enum.class));

        } else if (Struct.class.isAssignableFrom(signatureType.getDeclaredType())) {
            return StructByReferenceFromNativeConverter.newStructByReferenceConverter(runtime, signatureType.getDeclaredType().asSubclass(Struct.class),
                    ParameterFlags.parse(fromNativeContext.getAnnotations()), classLoader);

        } else if (closureManager != null && isDelegate(signatureType.getDeclaredType())) {
            return ClosureFromNativeConverter.getInstance(runtime, signatureType, classLoader, this);

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
            return new ByReferenceParameterConverter(runtime, ParameterFlags.parse(context.getAnnotations()));

        } else if (Struct.class.isAssignableFrom(javaType)) {
            return new StructByReferenceToNativeConverter(ParameterFlags.parse(context.getAnnotations()));

        } else if (NativeLong.class.isAssignableFrom(javaType)) {
            return NativeLongConverter.INSTANCE;

        } else if (StringBuilder.class.isAssignableFrom(javaType)) {
            return StringBuilderParameterConverter.getInstance(runtime, ParameterFlags.parse(context.getAnnotations()));

        } else if (StringBuffer.class.isAssignableFrom(javaType)) {
            return StringBufferParameterConverter.getInstance(runtime, ParameterFlags.parse(context.getAnnotations()));

        } else if (CharSequence.class.isAssignableFrom(javaType)) {
            return CharSequenceParameterConverter.getInstance(runtime, Charset.defaultCharset());

        } else if (Byte[].class.isAssignableFrom(javaType)) {
            return BoxedByteArrayParameterConverter.getInstance(runtime, ParameterFlags.parse(context.getAnnotations()));

        } else if (Short[].class.isAssignableFrom(javaType)) {
            return BoxedShortArrayParameterConverter.getInstance(runtime, ParameterFlags.parse(context.getAnnotations()));

        } else if (Integer[].class.isAssignableFrom(javaType)) {
            return BoxedIntegerArrayParameterConverter.getInstance(runtime, ParameterFlags.parse(context.getAnnotations()));

        } else if (Long[].class.isAssignableFrom(javaType)) {
            return sizeof(getNativeType(runtime, javaType.getComponentType(), context.getAnnotations())) == 4
                ? BoxedLong32ArrayParameterConverter.getInstance(runtime, ParameterFlags.parse(context.getAnnotations()))
                : BoxedLong64ArrayParameterConverter.getInstance(runtime, ParameterFlags.parse(context.getAnnotations()));

        } else if (NativeLong[].class.isAssignableFrom(javaType)) {
            return sizeof(getNativeType(runtime, javaType.getComponentType(), context.getAnnotations())) == 4
                    ? NativeLong32ArrayParameterConverter.getInstance(runtime, ParameterFlags.parse(context.getAnnotations()))
                    : NativeLong64ArrayParameterConverter.getInstance(runtime, ParameterFlags.parse(context.getAnnotations()));

        } else if (Float[].class.isAssignableFrom(javaType)) {
            return BoxedFloatArrayParameterConverter.getInstance(runtime, ParameterFlags.parse(context.getAnnotations()));

        } else if (Double[].class.isAssignableFrom(javaType)) {
            return BoxedDoubleArrayParameterConverter.getInstance(runtime, ParameterFlags.parse(context.getAnnotations()));

        } else if (Boolean[].class.isAssignableFrom(javaType)) {
            return BoxedBooleanArrayParameterConverter.getInstance(runtime, ParameterFlags.parse(context.getAnnotations()));

        } else if (javaType.isArray() && Pointer.class.isAssignableFrom(javaType.getComponentType())) {
            return runtime.addressSize() == 4
                    ? Pointer32ArrayParameterConverter.getInstance(runtime, ParameterFlags.parse(context.getAnnotations()))
                    : Pointer64ArrayParameterConverter.getInstance(runtime, ParameterFlags.parse(context.getAnnotations()));

        } else if (long[].class.isAssignableFrom(javaType) && sizeof(getNativeType(runtime, javaType.getComponentType(), context.getAnnotations())) == 4) {
            return Long32ArrayParameterConverter.getInstance(runtime, ParameterFlags.parse(context.getAnnotations()));

        } else if (javaType.isArray() && Struct.class.isAssignableFrom(javaType.getComponentType())) {
            return StructArrayParameterConverter.getInstance(runtime, javaType.getComponentType(), ParameterFlags.parse(context.getAnnotations()));

        } else if (javaType.isArray() && String.class.isAssignableFrom(javaType.getComponentType())) {
            return StringArrayParameterConverter.getInstance(runtime, ParameterFlags.parse(context.getAnnotations()));

        } else {
            return null;
        }
    }


    @Override
    public FromNativeType getFromNativeType(jnr.ffi.Runtime runtime, SignatureType type, FromNativeContext context) {
        return FromNativeTypes.create(getFromNativeConverter(type, context));
    }

    @Override
    public ToNativeType getToNativeType(jnr.ffi.Runtime runtime, SignatureType type, ToNativeContext context) {
        return ToNativeTypes.create(getToNativeConverter(type, context));
    }
}
