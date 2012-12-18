package jnr.ffi.provider.converters;

import jnr.ffi.annotations.In;
import jnr.ffi.annotations.NulTerminate;
import jnr.ffi.mapper.ToNativeContext;
import jnr.ffi.mapper.ToNativeConverter;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

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
        return new CharSequenceParameterConverter(Charset.defaultCharset());
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
