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
 * ByteByReference is used when the address of a primitive byte value must be passed
 * as a parameter to a function.
 *
 * <p>For example, the following C code,
 * <pre>
 * {@code
 * extern void get_a(char *ap);
 *
 * int foo(void) {
 *     char a;
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
 *     void get_a(@Out ByteByReference ap);
 * }
 * }
 * </pre>
 * <p>
 * and used like this
 * <pre>
 * ByteByReference ap = new ByteByReference();
 * lib.get_a(ap);
 * System.out.printf("a from lib=%d\n", a.byteValue());
 * </pre>
 */
public final class ByteByReference extends AbstractNumberReference<Byte> {

    /**
     * Creates a new reference to a byte value initialized to zero.
     */
    public ByteByReference() {
        super(Byte.valueOf((byte) 0));
    }

    /**
     * Creates a new reference to a byte value
     * 
     * @param value the initial native value
     */
    public ByteByReference(Byte value) {
        super(checkNull(value));
    }

    /**
     * Creates a new reference to a byte value
     *
     * @param value the initial native value
     */
    public ByteByReference(byte value) {
        super(value);
    }
    
    /**
     * Copies the Byte value to native memory
     *
     * @param runtime The current runtime.
     * @param buffer the native memory buffer
     */
    public void toNative(Runtime runtime, Pointer buffer, long offset) {
        buffer.putByte(offset, value);
    }

    /**
     * Copies the Byte value from native memory
     *
     * @param runtime The current runtime.
     * @param buffer the native memory buffer.
     */
    public void fromNative(Runtime runtime, Pointer buffer, long offset) {
        this.value = buffer.getByte(offset);
    }
    
    /**
     * Gets the native size of type of reference in bytes.
     * 
     * @return the size of a byte in bytes
     */
    public final int nativeSize(Runtime runtime) {
        return 1;
    }
}
