package jnr.ffi.provider.jffi;

import com.kenai.jffi.ObjectParameterType;

import java.nio.Buffer;
import java.util.EnumSet;

/**
 *
 */
final class HeapBufferParameterStrategy extends PointerParameterStrategy {

    public HeapBufferParameterStrategy(ObjectParameterType.ComponentType componentType) {
        super(HEAP, ObjectParameterType.create(ObjectParameterType.ARRAY, componentType));
    }

    @Override
    public long address(Object o) {
        return 0;
    }

    @Override
    public Object object(Object o) {
        return ((Buffer) o).array();
    }

    @Override
    public int offset(Object o) {
        Buffer buffer = (Buffer) o;
        return buffer.arrayOffset() + buffer.position();
    }

    @Override
    public int length(Object o) {
        return ((Buffer) o).remaining();
    }


    private static final HeapBufferParameterStrategy[] heapBufferStrategies;
    static {
        EnumSet<ObjectParameterType.ComponentType> componentTypes = EnumSet.allOf(ObjectParameterType.ComponentType.class);
        heapBufferStrategies = new HeapBufferParameterStrategy[componentTypes.size()];
        for (ObjectParameterType.ComponentType componentType : componentTypes) {
            heapBufferStrategies[componentType.ordinal()] = new HeapBufferParameterStrategy(componentType);
        }
    }

    static PointerParameterStrategy get(ObjectParameterType.ComponentType componentType) {
        return heapBufferStrategies[componentType.ordinal()];
    }

}
