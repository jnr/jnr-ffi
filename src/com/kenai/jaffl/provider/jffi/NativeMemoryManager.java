
package com.kenai.jaffl.provider.jffi;

import com.kenai.jaffl.Pointer;
import com.kenai.jaffl.provider.BoundedMemoryIO;
import java.nio.ByteBuffer;

public class NativeMemoryManager implements com.kenai.jaffl.provider.MemoryManager {
    private final NativeRuntime runtime;

    public NativeMemoryManager(NativeRuntime runtime) {
        this.runtime = runtime;
    }
    
    public Pointer allocate(int size) {
        return new ArrayMemoryIO(size);
    }

    public Pointer allocateDirect(int size) {
        return new BoundedMemoryIO(new AllocatedDirectMemoryIO(runtime, size, false), 0, size);
    }

    public Pointer allocateDirect(int size, boolean clear) {
        return new BoundedMemoryIO(new AllocatedDirectMemoryIO(runtime, size, clear), 0, size);
    }

    public Pointer wrap(ByteBuffer buffer) {
        return new ByteBufferMemoryIO(runtime, buffer);
    }

    public Pointer wrap(long address) {
        return new DirectMemoryIO(runtime, address);
    }

    public Pointer wrap(long address, long size) {
        return new BoundedMemoryIO(new DirectMemoryIO(runtime, address), 0, size);
    }
}
