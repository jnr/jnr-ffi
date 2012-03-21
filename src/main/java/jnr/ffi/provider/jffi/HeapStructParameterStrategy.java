package jnr.ffi.provider.jffi;

import com.kenai.jffi.ObjectParameterStrategy;
import com.kenai.jffi.ObjectParameterType;
import jnr.ffi.Pointer;
import jnr.ffi.Struct;
import jnr.ffi.provider.AbstractArrayMemoryIO;
import jnr.ffi.provider.DelegatingMemoryIO;
import jnr.ffi.provider.ParameterFlags;

/**
 *
 */
final class HeapStructParameterStrategy extends PointerParameterStrategy {
    static final PointerParameterStrategy INSTANCE = new HeapStructParameterStrategy();

    private HeapStructParameterStrategy() {
        super(HEAP, ObjectParameterType.create(ObjectParameterType.ARRAY, ObjectParameterType.ComponentType.BYTE));
    }

    @Override
    public long address(Object o) {
        return 0;
    }

    static Pointer getArrayMemory(Object o) {
        return Struct.getMemory((Struct) o, 0);
    }

    @Override
    public Object object(Object o) {
        return getArrayMemory(o).array();
    }

    @Override
    public int offset(Object o) {
        return getArrayMemory(o).arrayOffset();
    }

    @Override
    public int length(Object o) {
        return getArrayMemory(o).arrayLength();
    }
}
