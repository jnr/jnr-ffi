
package com.kenai.jaffl.provider.jffi;

import com.kenai.jaffl.Address;
import com.kenai.jaffl.LibraryOption;
import com.kenai.jaffl.NativeLong;
import com.kenai.jaffl.Platform;
import com.kenai.jaffl.Pointer;
import com.kenai.jaffl.annotations.IgnoreError;
import com.kenai.jaffl.annotations.SaveError;
import com.kenai.jaffl.byref.ByReference;
import com.kenai.jaffl.struct.Struct;
import com.kenai.jffi.Type;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.Buffer;
import java.util.Map;

final class InvokerUtil {
    static final Type getNativeReturnType(Method method) {
        return getNativeReturnType(method.getReturnType());
    }
    
    static final Type getNativeReturnType(Class type) {
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
            return Type.SINT64;
        } else if (NativeLong.class.isAssignableFrom(type)) {
            return Platform.getPlatform().longSize() == 32 ? Type.SINT32: Type.SINT64;
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
    static final Type getNativeParameterType(Class type) {
        if (Byte.class.isAssignableFrom(type) || byte.class == type) {
            return Type.SINT8;
        } else if (Short.class.isAssignableFrom(type) || short.class == type) {
            return Type.SINT16;
        } else if (Integer.class.isAssignableFrom(type) || int.class == type) {
            return Type.SINT32;
        } else if (Long.class.isAssignableFrom(type) || long.class == type) {
            return Type.SINT64;
        } else if (NativeLong.class.isAssignableFrom(type)) {
            return Platform.getPlatform().longSize() == 32 ? Type.SINT32: Type.SINT64;
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
        com.kenai.jaffl.CallingConvention convention = (com.kenai.jaffl.CallingConvention) libraryOptions.get(LibraryOption.CallingConvention);

        if (convention != null) switch (convention) {
            case DEFAULT:
                return com.kenai.jffi.CallingConvention.DEFAULT;
            case STDCALL:
                return com.kenai.jffi.CallingConvention.STDCALL;
        }

        return com.kenai.jffi.CallingConvention.DEFAULT;
    }
}
