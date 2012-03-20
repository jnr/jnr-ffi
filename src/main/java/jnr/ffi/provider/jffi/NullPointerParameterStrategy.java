package jnr.ffi.provider.jffi;

import com.kenai.jffi.ObjectParameterStrategy;
import jnr.ffi.Pointer;

/**
 *
 */
final class NullPointerParameterStrategy extends PointerParameterStrategy {
    static final PointerParameterStrategy INSTANCE = new NullPointerParameterStrategy();

    private NullPointerParameterStrategy() {
        super(DIRECT);
    }

    @Override
    public long getAddress(Object o) {
        return 0;
    }

    @Override
    public Object object(Object o) {
        throw new NullPointerException();
    }

    @Override
    public int offset(Object o) {
        throw new NullPointerException();
    }

    @Override
    public int length(Object o) {
        throw new NullPointerException();
    }
}
