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

import jnr.ffi.annotations.IgnoreError;
import jnr.ffi.annotations.SaveError;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class LastErrorTest {
    public interface ErrorSavingUnspecified {
        int setLastError(int error);
    }

    public interface MethodSaveError extends ErrorSavingUnspecified {
        @SaveError
        int setLastError(int error);
    }

    public interface MethodIgnoreError extends ErrorSavingUnspecified {
        @IgnoreError
        int setLastError(int error);
    }

    public interface MethodIgnoreAndSaveError extends ErrorSavingUnspecified {
        @IgnoreError
        @SaveError
        int setLastError(int error);
    }

    @Test
    public void testLastError() {
        assertSavesError(true, ErrorSavingUnspecified.class);
        assertSavesError(true, MethodSaveError.class);
        assertSavesError(false, MethodIgnoreError.class);
        assertSavesError(true, MethodIgnoreAndSaveError.class);

        assertSavesError(false, ErrorSavingUnspecified.class, LibraryOption.IgnoreError);
        assertSavesError(true, MethodSaveError.class, LibraryOption.IgnoreError);
        assertSavesError(false, MethodIgnoreError.class, LibraryOption.IgnoreError);
        assertSavesError(true, MethodIgnoreAndSaveError.class, LibraryOption.IgnoreError);

        assertSavesError(true, ErrorSavingUnspecified.class, LibraryOption.SaveError);
        assertSavesError(true, MethodSaveError.class, LibraryOption.SaveError);
        assertSavesError(false, MethodIgnoreError.class, LibraryOption.SaveError);
        assertSavesError(true, MethodIgnoreAndSaveError.class, LibraryOption.SaveError);

        assertSavesError(true, ErrorSavingUnspecified.class, LibraryOption.IgnoreError, LibraryOption.SaveError);
        assertSavesError(true, MethodSaveError.class, LibraryOption.IgnoreError, LibraryOption.SaveError);
        assertSavesError(false, MethodIgnoreError.class, LibraryOption.IgnoreError, LibraryOption.SaveError);
        assertSavesError(true, MethodIgnoreAndSaveError.class, LibraryOption.IgnoreError, LibraryOption.SaveError);
    }

    void assertSavesError(boolean expected, Class<? extends ErrorSavingUnspecified> cls) {
        assertSavesError(expected, cls, Collections.<LibraryOption, Object>emptyMap());
    }

    void assertSavesError(boolean expected, Class<? extends ErrorSavingUnspecified> cls, LibraryOption... options) {
        Map<LibraryOption, Object> optionsMap = new HashMap<LibraryOption, Object>();
        for (LibraryOption option : options) optionsMap.put(option, option);
        assertSavesError(expected, cls, optionsMap);
    }

    void assertSavesError(boolean expected, Class<? extends ErrorSavingUnspecified> cls, Map<LibraryOption, ?> options) {
        ErrorSavingUnspecified methodSaveError = TstUtil.loadTestLib(cls, options);
        Runtime runtime = Runtime.getRuntime(methodSaveError);

        // clear errno
        runtime.setLastError(0);

        final int MAGIC = 0xdeadbeef;
        methodSaveError.setLastError(MAGIC);

        if (expected) {
            assertEquals(MAGIC, runtime.getLastError(), "Errno value was not saved for " + cls);
        } else {
            assertNotEquals(MAGIC, runtime.getLastError(), "Errno value was saved for " + cls);
        }
    }
}