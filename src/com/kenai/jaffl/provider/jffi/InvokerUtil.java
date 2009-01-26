
package com.kenai.jaffl.provider.jffi;

import com.kenai.jaffl.NativeLong;
import com.kenai.jaffl.Platform;
import com.kenai.jaffl.Pointer;
import com.kenai.jaffl.byref.ByReference;
import com.kenai.jaffl.struct.Struct;
import com.kenai.jffi.Type;
import java.lang.reflect.Method;
import java.nio.Buffer;

final class InvokerUtil {
    static final Type getNativeReturnType(Method method) {
        Class type = method.getReturnType();
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
        } else if (Struct.class.isAssignableFrom(type)) {
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
}
