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

package jnr.ffi.provider.jffi;

/**
 * Indexes of JavaVM methods
 */
public class JNIInvokeInterface {
    public static final int DestroyJavaVM = 3;
    public static final int AttachCurrentThread = 4;
    public static final int DetachCurrentThread = 5;
    public static final int GetEnv = 6;
    public static final int AttachCurrentThreadAsDaemon = 7;
}
