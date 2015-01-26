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

import jnr.ffi.Address;
import jnr.ffi.Pointer;
import jnr.ffi.Runtime;

/**
 * AddressByReference is used when the address of a primitive pointer value must be passed
 * as a parameter to a function.
 *
 * <p>For example, the following C code,
 * <pre>
 * <code>
 *
 * extern void get_a(void** ap);
 *
 * void* foo(void) {
 *     void* a;
 *     // pass a reference to 'a' so get_a() can fill it out
 *     get_a({@literal &}a);
 *
 *     return a;
 * }
 *
 * </code>
 * </pre>
 *
 * <p>
 * Would be declared in java as
 *
 * <pre>
 * <code>
 *
 * interface Lib {
 *     void get_a(@Out AddressByReference ap);
 * }
 *
 * </code>
 * </pre>
 * <p>
 * and used like this
 *
 * <pre>
 * <code>
 *
 * AddressByReference ap = new AddressByReference();
 * lib.get_a(ap);
 * System.out.println("a from lib=" + a.getValue());
 *
 * </code>
 * </pre>
 */
public final class AddressByReference extends AbstractReference<Address> {

    /**
     * Creates a new reference to an integer value
     */
    public AddressByReference() {
        super(Address.valueOf(0));
    }

    /**
     * Creates a new reference to an address value
     * 
     * @param value the initial native value
     */
    public AddressByReference(Address value) {
        super(checkNull(value));
    }
    
    /**
     * Copies the address value to native memory
     *
     * @param runtime The current runtime.
     * @param memory  The native memory buffer
     * @param offset  The offset.
     */
    public void toNative(Runtime runtime, Pointer memory, long offset) {
        memory.putAddress(offset, value.nativeAddress());
    }

    /**
     * Copies the address value from native memory
     *
     * @param runtime The current runtime.
     * @param memory  The native memory buffer.
     * @param offset  The offset.
     */
    public void fromNative(Runtime runtime, Pointer memory, long offset) {
        value = Address.valueOf(memory.getAddress(offset));
    }
    
    /**
     * Gets the native size of type of reference
     * 
     * @param runtime The current runtime.
     * @return The size of the native type this reference points to
     */
    public int nativeSize(Runtime runtime) {
        return runtime.addressSize();
    }
}
