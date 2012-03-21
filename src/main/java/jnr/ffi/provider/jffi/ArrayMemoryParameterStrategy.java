package jnr.ffi.provider.jffi;

import com.kenai.jffi.ObjectParameterType;
import jnr.ffi.Pointer;

/**
 *
 */
final class ArrayMemoryParameterStrategy extends PointerParameterStrategy {
    static final PointerParameterStrategy INSTANCE = new ArrayMemoryParameterStrategy();

    private ArrayMemoryParameterStrategy() {
        super(HEAP, ObjectParameterType.create(ObjectParameterType.ARRAY, ObjectParameterType.ComponentType.BYTE));
    }

    @Override
    public long address(Object o) {
        return 0;
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
