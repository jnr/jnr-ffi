/* 
 * Copyright (C) 2007, 2008 Wayne Meissner
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

package jnr.ffi;

import jnr.ffi.util.EnumMapper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.*;

/**
 *
 * @author wayne
 */
public class EnumTest {

    public EnumTest() {
    }
    public static enum TestEnum implements EnumMapper.IntegerEnum {
        A(1),
        B(2),
        C(3),
        Z(100);
        TestEnum(int value) {
            this.value = value;
        }
        
        public int intValue() {
            return value;
        }
        private final int value;
    }
    
    public static interface TestLib {
        public int ret_int32_t(TestEnum e);
        public int add_int32_t(TestEnum i1, TestEnum i2);
        public int ret_int32_t(EnumSet<TestEnum> enumSet);
    }

    public static interface ReturnEnumLib {
        public TestEnum ret_int32_t(int e);
        public TestEnum ret_int32_t(TestEnum e);
        public TestEnum add_int32_t(int i1, int i2);
        public TestEnum add_int32_t(TestEnum i1, TestEnum i2);
    }
    static TestLib testlib;
    static ReturnEnumLib retenum;
    @BeforeClass
    public static void setUpClass() throws Exception {
        testlib = TstUtil.loadTestLib(TestLib.class);
        retenum = TstUtil.loadTestLib(ReturnEnumLib.class);
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

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    @Test
    public void enumArgument() {
        assertEquals("Wrong value returned for enum", TestEnum.Z.intValue(), testlib.ret_int32_t(TestEnum.Z));
        assertEquals("Wrong value returned for enum", TestEnum.C.intValue(), testlib.add_int32_t(TestEnum.A, TestEnum.B));
    }
    @Test
    public void returnEnum() {
        assertEquals("Wrong value returned for enum", TestEnum.Z, retenum.ret_int32_t(TestEnum.Z.intValue()));
        assertEquals("Wrong value returned for enum", TestEnum.C, retenum.add_int32_t(1, 2));
    }

    @Test
    public void enumSet() {
        assertEquals(TestEnum.A.intValue() | TestEnum.B.intValue(), testlib.ret_int32_t(EnumSet.of(TestEnum.A, TestEnum.B)));
    }
}