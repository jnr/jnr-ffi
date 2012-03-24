package jnr.ffi.provider;

/**
 *
 */
public final class IntPointer extends InAccessibleMemoryIO {
    private final long address;

    public IntPointer(jnr.ffi.Runtime runtime, long address) {
        super(runtime);
        this.address = address;
    }

    public IntPointer(jnr.ffi.Runtime runtime, int address) {
        super(runtime);
        this.address = address & 0xffffffffL;
    }

    public boolean isDirect() {
        return true;
    }

    public long address() {
        return address;
    }

    public long size() {
        return 0;
    }
}
