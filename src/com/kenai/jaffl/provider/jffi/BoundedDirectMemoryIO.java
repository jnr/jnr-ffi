
package com.kenai.jaffl.provider.jffi;

import com.kenai.jaffl.provider.BoundedMemoryIO;

class BoundedDirectMemoryIO extends BoundedMemoryIO implements DirectMemory {
    BoundedDirectMemoryIO(DirectMemoryIO parent, long offset, long size) {
        super(parent, offset, size);
    }

    public long getAddress() {
        return ((DirectMemory) getDelegatedMemoryIO()).getAddress();
    }

}
