package jnr.ffi.provider.jffi;

import com.kenai.jffi.ObjectParameterType;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

/**
 *
 */
final class StringParameterStrategy extends PointerParameterStrategy {
    private static final ObjectParameterType objectParameterType
            = ObjectParameterType.create(ObjectParameterType.ObjectType.ARRAY, ObjectParameterType.BYTE);

    private final ByteBuffer byteBuffer;

    public StringParameterStrategy(CharSequence s) {
        super(HEAP, objectParameterType);
        byteBuffer = Charset.defaultCharset().encode(CharBuffer.wrap(s));
    }

    @Override
    public long address(Object o) {
        return 0;
    }

    @Override
    public Object object(Object o) {
        return byteBuffer.array();
    }

    @Override
    public int offset(Object o) {
        return byteBuffer.arrayOffset();
    }

    @Override
    public int length(Object o) {
        return byteBuffer.remaining();
    }
}
