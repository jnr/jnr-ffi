
package com.kenai.jaffl.provider.jffi;

import com.kenai.jaffl.MemoryIO;
import com.kenai.jaffl.Pointer;
import java.nio.Buffer;
import java.nio.ByteBuffer;

public class NativeMemoryManager implements com.kenai.jaffl.provider.MemoryManager {

    public MemoryIO allocate(int size) {
        return new ArrayMemoryIO(size);
    }

    public MemoryIO allocateDirect(int size) {
        return new BoundedDirectMemoryIO(new AllocatedDirectMemoryIO(size, false), 0, size);
    }

    public MemoryIO allocateDirect(int size, boolean clear) {
        return new BoundedDirectMemoryIO(new AllocatedDirectMemoryIO(size, clear), 0, size);
    }

    public MemoryIO wrap(Pointer ptr) {
        if (ptr instanceof MemoryIO) {
            return (MemoryIO) ptr;

        } else if (ptr.isDirect()) {
            return MemoryUtil.newMemoryIO(ptr.address());
        }

        throw new UnsupportedOperationException("Unsupported Pointer type: " + ptr.getClass());
    }

    public MemoryIO wrap(Pointer ptr, int size) {
        if (ptr.isDirect()) {
            return MemoryUtil.newMemoryIO(ptr.address(), size);
        }

        throw new UnsupportedOperationException("Unsupported Pointer type: " + ptr.getClass());
    }

    public MemoryIO wrap(ByteBuffer buffer) {
        return new ByteBufferMemoryIO(buffer);
    }
}
