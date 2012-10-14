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


    public static final boolean requiresErrno(Method method) {
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

    static ResultType getResultType(NativeRuntime runtime, Class type, Annotation[] annotations, FromNativeConverter fromNativeConverter) {
        NativeType nativeType = getMethodResultNativeType(runtime, fromNativeConverter != null ? fromNativeConverter.nativeType() : type, annotations);
        return new ResultType(type, nativeType, annotations, fromNativeConverter);
    }

    static ParameterType getParameterType(NativeRuntime runtime, Class type, Annotation[] annotations, ToNativeConverter toNativeConverter) {
        NativeType nativeType = getMethodParameterNativeType(runtime, toNativeConverter != null ? toNativeConverter.nativeType() : type, annotations);
        return new ParameterType(type, nativeType, annotations, toNativeConverter);
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
        if (m.getAnnotation(StdCall.class) != null || m.getDeclaringClass().getAnnotation(StdCall.class) != null) {
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

        } else if (NativeLong.class.isAssignableFrom(type)) {
            return NativeType.SLONG;

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
                                                        TypeMapper typeMapper, NativeClosureManager closureManager,
                                                        ToNativeContext toNativeContext) {
        ToNativeConverter conv = typeMapper.getToNativeConverter(javaType);
        if (conv != null) {
            return new ParameterConverter(conv, toNativeContext);

        } else if (Enum.class.isAssignableFrom(javaType)) {
            return EnumMapper.getInstance(javaType.asSubclass(Enum.class));

        } else if (isDelegate(javaType)) {
            return closureManager.newClosureSite(javaType);

        } else if (ByReference.class.isAssignableFrom(javaType)) {
            return new ByReferenceParameterConverter(ParameterFlags.parse(annotations));

        } else {
            return null;
        }
    }


    static FromNativeConverter getFromNativeConverter(Class javaType, Annotation[] annotations,
                                                              TypeMapper typeMapper, NativeClosureManager closureManager,
                                                              FromNativeContext fromNativeContext) {
        FromNativeConverter conv = typeMapper.getFromNativeConverter(javaType);
        if (conv != null) {
            return new ResultConverter(conv, fromNativeContext);

        } else if (Enum.class.isAssignableFrom(javaType)) {
            return EnumMapper.getInstance(javaType.asSubclass(Enum.class));

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

        } else {
            return null;
        }
    }

}
