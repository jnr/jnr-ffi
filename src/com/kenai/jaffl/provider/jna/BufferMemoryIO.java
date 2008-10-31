package com.kenai.jaffl.provider.jna;

import com.kenai.jaffl.MemoryIO;
import com.kenai.jaffl.Pointer;
import com.kenai.jaffl.provider.AbstractBufferMemoryIO;
import java.nio.ByteBuffer;

/**
 *
 */
public class BufferMemoryIO extends AbstractBufferMemoryIO {
    public BufferMemoryIO(ByteBuffer buffer) {
        super(buffer);
    }
    @Override
    public MemoryIO getMemoryIO(long offset) {
        // FIXME
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public MemoryIO getMemoryIO(long offset, long size) {
        // FIXME
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Pointer getPointer(long offset) {
        // FIXME
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void putPointer(long offset, Pointer value) {
        // FIXME
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
