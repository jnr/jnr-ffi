
package com.kenai.jaffl.provider.jffi;

import com.kenai.jaffl.MemoryIO;
import com.kenai.jaffl.Pointer;
import com.kenai.jaffl.provider.AbstractArrayMemoryIO;


public final class ArrayMemoryIO extends AbstractArrayMemoryIO {

    public ArrayMemoryIO(int size) {
        super(size);
    }

    @Override
    public MemoryIO getMemoryIO(long offset) {
        return MemoryUtil.newMemoryIO(getAddress(offset));
    }

    @Override
    public MemoryIO getMemoryIO(long offset, long size) {
        return MemoryUtil.newMemoryIO(getAddress(offset), size);
    }

    @Override
    public Pointer getPointer(long offset) {
        return MemoryUtil.newPointer(getAddress(offset));
    }

    @Override
    public void putPointer(long offset, Pointer value) {
        putAddress(offset, value.address());
    }
}
