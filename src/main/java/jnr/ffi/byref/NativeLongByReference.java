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

package jnr.ffi.byref;


import jnr.ffi.NativeLong;
import jnr.ffi.Pointer;
import jnr.ffi.Runtime;

/**
 * NativeLongByReference is used when the address of a primitive C long must be passed
 * as a parameter to a function.
 *
 * <p>For example, the following C code,
 * <pre>
 * {@code
 * extern void get_a(long * ap);
 *
 * long foo(void) {
 *     long a;
 *     // pass a reference to 'a' so get_a() can fill it out
 *     get_a(&a);
 *
 *     return a;
 * }
 * }
 * </pre>
 * <p>
 * Would be declared in java as
 * <pre>
 * {@code
 * interface Lib {
 *     void get_a(@Out NativeLongByReference ap);
 * }
 * }
 * </pre>
 * <p>
 * and used like this
 * <pre>
 * NativeLongByReference ap = new NativeLongByReference();
 * lib.get_a(ap);
 * System.out.printf("a from lib=%d\n", a.longValue());
 * </pre>
 */
public final class NativeLongByReference extends AbstractNumberReference<NativeLong> {
    
    /**
     * Creates a new reference to a native long value initialized to zero.
     */
    public NativeLongByReference() {
        super(NativeLong.valueOf(0));
    }
    
    /**
     * Creates a new reference to a native long value
     * 
     * @param value the initial native value
     */
    public NativeLongByReference(NativeLong value) {
        super(checkNull(value));
    }

    /**
     * Creates a new reference to a native long value
     *
     * @param value the initial native value
     */
    public NativeLongByReference(long value) {
        super(NativeLong.valueOf(value));
    }
    
    /**
     * Copies the long value to native memory
     *
     * @param runtime the current runtime.
     * @param memory  the native memory buffer.
     * @param offset  the memory offset.
     */
    public void toNative(Runtime runtime, Pointer memory, long offset) {
        memory.putNativeLong(offset, value.longValue());
    }

    /**
     * Copies the long value from native memory
     *
     * @param runtime the current runtime.
     * @param memory  the native memory buffer.
     * @param offset  the memory offset.
     */
    public void fromNative(Runtime runtime, Pointer memory, long offset) {
        this.value = NativeLong.valueOf(memory.getNativeLong(offset));
    }
    
    /**
     * Gets the native size of type of reference in bytes.
     * 
     * @param  runtime the current runtime.
     * @return the size of a byte in bytes
     */
    public final int nativeSize(Runtime runtime) {
        return runtime.longSize();
    }
}
