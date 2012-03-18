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

import jnr.ffi.*;
import jnr.ffi.annotations.IgnoreError;
import jnr.ffi.annotations.LongLong;
import jnr.ffi.annotations.SaveError;
import jnr.ffi.annotations.TypeDefinition;
import jnr.ffi.byref.ByReference;
import jnr.ffi.Struct;
import com.kenai.jffi.Platform;
import com.kenai.jffi.Type;
import jnr.ffi.mapper.FromNativeConverter;
import jnr.ffi.mapper.ToNativeConverter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.Buffer;
import java.util.Map;

import static jnr.ffi.provider.jffi.AsmUtil.isDelegate;

final class InvokerUtil {

    static Type getAliasedType(NativeRuntime runtime, Class type, final Annotation[] annotations) {
        for (Annotation a : annotations) {
            TypeDefinition typedef = a.annotationType().getAnnotation(TypeDefinition.class);
            if (typedef != null) {
                return jffiType(runtime.findType(typedef.alias()));
            }
        }

        if (isLong32(type, annotations)) {
            return Type.SLONG;

        } else if (isLongLong(type, annotations)) {
            return Type.SLONG_LONG;
        }

        return null;
    }

    static Type getNativeReturnType(NativeRuntime runtime, Class type, final Annotation[] annotations) {
        Type resultType = getAliasedType(runtime, type, annotations);
        return resultType != null ? resultType : getNativeReturnType(type, annotations);
    }


    static Type getNativeReturnType(Class type, final Annotation[] annotations) {
        if (Void.class.isAssignableFrom(type) || void.class == type) {
            return Type.VOID;
        
        } else if (Boolean.class.isAssignableFrom(type) || boolean.class == type) {
            return Type.SINT32;
        
        } else if (Byte.class.isAssignableFrom(type) || byte.class == type) {
            return Type.SINT8;
        
        } else if (Short.class.isAssignableFrom(type) || short.class == type) {
            return Type.SINT16;
        
        } else if (Integer.class.isAssignableFrom(type) || int.class == type) {
            return Type.SINT32;
        
        } else if (Long.class.isAssignableFrom(type) || long.class == type) {
            return isLongLong(type, annotations) ? Type.SLONG_LONG : Type.SLONG;
        
        } else if (NativeLong.class.isAssignableFrom(type)) {
            return Type.SLONG;
        
        } else if (Float.class.isAssignableFrom(type) || float.class == type) {
            return Type.FLOAT;
        
        } else if (Double.class.isAssignableFrom(type) || double.class == type) {
            return Type.DOUBLE;
        
        } else if (Enum.class.isAssignableFrom(type)) {
            return Type.SINT32;
        
        } else if (Pointer.class.isAssignableFrom(type)) {
            return Type.POINTER;
        
        } else if (Address.class.isAssignableFrom(type)) {
            return Type.POINTER;
        
        } else if (Struct.class.isAssignableFrom(type)) {
            return Type.POINTER;
        
        } else if (String.class.isAssignableFrom(type)) {
            return Type.POINTER;
        
        } else {
            throw new IllegalArgumentException("Unsupported return type: " + type);
        }
    }

    static Type getNativeParameterType(NativeRuntime runtime, Class type, final Annotation[] annotations) {
        Type nativeType = getAliasedType(runtime, type, annotations);
        return nativeType != null ? nativeType : getNativeParameterType(type, annotations);
    }

    static final Type getNativeParameterType(Class type, final Annotation[] annotations) {
        if (Byte.class.isAssignableFrom(type) || byte.class == type) {
            return Type.SINT8;
        
        } else if (Short.class.isAssignableFrom(type) || short.class == type) {
            return Type.SINT16;
        
        } else if (Integer.class.isAssignableFrom(type) || int.class == type) {
            return Type.SINT32;
        
        } else if (Long.class.isAssignableFrom(type) || long.class == type) {
            return isLongLong(type, annotations) ? Type.SLONG_LONG : Type.SLONG;
        
        } else if (NativeLong.class.isAssignableFrom(type)) {
            return Type.SLONG;
        
        } else if (Float.class.isAssignableFrom(type) || float.class == type) {
            return Type.FLOAT;
        
        } else if (Double.class.isAssignableFrom(type) || double.class == type) {
            return Type.DOUBLE;
        
        } else if (Boolean.class.isAssignableFrom(type) || boolean.class == type) {
            return Type.SINT32;
        
        } else if (Enum.class.isAssignableFrom(type)) {
            return Type.SINT32;
        
        } else if (Pointer.class.isAssignableFrom(type)) {
            return Type.POINTER;
        
        } else if (Address.class.isAssignableFrom(type)) {
            return Type.POINTER;
        
        } else if (Struct.class.isAssignableFrom(type)) {
            return Type.POINTER;
        
        } else if (Buffer.class.isAssignableFrom(type)) {
            return Type.POINTER;
        
        } else if (CharSequence.class.isAssignableFrom(type)) {
            return Type.POINTER;
        
        } else if (ByReference.class.isAssignableFrom(type)) {
            return Type.POINTER;
        
        } else if (type.isArray()) {
            return Type.POINTER;

        } else if (isDelegate(type)) {
            return Type.POINTER;
        
        } else {
            throw new IllegalArgumentException("Unsupported parameter type: " + type);
        }
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

    public static final com.kenai.jffi.CallingConvention getCallingConvention(Map<LibraryOption, ?> libraryOptions) {
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
                throw new IllegalArgumentException("Unsupported parameter type: " + jnrType);
        }
    }

    static Type jffiType(jnr.ffi.Type jnrType) {
        return jffiType(jnrType.getNativeType());
    }

    static ResultType getResultType(NativeRuntime runtime, Class type, Annotation[] annotations, FromNativeConverter fromNativeConverter) {
        return new ResultType(type, getNativeReturnType(runtime, fromNativeConverter != null ? fromNativeConverter.nativeType() : type, annotations), annotations, fromNativeConverter);
    }

    static ParameterType getParameterType(NativeRuntime runtime, Class type, Annotation[] annotations, ToNativeConverter toNativeConverter) {
        return new ParameterType(type, getNativeParameterType(runtime, toNativeConverter != null ? toNativeConverter.nativeType() : type, annotations), annotations, toNativeConverter);
    }
}
