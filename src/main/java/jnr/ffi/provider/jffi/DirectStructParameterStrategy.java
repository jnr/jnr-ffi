package jnr.ffi.provider.jffi;

import com.kenai.jffi.ObjectParameterStrategy;
import jnr.ffi.Pointer;
import jnr.ffi.Struct;
import jnr.ffi.provider.AbstractArrayMemoryIO;
import jnr.ffi.provider.ParameterFlags;

/**
 *
 */
final class DirectStructParameterStrategy extends PointerParameterStrategy {
    static final PointerParameterStrategy INSTANCE = new DirectStructParameterStrategy();

    public DirectStructParameterStrategy() {
        super(DIRECT);
    }

    @Override
    public long getAddress(Object o) {
        return Struct.getMemory((Struct) o, ParameterFlags.DIRECT).address();
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
