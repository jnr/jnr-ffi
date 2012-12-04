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

@ToNativeConverter.NoContext
public class StringBufferParameterConverter implements ToNativeConverter<StringBuffer, Pointer>, ToNativeConverter.PostInvocation<StringBuffer, Pointer> {
    private final jnr.ffi.Runtime runtime;
    private final int parameterFlags;

    private StringBufferParameterConverter(jnr.ffi.Runtime runtime, int parameterFlags) {
        this.runtime = runtime;
        this.parameterFlags = parameterFlags;
    }

    public Class<Pointer> nativeType() {
        return Pointer.class;
    }

    public static StringBufferParameterConverter getInstance(jnr.ffi.Runtime runtime, int parameterFlags) {
        return new StringBufferParameterConverter(runtime, parameterFlags);
    }

    public Pointer toNative(StringBuffer parameter, ToNativeContext context) {
        if (parameter == null) {
            return null;

        } else {
            ByteBuffer buf = ParameterFlags.isIn(parameterFlags)
                    ? Charset.defaultCharset().encode(CharBuffer.wrap(parameter))
                    : ByteBuffer.allocate(parameter.capacity() + 1);

            if ((ParameterFlags.isOut(parameterFlags) && buf.capacity() < parameter.capacity() + 1) || !buf.hasArray()) {
                ArrayMemoryIO aio = new ArrayMemoryIO(runtime, parameter.capacity() + 1);
                buf.get(aio.array(), aio.arrayOffset(), buf.limit());

                return aio;

            } else {
                return new ArrayMemoryIO(runtime, buf.array(), buf.arrayOffset(), buf.limit());
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
            stringBuffer.delete(0, stringBuffer.length()).append(BufferUtil.getCharSequence(tmp, Charset.defaultCharset()));
        }
    }
}
