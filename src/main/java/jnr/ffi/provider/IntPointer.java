package jnr.ffi.provider;

import jnr.ffi.Pointer;

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

    @Override
    public int hashCode() {
        return (int) address;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Pointer && ((Pointer) obj).address() == address;
    }
}
