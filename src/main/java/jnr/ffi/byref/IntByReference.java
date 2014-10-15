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


import jnr.ffi.Pointer;
import jnr.ffi.Runtime;

/**
 * IntByReference is used when the address of a primitive int must be passed
 * as a parameter to a function.
 *
 * <p>
 * For example, the following C code,
 * <pre>
 * {@code
 * extern void get_a(int * ap);
 *
 * int foo(void) {
 *     int a;
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
 *     void get_a(@Out IntByReference ap);
 * }
 * }
 * </pre>
 * <p>
 * and used like this
 * <pre>
 * IntByReference ap = new IntByReference();
 * lib.get_a(ap);
 * System.out.printf("a from lib=%d\n", a.intValue());
 * </pre>
 */
public final class IntByReference extends AbstractNumberReference<Integer> {

    /**
     * Creates a new reference to an integer value initialized to zero.
     */
    public IntByReference() {
        super(Integer.valueOf(0));
    }

    /**
     * Creates a new reference to an integer value
     * 
     * @param value the initial native value
     */
    public IntByReference(Integer value) {
        super(checkNull(value));
    }

    /**
     * Creates a new reference to an integer value
     *
     * @param value the initial native value
     */
    public IntByReference(int value) {
        super(value);
    }
    
    /**
     * Copies the integer value to native memory
     *
     * @param runtime the current runtime.
     * @param buffer  the native memory buffer.
     * @param offset  the memory offset.
     */
    public void toNative(Runtime runtime, Pointer buffer, long offset) {
        buffer.putInt(offset, value);
    }

    /**
     * Copies the integer value from native memory
     *
     * @param runtime the current runtime.
     * @param buffer  the native memory buffer.
     * @param offset  the memory offset.
     */
    public void fromNative(Runtime runtime, Pointer buffer, long offset) {
        this.value = buffer.getInt(offset);
    }
    
    /**
     * Gets the native size of type of reference
     * 
     * @return Integer.SIZE
     */
    public int nativeSize(Runtime runtime) {
        return 4;
    }
}
