package jnr.ffi.provider.jffi;

import com.kenai.jffi.ObjectParameterStrategy;
import jnr.ffi.provider.AbstractBufferMemoryIO;

/**
 *
 */
final class DirectBufferMemoryParameterStrategy extends PointerParameterStrategy {
    static final PointerParameterStrategy INSTANCE = new DirectBufferMemoryParameterStrategy();

    public DirectBufferMemoryParameterStrategy() {
        super(DIRECT);
    }

    @Override
    public long getAddress(Object o) {
        AbstractBufferMemoryIO bufferMemoryIO = (AbstractBufferMemoryIO) o;
        return AsmRuntime.longValue(bufferMemoryIO.getByteBuffer());
    }

    @Override
    public Object object(Object o) {
        throw new RuntimeException("no array");
    }

    @Override
    public int offset(Object o) {
        throw new RuntimeException("no array");
    }

    @Override
    public int length(Object o) {
        throw new RuntimeException("no array");
    }
}
