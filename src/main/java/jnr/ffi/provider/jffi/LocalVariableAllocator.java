package jnr.ffi.provider.jffi;

/**
 *
 */
class LocalVariableAllocator {
    private int idx;

    LocalVariableAllocator(ParameterType[] parameterTypes) {
        this.idx = AsmUtil.calculateLocalVariableSpace(parameterTypes) + 1;
    }

    LocalVariable allocate(Class type) {
        LocalVariable var = new LocalVariable(idx);
        this.idx += AsmUtil.calculateLocalVariableSpace(type);
        return var;
    }

    int getSpaceUsed() {
        return idx;
    }
}
