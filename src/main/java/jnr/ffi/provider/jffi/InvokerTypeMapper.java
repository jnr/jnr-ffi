/*
 * Copyright (C) 2012 Wayne Meissner
 *
 * This file is part of the JNR project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jnr.ffi.provider.jffi;

import jnr.ffi.NativeLong;
import jnr.ffi.Pointer;
import jnr.ffi.Struct;
import jnr.ffi.annotations.Delegate;
import jnr.ffi.byref.ByReference;
import jnr.ffi.mapper.*;
import jnr.ffi.mapper.FromNativeType;
import jnr.ffi.mapper.ToNativeType;
import jnr.ffi.provider.ParameterFlags;
import jnr.ffi.provider.converters.*;

import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.Set;

final class InvokerTypeMapper extends AbstractSignatureTypeMapper implements SignatureTypeMapper {
    private final NativeClosureManager closureManager;
    private final AsmClassLoader classLoader;
    private final StructByReferenceResultConverterFactory structResultConverterFactory;
    

    public InvokerTypeMapper(NativeClosureManager closureManager, AsmClassLoader classLoader, boolean asmEnabled) {
        this.closureManager = closureManager;
        this.classLoader = classLoader;
        this.structResultConverterFactory = new StructByReferenceResultConverterFactory(classLoader, asmEnabled);
    }

    public FromNativeConverter getFromNativeConverter(SignatureType signatureType, FromNativeContext fromNativeContext) {
        FromNativeConverter converter;

        if (Enum.class.isAssignableFrom(signatureType.getDeclaredType())) {
            return EnumConverter.getInstance(signatureType.getDeclaredType().asSubclass(Enum.class));

        } else if (Struct.class.isAssignableFrom(signatureType.getDeclaredType())) {
            return structResultConverterFactory.get(signatureType.getDeclaredType().asSubclass(Struct.class), fromNativeContext);

        } else if (closureManager != null && isDelegate(signatureType.getDeclaredType())) {
            return ClosureFromNativeConverter.getInstance(fromNativeContext.getRuntime(), signatureType, classLoader, this);

        } else if (NativeLong.class == signatureType.getDeclaredType()) {
            return NativeLongConverter.getInstance();

        } else if (String.class == signatureType.getDeclaredType() || CharSequence.class == signatureType.getDeclaredType()) {
            return StringResultConverter.getInstance(fromNativeContext);

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
            return ByReferenceParameterConverter.getInstance(context);

        } else if (Struct.class.isAssignableFrom(javaType)) {
            return StructByReferenceToNativeConverter.getInstance(context);

        } else if (NativeLong.class.isAssignableFrom(javaType)) {
            return NativeLongConverter.getInstance();

        } else if (StringBuilder.class.isAssignableFrom(javaType)) {
            return StringBuilderParameterConverter.getInstance(ParameterFlags.parse(context.getAnnotations()), context);

        } else if (StringBuffer.class.isAssignableFrom(javaType)) {
            return StringBufferParameterConverter.getInstance(ParameterFlags.parse(context.getAnnotations()), context);

        } else if (CharSequence.class.isAssignableFrom(javaType)) {
            return CharSequenceParameterConverter.getInstance(context);

        } else if (Byte[].class.isAssignableFrom(javaType)) {
            return BoxedByteArrayParameterConverter.getInstance(context);

        } else if (Short[].class.isAssignableFrom(javaType)) {
            return BoxedShortArrayParameterConverter.getInstance(context);

        } else if (Integer[].class.isAssignableFrom(javaType)) {
            return BoxedIntegerArrayParameterConverter.getInstance(context);

        } else if (Long[].class.isAssignableFrom(javaType)) {
            return Types.getType(context.getRuntime(), javaType.getComponentType(), context.getAnnotations()).size() == 4
                ? BoxedLong32ArrayParameterConverter.getInstance(context)
                : BoxedLong64ArrayParameterConverter.getInstance(context);

        } else if (NativeLong[].class.isAssignableFrom(javaType)) {
            return Types.getType(context.getRuntime(), javaType.getComponentType(), context.getAnnotations()).size() == 4
                    ? NativeLong32ArrayParameterConverter.getInstance(context)
                    : NativeLong64ArrayParameterConverter.getInstance(context);

        } else if (Float[].class.isAssignableFrom(javaType)) {
            return BoxedFloatArrayParameterConverter.getInstance(context);

        } else if (Double[].class.isAssignableFrom(javaType)) {
            return BoxedDoubleArrayParameterConverter.getInstance(context);

        } else if (Boolean[].class.isAssignableFrom(javaType)) {
            return BoxedBooleanArrayParameterConverter.getInstance(context);

        } else if (javaType.isArray() && Pointer.class.isAssignableFrom(javaType.getComponentType())) {
            return context.getRuntime().addressSize() == 4
                    ? Pointer32ArrayParameterConverter.getInstance(context)
                    : Pointer64ArrayParameterConverter.getInstance(context);

        } else if (long[].class.isAssignableFrom(javaType) && Types.getType(context.getRuntime(), javaType.getComponentType(), context.getAnnotations()).size() == 4) {
            return Long32ArrayParameterConverter.getInstance(context);

        } else if (javaType.isArray() && Struct.class.isAssignableFrom(javaType.getComponentType())) {
            return StructArrayParameterConverter.getInstance(context, javaType.getComponentType());

        } else if (javaType.isArray() && CharSequence.class.isAssignableFrom(javaType.getComponentType())) {
            return CharSequenceArrayParameterConverter.getInstance(context);

        } else {
            return null;
        }
    }


    @Override
    public FromNativeType getFromNativeType(SignatureType type, FromNativeContext context) {
        return FromNativeTypes.create(getFromNativeConverter(type, context));
    }

    @Override
    public ToNativeType getToNativeType(SignatureType type, ToNativeContext context) {
        return ToNativeTypes.create(getToNativeConverter(type, context));
    }


    private static boolean isDelegate(Class klass) {
        for (Method m : klass.getMethods()) {
            if (m.isAnnotationPresent(Delegate.class)) {
                return true;
            }
        }

        return false;
    }

}
