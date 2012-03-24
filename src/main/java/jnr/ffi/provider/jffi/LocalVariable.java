package jnr.ffi.provider.jffi;

/**
 *
 */
class LocalVariable {
    final Class type;
    final int idx;

    public LocalVariable(Class type, int idx) {
        this.type = type;
        this.idx = idx;
    }
}
