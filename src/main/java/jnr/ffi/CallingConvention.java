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
 * Native function calling conventions.
 *
 * <p>This is only needed on windows platforms - all platforms assume
 * {@link #DEFAULT} as the calling convention.
 */
public enum CallingConvention {
    /**
     * The default C calling convention
     */
    DEFAULT,
    /**
     * Windows stdcall calling convention
     */
    STDCALL
}
