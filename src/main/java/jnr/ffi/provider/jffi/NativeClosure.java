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

package jnr.ffi.provider.jffi;


import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

/**
 *
 */
public abstract class NativeClosure extends WeakReference<jnr.ffi.Closure> implements com.kenai.jffi.Closure {
    NativeClosure next, prev;
    protected final NativeRuntime runtime;

    public NativeClosure(NativeRuntime runtime, jnr.ffi.Closure closure) {
        super(closure);
        this.runtime = runtime;
    }

    public NativeClosure(NativeRuntime runtime, jnr.ffi.Closure closure, ReferenceQueue<jnr.ffi.Closure> queue) {
        super(closure, queue);
        this.runtime = runtime;
    }

    public final void invoke(com.kenai.jffi.Closure.Buffer buffer) {
        jnr.ffi.Closure closure = get();
        if (closure == null) {
            buffer.setLongReturn(0L);
            return;
        }
        invoke(buffer, closure);
    }

    abstract protected void invoke(com.kenai.jffi.Closure.Buffer buffer, jnr.ffi.Closure closure);
}
