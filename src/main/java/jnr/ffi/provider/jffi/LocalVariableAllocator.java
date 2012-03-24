package jnr.ffi.provider.jffi;

/**
 *
 */
class LocalVariableAllocator {
    private int nextIndex;

    LocalVariableAllocator(SigType[] parameterTypes) {
        this.nextIndex = AsmUtil.calculateLocalVariableSpace(parameterTypes) + 1;
    }

    LocalVariableAllocator(Class... parameterTypes) {
        this.nextIndex = AsmUtil.calculateLocalVariableSpace(parameterTypes) + 1;
    }

    LocalVariableAllocator(int nextIndex) {
        this.nextIndex = nextIndex;
    }

    LocalVariable allocate(Class type) {
        LocalVariable var = new LocalVariable(type, nextIndex);
        this.nextIndex += AsmUtil.calculateLocalVariableSpace(type);
        return var;
    }

    int getSpaceUsed() {
        return nextIndex;
    }
}
