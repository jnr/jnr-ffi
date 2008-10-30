
package com.kenai.jaffl.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

public class MultiBufferPool implements BufferPool {
    
    /**
     * Creates a new instance of MultiBufferPool
     */
    public MultiBufferPool(int maxBufferSize, int maxItemsPerSize, boolean threadSafe) {
        this.maxBufferSize = maxBufferSize;
        this.maxItemsPerSize = maxItemsPerSize;
        maxPoolIndex = getSizeIndex(maxBufferSize);
        pools = new SimpleBufferPool[maxPoolIndex + 1];
        
        // Now create each of the buckets
        for (int i = 0; i <= maxPoolIndex; ++i) {
            if (threadSafe) {
                pools[i] = new SynchronizedPool(1 << i, maxItemsPerSize);
            } else {
                pools[i] = new SimpleBufferPool(1 << i, maxItemsPerSize);
            }
        }
    }
    public MultiBufferPool(int maxBufferSize, int maxItemsPerSize) {
        this(maxBufferSize, maxItemsPerSize, false);
    }
    
    private final int maxBufferSize, maxItemsPerSize, maxPoolIndex;
    private SimpleBufferPool[] pools;
    private static final int getSizeIndex(int size) {
        int start = 0;
        int ssize = size;
        //
        // Cut down the loop size by dividing up the address space
        //
        if (ssize > 0xffff) {
            start += 16;
            ssize >>= 16;
        }
        if (ssize > 0xff) {
            start += 8;
            ssize >>= 8;
        }
        if (ssize > 0xf) {
            start += 4;
        }
        for (int i = start; i < 32; ++i) {
            if ((1 << i) >= size) {
                //System.out.println("size " + size + " maps to pool index " + i);
                return i;
            }
        }
        return 32;
    }
    public ByteBuffer get(int size) {
        int index = getSizeIndex(size);
        if (index <= maxPoolIndex) {
            return pools[index].get(size);
        }
        return ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder());
    }
    public void put(ByteBuffer buf) {
        int index = getSizeIndex(buf.capacity());
        if (index <= maxPoolIndex) {
            pools[index].put(buf);
        }
    }
    public void putAll(List<ByteBuffer> list) {
        for (ByteBuffer buf : list) {
            put(buf);
        }
    }
    
    static class SynchronizedPool extends SimpleBufferPool {
        public SynchronizedPool(int bufferSize, int poolSize) {
            super(bufferSize, poolSize);
        }
        public synchronized ByteBuffer get(int size) {
            return super.get(size);
        }
        public synchronized void put(ByteBuffer buf) {
            super.put(buf);
        }
    }
}
