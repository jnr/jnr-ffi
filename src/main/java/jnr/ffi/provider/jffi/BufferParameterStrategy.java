/*
 * Copyright (C) 2012 Wayne Meissner
 *
 * This file is part of the JNR project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jnr.ffi.provider.jffi;

import com.kenai.jffi.MemoryIO;
import com.kenai.jffi.ObjectParameterType;

import java.nio.*;
import java.util.EnumSet;

/**
 *
 */
public final class BufferParameterStrategy extends ParameterStrategy {
    private static final int BYTE_POSITION_SHIFT = 0;
    private static final int SHORT_POSITION_SHIFT = 1;
    private static final int CHAR_POSITION_SHIFT = 1;
    private static final int BOOLEAN_POSITION_SHIFT = 2;
    private static final int INT_POSITION_SHIFT = 2;
    private static final int FLOAT_POSITION_SHIFT = 2;
    private static final int LONG_POSITION_SHIFT = 3;
    private static final int DOUBLE_POSITION_SHIFT = 3;

    private final int shift;

    private BufferParameterStrategy(StrategyType type, ObjectParameterType.ComponentType componentType) {
        super(type, ObjectParameterType.create(ObjectParameterType.ObjectType.ARRAY, componentType));
        this.shift = calculateShift(componentType);
    }

    public static long address(ByteBuffer ptr) {
        return address(ptr, BYTE_POSITION_SHIFT);
    }

    public static long address(ShortBuffer ptr) {
        return address(ptr, SHORT_POSITION_SHIFT);
    }

    public static long address(CharBuffer ptr) {
        return address(ptr, CHAR_POSITION_SHIFT);
    }

    public static long address(IntBuffer ptr) {
        return address(ptr, INT_POSITION_SHIFT);
    }

    public static long address(FloatBuffer ptr) {
        return address(ptr, FLOAT_POSITION_SHIFT);
    }

    public static long address(LongBuffer ptr) {
        return address(ptr, LONG_POSITION_SHIFT);
    }

    public static long address(DoubleBuffer ptr) {
        return address(ptr, DOUBLE_POSITION_SHIFT);
    }

    public static long address(Buffer buffer) {
        if (buffer instanceof ByteBuffer) {
            return address(buffer, BYTE_POSITION_SHIFT);

        } else if (buffer instanceof ShortBuffer) {
            return address(buffer, SHORT_POSITION_SHIFT);

        } else if (buffer instanceof CharBuffer) {
            return address(buffer, CHAR_POSITION_SHIFT);

        } else if (buffer instanceof IntBuffer) {
            return address(buffer, INT_POSITION_SHIFT);

        } else if (buffer instanceof LongBuffer) {
            return address(buffer, LONG_POSITION_SHIFT);

        } else if (buffer instanceof FloatBuffer) {
            return address(buffer, FLOAT_POSITION_SHIFT);

        } else if (buffer instanceof DoubleBuffer) {
            return address(buffer, DOUBLE_POSITION_SHIFT);

        } else if (buffer == null) {
            return address(buffer, BYTE_POSITION_SHIFT);

        } else {
            throw new IllegalArgumentException("unsupported java.nio.Buffer subclass: " + buffer.getClass());
        }
    }

    private static long address(Buffer ptr, int shift) {
        return ptr != null && ptr.isDirect() ? MemoryIO.getInstance().getDirectBufferAddress(ptr) + (ptr.position() << shift) : 0L;
    }

    @Override
    public long address(Object o) {
        return address((Buffer) o, shift);
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
                return BYTE_POSITION_SHIFT;

            case SHORT:
                return SHORT_POSITION_SHIFT;

            case CHAR:
                return CHAR_POSITION_SHIFT;

            case INT:
                return INT_POSITION_SHIFT;

            case BOOLEAN:
                return BOOLEAN_POSITION_SHIFT;

            case FLOAT:
                return FLOAT_POSITION_SHIFT;

            case LONG:
                return LONG_POSITION_SHIFT;

            case DOUBLE:
                return DOUBLE_POSITION_SHIFT;

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
