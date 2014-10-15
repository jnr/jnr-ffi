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
 * AddressByReference is used when the address of a pointer must be passed
 * as a parameter to a function.
 *
 * <p>
 * For example, the following C code,
 * <pre>
 * {@code
 * extern void get_a(void** ap);
 *
 * void* foo(void) {
 *     void* a;
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
 *     void get_a(@Out PointerByReference ap);
 * }
 * }
 * </pre>
 * <p>
 * and used like this
 * <pre>
 * {@code
 * PointerByReference ap = new PointerByReference();
 * lib.get_a(ap);
 * Pointer ptr = ap.getValue();
 * System.out.println("ptr from lib=" + a.getValue());
 * System.out.println("ptr contents=" + ptr.getInt(0));
 * }
 * </pre>
 */
public final class PointerByReference extends AbstractReference<Pointer>{
    /**
     * Creates a new reference to a pointer value with a null default value.
     */
    public PointerByReference() {
        super(null);
    }

    /**
     * Creates a new reference to a pointer value
     *
     * @param value the initial pointer value
     */
    public PointerByReference(Pointer value) {
        super(value);
    }

    public final void toNative(Runtime runtime, Pointer memory, long offset) {
        memory.putPointer(offset, this.value);
    }

    public final void fromNative(Runtime runtime, Pointer memory, long offset) {
        this.value = memory.getPointer(offset);
    }

    public final int nativeSize(Runtime runtime) {
        return runtime.addressSize();
    }
}
