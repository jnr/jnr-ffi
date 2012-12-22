package jnr.ffi.provider.converters;

import jnr.ffi.annotations.Encoding;
import jnr.ffi.annotations.In;
import jnr.ffi.annotations.NulTerminate;
import jnr.ffi.mapper.MethodParameterContext;
import jnr.ffi.mapper.ToNativeContext;
import jnr.ffi.mapper.ToNativeConverter;

import java.lang.annotation.Annotation;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;

/**
 * Converts a CharSequence (e.g. String) to a primitive ByteBuffer array parameter
 */
@ToNativeConverter.NoContext
@ToNativeConverter.Cacheable
public class CharSequenceParameterConverter implements ToNativeConverter<CharSequence, ByteBuffer> {
    private static final ToNativeConverter<CharSequence, ByteBuffer> DEFAULT = new CharSequenceParameterConverter(Charset.defaultCharset());
    private final Charset charset;


    public static ToNativeConverter<CharSequence, ByteBuffer> getInstance(Charset charset, ToNativeContext toNativeContext) {
        return Charset.defaultCharset().equals(charset) ? DEFAULT : new CharSequenceParameterConverter(charset);
    }

    public static ToNativeConverter<CharSequence, ByteBuffer> getInstance(ToNativeContext toNativeContext) {
        Charset charset = Charset.defaultCharset();

        if (toNativeContext instanceof MethodParameterContext) {
            // See if the interface class has a global @Encoding declaration
            Charset cs = getEncodingCharset(Arrays.asList(((MethodParameterContext) toNativeContext).getMethod().getDeclaringClass().getAnnotations()));
            if (cs != null) {
                charset = cs;
            }

            // Allow each method to override the default
            cs = getEncodingCharset(Arrays.asList(((MethodParameterContext) toNativeContext).getMethod().getAnnotations()));
            if (cs != null) {
                charset = cs;
            }
        }

        // Override on a per-parameter basis
        Charset cs = getEncodingCharset(toNativeContext.getAnnotations());
        if (cs != null) {
            charset = cs;
        }

        return getInstance(charset, toNativeContext);
    }

    private static Charset getEncodingCharset(Collection<Annotation> annotations) {
        for (Annotation a : annotations) {
            if (a instanceof Encoding) {
                return Charset.forName(((Encoding) a).value());
            }
        }

        return null;
    }

    private CharSequenceParameterConverter(Charset charset) {
        this.charset = charset;
    }

    @Override
    public ByteBuffer toNative(CharSequence string, ToNativeContext context) {
        if (string == null) {
            return null;
        }

        ByteBuffer byteBuffer = charset.encode(CharBuffer.wrap(string));
        return byteBuffer.hasArray() || byteBuffer.isDirect() ? byteBuffer : copyOf(byteBuffer);
    }

    private ByteBuffer copyOf(ByteBuffer byteBuffer) {
        // Have to copy to a fresh array backed ByteBuffer to ensure it can be passed down to the native code
        byte[] bytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(bytes);
        return ByteBuffer.wrap(bytes);
    }

    @Override
    @In
    @NulTerminate
    public Class<ByteBuffer> nativeType() {
        return ByteBuffer.class;
    }
}
