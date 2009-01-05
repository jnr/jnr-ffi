
package com.kenai.jaffl.provider.jffi;

import com.kenai.jaffl.MemoryIO;
import com.kenai.jaffl.Pointer;
import com.kenai.jaffl.provider.AbstractArrayMemoryIO;
import com.kenai.jaffl.provider.NullMemoryIO;


public final class ArrayMemoryIO extends AbstractArrayMemoryIO {

    public ArrayMemoryIO(int size) {
        super(size);
    }

    @Override
    public MemoryIO getMemoryIO(long offset) {
        final long address = getAddress(offset);
        return address != 0 ? new DirectMemoryIO(address) : new NullMemoryIO();
    }

    @Override
    public MemoryIO getMemoryIO(long offset, long size) {
        final long address = getAddress(offset);
        return address != 0 ? new BoundedDirectMemoryIO(new DirectMemoryIO(address), 0, size) : new NullMemoryIO();
    }

    @Override
    public Pointer getPointer(long offset) {
        long ptr = getAddress(offset);
        return ptr != 0 ? new JFFIPointer(ptr) : null;
    }

    @Override
    public void putPointer(long offset, Pointer value) {
        if (value == null) {
            putAddress(offset, 0L);
        } else if (value instanceof JFFIPointer) {
            putAddress(offset, ((JFFIPointer) value).address);
        }
        throw new IllegalArgumentException("Invalid Pointer");
    }
}
