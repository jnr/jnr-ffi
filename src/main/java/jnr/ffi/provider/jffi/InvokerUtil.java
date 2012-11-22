/*
 * Copyright (C) 2008-2010 Wayne Meissner
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

import com.kenai.jffi.*;
import com.kenai.jffi.CallingConvention;
import com.kenai.jffi.Platform;
import com.kenai.jffi.Type;
import jnr.ffi.*;
import jnr.ffi.NativeType;
import jnr.ffi.Struct;
import jnr.ffi.annotations.*;
import jnr.ffi.byref.ByReference;
import jnr.ffi.mapper.*;
import jnr.ffi.provider.EnumConverter;
import jnr.ffi.provider.ParameterFlags;
import jnr.ffi.util.EnumMapper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.Buffer;
import java.util.Map;

import static jnr.ffi.provider.jffi.AsmUtil.isDelegate;

final class InvokerUtil {

    static NativeType getAliasedNativeType(NativeRuntime runtime, Class type, final Annotation[] annotations) {
        for (Annotation a : annotations) {
            TypeDefinition typedef = a.annotationType().getAnnotation(TypeDefinition.class);
            if (typedef != null) {
                return nativeType(runtime.findType(typedef.alias()));
            }
        }

        if (isLong32(type, annotations)) {
            return NativeType.SLONG;

        } else if (isLongLong(type, annotations)) {
            return NativeType.SLONGLONG;
        }

        return null;
    }


    public static boolean requiresErrno(Method method) {
        boolean saveError = true;
        for (Annotation a : method.getAnnotations()) {
            if (a instanceof IgnoreError) {
                saveError = false;
            } else if (a instanceof SaveError) {
                saveError = true;
            }
        }
        return saveError;
    }

    public static com.kenai.jffi.CallingConvention getCallingConvention(Map<LibraryOption, ?> libraryOptions) {
        Object convention = libraryOptions.get(LibraryOption.CallingConvention);

        // If someone passed in the jffi calling convention, just use it.
        if (convention instanceof com.kenai.jffi.CallingConvention) {
            return (com.kenai.jffi.CallingConvention) convention;
        }
        
        if (convention instanceof jnr.ffi.CallingConvention) switch ((jnr.ffi.CallingConvention) convention) {
            case DEFAULT:
                return com.kenai.jffi.CallingConvention.DEFAULT;
            case STDCALL:
                return com.kenai.jffi.CallingConvention.STDCALL;

        } else if (convention != null) {
            throw new IllegalArgumentException("unknown calling convention: " + convention);
        }

        return com.kenai.jffi.CallingConvention.DEFAULT;
    }
    
    public static boolean hasAnnotation(Annotation[] annotations, Class<? extends Annotation> annotationClass) {
        for (Annotation a : annotations) {
            if (annotationClass.isInstance(a)) {
                return true;
            }
        }

        return false;
    }
    
    static boolean isLong32(Class type, Annotation[] annotations) {
        return (long.class == type || Long.class.isAssignableFrom(type))
                && Platform.getPlatform().longSize() == 32
                && !hasAnnotation(annotations, LongLong.class);
    }
    
    static boolean isLongLong(Class type, Annotation[] annotations) {
        return (long.class == type || Long.class.isAssignableFrom(type))
            && (hasAnnotation(annotations, LongLong.class) || Platform.getPlatform().longSize() == 64);
    }

    static Type jffiType(jnr.ffi.NativeType jnrType) {
        switch (jnrType) {
            case VOID:
                return Type.VOID;
            case SCHAR:
                return Type.SCHAR;
            case UCHAR:
                return Type.UCHAR;
            case SSHORT:
                return Type.SSHORT;
            case USHORT:
                return Type.USHORT;
            case SINT:
                return Type.SINT;
            case UINT:
                return Type.UINT;
            case SLONG:
                return Type.SLONG;
            case ULONG:
                return Type.ULONG;
            case SLONGLONG:
                return Type.SLONG_LONG;
            case ULONGLONG:
                return Type.ULONG_LONG;
            case FLOAT:
                return Type.FLOAT;
            case DOUBLE:
                return Type.DOUBLE;
            case ADDRESS:
                return Type.POINTER;
            default:
                throw new IllegalArgumentException("unsupported parameter type: " + jnrType);
        }
    }

    static NativeType nativeType(jnr.ffi.Type jnrType) {
        return jnrType.getNativeType();
    }

    static ResultType getResultType(NativeRuntime runtime, Class type, Annotation[] annotations,
                                    FromNativeConverter fromNativeConverter, FromNativeContext fromNativeContext) {
        NativeType nativeType = getMethodResultNativeType(runtime, fromNativeConverter != null ? fromNativeConverter.nativeType() : type, annotations);
        boolean useContext = fromNativeConverter != null && !fromNativeConverter.getClass().isAnnotationPresent(FromNativeConverter.NoContext.class);
        return new ResultType(type, nativeType, annotations, fromNativeConverter, useContext ? fromNativeContext : null);
    }

    static ParameterType getParameterType(NativeRuntime runtime, Class type, Annotation[] annotations,
                                          ToNativeConverter toNativeConverter) {
        NativeType nativeType = getMethodParameterNativeType(runtime, toNativeConverter != null ? toNativeConverter.nativeType() : type, annotations);
        return new ParameterType(type, nativeType, annotations, toNativeConverter, null);
    }

    static ParameterType getParameterType(NativeRuntime runtime, Class type, Annotation[] annotations,
                                          ToNativeConverter toNativeConverter, ToNativeContext toNativeContext) {
        NativeType nativeType = getMethodParameterNativeType(runtime, toNativeConverter != null ? toNativeConverter.nativeType() : type, annotations);
        return new ParameterType(type, nativeType, annotations, toNativeConverter, toNativeContext);
    }

    static ParameterType[] getParameterTypes(NativeRuntime runtime, TypeMapper typeMapper, NativeClosureManager closureManager,
                                             Method m) {
        final Class[] javaParameterTypes = m.getParameterTypes();
        final Annotation[][] parameterAnnotations = m.getParameterAnnotations();
        ParameterType[] parameterTypes = new ParameterType[javaParameterTypes.length];

        for (int pidx = 0; pidx < javaParameterTypes.length; ++pidx) {

            ToNativeConverter toNativeConverter = getToNativeConverter(javaParameterTypes[pidx], parameterAnnotations[pidx],
                    typeMapper, closureManager);
            ToNativeContext toNativeContext = toNativeConverter != null && !toNativeConverter.getClass().isAnnotationPresent(ToNativeConverter.NoContext.class)
                ? new MethodParameterContext(m, pidx) : null;
            parameterTypes[pidx] = getParameterType(runtime, javaParameterTypes[pidx],
                    parameterAnnotations[pidx], toNativeConverter, toNativeContext);
        }

        return parameterTypes;
    }

    static CallContext getCallContext(SigType resultType, SigType[] parameterTypes, com.kenai.jffi.CallingConvention convention, boolean requiresErrno) {
        com.kenai.jffi.Type[] nativeParamTypes = new com.kenai.jffi.Type[parameterTypes.length];

        for (int i = 0; i < nativeParamTypes.length; ++i) {
            nativeParamTypes[i] = jffiType(parameterTypes[i].nativeType);
        }

        return CallContextCache.getInstance().getCallContext(jffiType(resultType.nativeType),
                nativeParamTypes, convention, requiresErrno);
    }

    static CallContext getCallContext(NativeType resultType, NativeType[] parameterTypes, com.kenai.jffi.CallingConvention convention, boolean requiresErrno) {
        com.kenai.jffi.Type[] nativeParamTypes = new com.kenai.jffi.Type[parameterTypes.length];

        for (int i = 0; i < nativeParamTypes.length; ++i) {
            nativeParamTypes[i] = jffiType(parameterTypes[i]);
        }

        return CallContextCache.getInstance().getCallContext(jffiType(resultType),
                nativeParamTypes, convention, requiresErrno);
    }

    public static com.kenai.jffi.CallingConvention getNativeCallingConvention(Method m) {
        if (m.isAnnotationPresent(StdCall.class) || m.getDeclaringClass().isAnnotationPresent(StdCall.class)) {
            return CallingConvention.STDCALL;
        }

        return CallingConvention.DEFAULT;
    }

    static NativeType getNativeType(NativeRuntime runtime, Class type, final Annotation[] annotations) {
        NativeType aliasedType = getAliasedNativeType(runtime, type, annotations);
        if (aliasedType != null) {
            return aliasedType;

        } else if (Void.class.isAssignableFrom(type) || void.class == type) {
            return NativeType.VOID;

        } else if (Boolean.class.isAssignableFrom(type) || boolean.class == type) {
            return NativeType.SINT;

        } else if (Byte.class.isAssignableFrom(type) || byte.class == type) {
            return NativeType.SCHAR;

        } else if (Short.class.isAssignableFrom(type) || short.class == type) {
            return NativeType.SSHORT;

        } else if (Integer.class.isAssignableFrom(type) || int.class == type) {
            return NativeType.SINT;

        } else if (Long.class.isAssignableFrom(type) || long.class == type) {
            return isLongLong(type, annotations) ? NativeType.SLONGLONG : NativeType.SLONG;

        } else if (Float.class.isAssignableFrom(type) || float.class == type) {
            return NativeType.FLOAT;

        } else if (Double.class.isAssignableFrom(type) || double.class == type) {
            return NativeType.DOUBLE;

        } else if (Pointer.class.isAssignableFrom(type)) {
            return NativeType.ADDRESS;

        } else if (Address.class.isAssignableFrom(type)) {
            return NativeType.ADDRESS;

        } else if (Struct.class.isAssignableFrom(type)) {
            return NativeType.ADDRESS;

        } else if (String.class.isAssignableFrom(type)) {
            return NativeType.ADDRESS;

        } else if (Buffer.class.isAssignableFrom(type)) {
            return NativeType.ADDRESS;

        } else if (CharSequence.class.isAssignableFrom(type)) {
            return NativeType.ADDRESS;

        } else if (ByReference.class.isAssignableFrom(type)) {
            return NativeType.ADDRESS;

        } else if (type.isArray()) {
            return NativeType.ADDRESS;

        } else if (isDelegate(type)) {
            return NativeType.ADDRESS;

        } else {
            throw new IllegalArgumentException("unsupported type: " + type);
        }
    }

    static NativeType getMethodParameterNativeType(NativeRuntime runtime, Class parameterClass, Annotation[] annotations) {
        return getNativeType(runtime, parameterClass, annotations);
    }

    static NativeType getMethodResultNativeType(NativeRuntime runtime, Class parameterClass, Annotation[] annotations) {
        return getNativeType(runtime, parameterClass, annotations);
    }


    static ToNativeConverter getToNativeConverter(Class javaType, Annotation[] annotations,
                                                        TypeMapper typeMapper, NativeClosureManager closureManager) {
        ToNativeConverter conv = typeMapper.getToNativeConverter(javaType);
        if (conv != null) {
            return conv;

        } else if (Enum.class.isAssignableFrom(javaType)) {
            return EnumConverter.getInstance(javaType.asSubclass(Enum.class));

        } else if (isDelegate(javaType)) {
            return closureManager.newClosureSite(javaType);

        } else if (ByReference.class.isAssignableFrom(javaType)) {
            return new ByReferenceParameterConverter(ParameterFlags.parse(annotations));

        } else if (Struct.class.isAssignableFrom(javaType)) {
            return new StructByReferenceToNativeConverter(ParameterFlags.parse(annotations));

        } else if (NativeLong.class.isAssignableFrom(javaType)) {
            return NativeLongConverter.INSTANCE;

        } else if (StringBuilder.class.isAssignableFrom(javaType)) {
            return StringBuilderParameterConverter.getInstance(NativeRuntime.getInstance(), ParameterFlags.parse(annotations));

        } else if (StringBuffer.class.isAssignableFrom(javaType)) {
            return StringBufferParameterConverter.getInstance(NativeRuntime.getInstance(), ParameterFlags.parse(annotations));

        } else {
            return null;
        }
    }


    static FromNativeConverter getFromNativeConverter(Class javaType, Annotation[] annotations,
                                                              TypeMapper typeMapper, NativeClosureManager closureManager) {
        FromNativeConverter conv = typeMapper.getFromNativeConverter(javaType);
        if (conv != null) {
            return conv;

        } else if (Enum.class.isAssignableFrom(javaType)) {
            return EnumConverter.getInstance(javaType.asSubclass(Enum.class));

        } else if (Struct.class.isAssignableFrom(javaType)) {
            return StructByReferenceFromNativeConverter.newStructByReferenceConverter(javaType.asSubclass(Struct.class),
                    ParameterFlags.parse(annotations));

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

    static void generateFunctionInvocation(NativeRuntime runtime, AsmBuilder builder, Method m, long functionAddress, CallingConvention callingConvention, boolean saveErrno, TypeMapper typeMapper, NativeClosureManager closureManager, MethodGenerator[] generators) {
        ResultType resultType = getResultType(runtime, m.getReturnType(),
                m.getAnnotations(),
                getFromNativeConverter(m.getReturnType(), m.getAnnotations(), typeMapper, closureManager),
                new MethodResultContext(m));

        ParameterType[] parameterTypes = getParameterTypes(runtime, typeMapper, closureManager, m);

        Function function = getFunction(functionAddress,
                resultType, parameterTypes, saveErrno, callingConvention);

        for (MethodGenerator g : generators) {
            if (g.isSupported(resultType, parameterTypes, callingConvention)) {
                g.generate(builder, m.getName(), function, resultType, parameterTypes, !saveErrno);
                break;
            }
        }
    }


    private static Function getFunction(long address, ResultType resultType, ParameterType[] parameterTypes,
                                        boolean requiresErrno, CallingConvention convention) {
        return new Function(address, getCallContext(resultType, parameterTypes, convention, requiresErrno));
    }

    static boolean isReturnTypeSupported(Class type) {
        return type.isPrimitive() || Byte.class == type
                || Short.class == type || Integer.class == type
                || Long.class == type || Float.class == type
                || Double.class == type
                || Enum.class.isAssignableFrom(type)
                || Pointer.class == type || Address.class == type
                || String.class == type
                || Struct.class.isAssignableFrom(type)
                || Variable.class == type
                ;

    }

    static boolean isParameterTypeSupported(Class type) {
        return type.isPrimitive() || Byte.class == type
                || Short.class == type || Integer.class == type
                || Long.class == type || Float.class == type
                || Double.class == type
                || Pointer.class.isAssignableFrom(type) || Address.class.isAssignableFrom(type)
                || Enum.class.isAssignableFrom(type)
                || Buffer.class.isAssignableFrom(type)
                || (type.isArray() && type.getComponentType().isPrimitive())
                || Struct.class.isAssignableFrom(type)
                || (type.isArray() && Struct.class.isAssignableFrom(type.getComponentType()))
                || (type.isArray() && Pointer.class.isAssignableFrom(type.getComponentType()))
                || (type.isArray() && CharSequence.class.isAssignableFrom(type.getComponentType()))
                || CharSequence.class.isAssignableFrom(type)
                || ByReference.class.isAssignableFrom(type)
                || StringBuilder.class.isAssignableFrom(type)
                || StringBuffer.class.isAssignableFrom(type)
                || isDelegate(type)
                ;
    }



}
