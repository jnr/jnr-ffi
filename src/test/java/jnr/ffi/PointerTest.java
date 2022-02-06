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

package jnr.ffi;

import jnr.ffi.annotations.In;
import jnr.ffi.annotations.LongLong;
import jnr.ffi.annotations.Out;
import jnr.ffi.types.int32_t;
import jnr.ffi.types.int8_t;
import jnr.ffi.types.size_t;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.ByteOrder;
import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PointerTest {

    public static interface TestLib {
        Pointer ptr_return_array_element(@In Pointer[] array, int index);
        void ptr_set_array_element(@Out Pointer[] array, int index, Pointer value);
        byte ptr_ret_int8_t(Pointer p, int offset);
        byte ptr_ret_int8_t(Address p, int offset);
        short ptr_ret_int16_t(Pointer p, int offset);
        int ptr_ret_int32_t(Pointer p, int offset);
        @LongLong long ptr_ret_int64_t(Pointer p, int offset);
        float ptr_ret_float(Pointer p, int offset);
        double ptr_ret_double(Pointer p, int offset);
        void ptr_set_int8_t(Pointer p, int offset, @int8_t byte value);
        void ptr_set_int16_t(Pointer p, int offset, short value);
        void ptr_set_int32_t(Pointer p, int offset, @int32_t int value);
        void ptr_set_int32_t(Pointer p, int offset, @int32_t long value);
        void ptr_set_int64_t(Pointer p, int offset, @LongLong long value);
        void ptr_set_float(Pointer p, int offset, float value);
        void ptr_set_double(Pointer p, int offset, double value);
        void ptr_reverse_l5(Pointer p1, Pointer p2, Pointer p3, Pointer p4, Pointer p5);
        void ptr_reverse_l6(Pointer p1, Pointer p2, Pointer p3, Pointer p4, Pointer p5, Pointer p6);

        public static final class Foo extends Struct {
            public final UnsignedLong l1 = new UnsignedLong();
            public final UnsignedLong l2 = new UnsignedLong();
            public final UnsignedLong l3 = new UnsignedLong();

            public Foo(Runtime runtime) {
                super(runtime);
            }
        }
        int fill_struct_from_longs(@size_t long l1, @size_t long l2, @Out Foo[] foo, @size_t long l3);

        Pointer ptr_malloc(@size_t int size);
        void ptr_free(Pointer ptr);
    }
    static TestLib testlib;
    static Runtime runtime;

    @BeforeAll
    public static void beforeAll() throws Exception {
        testlib = TstUtil.loadTestLib(TestLib.class);
        runtime = Runtime.getRuntime(testlib);
    }

    static final int SIZE = 128;

    @Test
    public void testMalloc() {
        Pointer[] pointers = new Pointer[1024];
        for (int i  = 0; i < pointers.length; ++i) {
            pointers[i] = testlib.ptr_malloc(SIZE);
        }
        for (int i  = 0; i < pointers.length; ++i) {
            testlib.ptr_free(pointers[i]);
        }      
    }

    @Test public void nullTerminatedStringArray() {
        Runtime runtime = Runtime.getRuntime(testlib);
        Pointer[] array = new Pointer[10];
        String[] in = new String[array.length];
        for (int i = 0; i < array.length; i++) {
            array[i] = Memory.allocateDirect(runtime, 128);
            array[i].putString(0, in[i] = Integer.toString(i), 128, Charset.defaultCharset());
        }
        Pointer memory = Memory.allocateDirect(runtime, (2 * array.length + 1) * runtime.addressSize(), true);
        memory.put(array.length * runtime.addressSize(), array, 0, array.length);
        String[] out = memory.getNullTerminatedStringArray(array.length * runtime.addressSize());
        assertArrayEquals(in, out);
    }

    @Test public void nullTerminatedPointerArray() {
        Runtime runtime = Runtime.getRuntime(testlib);
        Pointer[] array = new Pointer[10];
        String[] in = new String[array.length];
        for (int i = 0; i < array.length; i++) {
            array[i] = Memory.allocateDirect(runtime, 128);
            array[i].putString(0, in[i] = Integer.toString(i), 128, Charset.defaultCharset());
        }
        Pointer memory = Memory.allocateDirect(runtime, (2 * array.length + 1) * runtime.addressSize(), true);
        memory.put(array.length * runtime.addressSize(), array, 0, array.length);
        Pointer[] out = memory.getNullTerminatedPointerArray(array.length * runtime.addressSize());
        assertArrayEquals(array, out);
    }

    @Test
    public void testAddressSetByte() {

        Pointer p = testlib.ptr_malloc(SIZE);
        byte MAGIC = (byte) 0xFE;
        for (int i = 0; i < SIZE; ++i) {
            p.putByte(i, MAGIC);
            assertEquals(MAGIC, testlib.ptr_ret_int8_t(Address.valueOf(p.address()), i), "Byte not set at offset " + i);
        }
    }

    @Test
    public void pointerArrayGetElement() {
        Pointer[] ary = new Pointer[10];
        Pointer p1 = ary[0] = runtime.getMemoryManager().newPointer(0xdeadbeef & runtime.addressMask());
        Pointer p2 = ary[9] = runtime.getMemoryManager().newPointer(0xfee1dead & runtime.addressMask());
        assertEquals(p1, testlib.ptr_return_array_element(ary, 0));
        assertEquals(p2, testlib.ptr_return_array_element(ary, 9));
    }

    @Test
    public void pointerArraySetElement() {
        Pointer[] ary = new Pointer[10];
        Pointer p1 = runtime.getMemoryManager().newPointer(0xdeadbeef & runtime.addressMask());
        Pointer p2 = runtime.getMemoryManager().newPointer(0xfee1dead & runtime.addressMask());
        testlib.ptr_set_array_element(ary, 0, p1);
        assertEquals(p1, ary[0]);
        testlib.ptr_set_array_element(ary, 9, p2);
        assertEquals(p2, ary[9]);
    }

    @Test public void mixObjectsAndPrimitives() {
        TestLib.Foo[] structs = Struct.arrayOf(runtime, TestLib.Foo.class, 1);
        TestLib.Foo foo = structs[0];

        testlib.fill_struct_from_longs(0xdeadL, 0xbeefL, structs, 0x1eefcafe);
        assertEquals(0xdeadL, foo.l1.get());
        assertEquals(0xbeefL, foo.l2.get());
        assertEquals(0x1eefcafeL, foo.l3.get());
    }

    @Test
    public void testTransferTo() {
        Pointer src = runtime.getMemoryManager().allocateDirect(128);
        Pointer dst = runtime.getMemoryManager().allocateDirect(128);
        for (int i = 0; i < src.size(); i++) {
             src.putByte(i, (byte)(i % 256));
        }

        src.transferTo(0, dst, 0, src.size());

        for (int i = 0; i < src.size(); i++) {
            assertEquals(src.getByte(i), dst.getByte(i));
        }
    }

    @Test
    public void testTransferToSrcOutOfBounds() {
        Pointer src = runtime.getMemoryManager().allocateDirect(128);
        Pointer dst = runtime.getMemoryManager().allocateDirect(128);
        assertThrows(IndexOutOfBoundsException.class, () -> {
            src.transferTo(10, dst, 0, src.size());
        });
    }

    @Test
    public void testTransferToDstOutOfBounds() {
        Pointer src = runtime.getMemoryManager().allocateDirect(128);
        Pointer dst = runtime.getMemoryManager().allocateDirect(128);
        assertThrows(IndexOutOfBoundsException.class, () -> {
            src.transferTo(0, dst, 10, src.size());
        });
    }

    @Test
    public void testTransferFrom() {
        Pointer src = runtime.getMemoryManager().allocateDirect(128);
        Pointer dst = runtime.getMemoryManager().allocateDirect(128);
        for (int i = 0; i < src.size(); i++) {
            src.putByte(i, (byte)(i & 0xFF));
        }

        dst.transferFrom(0, src, 0, src.size());

        for (int i = 0; i < src.size(); i++) {
            assertEquals(src.getByte(i), dst.getByte(i));
        }
    }

    @Test
    public void testTransferFromSrcOutOfBounds() {
        Pointer src = runtime.getMemoryManager().allocateDirect(128);
        Pointer dst = runtime.getMemoryManager().allocateDirect(128);
        assertThrows(IndexOutOfBoundsException.class, () -> {
            dst.transferFrom(0, dst, 10, src.size());
        });
    }

    @Test
    public void testTransferFromDstOutOfBounds() {
        Pointer src = runtime.getMemoryManager().allocateDirect(128);
        Pointer dst = runtime.getMemoryManager().allocateDirect(128);
        assertThrows(IndexOutOfBoundsException.class, () -> {
            dst.transferFrom(10, dst, 0, src.size());
        });
    }
}
