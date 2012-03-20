package jnr.ffi.provider.jffi;

import com.kenai.jffi.ObjectParameterStrategy;
import com.kenai.jffi.ObjectParameterType;
import jnr.ffi.provider.AbstractBufferMemoryIO;

/**
 *
 */
final class HeapBufferMemoryParameterStrategy extends PointerParameterStrategy {
    static final PointerParameterStrategy INSTANCE = new HeapBufferMemoryParameterStrategy();

    public HeapBufferMemoryParameterStrategy() {
        super(HEAP, ObjectParameterType.create(ObjectParameterType.ARRAY, ObjectParameterType.ComponentType.BYTE));
    }

    @Override
    public long getAddress(Object o) {
        return 0;
    }

    @Override
    public Object object(Object o) {
        return ((AbstractBufferMemoryIO) o).getByteBuffer().array();
    }

    @Override
    public int offset(Object o) {
        return ((AbstractBufferMemoryIO) o).getByteBuffer().arrayOffset();
    }

    @Override
    public int length(Object o) {
        return ((AbstractBufferMemoryIO) o).getByteBuffer().remaining();
    }
}
