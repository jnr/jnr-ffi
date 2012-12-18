package jnr.ffi.provider.converters;

import jnr.ffi.mapper.ToNativeContext;
import jnr.ffi.mapper.ToNativeConverter;
import jnr.ffi.provider.ParameterFlags;
import jnr.ffi.util.BufferUtil;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

@ToNativeConverter.NoContext
@ToNativeConverter.Cacheable
public class StringBuilderParameterConverter implements ToNativeConverter<StringBuilder, ByteBuffer>, ToNativeConverter.PostInvocation<StringBuilder, ByteBuffer> {
    private final Charset charset;
    private final int parameterFlags;

    private StringBuilderParameterConverter(Charset charset, int parameterFlags) {
        this.charset = charset;
        this.parameterFlags = parameterFlags;
    }

    public Class<ByteBuffer> nativeType() {
        return ByteBuffer.class;
    }

    public static StringBuilderParameterConverter getInstance(int parameterFlags, ToNativeContext toNativeContext) {
        return new StringBuilderParameterConverter(Charset.defaultCharset(), parameterFlags);
    }

    public static StringBuilderParameterConverter getInstance(Charset charset, int parameterFlags, ToNativeContext toNativeContext) {
        return new StringBuilderParameterConverter(charset, parameterFlags);
    }

    public ByteBuffer toNative(StringBuilder parameter, ToNativeContext context) {
        if (parameter == null) {
            return null;

        } else {
            ByteBuffer buf = ParameterFlags.isIn(parameterFlags)
                    ? charset.encode(CharBuffer.wrap(parameter))
                    : ByteBuffer.allocate(parameter.capacity() + 1);

            if ((ParameterFlags.isOut(parameterFlags) && buf.capacity() < parameter.capacity() + 1) || !buf.hasArray()) {
                byte[] array = new byte[parameter.capacity() + 1];
                buf.get(array, 0, buf.remaining());
                return ByteBuffer.wrap(array);

            } else {
                return buf;
            }
        }
    }

    public void postInvoke(StringBuilder stringBuilder, ByteBuffer buf, ToNativeContext context) {
        //
        // Copy the string back out if its an OUT parameter
        //
        if (ParameterFlags.isOut(parameterFlags) && stringBuilder != null && buf != null) {
            buf.limit(buf.capacity());
            stringBuilder.delete(0, stringBuilder.length()).append(BufferUtil.getCharSequence(buf, charset));
        }
    }
}
