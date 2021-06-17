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

package jnr.ffi.struct;

import jnr.ffi.Runtime;
import jnr.ffi.Struct;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class StructToStringTest {

    public StructToStringTest() {
    }

    public class IllegalAccessInToString extends Struct {
        private IllegalAccessInToString() {
            super(Runtime.getSystemRuntime());
        }
        private final Unsigned16 pad1 = new Unsigned16();
    }

    @Test
    public void illegalAccessInToString() throws Exception {
        String expectation = "pad1 = - IllegalAccessException -";
        String string = new IllegalAccessInToString().toString();
        assertTrue(string.contains(expectation),
                "Struct.toString doesn't contain expected '" + expectation + "':\n" + string
        );
    }

    public class NullPointerInToString extends Struct {
        private NullPointerInToString() {
            super(Runtime.getSystemRuntime());
        }
        public final Unsigned16 pad3 = null;
    }

    @Test
    public void nullPointerInToString() throws Exception {
        String expectation = "pad3 = - null -";
        String string = new NullPointerInToString().toString();
        assertTrue(string.contains(expectation),
                "Struct.toString doesn't contain expected '" + expectation + "':\n" + string
        );
    }

}
