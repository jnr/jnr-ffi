
package com.kenai.jaffl.struct;

import com.kenai.jaffl.MemoryIO;
import com.kenai.jaffl.Runtime;
import java.lang.reflect.Array;

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
    public final static int getMinimumAlignment(Struct struct) {
        return struct.__info.getMinimumAlignment();
    }
    
    public final static boolean isDirect(Struct struct) {
        return struct.__info.isDirect();
    }

    @SuppressWarnings("unchecked")
    public static final <T extends Struct> T[] newArray(Runtime runtime, Class<T> type, int length) {
        try {
            T[] array = (T[]) Array.newInstance(type, length);
            for (int i = 0; i < length; ++i) {
                array[i] = type.newInstance();
            }
            if (array.length > 0) {
                final int align = getMinimumAlignment(array[0]);
                final int mask = align - 1;
                int structSize = getSize(array[0]);
                if ((structSize & mask) != 0) {
                    structSize = (structSize & ~mask) + align;
                }
                MemoryIO memory = MemoryIO.allocateDirect(runtime, structSize * length);
                for (int i = 0; i < array.length; ++i) {
                    array[i].useMemory(memory.slice(structSize * i, structSize));
                }
            }
            return array;
        } catch (SecurityException ex) {
            throw new RuntimeException(ex);
        } catch (InstantiationException ex) {
            throw new RuntimeException(ex);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }
}
