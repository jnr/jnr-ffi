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

import java.lang.reflect.Field;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import jnr.ffi.provider.jffi.NativeRuntime;
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

    private PosixTestLib getPosixTestlib() {
        return (PosixTestLib) testlib;
    }

    private UnixTestLib getUnixTestlib() {
        return (UnixTestLib) testlib;
    }

    private LinuxTestLib getLinuxTestlib() {
        return (LinuxTestLib) testlib;
    }

    private DarwinTestLib getDarwinTestlib() {
        return (DarwinTestLib) testlib;
    }

    private WindowsTestLib getWindowsTestlib() {
        return (WindowsTestLib) testlib;
    }

    private boolean isPosixTest() {
        return testlib instanceof PosixTestLib;
    }

    private boolean isUnixTest() {
        return testlib instanceof UnixTestLib;
    }

    private boolean isLinuxTest() {
        return testlib instanceof LinuxTestLib;
    }

    private boolean isDarwinTest() {
        return testlib instanceof DarwinTestLib;
    }

    private boolean isWindowsTest() {
        return testlib instanceof WindowsTestLib;
    }

    private static Platform platform;

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

        int sizeof_int8_t();

        @int8_t
        long test__int8_t(@int8_t long value);

        int sizeof_u_int8_t();

        @u_int8_t
        long test__u_int8_t(@u_int8_t long value);

        int sizeof_int16_t();

        @int16_t
        long test__int16_t(@int16_t long value);

        int sizeof_u_int16_t();

        @u_int16_t
        int test__u_int16_t(@u_int16_t long value);

        int sizeof_int32_t();

        @int32_t
        long test__int32_t(@int32_t long value);

        int sizeof_u_int32_t();

        @u_int32_t
        long test__u_int32_t(@u_int32_t long value);

        int sizeof_int64_t();

        @int64_t
        long test__int64_t(@int64_t long value);

        int sizeof_u_int64_t();

        @u_int64_t
        long test__u_int64_t(@u_int64_t long value);

        int sizeof_unsigned_long_int();

        @unsigned_long_int
        long test__unsigned_long_int(@unsigned_long_int long value);

        int sizeof_signed_long_int();

        @signed_long_int
        long test__signed_long_int(@signed_long_int long value);
    }

    public static interface PosixTestLib extends TestLib {

        int sizeof_blkcnt_t();

        @blkcnt_t
        long test__blkcnt_t(@blkcnt_t long value);

        int sizeof_blksize_t();

        @blksize_t
        long test__blksize_t(@blksize_t long value);

        int sizeof_caddr_t();
//TODO        @caddr_t long test__caddr_t(@caddr_t  value);

        int sizeof_cc_t();

        @cc_t
        long test__cc_t(@cc_t long value);

        int sizeof_clock_t();

        @clock_t
        long test__clock_t(@clock_t long value);

        int sizeof_dev_t();

        @dev_t
        long test__dev_t(@dev_t long value);

        int sizeof_fsblkcnt_t();

        @fsblkcnt_t
        long test__fsblkcnt_t(@fsblkcnt_t long value);

        int sizeof_fsfilcnt_t();

        @fsfilcnt_t
        long test__fsfilcnt_t(@fsfilcnt_t long value);

        int sizeof_gid_t();

        @gid_t
        long test__gid_t(@gid_t long value);

        int sizeof_id_t();

        @id_t
        long test__id_t(@id_t long value);

        int sizeof_in_addr_t();

        @in_addr_t
        long test__in_addr_t(@in_addr_t long value);

        int sizeof_in_port_t();

        @in_port_t
        long test__in_port_t(@in_port_t long value);

        int sizeof_ino64_t();

        @ino64_t
        long test__ino64_t(@ino64_t long value);

        int sizeof_ino_t();

        @ino_t
        long test__ino_t(@ino_t long value);

        int sizeof_intptr_t();

        @intptr_t
        long test__intptr_t(@intptr_t long value);

        int sizeof_key_t();

        @key_t
        long test__key_t(@key_t long value);

        int sizeof_mode_t();

        @mode_t
        long test__mode_t(@mode_t long value);

        int sizeof_nfds_t();

        @nfds_t
        long test__nfds_t(@nfds_t long value);

        int sizeof_nlink_t();

        @nlink_t
        long test__nlink_t(@nlink_t long value);

        int sizeof_off_t();

        @off_t
        long test__off_t(@off_t long value);

        int sizeof_pid_t();

        @pid_t
        long test__pid_t(@pid_t long value);

        int sizeof_ptrdiff_t();

        @ptrdiff_t
        long test__ptrdiff_t(@ptrdiff_t long value);

        int sizeof_rlim_t();

        @rlim_t
        long test__rlim_t(@rlim_t long value);

        int sizeof_sa_family_t();

        @sa_family_t
        long test__sa_family_t(@sa_family_t long value);

        int sizeof_size_t();

        @size_t
        long test__size_t(@size_t long value);

        int sizeof_socklen_t();

        @socklen_t
        long test__socklen_t(@socklen_t long value);

        int sizeof_speed_t();

        @speed_t
        long test__speed_t(@speed_t long value);

        int sizeof_ssize_t();

        @ssize_t
        long test__ssize_t(@ssize_t long value);

        int sizeof_suseconds_t();

        @suseconds_t
        long test__suseconds_t(@suseconds_t long value);

        int sizeof_tcflag_t();

        @tcflag_t
        long test__tcflag_t(@tcflag_t long value);

        int sizeof_time_t();

        @time_t
        long test__time_t(@time_t long value);

        int sizeof_uid_t();

        @uid_t
        long test__uid_t(@uid_t long value);

        int sizeof_uintptr_t();

        @uintptr_t
        long test__uintptr_t(@uintptr_t long value);

        int sizeof_useconds_t();

        @useconds_t
        long test__useconds_t(@useconds_t long value);

        int sizeof_wchar_t();

        @wchar_t
        long test__wchar_t(@wchar_t long value);

        int sizeof_wint_t();

        @wint_t
        long test__wint_t(@wint_t long value);

//Prototype
//        int sizeof_();
//        @ long test__(@ long value);
    }

    public static interface UnixTestLib extends PosixTestLib {
    }

    public static interface LinuxTestLib extends PosixTestLib {

        int sizeof_eventfd_t();

        @eventfd_t
        long test__eventfd_t(@eventfd_t long value);

    }

    public static interface DarwinTestLib extends PosixTestLib {

        int sizeof_swblk_t();

        @swblk_t
        long test__swblk_t(@swblk_t long value);

    }

    public static interface WindowsTestLib extends TestLib {

        int sizeof_HANDLE();

        @HANDLE
        long test__HANDLE(@HANDLE long value);

    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        platform = Platform.getNativePlatform();
        if (platform.isUnix()) {
            switch (platform.getOS()) {
                case LINUX:
                    testlib = TstUtil.loadTestLib(LinuxTestLib.class);
                    break;
                case DARWIN:
                    testlib = TstUtil.loadTestLib(DarwinTestLib.class);
                    break;
                default:
                    testlib = TstUtil.loadTestLib(UnixTestLib.class);
            }

        } else if (platform.isWindows()) {
            testlib = TstUtil.loadTestLib(WindowsTestLib.class);
        } else {
            throw new RuntimeException("Unknown Platform! Dont know what to do!");
        }
        rt = NativeRuntime.getRuntime(testlib);
        ta = EnumSet.allOf(TypeAlias.class);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        testlib = null;
        assertArrayEquals(new TypeAlias[0], ta.toArray(new TypeAlias[0]));
    }

    @Test
    public void test_int8_t() {
        assertAndRemoveDataType(TypeAlias.int8_t, testlib.sizeof_int8_t(), testlib.test__int8_t(REQ_HEX_5));
    }

    @Test
    public void test_u_int8_t() {
        assertAndRemoveDataType(TypeAlias.u_int8_t, testlib.sizeof_u_int8_t(), testlib.test__u_int8_t(REQ_HEX_5));
    }

    @Test
    public void test_int16_t() {
        assertAndRemoveDataType(TypeAlias.int16_t, testlib.sizeof_int16_t(), testlib.test__int16_t(REQ_HEX_5));
    }

    @Test
    public void test_u_int16_t() {
        assertAndRemoveDataType(TypeAlias.u_int16_t, testlib.sizeof_u_int16_t(), testlib.test__u_int16_t(REQ_HEX_5));
    }

    @Test
    public void test_int32_t() {
        assertAndRemoveDataType(TypeAlias.int32_t, testlib.sizeof_int32_t(), testlib.test__int32_t(REQ_HEX_5));
    }

    @Test
    public void test_u_int32_t() {
        assertAndRemoveDataType(TypeAlias.u_int32_t, testlib.sizeof_u_int32_t(), testlib.test__u_int32_t(REQ_HEX_5));
    }

    @Test
    public void test_int64_t() {
        assertAndRemoveDataType(TypeAlias.int64_t, testlib.sizeof_int64_t(), testlib.test__int64_t(REQ_HEX_5));
    }

    @Test
    public void test_u_int64_t() {
        assertAndRemoveDataType(TypeAlias.u_int64_t, testlib.sizeof_u_int64_t(), testlib.test__u_int64_t(REQ_HEX_5));
    }

    @Test
    public void test_blkcnt_t() {
        if (!isPosixTest()) {
            ta.remove(TypeAlias.blkcnt_t);
            return;
        }
        assertAndRemoveDataType(TypeAlias.blkcnt_t, getPosixTestlib().sizeof_blkcnt_t(), getPosixTestlib().test__blkcnt_t(REQ_HEX_5));
    }

    @Test
    public void test_blksize_t() {
        if (!isPosixTest()) {
            ta.remove(TypeAlias.blksize_t);
            return;
        }
        assertAndRemoveDataType(TypeAlias.blksize_t, getPosixTestlib().sizeof_blksize_t(), getPosixTestlib().test__blksize_t(REQ_HEX_5));
    }

    @Test
    public void test_caddr_t() {
        if (!isPosixTest()) {
            ta.remove(TypeAlias.caddr_t);
            return;
        }

        TypeAlias ta = TypeAlias.caddr_t;
        Type type = rt.findType(ta);

        assertEquals(type.size(), getPosixTestlib().sizeof_caddr_t());
        assertEquals(NativeType.ADDRESS, type.getNativeType());

        this.ta.remove(ta);

//        assertAndRemoveDataType(TypeAlias.caddr_t, testlib.sizeof_caddr_t(), testlib.test__caddr_t(REQ_HEX_5));
    }

    @Test
    public void test_cc_t() {
        if (!isPosixTest()) {
            ta.remove(TypeAlias.cc_t);
            return;
        }
        assertAndRemoveDataType(TypeAlias.cc_t, getPosixTestlib().sizeof_cc_t(), getPosixTestlib().test__cc_t(REQ_HEX_5));
    }

    @Test
    public void test_clock_t() {
        if (!isPosixTest()) {
            ta.remove(TypeAlias.clock_t);
            return;
        }
        assertAndRemoveDataType(TypeAlias.clock_t, getPosixTestlib().sizeof_clock_t(), getPosixTestlib().test__clock_t(REQ_HEX_5));
    }

    @Test
    public void test_dev_t() {
        if (!isPosixTest()) {
            ta.remove(TypeAlias.dev_t);
            return;
        }
        assertAndRemoveDataType(TypeAlias.dev_t, getPosixTestlib().sizeof_dev_t(), getPosixTestlib().test__dev_t(REQ_HEX_5));
    }

    @Test //LINUX
    public void test_eventfd_t() {
        if (!isLinuxTest()) {
            ta.remove(TypeAlias.eventfd_t);
            return;
        }
        assertAndRemoveDataType(TypeAlias.eventfd_t, getLinuxTestlib().sizeof_eventfd_t(), getLinuxTestlib().test__eventfd_t(REQ_HEX_5));
    }

    @Test
    public void test_fsblkcnt_t() {
        if (!isPosixTest()) {
            ta.remove(TypeAlias.fsblkcnt_t);
            return;
        }
        assertAndRemoveDataType(TypeAlias.fsblkcnt_t, getPosixTestlib().sizeof_fsblkcnt_t(), getPosixTestlib().test__fsblkcnt_t(REQ_HEX_5));
    }

    @Test
    public void test_fsfilcnt_t() {
        if (!isPosixTest()) {
            ta.remove(TypeAlias.fsfilcnt_t);
            return;
        }
        assertAndRemoveDataType(TypeAlias.fsfilcnt_t, getPosixTestlib().sizeof_fsfilcnt_t(), getPosixTestlib().test__fsfilcnt_t(REQ_HEX_5));
    }

    @Test
    public void test_gid_t() {
        if (!isPosixTest()) {
            ta.remove(TypeAlias.gid_t);
            return;
        }
        assertAndRemoveDataType(TypeAlias.gid_t, getPosixTestlib().sizeof_gid_t(), getPosixTestlib().test__gid_t(REQ_HEX_5));
    }

    @Test
    public void test_id_t() {
        if (!isPosixTest()) {
            ta.remove(TypeAlias.id_t);
            return;
        }
        assertAndRemoveDataType(TypeAlias.id_t, getPosixTestlib().sizeof_id_t(), getPosixTestlib().test__id_t(REQ_HEX_5));
    }

    @Test
    public void test_in_addr_t() {
        if (!isPosixTest()) {
            ta.remove(TypeAlias.in_addr_t);
            return;
        }
        assertAndRemoveDataType(TypeAlias.in_addr_t, getPosixTestlib().sizeof_in_addr_t(), getPosixTestlib().test__in_addr_t(REQ_HEX_5));
    }

    @Test
    public void test_in_port_t() {
        if (!isPosixTest()) {
            ta.remove(TypeAlias.in_port_t);
            return;
        }
        assertAndRemoveDataType(TypeAlias.in_port_t, getPosixTestlib().sizeof_in_port_t(), getPosixTestlib().test__in_port_t(REQ_HEX_5));
    }

    @Test
    public void test_ino64_t() {
        if (!isPosixTest()) {
            ta.remove(TypeAlias.ino64_t);
            return;
        }
        assertAndRemoveDataType(TypeAlias.ino64_t, getPosixTestlib().sizeof_ino64_t(), getPosixTestlib().test__ino64_t(REQ_HEX_5));
    }

    @Test
    public void test_ino_t() {
        if (!isPosixTest()) {
            ta.remove(TypeAlias.ino_t);
            return;
        }
        assertAndRemoveDataType(TypeAlias.ino_t, getPosixTestlib().sizeof_ino_t(), getPosixTestlib().test__ino_t(REQ_HEX_5));
    }

    @Test
    public void test_intptr_t() {
        if (!isPosixTest()) {
            ta.remove(TypeAlias.intptr_t);
            return;
        }
        assertAndRemoveDataType(TypeAlias.intptr_t, getPosixTestlib().sizeof_intptr_t(), getPosixTestlib().test__intptr_t(REQ_HEX_5));
    }

    @Test
    public void test_key_t() {
        if (!isPosixTest()) {
            ta.remove(TypeAlias.key_t);
            return;
        }
        assertAndRemoveDataType(TypeAlias.key_t, getPosixTestlib().sizeof_key_t(), getPosixTestlib().test__key_t(REQ_HEX_5));
    }

    @Test
    public void test_mode_t() {
        if (!isPosixTest()) {
            ta.remove(TypeAlias.mode_t);
            return;
        }
        assertAndRemoveDataType(TypeAlias.mode_t, getPosixTestlib().sizeof_mode_t(), getPosixTestlib().test__mode_t(REQ_HEX_5));
    }

    @Test
    public void test_nfds_t() {
        if (!isPosixTest()) {
            ta.remove(TypeAlias.nfds_t);
            return;
        }
        assertAndRemoveDataType(TypeAlias.nfds_t, getPosixTestlib().sizeof_nfds_t(), getPosixTestlib().test__nfds_t(REQ_HEX_5));
    }

    @Test
    public void test_nlink_t() {
        if (!isPosixTest()) {
            ta.remove(TypeAlias.nlink_t);
            return;
        }
        assertAndRemoveDataType(TypeAlias.nlink_t, getPosixTestlib().sizeof_nlink_t(), getPosixTestlib().test__nlink_t(REQ_HEX_5));
    }

    @Test
    public void test_off_t() {
        if (!isPosixTest()) {
            ta.remove(TypeAlias.off_t);
            return;
        }
        assertAndRemoveDataType(TypeAlias.off_t, getPosixTestlib().sizeof_off_t(), getPosixTestlib().test__off_t(REQ_HEX_5));
    }

    @Test
    public void test_pid_t() {
        if (!isPosixTest()) {
            ta.remove(TypeAlias.pid_t);
            return;
        }
        assertAndRemoveDataType(TypeAlias.pid_t, getPosixTestlib().sizeof_pid_t(), getPosixTestlib().test__pid_t(REQ_HEX_5));
    }

    @Test
    public void test_ptrdiff_t() {
        if (!isPosixTest()) {
            ta.remove(TypeAlias.ptrdiff_t);
            return;
        }
        assertAndRemoveDataType(TypeAlias.ptrdiff_t, getPosixTestlib().sizeof_ptrdiff_t(), getPosixTestlib().test__ptrdiff_t(REQ_HEX_5));
    }

    @Test
    public void test_rlim_t() {
        if (!isPosixTest()) {
            ta.remove(TypeAlias.rlim_t);
            return;
        }
        assertAndRemoveDataType(TypeAlias.rlim_t, getPosixTestlib().sizeof_rlim_t(), getPosixTestlib().test__rlim_t(REQ_HEX_5));
    }

    @Test
    public void test_sa_family_t() {
        if (!isPosixTest()) {
            ta.remove(TypeAlias.sa_family_t);
            return;
        }
        assertAndRemoveDataType(TypeAlias.sa_family_t, getPosixTestlib().sizeof_sa_family_t(), getPosixTestlib().test__sa_family_t(REQ_HEX_5));
    }

    @Test
    public void test_size_t() {
        if (!isPosixTest()) {
            ta.remove(TypeAlias.size_t);
            return;
        }
        assertAndRemoveDataType(TypeAlias.size_t, getPosixTestlib().sizeof_size_t(), getPosixTestlib().test__size_t(REQ_HEX_5));
    }

    @Test
    public void test_socklen_t() {
        if (!isPosixTest()) {
            ta.remove(TypeAlias.size_t);
            return;
        }
        assertAndRemoveDataType(TypeAlias.socklen_t, getPosixTestlib().sizeof_socklen_t(), getPosixTestlib().test__socklen_t(REQ_HEX_5));
    }

    @Test
    public void test_speed_t() {
        if (!isPosixTest()) {
            ta.remove(TypeAlias.speed_t);
            return;
        }
        assertAndRemoveDataType(TypeAlias.speed_t, getPosixTestlib().sizeof_speed_t(), getPosixTestlib().test__speed_t(REQ_HEX_5));
    }

    @Test
    public void test_ssize_t() {
        if (!isPosixTest()) {
            ta.remove(TypeAlias.ssize_t);
            return;
        }
        assertAndRemoveDataType(TypeAlias.ssize_t, getPosixTestlib().sizeof_ssize_t(), getPosixTestlib().test__ssize_t(REQ_HEX_5));
    }

    @Test
    public void test_suseconds_t() {
        if (!isPosixTest()) {
            ta.remove(TypeAlias.suseconds_t);
            return;
        }
        assertAndRemoveDataType(TypeAlias.suseconds_t, getPosixTestlib().sizeof_suseconds_t(), getPosixTestlib().test__suseconds_t(REQ_HEX_5));
    }

    @Test
    public void test_swblk_t() {
        if (!isDarwinTest()) {
            ta.remove(TypeAlias.swblk_t);
            return;
        }
        assertAndRemoveDataType(TypeAlias.swblk_t, getDarwinTestlib().sizeof_swblk_t(), getDarwinTestlib().test__swblk_t(REQ_HEX_5));
    }

    @Test
    public void test_tcflag_t() {
        if (!isPosixTest()) {
            ta.remove(TypeAlias.tcflag_t);
            return;
        }
        assertAndRemoveDataType(TypeAlias.tcflag_t, getPosixTestlib().sizeof_tcflag_t(), getPosixTestlib().test__tcflag_t(REQ_HEX_5));
    }

    @Test
    public void test_time_t() {
        if (!isPosixTest()) {
            ta.remove(TypeAlias.time_t);
            return;
        }
        assertAndRemoveDataType(TypeAlias.time_t, getPosixTestlib().sizeof_time_t(), getPosixTestlib().test__time_t(REQ_HEX_5));
    }

    @Test
    public void test_uid_t() {
        if (!isPosixTest()) {
            ta.remove(TypeAlias.uid_t);
            return;
        }
        assertAndRemoveDataType(TypeAlias.uid_t, getPosixTestlib().sizeof_uid_t(), getPosixTestlib().test__uid_t(REQ_HEX_5));
    }

    @Test
    public void test_uintptr_t() {
        if (!isPosixTest()) {
            ta.remove(TypeAlias.uintptr_t);
            return;
        }
        assertAndRemoveDataType(TypeAlias.uintptr_t, getPosixTestlib().sizeof_uintptr_t(), getPosixTestlib().test__uintptr_t(REQ_HEX_5));
    }

    @Test
    public void test_useconds_t() {
        if (!isPosixTest()) {
            ta.remove(TypeAlias.useconds_t);
            return;
        }//TODO How to test ...
        assertAndRemoveDataType(TypeAlias.useconds_t, getPosixTestlib().sizeof_useconds_t(), getPosixTestlib().test__useconds_t(REQ_HEX_5));
    }

    @Test
    public void test_wchar_t() {
        if (!isPosixTest()) {
            ta.remove(TypeAlias.wchar_t);
            return;
        }
        assertAndRemoveDataType(TypeAlias.wchar_t, getPosixTestlib().sizeof_wchar_t(), getPosixTestlib().test__wchar_t(REQ_HEX_5));
    }

    @Test
    public void test_wint_t() {
        if (!isPosixTest()) {
            ta.remove(TypeAlias.wint_t);
            return;
        }
        assertAndRemoveDataType(TypeAlias.wint_t, getPosixTestlib().sizeof_wint_t(), getPosixTestlib().test__wint_t(REQ_HEX_5));
    }

    @Test
    public void test_unsigned_long_int() {
        assertAndRemoveDataType(TypeAlias.unsigned_long_int, testlib.sizeof_unsigned_long_int(), testlib.test__unsigned_long_int(REQ_HEX_5));
    }

    @Test
    public void test_signed_long_int() {
        assertAndRemoveDataType(TypeAlias.signed_long_int, testlib.sizeof_signed_long_int(), testlib.test__signed_long_int(REQ_HEX_5));
    }

    @Test
    public void test_HANDLE() {
        if (!isWindowsTest()) {
            ta.remove(TypeAlias.HANDLE);
            return;
        }
        assertAndRemoveDataType(TypeAlias.HANDLE, getWindowsTestlib().sizeof_HANDLE(), getWindowsTestlib().test__HANDLE(REQ_HEX_5));
    }

    @Test
    public void jnr_ffi_types_XXX() {
        ClassLoader cl = getClass().getClassLoader();
        for (TypeAlias ta : TypeAlias.values()) {
            try {
                Class c = cl.loadClass("jnr.ffi.types." + ta.name());
                assertNotNull(c);
            } catch (ClassNotFoundException c) {
                fail("No definitions for " + ta.name());
            }
        }
    }

    @Test
    public void jnr_ffi_Struct_XXX() {
        ClassLoader cl = getClass().getClassLoader();
        for (TypeAlias ta : TypeAlias.values()) {
            try {
                Class c = cl.loadClass("jnr.ffi.Struct$" + ta.name());
                assertNotNull(c);
            } catch (ClassNotFoundException c) {
                fail("No definitions in jnr.ffi.Struct for " + ta.name());
            }
        }
    }

    @Test
    public void jnr_ffi_StructLayout_XXX() {
        ClassLoader cl = getClass().getClassLoader();
        for (TypeAlias ta : TypeAlias.values()) {
            try {
                Class c = cl.loadClass("jnr.ffi.Struct$" + ta.name());
                assertNotNull(c);
            } catch (ClassNotFoundException c) {
                fail("No definitions in jnr.ffi.Struct for " + ta.name());
            }
        }
    }

    /**
     * Ensure that all TypeAliases are up to 
     */
    @Test
    public void testArchTypeAliases() {
        for (Platform.CPU cpu : Platform.CPU.values()) {
            for (Platform.OS os : Platform.OS.values()) {
                try {
                    Class clazz = Class.forName("jnr.ffi.provider.jffi.platform." + cpu + "." + os + ".TypeAliases");
                    Field aliasesField = clazz.getField("ALIASES");
                    Map<TypeAlias, jnr.ffi.NativeType> map = new EnumMap<TypeAlias, NativeType>(TypeAlias.class);
                    map.putAll((Map<TypeAlias, NativeType>) aliasesField.get(clazz));
                    Logger.getLogger(TypeAliasTest.class.getName()).log(Level.SEVERE, "TypeAliases for cpu: {0} os: {1}", new Object[]{cpu, os});
                    for (TypeAlias typeAlias: TypeAlias.values()) {
                        assertNotNull("No definitions for: " + typeAlias.name() + " in " + clazz.getCanonicalName(), map.get(typeAlias));
                    }
                    //Never expect this to fail ....
                    assertTrue(map.isEmpty());
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(TypeAliasTest.class.getName()).log(Level.INFO, "No TypeAliases for cpu: " + cpu + " os: " + os, ex);
                } catch (NoSuchFieldException ex) {
                    Logger.getLogger(TypeAliasTest.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SecurityException ex) {
                    Logger.getLogger(TypeAliasTest.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(TypeAliasTest.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

//Prototype
//    @Test
//    public void test_() {
//        assertAndRemoveDataType(TypeAlias., testlib.sizeof_(), testlib.test__(REQ_HEX_5));
//    }
}
