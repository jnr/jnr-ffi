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

package jnr.ffi.mapper;


import jnr.ffi.*;
import jnr.ffi.Runtime;
import jnr.ffi.annotations.In;
import jnr.ffi.annotations.LongLong;
import jnr.ffi.annotations.Out;
import jnr.ffi.provider.converters.CharSequenceParameterConverter;
import jnr.ffi.provider.converters.EnumSetConverter;
import jnr.ffi.provider.converters.StringResultConverter;
import jnr.ffi.types.size_t;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.ByteBuffer;

import static junit.framework.TestCase.*;

public class AnnotatedMappedTypeTest {
    public static final class CustomPointer {
        private final Pointer pointer;

        public CustomPointer(Pointer pointer) {
            this.pointer = pointer;
        }

        @ToNativeConverter.ToNative(nativeType = jnr.ffi.Pointer.class)
        public static Pointer toNative(CustomPointer value, ToNativeContext context) {
            return value != null ? value.pointer : null;
        }

        @FromNativeConverter.FromNative(nativeType = jnr.ffi.Pointer.class)
        public static CustomPointer fromNative(Pointer value, FromNativeContext context) {
            return value != null ? new CustomPointer(value) : null;
        }
    }

    public enum CustomEnum {
        FIRST("FIRST"),
        SECOND("SECOND");

        @ToNativeConverter.ToNative(nativeType = ByteBuffer.class)
        public static ByteBuffer toNative(CustomEnum value, ToNativeContext context) {
            if (value != null) {
                ByteBuffer byteBuffer = CharSequenceParameterConverter.getInstance(context)
                                                                      .toNative(value.text, context);
                return byteBuffer;
            } else {
                return null;
            }
        }

        @FromNativeConverter.FromNative(nativeType = Pointer.class)
        public static CustomEnum fromNative(Pointer value, FromNativeContext context) {
            if (value != null) {
                String fromNative = StringResultConverter.getInstance(context)
                                                         .fromNative(value, context);
                for (CustomEnum customEnum : CustomEnum.values()) {
                    if (customEnum.text.equals(fromNative)) {
                        return customEnum;
                    }
                }
            }
            return null;
        }

        private final String text;

        CustomEnum(String text) {this.text = text;}
    }

    public static interface TestLib {
        CustomPointer ptr_malloc(@size_t int size);
        void ptr_free(CustomPointer ptr);
        boolean string_equals(CustomEnum s1, String s2);
        CustomEnum string_duplicate(CustomEnum s1);
    }

    static TestLib testlib;
    static Runtime runtime;
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        testlib = TstUtil.loadTestLib(TestLib.class);
        runtime = Runtime.getRuntime(testlib);
    }
    
    @Test public void returnsInstanceOfCorrectClass() {
        assertSame(CustomPointer.class, testlib.ptr_malloc(1).getClass());
    }
    
    @Test public void toNative() {
        testlib.ptr_free(testlib.ptr_malloc(1));
    }


    @Test public void enumToNative() {
        assertTrue(testlib.string_equals(CustomEnum.FIRST, CustomEnum.FIRST.text));
    }
    @Test public void enumFromNative() {
        assertEquals(CustomEnum.SECOND,testlib.string_duplicate(CustomEnum.SECOND));
    }
}
