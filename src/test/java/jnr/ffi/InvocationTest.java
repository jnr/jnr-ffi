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

import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.invoke.MethodHandles;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class InvocationTest {
    public static interface TestLib {
        int ret_int32_t(int i);
    }

    public static interface TestLibWithDefault {
        static final MethodHandles.Lookup lookup = MethodHandles.lookup();
        int ret_int32_t(int i);
        default int doesNotExist(int i) {
            return ret_int32_t(i);
        }
    }

    static TestLib testlib;

    @BeforeClass
    public static void setUpClass() throws Exception {
        testlib = TstUtil.loadTestLib(TestLib.class);
    }

    @Test
    public void hammer() throws Throwable {
        for (int i = 0; i < 1000000; i++) {
            assertEquals(i, testlib.ret_int32_t(i));
        }
    }

    @Test
    public void testDefault() throws Throwable {
        TestLibWithDefault withDefault = TstUtil.loadTestLib(TestLibWithDefault.class, TestLibWithDefault.lookup);

        assertEquals(testlib.ret_int32_t(1234), withDefault.doesNotExist(1234));
    }
}
