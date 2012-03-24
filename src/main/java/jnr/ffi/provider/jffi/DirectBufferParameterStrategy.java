package jnr.ffi.provider.jffi;

import com.kenai.jffi.MemoryIO;
import com.kenai.jffi.ObjectParameterType;

import java.nio.Buffer;
import java.util.EnumSet;

/**
 *
 */
final class DirectBufferParameterStrategy extends PointerParameterStrategy {
    private final int shift;

    public DirectBufferParameterStrategy(ObjectParameterType.ComponentType componentType) {
        super(DIRECT, ObjectParameterType.create(ObjectParameterType.ObjectType.BUFFER, componentType));
        this.shift = calculateShift(componentType);
    }

    @Override
    public long address(Object o) {
        Buffer buffer = (Buffer) o;
        return MemoryIO.getInstance().getDirectBufferAddress(buffer) + (buffer.position() << shift);
    }

    @Override
    public Object object(Object o) {
        return o;
    }

    @Override
    public int offset(Object o) {
        return ((Buffer) o).position() << shift;
    }

    @Override
    public int length(Object o) {
        return ((Buffer) o).remaining() << shift;
    }

    private static int calculateShift(ObjectParameterType.ComponentType componentType) {
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

    private static final DirectBufferParameterStrategy[] directBufferStrategies;
    static {
        EnumSet<ObjectParameterType.ComponentType> componentTypes = EnumSet.allOf(ObjectParameterType.ComponentType.class);
        directBufferStrategies = new DirectBufferParameterStrategy[componentTypes.size()];
        for (ObjectParameterType.ComponentType componentType : componentTypes) {
            directBufferStrategies[componentType.ordinal()] = new DirectBufferParameterStrategy(componentType);
        }
    }

    static PointerParameterStrategy get(ObjectParameterType.ComponentType componentType) {
        return directBufferStrategies[componentType.ordinal()];
    }

}
