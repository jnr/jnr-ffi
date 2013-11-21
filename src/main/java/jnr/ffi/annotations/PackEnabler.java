/*
 * Copyright (C) 2011 Wayne Meissner
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

package jnr.ffi.annotations;

/**
 * Class to allow explicit enabling of {@link Pack} annotations based on static
 * properties.
 *
 * This can be used, for example, to do platform-specific packing, e.g.
 *
 *     Class OSXPackEnabler extends PackEnabler {
 *         static boolean enable() {
 *             return System.getProperty("os.name").contains("OS X");
 *         }
 *     }
 *
 *     @Pack(padding = 2, enabler = OSXPackEnabler.class)
 *     public final class NativeStruct extends Struct {
 *        // ...
 *
 * The default value always enables explicit packing annotations, if found.
 */
public abstract class PackEnabler {
    /* Default to enabling */
    static public boolean enable() { return true; }
}

