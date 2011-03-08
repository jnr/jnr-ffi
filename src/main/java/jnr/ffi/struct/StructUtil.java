/*
 * Copyright (C) 2008-2010 Wayne Meissner
 *
 * This file is part of the JNR project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jnr.ffi.struct;

import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import java.lang.reflect.Array;

/**
 *
 */
public final class StructUtil {
    private StructUtil() {}
    public final static Pointer getMemory(Struct struct) {
        return struct.__info.getMemory(0);
    }
    public final static Pointer getMemory(Struct struct, int flags) {
        return struct.__info.getMemory(flags);
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
                int structSize = align + ((getSize(array[0]) - 1) & ~(align - 1));

                Pointer memory = runtime.getMemoryManager().allocateDirect(structSize * length);
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
