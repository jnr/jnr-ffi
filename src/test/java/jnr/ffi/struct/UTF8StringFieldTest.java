/* 
 * Copyright (C) 2008 Wayne Meissner
 * 
 * This file is part of jffi.
 *
 * This code is free software: you can redistribute it and/or modify it under 
 * the terms of the GNU Lesser General Public License version 3 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License 
 * version 3 for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * version 3 along with this work.  If not, see <http://www.gnu.org/licenses/>.
 */

package jnr.ffi.struct;

import jnr.ffi.TstUtil;
import jnr.ffi.Library;
import jnr.ffi.Runtime;
import jnr.ffi.annotations.In;
import jnr.ffi.annotations.Out;
import jnr.ffi.annotations.Pinned;
import jnr.ffi.annotations.Transient;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author wayne
 */
public class UTF8StringFieldTest {
    public UTF8StringFieldTest() {
    }
    
    public static class StringFieldStruct extends Struct {
        public final UTF8String string = new UTF8String(32);

        public StringFieldStruct() {
            super(runtime);
        }

    }
    public static interface TestLib {
        // This makes use of the string being the first field in the struct
        int string_equals(@Pinned @In @Transient StringFieldStruct s1, String s2);
        int copyByteBuffer(@Pinned @Out StringFieldStruct dst, @In byte[] src, int len);
        int copyByteBuffer(@Pinned @Out byte[] dst, @Pinned @In @Transient StringFieldStruct src, int len);
        int copyByteBuffer(@Pinned @Out StringBuilder dst, @Pinned @In @Transient StringFieldStruct src, int len);
    }

    static TestLib testlib;
    static Runtime runtime;

    @BeforeClass
    public static void setUpClass() throws Exception {
        testlib = TstUtil.loadTestLib(TestLib.class);
        runtime = Library.getRuntime(testlib);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }
    @Test public void stringFieldFirstInStruct() {
        StringFieldStruct s = new StringFieldStruct();
        final String MAGIC = "test";
        s.string.set(MAGIC);
        StringBuilder tmp = new StringBuilder(s.string.length());
        testlib.copyByteBuffer(tmp, s, s.string.length());
        assertEquals("String not put into struct correctly", MAGIC, tmp.toString());
    }

    public static final class SockaddrUnix extends Struct {
        private final Signed8 sun_len = new Signed8();
        private final Signed8 sun_family = new Signed8();
        private final UTF8String sun_path = new UTF8String(100);

        public SockaddrUnix() {
            super(runtime);
        }
    }

    @Test public void testSockaddrUnix() {
        final int SUN_LEN = 1;
        final int SUN_FAM = 2;
        final String SUN_PATH = "test";

        SockaddrUnix s = new SockaddrUnix();
        s.sun_len.set(SUN_LEN);
        s.sun_family.set(SUN_FAM);
        s.sun_path.set(SUN_PATH);
        assertEquals("Incorrect sun_len value", SUN_LEN, s.sun_len.intValue());
        assertEquals("Incorrect sun_fam value", SUN_FAM, s.sun_family.intValue());
        assertEquals("Incorrect sun_path value", SUN_PATH, s.sun_path.toString());
    }
}