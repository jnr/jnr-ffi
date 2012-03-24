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

import com.kenai.jffi.CallContext;
import com.kenai.jffi.CallContextCache;
import com.kenai.jffi.ClosurePool;
import jnr.ffi.CallingConvention;
import jnr.ffi.NativeLong;
import jnr.ffi.Pointer;
import jnr.ffi.annotations.Delegate;
import jnr.ffi.mapper.*;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import static jnr.ffi.provider.jffi.AsmUtil.boxValue;
import static jnr.ffi.provider.jffi.AsmUtil.unboxedReturnType;
import static jnr.ffi.provider.jffi.ClosureUtil.*;
import static jnr.ffi.provider.jffi.CodegenUtils.*;
import static jnr.ffi.provider.jffi.CodegenUtils.p;
import static jnr.ffi.provider.jffi.CodegenUtils.sig;
import static jnr.ffi.provider.jffi.InvokerUtil.getCallContext;
import static jnr.ffi.provider.jffi.InvokerUtil.getNativeCallingConvention;
import static jnr.ffi.provider.jffi.NumberUtil.*;
import static org.objectweb.asm.Opcodes.*;

/**
 *
 */
public final class NativeClosureFactory<T extends Object> implements ToNativeConverter<Object, Pointer> {
    private final NativeRuntime runtime;
    private final ConcurrentMap<Integer, NativeClosurePointer> closures = new ConcurrentHashMap<Integer, NativeClosurePointer>();
    private final ClosurePool closurePool;
    private final ClosureInvoker closureInvoker;
    private final ReferenceQueue<Object> referenceQueue = new ReferenceQueue<Object>();

    protected NativeClosureFactory(NativeRuntime runtime, CallContext callContext,
                                   ClosureInvoker closureInvoker) {
        this.runtime = runtime;
        this.closureInvoker = closureInvoker;
        this.closurePool = com.kenai.jffi.ClosureManager.getInstance().getClosurePool(callContext);
    }

    static <T extends Object> NativeClosureFactory newClosureFactory(NativeRuntime runtime, Class<T> closureClass, TypeMapper typeMapper) {

        Method callMethod = null;
        for (Method m : closureClass.getMethods()) {
            if (m.isAnnotationPresent(Delegate.class) && Modifier.isPublic(m.getModifiers())
                    && !Modifier.isStatic(m.getModifiers())) {
                callMethod = m;
                break;
            }
        }
        if (callMethod == null) {
            throw new NoSuchMethodError("no public non-static delegate method defined in " + closureClass.getName());
        }

        Class[] parameterTypes = callMethod.getParameterTypes();
        FromNativeType[] parameterSigTypes = new FromNativeType[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; ++i) {
            parameterSigTypes[i] = getParameterType(runtime, callMethod, i, typeMapper);
        }
        ToNativeType resultType = getResultType(runtime, callMethod, typeMapper);

        try {

            ClosureInvoker closureInvoker = ClosureInvoker.newClosureInvoker(runtime, callMethod, resultType, parameterSigTypes);
            return new NativeClosureFactory(runtime,
                    getCallContext(resultType, parameterSigTypes, getNativeCallingConvention(callMethod), false),
                    closureInvoker);
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }


    private void expunge(Reference<? extends Object> ref) {
        NativeClosure cl = NativeClosure.class.cast(ref);
        Integer key = cl.getKey();
        NativeClosurePointer ptr = closures.get(key);
        if (ptr == null) {
            return;
        }

        // Fast case - no chained elements; can just remove from the hash map
        if (ptr.next == null && closures.remove(key, ptr)) {
            return;
        }

        // Remove from chained list
        synchronized (closures) {
            remove: while ((ptr = closures.get(key)) != null) {
                for (NativeClosurePointer prev = ptr; ptr != null; prev = ptr, ptr = ptr.next) {
                    if (ptr.getNativeClosure() == cl) {
                        if (prev != ptr) {
                            // if not first element in list, just remove this one
                            prev.next = ptr.next;
                            break remove;

                            // else replace the map entry with this ptr
                        } else if (ptr.next != null && !closures.replace(key, ptr, ptr.next)) {
                            continue remove;
                        }
                    }
                }
            }
        }
    }

    private void expunge() {
        Reference<? extends Object> ref;
        while ((ref = referenceQueue.poll()) != null) {
            expunge(ref);
        }
    }

    public final Pointer toNative(Object callable, ToNativeContext context) {
        expunge();

        Integer key = System.identityHashCode(callable);
        NativeClosurePointer ptr = closures.get(key);

        if (ptr != null) {
            // Simple case - no identity hash code clash - just return the ptr
            if (ptr.getCallable() == callable) {
                return ptr;
            }

            // There has been a key clash, search the list
            synchronized (closures) {
                while ((ptr = ptr.next) != null) {
                    if (ptr.getCallable() == callable) {
                        return ptr;
                    }
                }
            }
        }

        return newClosure(callable, key);
    }

    public Class<Pointer> nativeType() {
        return Pointer.class;
    }

    NativeClosurePointer newClosure(Object callable, Integer key) {
        NativeClosure nativeClosure = new NativeClosure(closureInvoker, callable, referenceQueue, key);
        NativeClosurePointer ptr = new NativeClosurePointer(runtime, nativeClosure,
                closurePool.newClosureHandle(nativeClosure));

        expunge();
        if (closures.putIfAbsent(key, ptr) == null) {
            return ptr;
        }

        synchronized (closures) {
            do {
                // prepend and make new pointer the list head
                ptr.next = closures.get(key);

                // If old value already removed (e.g. by expunge), just put the new value in
                if (ptr.next == null && closures.putIfAbsent(key, ptr) == null) {
                    break;
                }
            } while (closures.replace(key, ptr.next, ptr));
        }

        return ptr;
    }
}
