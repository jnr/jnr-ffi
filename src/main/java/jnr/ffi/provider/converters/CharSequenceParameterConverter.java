package jnr.ffi.provider.converters;

import jnr.ffi.annotations.In;
import jnr.ffi.annotations.NulTerminate;
import jnr.ffi.mapper.ToNativeContext;
import jnr.ffi.mapper.ToNativeConverter;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

/**
 * Converts a CharSequence (e.g. String) to a primitive byte[] array parameter
 */
@ToNativeConverter.NoContext
@ToNativeConverter.Cacheable
public class CharSequenceParameterConverter implements ToNativeConverter<CharSequence, byte[]> {
    private static final ToNativeConverter<CharSequence, byte[]> DEFAULT = new CharSequenceParameterConverter(Charset.defaultCharset());
    private final Charset charset;

    public static ToNativeConverter<CharSequence, byte[]> getInstance(jnr.ffi.Runtime runtime, Charset charset) {
        return Charset.defaultCharset().equals(charset) ? DEFAULT : new CharSequenceParameterConverter(charset);
    }

    private CharSequenceParameterConverter(Charset charset) {
        this.charset = charset;
    }

    @Override
    public byte[] toNative(CharSequence string, ToNativeContext context) {
        if (string == null) {
            return null;
        }

        ByteBuffer byteBuffer = charset.encode(CharBuffer.wrap(string));
        return byteBuffer.hasArray() && byteBuffer.arrayOffset() == 0 ? byteBuffer.array() : copyOf(byteBuffer);
    }

    private byte[] copyOf(ByteBuffer byteBuffer) {
        // Have to copy to a fresh byte[] array to ensure the first byte starts at index == 0
        byte[] bytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(bytes);
        return bytes;
    }

    @Override
    @In
    @NulTerminate
    public Class<byte[]> nativeType() {
        return byte[].class;
    }
}
