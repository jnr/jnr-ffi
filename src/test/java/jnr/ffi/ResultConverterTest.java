/* 
 * Copyright (C) 2011 Wayne Meissner
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

import jnr.ffi.mapper.*;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.Map;
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
public class ResultConverterTest {
    public static final class TestType {
        public final String str;

        public TestType(String str) {
            this.str = str;
        }

    }

    public static interface TestLib {
        TestType strdup(CharSequence cs);
        void cfree(Pointer ptr);
    }

    public static interface Libc {
        Pointer calloc(int nmemb, int size);
        Pointer malloc(int size);
        void free(Pointer ptr);
        void cfree(Pointer ptr);
    }

    public static final class TestTypeResultConverter implements FromNativeConverter<TestType, Pointer> {

        public Class<Pointer> nativeType() {
            return Pointer.class;
        }

        public TestType fromNative(Pointer nativeValue, FromNativeContext context) {
            return new TestType(nativeValue.getString(0));
        }
    }

    static final TypeMapper mapper = new TypeMapper() {

        public FromNativeConverter getFromNativeConverter(Class type) {
            if (TestType.class == type) {
                return new TestTypeResultConverter();
            }
            return null;
        }

        public ToNativeConverter getToNativeConverter(Class type) {
            return null;
        }
    };
    static TestLib testlib;
    static Runtime runtime;

    public ResultConverterTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        Map<LibraryOption, Object> options = new HashMap<LibraryOption, Object>();
        options.put(LibraryOption.TypeMapper, mapper);
        System.setProperty("jaffl.compiler.dump", "true");
        testlib = TstUtil.loadTestLib(TestLib.class, options);
        runtime = Runtime.getRuntime(testlib);
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
    @Test public void testCustomResult() {
        final String MAGIC = "test";
        TestType t = testlib.strdup(MAGIC);
        assertNotNull(t);
        assertEquals("contents not set", MAGIC, t.str);
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface PosixError {

    }

    @FromNativeConverter.NoContext
    public static final class PosixErrorConverter implements FromNativeConverter<Integer, Integer> {
        public final Integer fromNative(Integer nativeValue, FromNativeContext context) {
            if (nativeValue < 0) {
                throw new RuntimeException("posix error!");
            }

            return nativeValue;
        }

        public Class<Integer> nativeType() {
            return Integer.class;
        }
    }

    static final SignatureTypeMapper posixTypeMapper = new AbstractSignatureTypeMapper() {

        public FromNativeConverter getFromNativeConverter(SignatureType type, FromNativeContext context) {
            if (int.class == type.getDeclaredType()) {
                for (Annotation a : context.getAnnotations()) {
                    if (a instanceof PosixError) {
                        return new PosixErrorConverter();
                    }
                }
            }

            return null;
        }

        public ToNativeConverter getToNativeConverter(SignatureType type, ToNativeContext context) {
            return null;
        }
    };

    public static interface Posix {
        @PosixError int ret_int32_t(int v);
    }

    @Test public void testPosixError() {
        System.setProperty("jnr.ffi.compile.dump", "true");
        Map<LibraryOption, Object> options = new HashMap<LibraryOption, Object>();
        options.put(LibraryOption.TypeMapper, posixTypeMapper);
        Posix posix = TstUtil.loadTestLib(Posix.class, options);
        assertEquals(1, posix.ret_int32_t(1));
        try {
            posix.ret_int32_t(-1);
        } catch (RuntimeException re) {
            System.out.println(re);
        }
    }
}
