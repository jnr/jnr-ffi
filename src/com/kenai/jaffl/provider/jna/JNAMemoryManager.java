
package com.kenai.jaffl.provider.jna;

import com.kenai.jaffl.MemoryIO;
import com.kenai.jaffl.Pointer;
import com.kenai.jaffl.provider.MemoryManager;
import com.sun.jna.Native;
import java.nio.Buffer;

/**
 *
 */
class JNAMemoryManager implements MemoryManager {
    JNAMemoryManager() {}
    
    
    public MemoryIO allocate(int size) {
        return allocateDirect(size, true);
    }

    public MemoryIO allocateDirect(int size) {
        return new PointerMemoryIO(new com.sun.jna.Memory(size));
    }

    public MemoryIO allocateDirect(int size, boolean clear) {
        com.sun.jna.Memory m = new com.sun.jna.Memory(size);
        if (clear) m.clear();
        return new PointerMemoryIO(m);
    }

    public MemoryIO wrap(Pointer address) {
        return new PointerMemoryIO(((JNAPointer) address).getNativePointer());
    }

    public MemoryIO wrap(Pointer address, int size) {
        return new PointerMemoryIO(((JNAPointer) address).getNativePointer());
    }

    public Pointer getBufferPointer(Buffer buffer) {
        return new JNAPointer(Native.getDirectBufferPointer(buffer));
    }

}
