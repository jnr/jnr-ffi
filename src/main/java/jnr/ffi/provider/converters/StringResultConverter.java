package jnr.ffi.provider.converters;

import jnr.ffi.Pointer;
import jnr.ffi.mapper.FromNativeContext;
import jnr.ffi.mapper.FromNativeConverter;

import java.nio.charset.Charset;

/**
 * Converts a native pointer result into a java String
 */
@FromNativeConverter.NoContext
public class StringResultConverter implements FromNativeConverter<String, Pointer> {
    private static final FromNativeConverter DEFAULT = new StringResultConverter(Charset.defaultCharset());
    private final Charset charset;

    private StringResultConverter(Charset charset) {
        this.charset = charset;
    }

    public static FromNativeConverter getInstance(Charset cs) {
        return Charset.defaultCharset().equals(cs) ? DEFAULT : new StringResultConverter(cs);
    }

    @Override
    public String fromNative(Pointer nativeValue, FromNativeContext context) {
        return nativeValue != null ? nativeValue.getString(0, Integer.MAX_VALUE, charset) : null;
    }

    @Override
    public Class<Pointer> nativeType() {
        return Pointer.class;
    }
}
