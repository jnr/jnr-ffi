/*
 * Copyright (C) 2008-2010 Wayne Meissner
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

/**
 * Provides access to the unix errno and windows GetLastError() value.
 */
public final class LastError {
    private LastError() {}

    /**
     * Gets the value of errno from the last native call.
     *
     * @param runtime FFI runtime to get errno for.
     * @return An integer containing the errno value.
     */
    public static int getLastError(Runtime runtime) {
        return runtime.getLastError();
    }

    /**
     * Sets the native errno value.
     *
     * @param runtime FFI runtime to set errno for.
     * @param error The value to set errno to.
     */
    public static void setLastError(Runtime runtime, int error) {
        runtime.setLastError(error);
    }
}
