
package com.kenai.jaffl.provider.jffi;

class AllocatedDirectMemoryIO extends DirectMemoryIO {
    private volatile boolean allocated = true;
    public AllocatedDirectMemoryIO(int size, boolean clear) {
        super(IO.allocateMemory(size, clear));
        if (address == 0L) {
            throw new OutOfMemoryError("Failed to allocate " + size + " bytes");
        }
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
