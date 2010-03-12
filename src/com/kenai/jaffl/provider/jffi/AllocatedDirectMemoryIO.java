
package com.kenai.jaffl.provider.jffi;

import com.kenai.jaffl.Pointer;
import com.kenai.jaffl.Runtime;

class AllocatedDirectMemoryIO extends DirectMemoryIO {
    private volatile boolean allocated = true;
    private final int size;
    
    public AllocatedDirectMemoryIO(Runtime runtime, int size, boolean clear) {
        super(runtime, IO.allocateMemory(size, clear));
        this.size = size;
        if (address == 0L) {
            throw new OutOfMemoryError("Failed to allocate " + size + " bytes");
        }
    }
    public AllocatedDirectMemoryIO(int size, boolean clear) {
        this(NativeRuntime.getInstance(), size, clear);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AllocatedDirectMemoryIO) {
            AllocatedDirectMemoryIO mem = (AllocatedDirectMemoryIO) obj;
            return mem.size == size && mem.address() == address;
        }
        
        return super.equals(obj);
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            if (allocated) {
                IO.freeMemory(address);
                allocated = false;
            }
        } finally {
            super.finalize();
        }
    }
}
