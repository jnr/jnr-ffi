package jnr.ffi.provider.jffi;

/**
 *
 */
abstract class AbstractDirectPointerParameterStrategy extends PointerParameterStrategy {

    public AbstractDirectPointerParameterStrategy() {
        super(DIRECT);
    }

    @Override
    public Object object(Object o) {
        throw new UnsupportedOperationException("no array");
    }

    @Override
    public int offset(Object o) {
        throw new UnsupportedOperationException("no array");
    }

    @Override
    public int length(Object o) {
        throw new UnsupportedOperationException("no array");
    }
}
