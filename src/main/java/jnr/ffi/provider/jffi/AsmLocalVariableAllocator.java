package jnr.ffi.provider.jffi;

/**
 *
 */
class AsmLocalVariableAllocator {
    private int idx;

    AsmLocalVariableAllocator(ParameterType[] parameterTypes) {
        this.idx = AsmUtil.calculateLocalVariableSpace(parameterTypes);
    }

    AsmLocalVariable allocate(Class type) {
        this.idx += AsmUtil.calculateLocalVariableSpace(type);
        return new AsmLocalVariable(idx);
    }
}
