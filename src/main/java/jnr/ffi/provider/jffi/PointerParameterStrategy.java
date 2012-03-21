package jnr.ffi.provider.jffi;

import com.kenai.jffi.ObjectParameterStrategy;
import com.kenai.jffi.ObjectParameterType;

/**
 *
 */
abstract public class PointerParameterStrategy extends ObjectParameterStrategy {
    /* objectCount is accessed directly from asm code - do not change */
    public final int objectCount;

    protected PointerParameterStrategy(StrategyType type) {
        super(type);
        objectCount = type == HEAP ? 1 : 0;
    }

    protected PointerParameterStrategy(StrategyType type, ObjectParameterType parameterType) {
        super(type, parameterType);
        objectCount = type == HEAP ? 1 : 0;
    }


    public long getAddress(Object o) {
        return address(o);
    }

    public abstract long address(Object o);
}
