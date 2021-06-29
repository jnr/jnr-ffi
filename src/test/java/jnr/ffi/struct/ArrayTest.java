/*
 * Copyright (C) 2007-2010 Wayne Meissner
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
import jnr.ffi.Struct;
import jnr.ffi.TstUtil;
import jnr.ffi.annotations.In;
import jnr.ffi.provider.AbstractArrayMemoryIO;
import jnr.ffi.provider.DelegatingMemoryIO;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public class ArrayTest {
    public static interface TestLib {
        byte ptr_ret_int8_t(@In s8[] s, int index);
        class PointerStruct extends Struct {

            public final Signed8 s8 = new Signed8();
            public final Pointer p = new Pointer();

            public PointerStruct(Runtime runtime) {
                super(runtime);
            }
        }
    }

    static TestLib testlib;
    static Runtime runtime;
    public ArrayTest() {
    }

    @BeforeAll
    public static void setUpClass() throws Exception {
        testlib = TstUtil.loadTestLib(TestLib.class);
        runtime = Runtime.getRuntime(testlib);
    }

    @AfterAll
    public static void tearDownClass() throws Exception {
    }

    @BeforeEach
    public void setUp() {
    }

    @AfterEach
    public void tearDown() {
    }
    public static final class s8 extends Struct {
        public final Signed8 s8 = new Signed8();

        public s8(Runtime runtime) {
            super(runtime);
        }

    }
    public static final class s32 extends Struct {
        public final Signed8 s8 = new Signed8();
        public s32(Runtime runtime) {
            super(runtime);
        }
    }
    
    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    @Test public void s8Array() {
        s8[] array = Struct.arrayOf(runtime, s8.class, 10);
        assertEquals(10, array.length, "Array length incorrect");
        for (int i = 0; i < array.length; ++i) {
            assertNotNull(Struct.getMemory(array[i]), "Memory not allocated for array member");
        }
        Pointer ptr = ((DelegatingMemoryIO) Struct.getMemory(array[0])).getDelegatedMemoryIO();
        for (int i = 0; i < array.length; ++i) {
            assertSame(ptr, ((DelegatingMemoryIO) Struct.getMemory(array[i])).getDelegatedMemoryIO(), "Different backing memory");
        }
        if (ptr instanceof AbstractArrayMemoryIO) {
            assertEquals(array.length, ((AbstractArrayMemoryIO) ptr).length(), "Incorrect size");
        }
        for (int i = 0; i < array.length; ++i) {
            array[i].s8.set((byte) i);
        }
        for (int i = 0; i < array.length; ++i) {
            assertEquals((byte) i, testlib.ptr_ret_int8_t(array, i),
                    "Incorrect value written to native memory at index " + i);
        }
    }
}