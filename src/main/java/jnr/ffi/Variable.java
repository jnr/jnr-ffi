/*
 * Copyright (C) 2012 Wayne Meissner
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

package jnr.ffi;

/**
 * Access library global variables.
 *
 * <p>
 *     To access global variables, declare a method with a parameterized return type of this class.
 * <p>
 * <b>Example</b>
 * <pre>
 *     {@code
 *
 *     public interface MyLib {
 *         public Variable<Long> my_int_var();
 *     }
 *
 *     MyLib lib = LibraryLoader.create(MyLib.class).load("mylib"):
 *     System.out.println("native value=" + lib.my_int_var().get())
 *
 *     lib.my_int_var().set(0xdeadbeef);
 *     }
 * </pre>
 */
public interface Variable<T> {
    /**
     * Gets the current value of the global variable
     *
     * @return The value of the variable
     */
    public T get();

    /**
     * Sets the global variable to a value
     *
     * @param value The value to set the global variable to.
     */
    public void set(T value);
}
