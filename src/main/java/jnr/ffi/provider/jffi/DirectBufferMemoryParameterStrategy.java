package jnr.ffi.provider.jffi;

import com.kenai.jffi.MemoryIO;
import com.kenai.jffi.ObjectParameterType;
import jnr.ffi.provider.AbstractBufferMemoryIO;

import java.nio.Buffer;
import java.util.EnumSet;

import static jnr.ffi.provider.jffi.DirectBufferParameterStrategy.calculateShift;

/**
 *
 */
final class DirectBufferMemoryParameterStrategy extends AbstractDirectPointerParameterStrategy {
    private final int shift;

    public DirectBufferMemoryParameterStrategy(ObjectParameterType.ComponentType componentType) {
        this.shift = calculateShift(componentType);
    }

    @Override
    public long address(Object o) {
        AbstractBufferMemoryIO bufferMemoryIO = (AbstractBufferMemoryIO) o;
        Buffer buffer = bufferMemoryIO.getByteBuffer();
        return MemoryIO.getInstance().getDirectBufferAddress(buffer) + (buffer.position() << shift);
    }

    private static final DirectBufferMemoryParameterStrategy[] directBufferStrategies;
    static {
        EnumSet<ObjectParameterType.ComponentType> componentTypes = EnumSet.allOf(ObjectParameterType.ComponentType.class);
        directBufferStrategies = new DirectBufferMemoryParameterStrategy[componentTypes.size()];
        for (ObjectParameterType.ComponentType componentType : componentTypes) {
            directBufferStrategies[componentType.ordinal()] = new DirectBufferMemoryParameterStrategy(componentType);
        }
    }

    static PointerParameterStrategy get(ObjectParameterType.ComponentType componentType) {
        return directBufferStrategies[componentType.ordinal()];
    }
}
