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

package com.kenai.jaffl.struct;

import com.kenai.jaffl.annotations.In;
import com.kenai.jaffl.annotations.Out;
import com.kenai.jaffl.annotations.Pinned;
import com.kenai.jaffl.annotations.Transient;
import com.kenai.jaffl.TstUtil;
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
public class AsciiStringFieldTest {
    public AsciiStringFieldTest() {
    }
    private class StringFieldStruct extends Struct {
        public final String string = new AsciiString(32);
    }
    private static interface TestLib {
        // This makes use of the string being the first field in the struct
        int string_equals(@Pinned @In @Transient StringFieldStruct s1, String s2);
        int copyByteBuffer(@Pinned @Out StringFieldStruct dst, @In byte[] src, int len);
        int copyByteBuffer(@Pinned @Out byte[] dst, @Pinned @In @Transient StringFieldStruct src, int len);
        int copyByteBuffer(@Pinned @Out StringBuilder dst, @Pinned @In @Transient StringFieldStruct src, int len);
    }
    static TestLib testlib;
    @BeforeClass
    public static void setUpClass() throws Exception {
        testlib = TstUtil.loadTestLib(TestLib.class);
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

    @Test
    public void stringFieldFirstInStruct() {
        StringFieldStruct s = new StringFieldStruct();
        final String MAGIC = "test";
        s.string.set(MAGIC);
        StringBuilder tmp = new StringBuilder(s.string.length());
        testlib.copyByteBuffer(tmp, s, s.string.length());
        assertEquals("String not put into struct correctly", MAGIC, tmp.toString());
    }
}