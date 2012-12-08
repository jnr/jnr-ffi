package jnr.ffi.provider.jffi;

import jnr.ffi.Struct;
import jnr.ffi.mapper.*;
import jnr.ffi.provider.converters.EnumConverter;
import jnr.ffi.provider.ParameterFlags;
import jnr.ffi.provider.converters.StringResultConverter;

import java.nio.charset.Charset;

final class ClosureTypeMapper implements SignatureTypeMapper {
    public FromNativeConverter getFromNativeConverter(SignatureType type, FromNativeContext context) {
        if (Enum.class.isAssignableFrom(type.getDeclaredType())) {
            return EnumConverter.getInstance(type.getDeclaredType().asSubclass(Enum.class));

        } else if (CharSequence.class.isAssignableFrom(type.getDeclaredType())) {
            return StringResultConverter.getInstance(Charset.defaultCharset());

        } else {
            return null;
        }
    }

    public ToNativeConverter getToNativeConverter(SignatureType type, ToNativeContext context) {
        if (Enum.class.isAssignableFrom(type.getDeclaredType())) {
            return EnumConverter.getInstance(type.getDeclaredType().asSubclass(Enum.class));

        } else if (Struct.class.isAssignableFrom(type.getDeclaredType())) {
            return new StructByReferenceToNativeConverter(ParameterFlags.parse(context.getAnnotations()));


        } else {
            return null;
        }
    }
}
