package jnr.ffi.provider.converters;

import jnr.ffi.Pointer;
import jnr.ffi.PointerWrapper;
import jnr.ffi.mapper.FromNativeContext;
import jnr.ffi.mapper.FromNativeConverter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Andrew Yefanov.
 * @since 30.08.2017.
 */
public class PointerWrapperFromNativeConverter implements FromNativeConverter<PointerWrapper, Pointer> {

    private final Constructor<PointerWrapper> constructor;

    private PointerWrapperFromNativeConverter(Constructor constructor) {
        this.constructor = constructor;
    }

    public static FromNativeConverter<PointerWrapper, Pointer> getInstance(Class<PointerWrapper> wrapperClass) {
        try {
            Constructor<PointerWrapper> constructor = wrapperClass.getConstructor(Pointer.class);
            if (!constructor.isAccessible()) {
                constructor.setAccessible(true);
            }
            return new PointerWrapperFromNativeConverter(constructor);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(wrapperClass.getName() + " has no constructor that accepts jnr.ffi.Pointer");
        }
    }

    @Override
    public PointerWrapper fromNative(Pointer nativeValue, FromNativeContext context) {
        try {
            return nativeValue == null ? null : constructor.newInstance(nativeValue);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Class<Pointer> nativeType() {
        return Pointer.class;
    }
}