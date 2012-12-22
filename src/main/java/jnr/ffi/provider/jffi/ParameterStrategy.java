package jnr.ffi.provider.jffi;

import com.kenai.jffi.ObjectParameterStrategy;
import com.kenai.jffi.ObjectParameterType;

/**
 *
 */
abstract public class ParameterStrategy extends ObjectParameterStrategy {
    /* objectCount is accessed directly from asm code - do not change */
    public final int objectCount;

    protected ParameterStrategy(StrategyType type) {
        super(type);
        objectCount = type == HEAP ? 1 : 0;
    }

    protected ParameterStrategy(StrategyType type, ObjectParameterType parameterType) {
        super(type, parameterType);
        objectCount = type == HEAP ? 1 : 0;
    }
}
