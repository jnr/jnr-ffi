
package com.kenai.jaffl.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class SimpleBufferPool implements BufferPool {
    private final int bufferSize, poolSize;
    private final BufferPool parent;
    private final ArrayList<ByteBuffer> list;
    
    /**
     * Creates a new instance of SimpleBufferPool
     * @param bufferSize The Size of ByteBuffer this pool should return
     * @param poolSize The maximum number of ByteBuffers to cache
     */
    public SimpleBufferPool(int bufferSize, int poolSize) {
        this(new DefaultPool(), bufferSize, poolSize);
    }
    
    /** Creates a new instance of SimpleBufferPool
     * @param parent The parent pool from which to to fetch/return extra buffers.
     * @param bufferSize The Size of ByteBuffer this pool should return.
     * @param poolSize The maximum number of ByteBuffers to cache.
     */
    public SimpleBufferPool(BufferPool parent, int bufferSize, int poolSize) {
        this.parent = parent;
        this.bufferSize = bufferSize;
        this.poolSize = poolSize;
        this.list = new ArrayList<ByteBuffer>(poolSize);
    }
    
    public ByteBuffer get(int size) {
        if (size <= bufferSize && !list.isEmpty()) {
            //System.out.println("Returning cached buffer for size=" + size);
            // Removing from the end of the ArrayList is O(1)
            ByteBuffer buf = list.remove(list.size() - 1);
            buf.rewind().limit(size);
            return buf;
        }
        // Fetch a new buffer from the parent pool
        //System.out.println("Allocating new direct ByteBuffer");
        // Default to allocating a new buffer - make it at least bufferSize so it
        // can be added back to the pool later
        // This also handles buffers that are larger than the pool bufferSize.
//        System.out.println("Requested buffer size of " + size + " is larger than " + bufferSize);
        ByteBuffer buf = parent.get(Math.max(size, bufferSize));
        buf.rewind().limit(size);
        return buf;
    }
    public void put(ByteBuffer buf) {
        if (list.size() < poolSize && buf.capacity() == bufferSize) {
            //System.out.println("Storing ByteBuffer in pool size=" + bufferSize);
            // Adding at the end of the ArrayList is O(1)
            list.add(buf);
        } else {
            parent.put(buf);
        }
    }
    public void putAll(List<ByteBuffer> list) {
        for (ByteBuffer buf : list) {
            put(buf);
        }
    }
    static class DefaultPool implements BufferPool {
        public ByteBuffer get(int size) {
            return ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder());
        }
        // Just let the GC collect the buffers
        public void put(ByteBuffer buffer) { }
        public void putAll(List<ByteBuffer> list) { }
    }
}
