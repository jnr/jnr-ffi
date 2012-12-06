package jnr.ffi.provider.jffi;

import jnr.ffi.Struct;
import jnr.ffi.mapper.*;
import jnr.ffi.provider.converters.EnumConverter;
import jnr.ffi.provider.ParameterFlags;
import jnr.ffi.provider.converters.StringResultConverter;

import java.nio.charset.Charset;

final class ClosureTypeMapper implements TypeMapper {
    public FromNativeConverter getFromNativeConverter(Class type, FromNativeContext context) {
        if (Enum.class.isAssignableFrom(type)) {
            return EnumConverter.getInstance(type.asSubclass(Enum.class));

        } else if (CharSequence.class.isAssignableFrom(type)) {
            return StringResultConverter.getInstance(Charset.defaultCharset());

        } else {
            return null;
        }
    }

    public ToNativeConverter getToNativeConverter(Class type, ToNativeContext context) {
        if (Enum.class.isAssignableFrom(type)) {
            return EnumConverter.getInstance(type.asSubclass(Enum.class));

        } else if (Struct.class.isAssignableFrom(type)) {
            return new StructByReferenceToNativeConverter(ParameterFlags.parse(context.getAnnotations()));


        } else {
            return null;
        }
    }
}
