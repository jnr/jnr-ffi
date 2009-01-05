
package com.kenai.jaffl.provider.jffi;

import com.kenai.jaffl.MemoryIO;
import com.kenai.jaffl.Pointer;
import com.kenai.jaffl.provider.NullMemoryIO;
import java.nio.Buffer;
import java.nio.ByteBuffer;

public class MemoryManager implements com.kenai.jaffl.provider.MemoryManager {

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
        if (ptr instanceof JFFIPointer) {
            long address = ((JFFIPointer) ptr).address;
            return address != 0 ? new DirectMemoryIO(address) : new NullMemoryIO();
        }
        throw new UnsupportedOperationException("Unsupported Pointer type");
    }

    public MemoryIO wrap(Pointer ptr, int size) {
        if (ptr instanceof JFFIPointer) {
            long address = ((JFFIPointer) ptr).address;
            return address != 0 ? new BoundedDirectMemoryIO(new DirectMemoryIO(address), 0, size) : new NullMemoryIO();
        }
        throw new UnsupportedOperationException("Unsupported Pointer type");
    }

    public MemoryIO wrap(ByteBuffer buffer) {
        return new ByteBufferMemoryIO(buffer);
    }

    public Pointer getBufferPointer(Buffer buffer) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
