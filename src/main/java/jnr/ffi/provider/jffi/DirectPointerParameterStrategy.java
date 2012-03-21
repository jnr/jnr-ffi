package jnr.ffi.provider.jffi;

import com.kenai.jffi.ObjectParameterStrategy;
import jnr.ffi.Pointer;

/**
 *
 */
final class DirectPointerParameterStrategy extends AbstractDirectPointerParameterStrategy {
    static final PointerParameterStrategy INSTANCE = new DirectPointerParameterStrategy();

    @Override
    public long getAddress(Object o) {
        return ((Pointer) o).address();
    }
}
