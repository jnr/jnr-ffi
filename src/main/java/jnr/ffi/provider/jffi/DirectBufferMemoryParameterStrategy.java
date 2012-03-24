package jnr.ffi.provider.jffi;

import jnr.ffi.provider.AbstractBufferMemoryIO;

/**
 *
 */
final class DirectBufferMemoryParameterStrategy extends AbstractDirectPointerParameterStrategy {
    static final PointerParameterStrategy INSTANCE = new DirectBufferMemoryParameterStrategy();

    @Override
    public long address(Object o) {
        AbstractBufferMemoryIO bufferMemoryIO = (AbstractBufferMemoryIO) o;
        return AsmRuntime.longValue(bufferMemoryIO.getByteBuffer());
    }
}
