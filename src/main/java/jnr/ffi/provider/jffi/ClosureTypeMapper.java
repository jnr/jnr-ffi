package jnr.ffi.provider.jffi;

import jnr.ffi.Struct;
import jnr.ffi.mapper.*;
import jnr.ffi.provider.converters.EnumConverter;
import jnr.ffi.provider.ParameterFlags;

final class ClosureTypeMapper implements TypeMapper {
    public FromNativeConverter getFromNativeConverter(Class type, FromNativeContext context) {
        if (Enum.class.isAssignableFrom(type)) {
            return EnumConverter.getInstance(type.asSubclass(Enum.class));

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
