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
        boolean signed = sizeOf < 0;
        if (signed) {
            sizeOf = -sizeOf;
        }
        assertEquals("sizeof mismatch", type.size(), sizeOf);
        if (resp == RSP_HEX_5__S_INT_08) {
            assertTrue("Must be signed", signed);
            assertEquals(NativeType.SCHAR, type.getNativeType());
        } else if (resp == RSP_HEX_5__U_INT_08) {
            assertFalse("Must be unsigned", signed);
            assertEquals(NativeType.UCHAR, type.getNativeType());
        } else if (resp == RSP_HEX_5__S_INT_16) {
            assertTrue("Must be signed", signed);
            assertEquals(NativeType.SSHORT, type.getNativeType());
        } else if (resp == RSP_HEX_5__U_INT_16) {
            assertFalse("Must be unsigned", signed);
            assertEquals(NativeType.USHORT, type.getNativeType());
        } else if (resp == RSP_HEX_5__S_INT_32) {
            assertTrue("Must be signed", signed);
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
            assertFalse("Must be unsigned", signed);
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
        } else if ((resp == RSP_HEX_5__S_INT_64) || (resp == RSP_HEX_5__U_INT_64)) {
            switch (type.getNativeType()) {
                case SLONGLONG:
                    assertTrue("Must be signed", signed);
                    break;
                case ULONGLONG:
            assertFalse("Must be unsigned", signed);
                    break;
                case SLONG:
                    assertTrue("Must be signed", signed);
                    assertEquals(8, rt.longSize());
                    break;
                case ULONG:
            assertFalse("Must be unsigned", signed);
                    assertEquals(8, rt.longSize());
                    break;
                default:
                    fail("Expected DataType signed or unsigned 8 byte");
            }
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

        int sizeOf_blkcnt_t();

        @blkcnt_t
        long test__blkcnt_t(@blkcnt_t long value);

        int sizeOf_blksize_t();

        @blksize_t
        long test__blksize_t(@blksize_t long value);

        int sizeOf_caddr_t();
//TODO        @caddr_t long test__caddr_t(@caddr_t  value);

        int sizeOf_cc_t();

        @cc_t
        long test__cc_t(@cc_t long value);

        int sizeOf_clock_t();

        @clock_t
        long test__clock_t(@clock_t long value);

        int sizeOf_dev_t();

        @dev_t
        long test__dev_t(@dev_t long value);

        int sizeOf_eventfd_t();

        @eventfd_t
        long test__eventfd_t(@eventfd_t long value);

        int sizeOf_fsblkcnt_t();

        @fsblkcnt_t
        long test__fsblkcnt_t(@fsblkcnt_t long value);

        int sizeOf_fsfilcnt_t();

        @fsfilcnt_t
        long test__fsfilcnt_t(@fsfilcnt_t long value);

        int sizeOf_gid_t();

        @gid_t
        long test__gid_t(@gid_t long value);

        int sizeOf_id_t();

        @id_t
        long test__id_t(@id_t long value);

        int sizeOf_in_addr_t();

        @in_addr_t
        long test__in_addr_t(@in_addr_t long value);

        int sizeOf_in_port_t();

        @in_port_t
        long test__in_port_t(@in_port_t long value);

        int sizeOf_ino64_t();

        @ino64_t
        long test__ino64_t(@ino64_t long value);

        int sizeOf_ino_t();

        @ino_t
        long test__ino_t(@ino_t long value);

        int sizeOf_intptr_t();

        @intptr_t
        long test__intptr_t(@intptr_t long value);

        int sizeOf_key_t();

        @key_t
        long test__key_t(@key_t long value);

        int sizeOf_mode_t();

        @mode_t
        long test__mode_t(@mode_t long value);

        int sizeOf_nfds_t();

        @nfds_t
        long test__nfds_t(@nfds_t long value);

        int sizeOf_nlink_t();

        @nlink_t
        long test__nlink_t(@nlink_t long value);

        int sizeOf_off_t();

        @off_t
        long test__off_t(@off_t long value);

        int sizeOf_pid_t();

        @pid_t
        long test__pid_t(@pid_t long value);

        int sizeOf_ptrdiff_t();

        @ptrdiff_t
        long test__ptrdiff_t(@ptrdiff_t long value);

        int sizeOf_rlim_t();

        @rlim_t
        long test__rlim_t(@rlim_t long value);

        int sizeOf_sa_family_t();

        @sa_family_t
        long test__sa_family_t(@sa_family_t long value);

        int sizeOf_size_t();

        @size_t
        long test__size_t(@size_t long value);

        int sizeOf_socklen_t();

        @socklen_t
        long test__socklen_t(@socklen_t long value);

        int sizeOf_speed_t();

        @speed_t
        long test__speed_t(@speed_t long value);

        int sizeOf_ssize_t();

        @ssize_t
        long test__ssize_t(@ssize_t long value);

        int sizeOf_suseconds_t();

        @suseconds_t
        long test__suseconds_t(@suseconds_t long value);

        int sizeOf_swblk_t();

        @swblk_t
        long test__swblk_t(@swblk_t long value);

        int sizeOf_tcflag_t();

        @tcflag_t
        long test__tcflag_t(@tcflag_t long value);

        int sizeOf_time_t();

        @time_t
        long test__time_t(@time_t long value);

        int sizeOf_uid_t();

        @uid_t
        long test__uid_t(@uid_t long value);

        int sizeOf_uintptr_t();

        @uintptr_t
        long test__uintptr_t(@uintptr_t long value);

        int sizeOf_useconds_t();

        @useconds_t
        long test__useconds_t(@useconds_t long value);

        int sizeOf_wchar_t();

        @wchar_t
        long test__wchar_t(@wchar_t long value);

        int sizeOf_wint_t();

        @wint_t
        long test__wint_t(@wint_t long value);

//Prototype
//        int sizeOf_();
//        @ long test__(@ long value);
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        testlib = TstUtil.loadTestLib(TestLib.class);
        testlib.sizeOf_int8_t();
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

    @Test
    public void test_blkcnt_t() {
        assertAndRemoveDataType(TypeAlias.blkcnt_t, testlib.sizeOf_blkcnt_t(), testlib.test__blkcnt_t(REQ_HEX_5));
    }

    @Test
    public void test_blksize_t() {
        assertAndRemoveDataType(TypeAlias.blksize_t, testlib.sizeOf_blksize_t(), testlib.test__blksize_t(REQ_HEX_5));
    }

    @Test
    public void test_caddr_t() {

        TypeAlias ta = TypeAlias.caddr_t;
        Type type = rt.findType(ta);

        assertEquals(type.size(), testlib.sizeOf_caddr_t());
        //TODO How to test properly
        this.ta.remove(ta);

//        assertAndRemoveDataType(TypeAlias.caddr_t, testlib.sizeOf_caddr_t(), testlib.test__caddr_t(REQ_HEX_5));
    }

    @Test
    public void test_cc_t() {
        assertAndRemoveDataType(TypeAlias.cc_t, testlib.sizeOf_cc_t(), testlib.test__cc_t(REQ_HEX_5));
    }

    @Test
    public void test_clock_t() {
        assertAndRemoveDataType(TypeAlias.clock_t, testlib.sizeOf_clock_t(), testlib.test__clock_t(REQ_HEX_5));
    }

    @Test
    public void test_dev_t() {
        assertAndRemoveDataType(TypeAlias.dev_t, testlib.sizeOf_dev_t(), testlib.test__dev_t(REQ_HEX_5));
    }

    @Test //LINUX
    public void test_eventfd_t() {
        assertAndRemoveDataType(TypeAlias.eventfd_t, testlib.sizeOf_eventfd_t(), testlib.test__eventfd_t(REQ_HEX_5));
    }

    @Test
    public void test_fsblkcnt_t() {
        assertAndRemoveDataType(TypeAlias.fsblkcnt_t, testlib.sizeOf_fsblkcnt_t(), testlib.test__fsblkcnt_t(REQ_HEX_5));
    }

    @Test
    public void test_fsfilcnt_t() {
        assertAndRemoveDataType(TypeAlias.fsfilcnt_t, testlib.sizeOf_fsfilcnt_t(), testlib.test__fsfilcnt_t(REQ_HEX_5));
    }

    @Test
    public void test_gid_t() {
        assertAndRemoveDataType(TypeAlias.gid_t, testlib.sizeOf_gid_t(), testlib.test__gid_t(REQ_HEX_5));
    }

    @Test
    public void test_id_t() {
        assertAndRemoveDataType(TypeAlias.id_t, testlib.sizeOf_id_t(), testlib.test__id_t(REQ_HEX_5));
    }

    @Test
    public void test_in_addr_t() {
        assertAndRemoveDataType(TypeAlias.in_addr_t, testlib.sizeOf_in_addr_t(), testlib.test__in_addr_t(REQ_HEX_5));
    }

    @Test
    public void test_in_port_t() {
        assertAndRemoveDataType(TypeAlias.in_port_t, testlib.sizeOf_in_port_t(), testlib.test__in_port_t(REQ_HEX_5));
    }

    @Test
    public void test_ino64_t() {
        assertAndRemoveDataType(TypeAlias.ino64_t, testlib.sizeOf_ino64_t(), testlib.test__ino64_t(REQ_HEX_5));
    }

    @Test
    public void test_ino_t() {
        assertAndRemoveDataType(TypeAlias.ino_t, testlib.sizeOf_ino_t(), testlib.test__ino_t(REQ_HEX_5));
    }

    @Test
    public void test_intptr_t() {
        assertAndRemoveDataType(TypeAlias.intptr_t, testlib.sizeOf_intptr_t(), testlib.test__intptr_t(REQ_HEX_5));
    }

    @Test
    public void test_key_t() {
        assertAndRemoveDataType(TypeAlias.key_t, testlib.sizeOf_key_t(), testlib.test__key_t(REQ_HEX_5));
    }

    @Test
    public void test_mode_t() {
        assertAndRemoveDataType(TypeAlias.mode_t, testlib.sizeOf_mode_t(), testlib.test__mode_t(REQ_HEX_5));
    }

    @Test
    public void test_nfds_t() {
        assertAndRemoveDataType(TypeAlias.nfds_t, testlib.sizeOf_nfds_t(), testlib.test__nfds_t(REQ_HEX_5));
    }

    @Test
    public void test_nlink_t() {
        assertAndRemoveDataType(TypeAlias.nlink_t, testlib.sizeOf_nlink_t(), testlib.test__nlink_t(REQ_HEX_5));
    }

    @Test
    public void test_off_t() {
        assertAndRemoveDataType(TypeAlias.off_t, testlib.sizeOf_off_t(), testlib.test__off_t(REQ_HEX_5));
    }

    @Test
    public void test_pid_t() {
        assertAndRemoveDataType(TypeAlias.pid_t, testlib.sizeOf_pid_t(), testlib.test__pid_t(REQ_HEX_5));
    }

    @Test
    public void test_ptrdiff_t() {
        assertAndRemoveDataType(TypeAlias.ptrdiff_t, testlib.sizeOf_ptrdiff_t(), testlib.test__ptrdiff_t(REQ_HEX_5));
    }

    @Test
    public void test_rlim_t() {
        assertAndRemoveDataType(TypeAlias.rlim_t, testlib.sizeOf_rlim_t(), testlib.test__rlim_t(REQ_HEX_5));
    }

    @Test
    public void test_sa_family_t() {
        assertAndRemoveDataType(TypeAlias.sa_family_t, testlib.sizeOf_sa_family_t(), testlib.test__sa_family_t(REQ_HEX_5));
    }

    @Test
    public void test_size_t() {
        assertAndRemoveDataType(TypeAlias.size_t, testlib.sizeOf_size_t(), testlib.test__size_t(REQ_HEX_5));
    }

    @Test
    public void test_socklen_t() {
        assertAndRemoveDataType(TypeAlias.socklen_t, testlib.sizeOf_socklen_t(), testlib.test__socklen_t(REQ_HEX_5));
    }

    @Test
    public void test_speed_t() {
        assertAndRemoveDataType(TypeAlias.speed_t, testlib.sizeOf_speed_t(), testlib.test__speed_t(REQ_HEX_5));
    }

    @Test
    public void test_ssize_t() {
        assertAndRemoveDataType(TypeAlias.ssize_t, testlib.sizeOf_ssize_t(), testlib.test__ssize_t(REQ_HEX_5));
    }

    @Test
    public void test_suseconds_t() {
        assertAndRemoveDataType(TypeAlias.suseconds_t, testlib.sizeOf_suseconds_t(), testlib.test__suseconds_t(REQ_HEX_5));
    }

    @Test
    public void test_swblk_t() {
        //TODO Platform APPLE
        //    assertAndRemoveDataType(TypeAlias.swblk_t, testlib.sizeOf_swblk_t(), testlib.test__swblk_t(REQ_HEX_5));
        ta.remove(TypeAlias.swblk_t);
    }

    @Test
    public void test_tcflag_t() {
        assertAndRemoveDataType(TypeAlias.tcflag_t, testlib.sizeOf_tcflag_t(), testlib.test__tcflag_t(REQ_HEX_5));
    }

    @Test
    public void test_time_t() {
        assertAndRemoveDataType(TypeAlias.time_t, testlib.sizeOf_time_t(), testlib.test__time_t(REQ_HEX_5));
    }

    @Test
    public void test_uid_t() {
        assertAndRemoveDataType(TypeAlias.uid_t, testlib.sizeOf_uid_t(), testlib.test__uid_t(REQ_HEX_5));
    }

    @Test
    public void test_uintptr_t() {
        assertAndRemoveDataType(TypeAlias.uintptr_t, testlib.sizeOf_uintptr_t(), testlib.test__uintptr_t(REQ_HEX_5));
    }

    @Test
    public void test_useconds_t() {
//TODO how to test
        ta.remove(TypeAlias.useconds_t);
//        assertAndRemoveDataType(TypeAlias.useconds_t, testlib.sizeOf_useconds_t(), testlib.test__useconds_t(REQ_HEX_5));
    }

    @Test
    public void test_wchar_t() {
        assertAndRemoveDataType(TypeAlias.wchar_t, testlib.sizeOf_wchar_t(), testlib.test__wchar_t(REQ_HEX_5));
    }

    @Test
    public void test_wint_t() {
        assertAndRemoveDataType(TypeAlias.wint_t, testlib.sizeOf_wint_t(), testlib.test__wint_t(REQ_HEX_5));
    }

//Prototype
//    @Test
//    public void test_() {
//        assertAndRemoveDataType(TypeAlias., testlib.sizeOf_(), testlib.test__(REQ_HEX_5));
//    }
}
