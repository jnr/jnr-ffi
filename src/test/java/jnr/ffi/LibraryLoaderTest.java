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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;
import jnr.ffi.mapper.FunctionMapper;
import jnr.ffi.provider.FFIProvider;

import org.junit.Test;

public class LibraryLoaderTest {

    public static interface TestLib {
        int setLastError(int error);
    }

    @Test public void loadShouldNotThrowExceptions() {
        try {
            LibraryLoader.create(TestLib.class).load("non-existent-library");
        } catch (Throwable t) {
            fail("load raised exception " + t);
        }
    }

    @Test(expected = UnsatisfiedLinkError.class)
    public void failImmediatelyShouldThrowULE() {
        LibraryLoader.create(TestLib.class).failImmediately().load("non-existent-library");
    }

    @Test(expected = UnsatisfiedLinkError.class)
    public void invocationOnFailedLoadShouldThrowULE() {
        TestLib lib = LibraryLoader.create(TestLib.class).failImmediately().load("non-existent-library");
        lib.setLastError(0);
    }

    // Loadable library interface to test function mapping
    public static interface TestLibMapped {
        int set(int error);
    }

    @Test
    public void optionWithFunctionMapper() {
        LibraryLoader<TestLibMapped> loader = FFIProvider.getSystemProvider()
                .createLibraryLoader(TestLibMapped.class);
        loader.library("test");

        // Define function mappings by a FunctionMapper entry in options
        FunctionMapper.Builder shortNames = new FunctionMapper.Builder();
        shortNames.map("set", "setLastError");
        loader.option(LibraryOption.FunctionMapper, shortNames.build());

        // Load and call the mapped function
        TestLibMapped lib = loader.load();
        lib.set(1);
    }

    @Test
    public void mapperWithFuctionMapper() {
        LibraryLoader<TestLibMapped> loader = FFIProvider.getSystemProvider()
                .createLibraryLoader(TestLibMapped.class);
        loader.library("test");

        // Define function mappings via the type-safe API
        FunctionMapper.Builder shortNames = new FunctionMapper.Builder();
        shortNames.map("set", "setLastError");
        loader.mapper(shortNames.build());

        // Load and call the mapped function
        TestLibMapped lib = loader.load();
        lib.set(2);
    }

    @Test
    public void mapMethod() {
        LibraryLoader<TestLibMapped> loader = FFIProvider.getSystemProvider()
                .createLibraryLoader(TestLibMapped.class);
        loader.library("test");

        // Define function mappings via the String API
        loader.map("set", "setLastError");

        // Load and call the mapped function
        TestLibMapped lib = loader.load();
        lib.set(3);
    }

    // Loadable library interface to test complicated function mapping
    public static interface TestLibMath {
        public int add(int i1, int i2); // = add_int32_t
        public int sub(int i1, int i2); // = sub_int32_t
        public int mul(int i1, int i2); // = mul_int32_t
        public int div(int i1, int i2); // = div_int32_t
        public double addd(double f1, double f2); // = add_double
        public double subd(double f1, double f2); // = add_double
    }

    @Test
    public void functionMappersCombine() {
        LibraryLoader<TestLibMath> loader = FFIProvider.getSystemProvider()
                .createLibraryLoader(TestLibMath.class);
        loader.library("test");

        // Define function mappings by a FunctionMapper entry in options
        FunctionMapper.Builder shortNames = new FunctionMapper.Builder();
        shortNames.map("add", "add_int32_t");
        shortNames.map("sub", "sub_int32_t");
        loader.option(LibraryOption.FunctionMapper, shortNames.build());

        // Define function mappings via the type-safe API
        shortNames = new FunctionMapper.Builder();
        shortNames.map("mul", "mul_int32_t");
        shortNames.map("div", "div_int32_t");
        loader.mapper(shortNames.build());

        // Define function mappings via the String API
        loader.map("addd", "add_double");
        loader.map("subd", "sub_double");

        TestLibMath lib = loader.load();
        assertNotNull("Could not load libtest", lib);

        // Do some arithmetic with the library to prove methods exist
        int sum = lib.sub(lib.add(10, 5), 3); // 10+5-3 = 12
        int prod = lib.mul(sum, 7);
        int answer = lib.div(prod, 2);
        assertEquals(answer, 42);

        double a = lib.subd(lib.addd(30.0, 20.0), 8.0);
        assertEquals(a, 42.0, 1e-4);
    }
}
