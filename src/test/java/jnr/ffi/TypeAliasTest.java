/*
 * Copyright (C) 2018 Arne Plöse
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

import java.util.EnumSet;
import java.util.Set;
import jnr.ffi.provider.jffi.NativeRuntime;
import jnr.ffi.provider.jffi.platform.aarch64.linux.TypeAliases;
import jnr.ffi.types.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 */
public class TypeAliasTest {

    private final static long REQ_HEX_5 = 0x5555555555555555L;

    private final static long RSP_HEX_5__S_INT_08 = 0xffffffffffffffaaL;
    private final static long RSP_HEX_5__S_INT_16 = 0xffffffffffffaaaaL;
    private final static long RSP_HEX_5__S_INT_32 = 0xffffffffaaaaaaaaL;
    private final static long RSP_HEX_5__S_INT_64 = 0xaaaaaaaaaaaaaaaaL;

    private final static long RSP_HEX_5__U_INT_08 = 0x00000000000000aaL;
    private final static long RSP_HEX_5__U_INT_16 = 0x000000000000aaaaL;
    private final static long RSP_HEX_5__U_INT_32 = 0x00000000aaaaaaaaL;
    private final static long RSP_HEX_5__U_INT_64 = 0xaaaaaaaaaaaaaaaaL;

    private final static long REQ_HEX_A = 0xaaaaaaaaaaaaaaaaL;

    private final static long RSP_HEX_A__S_INT_08 = 0x0000000000000055L;
    private final static long RSP_HEX_A__S_INT_16 = 0x0000000000005555L;
    private final static long RSP_HEX_A__S_INT_32 = 0x0000000055555555L;
    private final static long RSP_HEX_A__S_INT_64 = 0x5555555555555555L;

    private final static long RSP_HEX_A__U_INT_08 = 0x0000000000000055L;
    private final static long RSP_HEX_A__U_INT_16 = 0x0000000000005555L;
    private final static long RSP_HEX_A__U_INT_32 = 0x0000000055555555L;
    private final static long RSP_HEX_A__U_INT_64 = 0x5555555555555555L;

    public TypeAliasTest() {
    }

    //Make sure all TypeAlias are tested ....
    private static Set<TypeAlias> ta;
    private static Runtime rt;
    private static TestLib testlib;

    private void assertAndRemoveDataType(TypeAlias ta, int sizeOf, long resp) {
        Type type = rt.findType(ta);
        assertEquals("sizeof mismatch", type.size(), sizeOf);
        if (resp == RSP_HEX_5__S_INT_08) {
            assertEquals(NativeType.SCHAR, type.getNativeType());
        } else if (resp == RSP_HEX_5__U_INT_08) {
            assertEquals(NativeType.UCHAR, type.getNativeType());
        } else if (resp == RSP_HEX_5__S_INT_16) {
            assertEquals(NativeType.SSHORT, type.getNativeType());
        } else if (resp == RSP_HEX_5__U_INT_16) {
            assertEquals(NativeType.USHORT, type.getNativeType());
        } else if (resp == RSP_HEX_5__S_INT_32) {
            switch (type.getNativeType()) {
                case SINT:
                    assertTrue(true);
                    break;
                case SLONG:
                    assertEquals(4, rt.longSize());
                    break;
                default:
                    fail("Expected DataType signed 4 byte");
            }
        } else if (resp == RSP_HEX_5__U_INT_32) {
            switch (type.getNativeType()) {
                case UINT:
                    assertTrue(true);
                    break;
                case ULONG:
                    assertEquals(4, rt.longSize());
                    break;
                default:
                    fail("Expected DataType unsigned 4 byte");
            }
        } else if (resp == RSP_HEX_5__S_INT_64) {
            switch (type.getNativeType()) {
                case SLONGLONG:
                    assertTrue(true);
                    break;
                case ULONGLONG:
                    assertTrue(true);
                    break;
                case SLONG:
                    assertEquals(8, rt.longSize());
                    break;
                case ULONG:
                    assertEquals(8, rt.longSize());
                    break;
                default:
                    fail("Expected DataType signed or unsigned 8 byte");
            }
        } else if (resp == RSP_HEX_5__U_INT_64) {
            assertEquals(NativeType.ULONGLONG, type.getNativeType());
        } else {
            fail("Should never happen");
        }
        this.ta.remove(ta);
    }

    public static interface TestLib {

        int sizeOf_int8_t();

        @int8_t
        long test__int8_t(@int8_t long value);

        int sizeOf_u_int8_t();

        @u_int8_t
        long test__u_int8_t(@u_int8_t long value);

        int sizeOf_int16_t();

        @int16_t
        long test__int16_t(@int16_t long value);

        int sizeOf_u_int16_t();

        @u_int16_t
        int test__u_int16_t(@u_int16_t long value);

        int sizeOf_int32_t();

        @int32_t
        long test__int32_t(@int32_t long value);

        int sizeOf_u_int32_t();

        @u_int32_t
        long test__u_int32_t(@u_int32_t long value);

        int sizeOf_int64_t();

        @int64_t
        long test__int64_t(@int64_t long value);

        int sizeOf_u_int64_t();

        @u_int64_t
        long test__u_int64_t(@u_int64_t long value);

    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        testlib = TstUtil.loadTestLib(TestLib.class);
        rt = NativeRuntime.getRuntime(testlib);
        ta = EnumSet.allOf(TypeAlias.class);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        testlib = null;
        assertArrayEquals(new TypeAliases[0], ta.toArray(new TypeAlias[0]));
    }

    @Test
    public void test_int8_t() {
        assertAndRemoveDataType(TypeAlias.int8_t, testlib.sizeOf_int8_t(), testlib.test__int8_t(REQ_HEX_5));
    }

    @Test
    public void test_u_int8_t() {
        assertAndRemoveDataType(TypeAlias.u_int8_t, testlib.sizeOf_u_int8_t(), testlib.test__u_int8_t(REQ_HEX_5));
    }

    @Test
    public void test_int16_t() {
        assertAndRemoveDataType(TypeAlias.int16_t, testlib.sizeOf_int16_t(), testlib.test__int16_t(REQ_HEX_5));
    }

    @Test
    public void test_u_int16_t() {
        assertAndRemoveDataType(TypeAlias.u_int16_t, testlib.sizeOf_u_int16_t(), testlib.test__u_int16_t(REQ_HEX_5));
    }

    @Test
    public void test_int32_t() {
        assertAndRemoveDataType(TypeAlias.int32_t, testlib.sizeOf_int32_t(), testlib.test__int32_t(REQ_HEX_5));
    }

    @Test
    public void test_u_int32_t() {
        assertAndRemoveDataType(TypeAlias.u_int32_t, testlib.sizeOf_u_int32_t(), testlib.test__u_int32_t(REQ_HEX_5));
    }

    @Test
    public void test_int64_t() {
        assertAndRemoveDataType(TypeAlias.int64_t, testlib.sizeOf_int64_t(), testlib.test__int64_t(REQ_HEX_5));
    }

    @Test
    public void test_u_int64_t() {
        assertAndRemoveDataType(TypeAlias.u_int64_t, testlib.sizeOf_u_int64_t(), testlib.test__u_int64_t(REQ_HEX_5));
    }

}
