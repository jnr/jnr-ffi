package jnr.ffi.provider.converters;

import jnr.ffi.Pointer;
import jnr.ffi.annotations.Encoding;
import jnr.ffi.mapper.*;

import java.lang.annotation.Annotation;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;

/**
 * Converts a native pointer result into a java String
 */
@FromNativeConverter.NoContext
@FromNativeConverter.Cacheable
public class StringResultConverter implements FromNativeConverter<String, Pointer> {
    private static final FromNativeConverter<String, Pointer> DEFAULT = new StringResultConverter(Charset.defaultCharset());
    private final Charset charset;

    private StringResultConverter(Charset charset) {
        this.charset = charset;
    }

    public static FromNativeConverter<String, Pointer> getInstance(Charset cs) {
        return Charset.defaultCharset().equals(cs) ? DEFAULT : new StringResultConverter(cs);
    }

    public static FromNativeConverter<String, Pointer> getInstance(FromNativeContext fromNativeContext) {
        Charset charset = Charset.defaultCharset();

        if (fromNativeContext instanceof MethodResultContext) {
            // See if the interface class has a global @Encoding declaration
            Encoding e = getEncoding(Arrays.asList(((MethodResultContext) fromNativeContext).getMethod().getDeclaringClass().getAnnotations()));
            if (e != null) {
                charset = Charset.forName(e.value());
            }
        }

        // Allow each method to override the default
        Encoding e = getEncoding(fromNativeContext.getAnnotations());
        if (e != null) {
            charset = Charset.forName(e.value());
        }

        return getInstance(charset);
    }

    @Override
    public String fromNative(Pointer nativeValue, FromNativeContext context) {
        return nativeValue != null ? nativeValue.getString(0, Integer.MAX_VALUE, charset) : null;
    }

    @Override
    public Class<Pointer> nativeType() {
        return Pointer.class;
    }

    private static Encoding getEncoding(Collection<Annotation> annotations) {
        for (Annotation a : annotations) {
            if (a instanceof Encoding) {
                return (Encoding) a;
            }
        }

        return null;
    }
}
