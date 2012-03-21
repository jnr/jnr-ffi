package jnr.ffi.provider.jffi;

/**
 *
 */
final class DirectMemoryParameterStrategy extends AbstractDirectPointerParameterStrategy {
    static final PointerParameterStrategy INSTANCE = new DirectMemoryParameterStrategy();

    @Override
    public final long getAddress(Object o) {
        return ((DirectMemoryIO) o).address;
    }
}
