
package com.kenai.jaffl.provider.jffi;

import com.kenai.jaffl.Pointer;
import com.kenai.jaffl.Runtime;
import com.kenai.jaffl.provider.AbstractArrayMemoryIO;


public final class ArrayMemoryIO extends AbstractArrayMemoryIO {

    public ArrayMemoryIO(Runtime runtime, int size) {
        super(runtime, size);
    }

    public ArrayMemoryIO(int size) {
        super(NativeRuntime.getInstance(), size);
    }

    @Override
    public Pointer getPointer(long offset) {
        return MemoryUtil.newPointer(getAddress(offset));
    }

    @Override
    public Pointer getPointer(long offset, long size) {
        return MemoryUtil.newPointer(getAddress(offset), size);
    }

    @Override
    public void putPointer(long offset, Pointer value) {
        putAddress(offset, value.address());
    }
}
