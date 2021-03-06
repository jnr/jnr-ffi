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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Set;

import jnr.ffi.util.EnumMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

    public static enum BitField {
        A(0x1),
        B(0x2),
        C(0x4),
        D(0x8);

        BitField(int value) {
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
        public int ret_int32_t(Set<TestEnum> enumSet);
    }

    public static interface ReturnEnumLib {
        public TestEnum ret_int32_t(int e);
        public Set<BitField> ret_int32_t(Set<BitField> bitfield);
        public TestEnum ret_int32_t(TestEnum e);
        public TestEnum add_int32_t(int i1, int i2);
        public TestEnum add_int32_t(TestEnum i1, TestEnum i2);
    }
    static TestLib testlib;
    static ReturnEnumLib retenum;
    @BeforeAll
    public static void setUpClass() throws Exception {
        testlib = TstUtil.loadTestLib(TestLib.class);
        retenum = TstUtil.loadTestLib(ReturnEnumLib.class);
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

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    @Test
    public void enumArgument() {
        assertEquals(TestEnum.Z.intValue(), testlib.ret_int32_t(TestEnum.Z), "Wrong value returned for enum");
        assertEquals(TestEnum.C.intValue(), testlib.add_int32_t(TestEnum.A, TestEnum.B), "Wrong value returned for enum");
    }
    @Test
    public void returnEnum() {
        assertEquals(TestEnum.Z, retenum.ret_int32_t(TestEnum.Z.intValue()), "Wrong value returned for enum");
        assertEquals(TestEnum.C, retenum.add_int32_t(1, 2), "Wrong value returned for enum");
    }

    @Test
    public void enumSetParameter() {
        assertEquals(TestEnum.A.intValue() | TestEnum.B.intValue(), testlib.ret_int32_t(EnumSet.of(TestEnum.A, TestEnum.B)));
    }

    @Test
    public void enumSetResult() {
        EnumSet<BitField> MAGIC = EnumSet.of(BitField.A, BitField.B);
        assertEquals(MAGIC, retenum.ret_int32_t(MAGIC));
    }
}