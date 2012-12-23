package jnr.ffi.provider.converters;

import jnr.ffi.Pointer;
import jnr.ffi.annotations.Encoding;
import jnr.ffi.mapper.*;

import java.lang.annotation.Annotation;
import java.lang.ref.Reference;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Arrays;
import java.util.Collection;

import static jnr.ffi.provider.converters.StringUtil.getDecoder;

/**
 * Converts a native pointer result into a java String
 */
@FromNativeConverter.NoContext
@FromNativeConverter.Cacheable
public class StringResultConverter implements FromNativeConverter<String, Pointer> {
    private static final FromNativeConverter<String, Pointer> DEFAULT = new StringResultConverter(Charset.defaultCharset());
    private final ThreadLocal<Reference<CharsetDecoder>> localDecoder = new ThreadLocal<Reference<CharsetDecoder>>();
    private final Charset charset;
    private final int terminatorWidth;

    private StringResultConverter(Charset charset) {
        this.charset = charset;
        this.terminatorWidth = StringUtil.terminatorWidth(charset);
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
    public String fromNative(Pointer pointer, FromNativeContext context) {
        if (pointer == null) {
            return null;
        }

        Search: for (int idx = 0; ; ) {
            idx += pointer.indexOf(idx, (byte) 0);
            for (int tcount = 1; tcount < terminatorWidth; tcount++) {
                if (pointer.getByte(idx + tcount) != 0) {
                    idx += tcount;
                    continue Search;
                }
            }

            byte[] bytes = new byte[idx];
            pointer.get(0, bytes, 0, bytes.length);
            try {
                return getDecoder(charset, localDecoder).reset().decode(ByteBuffer.wrap(bytes)).toString();
            } catch (CharacterCodingException cce) {
                throw new RuntimeException(cce);
            }
        }
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
