package jnr.ffi.provider.converters;

import jnr.ffi.Pointer;
import jnr.ffi.mapper.ToNativeContext;
import jnr.ffi.mapper.ToNativeConverter;
import jnr.ffi.provider.ParameterFlags;
import jnr.ffi.provider.jffi.ArrayMemoryIO;
import jnr.ffi.util.BufferUtil;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

@ToNativeConverter.Cacheable
public class StringBufferParameterConverter implements ToNativeConverter<StringBuffer, Pointer>, ToNativeConverter.PostInvocation<StringBuffer, Pointer> {
    private final Charset charset;
    private final int parameterFlags;

    private StringBufferParameterConverter(Charset charset, int parameterFlags) {
        this.charset = charset;
        this.parameterFlags = parameterFlags;
    }

    public Class<Pointer> nativeType() {
        return Pointer.class;
    }

    public static StringBufferParameterConverter getInstance(int parameterFlags, ToNativeContext toNativeContext) {
        return new StringBufferParameterConverter(Charset.defaultCharset(), parameterFlags);
    }

    public static StringBufferParameterConverter getInstance(Charset charset, int parameterFlags, ToNativeContext toNativeContext) {
        return new StringBufferParameterConverter(charset, parameterFlags);
    }

    public Pointer toNative(StringBuffer parameter, ToNativeContext context) {
        if (parameter == null) {
            return null;

        } else {
            ByteBuffer buf = ParameterFlags.isIn(parameterFlags)
                    ? charset.encode(CharBuffer.wrap(parameter))
                    : ByteBuffer.allocate(parameter.capacity() + 1);

            if ((ParameterFlags.isOut(parameterFlags) && buf.capacity() < parameter.capacity() + 1) || !buf.hasArray()) {
                ArrayMemoryIO aio = new ArrayMemoryIO(context.getRuntime(), parameter.capacity() + 1);
                buf.get(aio.array(), aio.arrayOffset(), buf.limit());

                return aio;

            } else {
                return new ArrayMemoryIO(context.getRuntime(), buf.array(), buf.arrayOffset(), buf.limit());
            }
        }
    }

    public void postInvoke(StringBuffer stringBuffer, Pointer pointer, ToNativeContext context) {
        //
        // Copy the string back out if its an OUT parameter
        //
        if (ParameterFlags.isOut(parameterFlags) && stringBuffer != null && pointer != null) {
            ArrayMemoryIO aio = (ArrayMemoryIO) pointer;
            final ByteBuffer tmp = ByteBuffer.wrap(aio.array(), aio.arrayOffset(), aio.arrayLength());
            stringBuffer.delete(0, stringBuffer.length()).append(BufferUtil.getCharSequence(tmp, charset));
        }
    }
}
