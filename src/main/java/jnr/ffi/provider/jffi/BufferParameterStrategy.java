package jnr.ffi.provider.jffi;

import com.kenai.jffi.MemoryIO;
import com.kenai.jffi.ObjectParameterType;

import java.nio.Buffer;
import java.util.EnumSet;

/**
 *
 */
public final class BufferParameterStrategy extends ParameterStrategy {
    private final int shift;

    private BufferParameterStrategy(StrategyType type, ObjectParameterType.ComponentType componentType) {
        super(type, ObjectParameterType.create(ObjectParameterType.ObjectType.ARRAY, componentType));
        this.shift = calculateShift(componentType);
    }

    public long address(Buffer buffer) {
        return buffer != null && buffer.isDirect() ? MemoryIO.getInstance().getDirectBufferAddress(buffer) + (buffer.position() << shift) : 0L;
    }

    @Override
    public long address(Object o) {
        return address((Buffer) o);
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

    static int calculateShift(ObjectParameterType.ComponentType componentType) {
        switch (componentType) {
            case BYTE:
                return 0;

            case SHORT:
            case CHAR:
                return 1;

            case INT:
            case BOOLEAN:
            case FLOAT:
                return 2;

            case LONG:
            case DOUBLE:
                return 3;
            default:
                throw new IllegalArgumentException("unsupported component type: " + componentType);
        }
    }


    private static final BufferParameterStrategy[] DIRECT_BUFFER_PARAMETER_STRATEGIES;
    private static final BufferParameterStrategy[] HEAP_BUFFER_PARAMETER_STRATEGIES;
    static {
        EnumSet<ObjectParameterType.ComponentType> componentTypes = EnumSet.allOf(ObjectParameterType.ComponentType.class);
        DIRECT_BUFFER_PARAMETER_STRATEGIES = new BufferParameterStrategy[componentTypes.size()];
        HEAP_BUFFER_PARAMETER_STRATEGIES = new BufferParameterStrategy[componentTypes.size()];
        for (ObjectParameterType.ComponentType componentType : componentTypes) {
            DIRECT_BUFFER_PARAMETER_STRATEGIES[componentType.ordinal()] = new BufferParameterStrategy(DIRECT, componentType);
            HEAP_BUFFER_PARAMETER_STRATEGIES[componentType.ordinal()] = new BufferParameterStrategy(HEAP, componentType);
        }
    }

    static BufferParameterStrategy direct(ObjectParameterType.ComponentType componentType) {
        return DIRECT_BUFFER_PARAMETER_STRATEGIES[componentType.ordinal()];
    }

    static BufferParameterStrategy heap(ObjectParameterType.ComponentType componentType) {
        return HEAP_BUFFER_PARAMETER_STRATEGIES[componentType.ordinal()];
    }
}
