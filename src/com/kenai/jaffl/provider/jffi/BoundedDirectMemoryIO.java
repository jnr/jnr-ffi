
package com.kenai.jaffl.provider.jffi;

import com.kenai.jaffl.provider.BoundedMemoryIO;

class BoundedDirectMemoryIO extends BoundedMemoryIO {
    BoundedDirectMemoryIO(DirectMemoryIO parent, long offset, long size) {
        super(parent, offset, size);
    }

    @Override
    public long address() {
        return getDelegatedMemoryIO().address();
    }

}
