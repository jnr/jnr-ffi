package jnr.ffi.provider.jffi;

import jnr.ffi.Struct;
import jnr.ffi.provider.ParameterFlags;

/**
 *
 */
final class DirectStructParameterStrategy extends AbstractDirectPointerParameterStrategy {
    static final PointerParameterStrategy INSTANCE = new DirectStructParameterStrategy();


    @Override
    public long getAddress(Object o) {
        return Struct.getMemory((Struct) o, ParameterFlags.DIRECT).address();
    }
}
