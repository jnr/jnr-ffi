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

import jnr.ffi.NativeType;
import jnr.ffi.Pointer;
import jnr.ffi.provider.FromNativeType;
import jnr.ffi.provider.ToNativeType;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.PrintWriter;
import java.lang.ref.Reference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicLong;

import static jnr.ffi.provider.jffi.AsmUtil.*;
import static jnr.ffi.provider.jffi.CodegenUtils.*;
import static jnr.ffi.provider.jffi.NumberUtil.convertPrimitive;
import static jnr.ffi.provider.jffi.NumberUtil.sizeof;
import static org.objectweb.asm.Opcodes.*;

/**
 *
 */
public abstract class NativeClosureProxy {
    protected final jnr.ffi.Runtime runtime;
    volatile Reference<?> closureReference;

    protected NativeClosureProxy(NativeRuntime runtime) {
        this.runtime = runtime;
    }

    protected Object getCallable() {
        Object callable = closureReference != null ? closureReference.get() : null;
        if (callable != null) {
            return callable;
        }
        throw new NullPointerException("callable is null");
    }

    static class Factory {
        private final jnr.ffi.Runtime runtime;
        private final Constructor<? extends NativeClosureProxy> constructor;
        private final Object[] objectFields;
        private final Method invokeMethod;

        Factory(jnr.ffi.Runtime runtime, Constructor<? extends NativeClosureProxy> constructor,
                Method invokeMethod, Object[] objectFields) {
            this.runtime = runtime;
            this.constructor = constructor;
            this.invokeMethod = invokeMethod;
            this.objectFields = objectFields;
        }

        NativeClosureProxy newClosureProxy() {
            try {
                return constructor.newInstance(runtime, objectFields);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }

        Method getInvokeMethod() {
            return invokeMethod;
        }
    }

    public final static boolean DEBUG = Boolean.getBoolean("jnr.ffi.compile.dump");
    private static final AtomicLong nextClassID = new AtomicLong(0);

    static Factory newProxyFactory(jnr.ffi.Runtime runtime, Method callMethod,
                            ToNativeType resultType, FromNativeType[] parameterTypes, AsmClassLoader classLoader) {
        final String closureProxyClassName = p(NativeClosureProxy.class) + "$$impl$$" + nextClassID.getAndIncrement();
        final ClassWriter closureClassWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        final ClassVisitor closureClassVisitor = DEBUG ? AsmUtil.newCheckClassAdapter(closureClassWriter) : closureClassWriter;
        AsmBuilder builder = new AsmBuilder(runtime, closureProxyClassName, closureClassVisitor, classLoader);

        closureClassVisitor.visit(V1_6, ACC_PUBLIC | ACC_FINAL, closureProxyClassName, null, p(NativeClosureProxy.class),
                new String[]{ });

        Class[] nativeParameterClasses = new Class[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            nativeParameterClasses[i] = getNativeClass(parameterTypes[i].getNativeType());
        }

        Class nativeResultClass = getNativeClass(resultType.getNativeType());

        SkinnyMethodAdapter mv = new SkinnyMethodAdapter(closureClassVisitor, ACC_PUBLIC | ACC_FINAL, "invoke",
                sig(nativeResultClass, nativeParameterClasses),
                null, null);
        mv.start();

        // Cast the callable instance to the correct class
        mv.aload(0);
        mv.invokevirtual(NativeClosureProxy.class, "getCallable", Object.class);
        mv.checkcast(p(callMethod.getDeclaringClass()));

        LocalVariable[] parameterVariables = AsmUtil.getParameterVariables(nativeParameterClasses);

        // Construct callback method
        LocalVariableAllocator localVariableAllocator = new LocalVariableAllocator(nativeParameterClasses);

        for (int i = 0; i < parameterTypes.length; ++i) {
            FromNativeType parameterType = parameterTypes[i];
            Class parameterClass = parameterType.effectiveJavaType();

            if (!isParameterTypeSupported(parameterClass)) {
                throw new IllegalArgumentException("unsupported closure parameter type " + parameterTypes[i].getDeclaredType());
            }

            AsmUtil.load(mv, nativeParameterClasses[i], parameterVariables[i]);
            if (!parameterClass.isPrimitive()) {
                emitFromNativeConversion(builder, mv, parameterTypes[i], nativeParameterClasses[i]);
            } else {
                convertPrimitive(mv, nativeParameterClasses[i], parameterClass, parameterType.getNativeType());
            }
        }

        // dispatch to java method
        if (callMethod.getDeclaringClass().isInterface()) {
            mv.invokeinterface(p(callMethod.getDeclaringClass()), callMethod.getName(), sig(callMethod.getReturnType(), callMethod.getParameterTypes()));
        } else {
            mv.invokevirtual(p(callMethod.getDeclaringClass()), callMethod.getName(), sig(callMethod.getReturnType(), callMethod.getParameterTypes()));
        }

        if (!isReturnTypeSupported(resultType.effectiveJavaType())) {
            throw new IllegalArgumentException("unsupported closure return type " + resultType.getDeclaredType());
        }

        emitToNativeConversion(builder, mv, resultType);
        if (!resultType.effectiveJavaType().isPrimitive()) {
            if (Number.class.isAssignableFrom(resultType.effectiveJavaType())) {
                AsmUtil.unboxNumber(mv, resultType.effectiveJavaType(), nativeResultClass, resultType.getNativeType());

            } else if (Boolean.class.isAssignableFrom(resultType.effectiveJavaType())) {
                AsmUtil.unboxBoolean(mv, nativeResultClass);

            } else if (Pointer.class.isAssignableFrom(resultType.effectiveJavaType())) {
                AsmUtil.unboxPointer(mv, nativeResultClass);

            }
        }

        emitReturnOp(mv, nativeResultClass);
        mv.visitMaxs(10, 10 + localVariableAllocator.getSpaceUsed());
        mv.visitEnd();

        SkinnyMethodAdapter closureInit = new SkinnyMethodAdapter(closureClassVisitor, ACC_PUBLIC, "<init>",
                sig(void.class, NativeRuntime.class, Object[].class),
                null, null);
        closureInit.start();
        closureInit.aload(0);
        closureInit.aload(1);
        closureInit.invokespecial(p(NativeClosureProxy.class), "<init>", sig(void.class, NativeRuntime.class));

        AsmBuilder.ObjectField[] fields = builder.getObjectFieldArray();
        Object[] fieldObjects = new Object[fields.length];
        for (int i = 0; i < fieldObjects.length; i++) {
            fieldObjects[i] = fields[i].value;
            String fieldName = fields[i].name;
            builder.getClassVisitor().visitField(ACC_PRIVATE | ACC_FINAL, fieldName, ci(fields[i].klass), null, null);
            closureInit.aload(0);
            closureInit.aload(2);
            closureInit.pushInt(i);
            closureInit.aaload();
            if (fields[i].klass.isPrimitive()) {
                Class unboxedType = unboxedType(fields[i].klass);
                closureInit.checkcast(unboxedType);
                unboxNumber(closureInit, unboxedType, fields[i].klass);
            } else {
                closureInit.checkcast(fields[i].klass);
            }
            closureInit.putfield(builder.getClassNamePath(), fieldName, ci(fields[i].klass));
        }

        closureInit.voidreturn();
        closureInit.visitMaxs(10, 10);
        closureInit.visitEnd();

        closureClassVisitor.visitEnd();

        try {
            byte[] closureImpBytes = closureClassWriter.toByteArray();
            if (DEBUG) {
                ClassVisitor trace = AsmUtil.newTraceClassVisitor(new PrintWriter(System.err));
                new ClassReader(closureImpBytes).accept(trace, 0);
            }
            ClassLoader cl = NativeClosureFactory.class.getClassLoader();
            if (cl == null) {
                cl = Thread.currentThread().getContextClassLoader();
            }
            if (cl == null) {
                cl = ClassLoader.getSystemClassLoader();
            }
            Class<? extends NativeClosureProxy> klass = builder.getClassLoader().defineClass(c(closureProxyClassName), closureImpBytes);
            Constructor<? extends NativeClosureProxy> constructor
                    = klass.getConstructor(NativeRuntime.class, Object[].class);

            return new Factory(runtime, constructor, klass.getMethod("invoke", nativeParameterClasses), fieldObjects);
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
                || Double.class == type
                || Pointer.class == type
                ;
    }

    private static boolean isParameterTypeSupported(Class type) {
        return type.isPrimitive()
                || boolean.class == type || Boolean.class == type
                || Byte.class == type
                || Short.class == type || Integer.class == type
                || Long.class == type || Float.class == type
                || Double.class == type
                || Pointer.class == type
                /*
                || CharSequence.class == type
                || Buffer.class.isAssignableFrom(type)
                || (type.isArray() && type.getComponentType().isPrimitive())
                || (type.isArray() && Struct.class.isAssignableFrom(type.getComponentType()))
                || (type.isArray() && Pointer.class.isAssignableFrom(type.getComponentType()))
                || (type.isArray() && CharSequence.class.isAssignableFrom(type.getComponentType()))
                || ByReference.class.isAssignableFrom(type)
                */
                ;
    }

    static Class getNativeClass(NativeType nativeType) {
        switch (nativeType) {
            case SCHAR:
            case UCHAR:
                return byte.class;

            case SSHORT:
            case USHORT:
                return short.class;

            case SINT:
            case UINT:
                return int.class;

            case SLONG:
            case ULONG:
            case ADDRESS:
                return sizeof(nativeType) <= 4 ? int.class : long.class;

            case SLONGLONG:
            case ULONGLONG:
                return long.class;


            case FLOAT:
                return float.class;

            case DOUBLE:
                return double.class;

            case VOID:
                return void.class;

            default:
                throw new IllegalArgumentException("unsupported native type: " + nativeType);
        }
    }
}
