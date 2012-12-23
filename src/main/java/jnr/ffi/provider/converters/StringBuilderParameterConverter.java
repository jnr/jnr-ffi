package jnr.ffi.provider.converters;

import jnr.ffi.mapper.ToNativeContext;
import jnr.ffi.mapper.ToNativeConverter;
import jnr.ffi.provider.ParameterFlags;
import jnr.ffi.util.BufferUtil;

import java.lang.ref.Reference;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.*;

import static jnr.ffi.provider.converters.StringUtil.*;

@ToNativeConverter.NoContext
@ToNativeConverter.Cacheable
public class StringBuilderParameterConverter implements ToNativeConverter<StringBuilder, ByteBuffer>, ToNativeConverter.PostInvocation<StringBuilder, ByteBuffer> {
    private final ThreadLocal<Reference<CharsetEncoder>> localEncoder = new ThreadLocal<Reference<CharsetEncoder>>();
    private final ThreadLocal<Reference<CharsetDecoder>> localDecoder = new ThreadLocal<Reference<CharsetDecoder>>();
    private final Charset charset;
    private final int parameterFlags;
    private final int terminatorWidth;

    private StringBuilderParameterConverter(Charset charset, int parameterFlags) {
        this.charset = charset;
        this.parameterFlags = parameterFlags;
        this.terminatorWidth = terminatorWidth(charset);
    }

    public Class<ByteBuffer> nativeType() {
        return ByteBuffer.class;
    }

    public static StringBuilderParameterConverter getInstance(int parameterFlags, ToNativeContext toNativeContext) {
        return new StringBuilderParameterConverter(getCharset(toNativeContext), parameterFlags);
    }

    public static StringBuilderParameterConverter getInstance(Charset charset, int parameterFlags, ToNativeContext toNativeContext) {
        return new StringBuilderParameterConverter(charset, parameterFlags);
    }

    public ByteBuffer toNative(StringBuilder parameter, ToNativeContext context) {
        if (parameter == null) {
            return null;

        } else {
            CharsetEncoder encoder = getEncoder(charset, localEncoder);
            ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[parameter.capacity() * (int) Math.ceil(encoder.maxBytesPerChar()) + 4]);

            if (ParameterFlags.isIn(parameterFlags)) {
                byteBuffer.mark();
                encoder.reset();
                CoderResult result = encoder.encode(CharBuffer.wrap(parameter), byteBuffer, true);
                if (result.isUnderflow()) result = encoder.flush(byteBuffer);
                if (result.isError()) throwException(result);
                byteBuffer.reset();
            }

            return byteBuffer;
        }
    }

    public void postInvoke(StringBuilder stringBuilder, ByteBuffer buf, ToNativeContext context) {
        //
        // Copy the string back out if its an OUT parameter
        //
        if (ParameterFlags.isOut(parameterFlags) && stringBuilder != null && buf != null) {
            buf.limit(stringLength(buf, terminatorWidth));
            try {
                stringBuilder.delete(0, stringBuilder.length()).append(getDecoder(charset, localDecoder).reset().decode(buf));
            } catch (CharacterCodingException cce) {
                throw new RuntimeException(cce);
            }
        }
    }
}
