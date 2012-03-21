package jnr.ffi.provider.jffi;

import com.kenai.jffi.ObjectParameterType;

/**
 *
 */
abstract class PrimitiveArrayParameterStrategy extends PointerParameterStrategy {
    static final PrimitiveArrayParameterStrategy BYTE = new PrimitiveArrayParameterStrategy(ObjectParameterType.BYTE) {
        public int length(Object o) {
            return ((byte[]) o).length;
        }
    };

    static final PrimitiveArrayParameterStrategy SHORT = new PrimitiveArrayParameterStrategy(ObjectParameterType.SHORT) {
        public int length(Object o) {
            return ((short[]) o).length;
        }
    };

    static final PrimitiveArrayParameterStrategy CHAR = new PrimitiveArrayParameterStrategy(ObjectParameterType.CHAR) {
        public int length(Object o) {
            return ((char[]) o).length;
        }
    };

    static final PrimitiveArrayParameterStrategy INT = new PrimitiveArrayParameterStrategy(ObjectParameterType.INT) {
        public int length(Object o) {
            return ((int[]) o).length;
        }
    };

    static final PrimitiveArrayParameterStrategy LONG = new PrimitiveArrayParameterStrategy(ObjectParameterType.LONG) {
        public int length(Object o) {
            return ((long[]) o).length;
        }
    };

    static final PrimitiveArrayParameterStrategy FLOAT = new PrimitiveArrayParameterStrategy(ObjectParameterType.FLOAT) {
        public int length(Object o) {
            return ((float[]) o).length;
        }
    };

    static final PrimitiveArrayParameterStrategy DOUBLE = new PrimitiveArrayParameterStrategy(ObjectParameterType.DOUBLE) {
        public int length(Object o) {
            return ((double[]) o).length;
        }
    };

    static final PrimitiveArrayParameterStrategy BOOLEAN = new PrimitiveArrayParameterStrategy(ObjectParameterType.BOOLEAN) {
        public int length(Object o) {
            return ((boolean[]) o).length;
        }
    };

    PrimitiveArrayParameterStrategy(ObjectParameterType.ComponentType componentType) {
        super(HEAP, ObjectParameterType.create(ObjectParameterType.ObjectType.ARRAY, componentType));
    }

    @Override
    public final long address(Object o) {
        return 0;
    }

    @Override
    public final Object object(Object o) {
        return o;
    }

    @Override
    public final int offset(Object o) {
        return 0;
    }
}
