package jnr.ffi.provider.jffi;

import com.kenai.jffi.Type;
import jnr.ffi.NativeType;

import java.lang.annotation.Annotation;

import static jnr.ffi.provider.jffi.NumberUtil.sizeof;

/**
 *
 */
abstract class SigType {
    final Class javaType, convertedType;
    final NativeType nativeType;
    final Annotation[] annotations;


    SigType(Class javaType, com.kenai.jffi.Type jffiType, Annotation[] annotations, Class convertedType) {
        this.javaType = javaType;
        this.annotations = annotations.clone();
        this.convertedType = convertedType;
        this.nativeType = getNativeType(jffiType);
    }

    final Class getDeclaredType() {
        return javaType;
    }

    final Class effectiveJavaType() {
        return convertedType;
    }

    final int size() {
        return sizeof(nativeType);
    }

    private static NativeType getNativeType(Type type) {
        if (Type.SCHAR == type || Type.SINT8 == type) {
            return NativeType.SCHAR;

        } else if (Type.UCHAR == type || Type.UINT8 == type) {
            return NativeType.UCHAR;
        }
        else if (Type.SSHORT == type || Type.SINT16 == type) {
            return NativeType.SSHORT;

        } else if (Type.USHORT == type || Type.UINT16 == type) {
            return NativeType.USHORT;

        } else if (Type.SINT == type || Type.SINT32 == type) {
            return NativeType.SINT;

        } else if (Type.UINT == type || Type.UINT32 == type) {
            return NativeType.UINT;

        } else if (Type.SLONG == type) {
            return NativeType.SLONG;

        } else if (Type.ULONG == type) {
            return NativeType.ULONG;

        } else if (Type.SLONG_LONG == type || Type.SINT64 == type) {
            return NativeType.SLONGLONG;

        } else if (Type.ULONG_LONG == type || Type.UINT64 == type) {
            return NativeType.ULONGLONG;

        } else if (Type.FLOAT == type) {
            return NativeType.FLOAT;

        } else if (Type.DOUBLE == type) {
            return NativeType.DOUBLE;

        } else if (Type.VOID == type) {
            return NativeType.VOID;

        } else if (Type.POINTER == type) {
            return NativeType.ADDRESS;

        } else {
            throw new IllegalArgumentException("unknown type: " + type);
        }
    }
}
