package jnr.ffi.provider.jffi;

import com.kenai.jffi.ObjectParameterStrategy;
import jnr.ffi.Pointer;

/**
 *
 */
final class DirectPointerParameterStrategy extends PointerParameterStrategy {
    static final PointerParameterStrategy INSTANCE = new DirectPointerParameterStrategy();

    public DirectPointerParameterStrategy() {
        super(DIRECT);
    }

    @Override
    public long getAddress(Object o) {
        return ((Pointer) o).address();
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
