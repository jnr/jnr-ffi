package jnr.ffi.provider.jffi;

import com.kenai.jffi.ObjectParameterType;
import jnr.ffi.Pointer;

/**
 *
 */
public final class PointerParameterStrategy extends ParameterStrategy {
    public static final PointerParameterStrategy DIRECT = new PointerParameterStrategy(StrategyType.DIRECT);
    public static final PointerParameterStrategy HEAP = new PointerParameterStrategy(StrategyType.HEAP);

    PointerParameterStrategy(StrategyType type) {
        super(type, ObjectParameterType.create(ObjectParameterType.ARRAY, ObjectParameterType.BYTE));
    }

    @Override
    public long address(Object o) {
        return address((Pointer) o);
    }

    public long address(Pointer pointer) {
        return pointer != null ? pointer.address() : 0L;
    }

    @Override
    public Object object(Object o) {
        return ((Pointer) o).array();
    }

    @Override
    public int offset(Object o) {
        return ((Pointer) o).arrayOffset();
    }

    @Override
    public int length(Object o) {
        return ((Pointer) o).arrayLength();
    }
}
