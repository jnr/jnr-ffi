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
import com.kenai.jffi.ClosureManager;
import com.kenai.jffi.ClosurePool;
import jnr.ffi.NativeLong;
import jnr.ffi.Pointer;
import jnr.ffi.annotations.Delegate;
import jnr.ffi.mapper.ToNativeContext;
import jnr.ffi.mapper.ToNativeConverter;
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

import static jnr.ffi.provider.jffi.CodegenUtils.*;
import static jnr.ffi.provider.jffi.CodegenUtils.sig;
import static jnr.ffi.provider.jffi.NumberUtil.*;
import static org.objectweb.asm.Opcodes.*;

/**
 *
 */
public final class NativeClosureFactory<T extends Object> implements ToNativeConverter<Object, Pointer> {
    public final static boolean DEBUG = Boolean.getBoolean("jnr.ffi.compile.dump");
    private static final AtomicLong nextClassID = new AtomicLong(0);

    private final NativeRuntime runtime;
    private final CallContext callContext;
    private final Constructor<? extends NativeClosure> nativeClosureConstructor;
    private final ConcurrentMap<Integer, NativeClosurePointer> closures = new ConcurrentHashMap<Integer, NativeClosurePointer>();
    private final ClosurePool closurePool;
    private final ReferenceQueue<Object> referenceQueue = new ReferenceQueue<Object>();

    protected NativeClosureFactory(NativeRuntime runtime, CallContext callContext,
                                   Constructor<? extends NativeClosure> nativeClosureConstructor) {
        this.runtime = runtime;
        this.callContext = callContext;
        this.nativeClosureConstructor = nativeClosureConstructor;
        this.closurePool = com.kenai.jffi.ClosureManager.getInstance().getClosurePool(callContext);
    }

    static <T extends Object> NativeClosureFactory newClosureFactory(NativeRuntime runtime, Class<T> closureClass) {
        final long classIdx = nextClassID.getAndIncrement();

        final String closureInstanceClassName = p(NativeClosureFactory.class) + "$ClosureInstance";
        final ClassWriter closureClassWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        final ClassVisitor closureClassVisitor = DEBUG ? AsmUtil.newCheckClassAdapter(closureClassWriter) : closureClassWriter;

        closureClassVisitor.visit(V1_5, ACC_PUBLIC | ACC_FINAL, closureInstanceClassName, null, p(NativeClosure.class),
                        new String[]{ p(com.kenai.jffi.Closure.class) });

        SkinnyMethodAdapter closureInit = new SkinnyMethodAdapter(closureClassVisitor.visitMethod(ACC_PUBLIC, "<init>",
               sig(void.class, NativeRuntime.class, Object.class, ReferenceQueue.class, Integer.class),
               null, null));
        closureInit.start();
        closureInit.aload(0);
        closureInit.aload(1);
        closureInit.aload(2);
        closureInit.aload(3);
        closureInit.aload(4);

        closureInit.invokespecial(p(NativeClosure.class), "<init>",
                sig(void.class, NativeRuntime.class, Object.class, ReferenceQueue.class, Integer.class));

        closureInit.voidreturn();
        closureInit.visitMaxs(10, 10);
        closureInit.visitEnd();


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

        SkinnyMethodAdapter closureInvoke = new SkinnyMethodAdapter(closureClassVisitor.visitMethod(ACC_PUBLIC, "invoke",
                       sig(void.class, com.kenai.jffi.Closure.Buffer.class, Object.class),
                       null, null));
        closureInvoke.start();

        if (void.class != callMethod.getReturnType() && Void.class != callMethod.getReturnType()) {
            // If the Callable returns a value, push the Callable.Buffer on the stack
            // for the call to Callable.Buffer#set<Foo>Return()
            closureInvoke.aload(1);
        }

        // Cast the Callable instance to the Callable subclass
        closureInvoke.aload(2);
        closureInvoke.checkcast(p(closureClass));

        // Construct callback method
        Class[] parameterTypes = callMethod.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; ++i) {
            Class parameterType = parameterTypes[i];
            if (!isParameterTypeSupported(parameterType)) {
                throw new IllegalArgumentException("unsupported closure parameter type " + parameterType);
            }

            // Load the Callable.Buffer for the parameter set call
            closureInvoke.aload(1);

            // Load the parameter index
            closureInvoke.pushInt(i);

            Class type = parameterType.isPrimitive() ? parameterType : AsmUtil.unboxedType(parameterType);

            if (byte.class == type) {
                closureInvoke.invokeinterface(p(com.kenai.jffi.Closure.Buffer.class), "getByte", sig(type, int.class));

            } else if (char.class == type) {
                closureInvoke.invokeinterface(p(com.kenai.jffi.Closure.Buffer.class), "getShort", sig(short.class, int.class));

            } else if (short.class == type) {
                closureInvoke.invokeinterface(p(com.kenai.jffi.Closure.Buffer.class), "getShort", sig(type, int.class));

            } else if (int.class == type) {
                closureInvoke.invokeinterface(p(com.kenai.jffi.Closure.Buffer.class), "getInt", sig(type, int.class));

            } else if (long.class == type && (long.class == parameterType || Long.class == parameterType)) {
                // Handle native long
                if (isLong32(parameterType, callMethod.getParameterAnnotations()[i])) {
                    closureInvoke.invokeinterface(p(com.kenai.jffi.Closure.Buffer.class), "getInt", sig(int.class, int.class));
                    widen(closureInvoke, int.class, long.class);
                } else {
                    closureInvoke.invokeinterface(p(com.kenai.jffi.Closure.Buffer.class), "getLong", sig(long.class, int.class));
                }

            } else if (long.class == type) {
                // This long type is used by Pointer/Struct/String, etc
                closureInvoke.invokeinterface(p(com.kenai.jffi.Closure.Buffer.class), "getLong", sig(long.class, int.class));

            } else if (float.class == type) {
                closureInvoke.invokeinterface(p(com.kenai.jffi.Closure.Buffer.class), "getFloat", sig(type, int.class));

            } else if (double.class == type) {
                closureInvoke.invokeinterface(p(com.kenai.jffi.Closure.Buffer.class), "getDouble", sig(type, int.class));

            } else {
                throw new IllegalArgumentException("unsupported closure parameter type " + parameterType);
            }

            AsmUtil.boxValue(closureInvoke, parameterType, type);
        }

        // dispatch to java method
        if (callMethod.getDeclaringClass().isInterface()) {
            closureInvoke.invokeinterface(p(closureClass), callMethod.getName(), sig(callMethod.getReturnType(), callMethod.getParameterTypes()));
        } else {
            closureInvoke.invokevirtual(p(closureClass), callMethod.getName(), sig(callMethod.getReturnType(), callMethod.getParameterTypes()));
        }

        Class returnType = callMethod.getReturnType();
        if (!isReturnTypeSupported(returnType)) {
            throw new IllegalArgumentException("unsupported closure return type " + returnType.getName());
        }
        Annotation[] returnAnnotations = callMethod.getAnnotations();
        Class nativeReturnType = AsmUtil.unboxedType(returnType);

        if (isLong32(returnType, returnAnnotations)) {
            nativeReturnType = int.class;
        }

        if (Number.class.isAssignableFrom(returnType)) {
            AsmUtil.unboxNumber(closureInvoke, returnType, nativeReturnType);

        } else if (Boolean.class.isAssignableFrom(returnType)) {
            AsmUtil.unboxBoolean(closureInvoke, nativeReturnType);

        } else if (Pointer.class.isAssignableFrom(returnType)) {
            AsmUtil.unboxPointer(closureInvoke, nativeReturnType);

        } else if (Enum.class.isAssignableFrom(returnType)) {
            AsmUtil.unboxEnum(closureInvoke, nativeReturnType);
        }

        if (void.class == nativeReturnType || Void.class == nativeReturnType) {
            // No return value to set, so no call to set<Foo>Return()

        } else if (byte.class == nativeReturnType) {
            closureInvoke.invokeinterface(p(com.kenai.jffi.Closure.Buffer.class), "setByteReturn", sig(void.class, byte.class));

        } else if (short.class == nativeReturnType || char.class == nativeReturnType) {
            closureInvoke.invokeinterface(p(com.kenai.jffi.Closure.Buffer.class), "setShortReturn", sig(void.class, short.class));

        } else if (int.class == nativeReturnType) {
            closureInvoke.invokeinterface(p(com.kenai.jffi.Closure.Buffer.class), "setIntReturn", sig(void.class, int.class));

        } else if (long.class == nativeReturnType) {
            closureInvoke.invokeinterface(p(com.kenai.jffi.Closure.Buffer.class), "setLongReturn", sig(void.class, long.class));

        } else if (float.class == nativeReturnType) {
            closureInvoke.invokeinterface(p(com.kenai.jffi.Closure.Buffer.class), "setFloatReturn", sig(void.class, float.class));

        } else if (double.class == nativeReturnType) {
            closureInvoke.invokeinterface(p(com.kenai.jffi.Closure.Buffer.class), "setDoubleReturn", sig(void.class, double.class));

        } else if (boolean.class == nativeReturnType) {
            closureInvoke.invokeinterface(p(com.kenai.jffi.Closure.Buffer.class), "setIntReturn", sig(void.class, int.class));
        }

        closureInvoke.voidreturn();
        closureInvoke.visitMaxs(10, 10);
        closureInvoke.visitEnd();

        closureClassVisitor.visitEnd();


        try {
            byte[] closureImpBytes = closureClassWriter.toByteArray();
            if (DEBUG) {
                ClassVisitor trace = AsmUtil.newTraceClassVisitor(new PrintWriter(System.err));
                new ClassReader(closureImpBytes).accept(trace, 0);
                trace.visitEnd();
            }
            ClassLoader cl = NativeClosureFactory.class.getClassLoader();
            if (cl == null) {
                cl = Thread.currentThread().getContextClassLoader();
            }
            if (cl == null) {
                cl = ClassLoader.getSystemClassLoader();
            }
            AsmClassLoader asm = new AsmClassLoader(cl);
            Class<? extends NativeClosure> nativeClosureClass = asm.defineClass(c(closureInstanceClassName), closureImpBytes);
            Constructor<? extends NativeClosure> nativeClosureConstructor
                    = nativeClosureClass.getConstructor(NativeRuntime.class, Object.class, ReferenceQueue.class, Integer.class);

            return new NativeClosureFactory(runtime, getCallContext(callMethod), nativeClosureConstructor);
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    private static boolean isReturnTypeSupported(Class type) {
        return type.isPrimitive()
                || boolean.class == type || Boolean.class == type
                || Byte.class == type
                || Short.class == type || Integer.class == type
                || Long.class == type || Float.class == type
                || Double.class == type || NativeLong.class == type
                || Enum.class.isAssignableFrom(type)
                || Pointer.class == type
                ;
    }

    private static boolean isParameterTypeSupported(Class type) {
        return type.isPrimitive()
                || boolean.class == type || Boolean.class == type
                || Byte.class == type
                || Short.class == type || Integer.class == type
                || Long.class == type || Float.class == type
                || Double.class == type || NativeLong.class == type
                || Pointer.class == type
                || String.class == type
                /*
                || CharSequence.class == type
                || Buffer.class.isAssignableFrom(type)
                || Struct.class.isAssignableFrom(type)
                || Enum.class.isAssignableFrom(type)
                || (type.isArray() && type.getComponentType().isPrimitive())
                || (type.isArray() && Struct.class.isAssignableFrom(type.getComponentType()))
                || (type.isArray() && Pointer.class.isAssignableFrom(type.getComponentType()))
                || (type.isArray() && CharSequence.class.isAssignableFrom(type.getComponentType()))
                || ByReference.class.isAssignableFrom(type)
                */
                ;
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
            retry: for (NativeClosurePointer prev = ptr; ptr != null; ptr = ptr.next) {
                if (ptr.getNativeClosure() == cl) {
                    if (prev != null) {
                        prev.next = ptr.next;
                        break;

                    } else if (ptr.next != null && !closures.replace(key, ptr, ptr.next)) {
                        continue retry;
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
        NativeClosure nativeClosure;
        try {
            nativeClosure = nativeClosureConstructor.newInstance(NativeRuntime.getInstance(), callable, referenceQueue, key);
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

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

    private static CallContext getCallContext(Method m) {
        com.kenai.jffi.Type resultType = ClosureUtil.getNativeResultType(m);
        com.kenai.jffi.Type[] parameterTypes = new com.kenai.jffi.Type[m.getParameterTypes().length];

        for (int i = 0; i < parameterTypes.length; i++) {
            parameterTypes[i] = ClosureUtil.getNativeParameterType(m, i);
        }
        

        return CallContextCache.getInstance().getCallContext(resultType, parameterTypes,
                ClosureUtil.getNativeCallingConvention(m));
    }
}
