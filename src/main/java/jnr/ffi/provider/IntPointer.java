package jnr.ffi.provider;

import jnr.ffi.Pointer;

/**
 *
 */
public final class IntPointer extends InAccessibleMemoryIO {
    public IntPointer(jnr.ffi.Runtime runtime, long address) {
        super(runtime, address, true);
    }

    public IntPointer(jnr.ffi.Runtime runtime, int address) {
        super(runtime, address & 0xffffffffL, true);
    }

    public long size() {
        return 0;
    }

    @Override
    public int hashCode() {
        return (int) address();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Pointer && ((Pointer) obj).address() == address();
    }
}
