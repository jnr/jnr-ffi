
package com.kenai.jaffl.provider.jffi;

import com.kenai.jaffl.MemoryIO;
import com.kenai.jaffl.Pointer;
import com.kenai.jaffl.provider.BoundedMemoryIO;
import java.nio.ByteBuffer;

public class NativeMemoryManager implements com.kenai.jaffl.provider.MemoryManager {

    public Pointer allocate(int size) {
        return new ArrayMemoryIO(size);
    }

    public Pointer allocateDirect(int size) {
        return new BoundedMemoryIO(new AllocatedDirectMemoryIO(size, false), 0, size);
    }

    public Pointer allocateDirect(int size, boolean clear) {
        return new BoundedMemoryIO(new AllocatedDirectMemoryIO(size, clear), 0, size);
    }

    public MemoryIO wrap(ByteBuffer buffer) {
        return new ByteBufferMemoryIO(buffer);
    }
}
