
package com.kenai.jaffl.provider.jffi;

import com.kenai.jaffl.Pointer;
import com.kenai.jaffl.Runtime;

class AllocatedDirectMemoryIO extends DirectMemoryIO {
    private volatile boolean allocated = true;
    
    public AllocatedDirectMemoryIO(Runtime runtime, int size, boolean clear) {
        super(runtime, IO.allocateMemory(size, clear));
        if (address == 0L) {
            throw new OutOfMemoryError("Failed to allocate " + size + " bytes");
        }
    }
    public AllocatedDirectMemoryIO(int size, boolean clear) {
        this(NativeRuntime.getInstance(), size, clear);
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
