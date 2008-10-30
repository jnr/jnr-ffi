
package com.kenai.jaffl.struct;

import com.kenai.jaffl.MemoryIO;

/**
 *
 */
public final class StructUtil {
    private StructUtil() {}
    public final static MemoryIO getMemoryIO(Struct struct) {
        return struct.__info.getMemoryIO(0);
    }
    public final static MemoryIO getMemoryIO(Struct struct, int flags) {
        return struct.__info.getMemoryIO(flags);
    }
    public final static int getSize(Struct struct) {
        return struct.__info.size();
    }
}
