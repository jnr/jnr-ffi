package jnr.ffi.provider.jffi;

/**
 *
 */
class LocalVariableAllocator {
    private int idx;

    LocalVariableAllocator(ParameterType[] parameterTypes) {
        this.idx = AsmUtil.calculateLocalVariableSpace(parameterTypes);
    }

    LocalVariable allocate(Class type) {
        this.idx += AsmUtil.calculateLocalVariableSpace(type);
        return new LocalVariable(idx);
    }
}
