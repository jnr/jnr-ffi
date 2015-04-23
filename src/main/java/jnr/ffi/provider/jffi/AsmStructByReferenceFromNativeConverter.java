/*
 * Copyright (C) 2012 Wayne Meissner
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

import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import jnr.ffi.Struct;
import jnr.ffi.mapper.FromNativeContext;
import jnr.ffi.mapper.FromNativeConverter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;

import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static jnr.ffi.provider.jffi.CodegenUtils.*;
import static org.objectweb.asm.Opcodes.*;

@FromNativeConverter.NoContext
@FromNativeConverter.Cacheable
abstract public class AsmStructByReferenceFromNativeConverter implements FromNativeConverter<Struct, Pointer> {
    private final jnr.ffi.Runtime runtime;
    private final int flags;

    protected AsmStructByReferenceFromNativeConverter(jnr.ffi.Runtime runtime, int flags) {
        this.runtime = runtime;
        this.flags = flags;
    }

    public final Class<Pointer> nativeType() {
        return Pointer.class;
    }

    // getRuntime() is called from generated code
    protected final jnr.ffi.Runtime getRuntime() {
        return runtime;
    }
    
    static final Map<Class<? extends Struct>, Class<? extends AsmStructByReferenceFromNativeConverter>> converterClasses
            = new ConcurrentHashMap<Class<? extends Struct>, Class<? extends AsmStructByReferenceFromNativeConverter>>();
    static AsmStructByReferenceFromNativeConverter newStructByReferenceConverter(jnr.ffi.Runtime runtime, Class<? extends Struct> structClass, int flags, AsmClassLoader classLoader) {
        try {
            return newStructByReferenceClass(structClass, classLoader).getConstructor(jnr.ffi.Runtime.class, int.class).newInstance(runtime, flags);

        } catch (NoSuchMethodException nsme) {
            throw new RuntimeException(nsme);
        } catch (IllegalAccessException iae) {
            throw new RuntimeException(iae);
        } catch (InstantiationException ie) {
            throw new RuntimeException(ie);
        } catch (InvocationTargetException ite) {
            throw new RuntimeException(ite);
        }
    }

    private static final AtomicLong nextClassID = new AtomicLong(0);

    static Class<? extends AsmStructByReferenceFromNativeConverter> newStructByReferenceClass(Class<? extends Struct> structClass, AsmClassLoader classLoader) {

        try {
            Constructor<? extends Struct> cons = structClass.asSubclass(Struct.class).getConstructor(jnr.ffi.Runtime.class);
            if (!Modifier.isPublic(cons.getModifiers())) {
                throw new RuntimeException(structClass.getName() + " constructor is not public");
            }
        } catch (NoSuchMethodException ex) {
            throw new RuntimeException("struct subclass " + structClass.getName() + " has no constructor that takes a "
                    + jnr.ffi.Runtime.class.getName(), ex);
        }


        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        ClassVisitor cv = AsmLibraryLoader.DEBUG ? AsmUtil.newCheckClassAdapter(cw) : cw;

        final String className = p(structClass) + "$$jnr$$StructByReferenceFromNativeConverter$$" + nextClassID.getAndIncrement();

        cv.visit(V1_5, ACC_PUBLIC | ACC_FINAL, className, null, p(AsmStructByReferenceFromNativeConverter.class),
                new String[0]);

        cv.visitAnnotation(ci(FromNativeConverter.NoContext.class), true);

        // Create the constructor to set the instance fields
        SkinnyMethodAdapter init = new SkinnyMethodAdapter(cv, ACC_PUBLIC, "<init>", sig(void.class, jnr.ffi.Runtime.class, int.class), null, null);
        init.start();
        // Invoke the super class constructor as super(Library)
        init.aload(0);
        init.aload(1);
        init.iload(2);
        init.invokespecial(p(AsmStructByReferenceFromNativeConverter.class), "<init>", sig(void.class, jnr.ffi.Runtime.class, int.class));
        init.voidreturn();
        init.visitMaxs(10, 10);
        init.visitEnd();

        SkinnyMethodAdapter fromNative = new SkinnyMethodAdapter(cv, ACC_PUBLIC | ACC_FINAL, "fromNative",
                sig(structClass, Pointer.class, FromNativeContext.class), null, null);

        fromNative.start();
        Label nullPointer = new Label();
        fromNative.aload(1);
        fromNative.ifnull(nullPointer);

        // Create an instance of the struct subclass
        fromNative.newobj(p(structClass));
        fromNative.dup();
        fromNative.aload(0);
        fromNative.invokevirtual(p(AsmStructByReferenceFromNativeConverter.class), "getRuntime", sig(Runtime.class));
        fromNative.invokespecial(structClass, "<init>", void.class, jnr.ffi.Runtime.class);

        // associate the memory with the struct and return the struct
        fromNative.dup();
        fromNative.aload(1);
        fromNative.invokevirtual(structClass, "useMemory", void.class, Pointer.class);
        fromNative.areturn();

        fromNative.label(nullPointer);
        fromNative.aconst_null();
        fromNative.areturn();

        fromNative.visitAnnotation(ci(FromNativeConverter.NoContext.class), true);
        fromNative.visitMaxs(10, 10);
        fromNative.visitEnd();

        fromNative = new SkinnyMethodAdapter(cv, ACC_PUBLIC | ACC_FINAL, "fromNative",
                sig(Object.class, Object.class, FromNativeContext.class), null, null);
        fromNative.start();
        fromNative.aload(0);
        fromNative.aload(1);
        fromNative.checkcast(Pointer.class);
        fromNative.aload(2);
        fromNative.invokevirtual(className, "fromNative", sig(structClass, Pointer.class, FromNativeContext.class));
        fromNative.areturn();
        fromNative.visitAnnotation(ci(FromNativeConverter.NoContext.class), true);
        fromNative.visitMaxs(10, 10);
        fromNative.visitEnd();
        cv.visitEnd();

        try {
            byte[] bytes = cw.toByteArray();
            if (AsmLibraryLoader.DEBUG) {
                ClassVisitor trace = AsmUtil.newTraceClassVisitor(new PrintWriter(System.err));
                new ClassReader(bytes).accept(trace, 0);
            }

            return classLoader.defineClass(className.replace("/", "."), bytes);
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }
}
