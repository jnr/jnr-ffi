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

import com.kenai.jffi.CallContext;
import jnr.ffi.CallingConvention;
import jnr.ffi.Pointer;
import jnr.ffi.annotations.StdCall;
import jnr.ffi.mapper.*;
import jnr.ffi.provider.InAccessibleMemoryIO;
import jnr.ffi.provider.ParameterType;
import jnr.ffi.provider.ResultType;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicLong;

import static jnr.ffi.provider.jffi.ClosureUtil.getDelegateMethod;
import static jnr.ffi.provider.jffi.CodegenUtils.*;
import static jnr.ffi.provider.jffi.InvokerUtil.*;
import static org.objectweb.asm.Opcodes.*;

/**
 *
 */
abstract public class ClosureFromNativeConverter implements FromNativeConverter<Object, Pointer> {
    @Override
    public Class<Pointer> nativeType() {
        return Pointer.class;
    }

    public static FromNativeConverter<?, Pointer> getInstance(jnr.ffi.Runtime runtime, SignatureType type, AsmClassLoader classLoader, SignatureTypeMapper typeMapper) {
        return newClosureConverter(runtime, classLoader, type.getDeclaredType(), typeMapper);
    }

    public static final class ProxyConverter extends ClosureFromNativeConverter {
        private final jnr.ffi.Runtime runtime;
        private final Constructor closureConstructor;
        private final Object[] initFields;

        public ProxyConverter(jnr.ffi.Runtime runtime, Constructor closureConstructor, Object[] initFields) {
            this.runtime = runtime;
            this.closureConstructor = closureConstructor;
            this.initFields = initFields.clone();
        }

        @Override
        public Object fromNative(Pointer nativeValue, FromNativeContext context) {
            try {
                return closureConstructor.newInstance(runtime, nativeValue.address(), initFields);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }
    }

    public static abstract class AbstractClosurePointer extends InAccessibleMemoryIO {
        public static final com.kenai.jffi.Invoker ffi = com.kenai.jffi.Invoker.getInstance();
        protected final long functionAddress;

        protected AbstractClosurePointer(jnr.ffi.Runtime runtime, long functionAddress) {
            super(runtime, functionAddress, true);
            this.functionAddress = functionAddress;
        }

        @Override
        public final long size() {
            return 0;
        }
    }


    private static final AtomicLong nextClassID = new AtomicLong(0);
    private static FromNativeConverter newClosureConverter(jnr.ffi.Runtime runtime, AsmClassLoader classLoader, Class closureClass,
                                                                        SignatureTypeMapper typeMapper) {

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        ClassVisitor cv = AsmLibraryLoader.DEBUG ? AsmUtil.newCheckClassAdapter(cw) : cw;

        final String className = p(closureClass) + "$jnr$fromNativeConverter$" + nextClassID.getAndIncrement();
        AsmBuilder builder = new AsmBuilder(runtime, className, cv, classLoader);

        cv.visit(V1_6, ACC_PUBLIC | ACC_FINAL, className, null, p(AbstractClosurePointer.class),
                new String[] { p(closureClass) } );

        cv.visitAnnotation(ci(FromNativeConverter.NoContext.class), true);

        generateInvocation(runtime, builder, closureClass, typeMapper);

        // Create the constructor to set the instance fields
        SkinnyMethodAdapter init = new SkinnyMethodAdapter(cv, ACC_PUBLIC, "<init>",
                sig(void.class, jnr.ffi.Runtime.class, long.class, Object[].class), null, null);
        init.start();
        // Invoke the super class constructor as super(functionAddress)
        init.aload(0);
        init.aload(1);
        init.lload(2);
        init.invokespecial(p(AbstractClosurePointer.class), "<init>", sig(void.class, jnr.ffi.Runtime.class, long.class));
        builder.emitFieldInitialization(init, 4);
        init.voidreturn();
        init.visitMaxs(10, 10);
        init.visitEnd();

        Class implClass = loadClass(classLoader, className, cw);
        try {
            return new ProxyConverter(runtime, implClass.getConstructor(jnr.ffi.Runtime.class, long.class, Object[].class), builder.getObjectFieldValues());
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    private static Class loadClass(AsmClassLoader classLoader, String className, ClassWriter cw) {
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

    private static void generateInvocation(jnr.ffi.Runtime runtime, AsmBuilder builder, Class closureClass, SignatureTypeMapper typeMapper) {
        Method closureMethod = getDelegateMethod(closureClass);

        FromNativeContext resultContext = new MethodResultContext(runtime, closureMethod);
        SignatureType signatureType = DefaultSignatureType.create(closureMethod.getReturnType(), resultContext);
        jnr.ffi.mapper.FromNativeType fromNativeType = typeMapper.getFromNativeType(signatureType, resultContext);
        FromNativeConverter fromNativeConverter = fromNativeType != null ? fromNativeType.getFromNativeConverter() : null;
        ResultType resultType = getResultType(runtime, closureMethod.getReturnType(),
                resultContext.getAnnotations(), fromNativeConverter, resultContext);

        ParameterType[] parameterTypes = getParameterTypes(runtime, typeMapper, closureMethod);

        // Allow individual methods to set the calling convention to stdcall
        CallingConvention callingConvention = closureClass.isAnnotationPresent(StdCall.class)
                ? CallingConvention.STDCALL : CallingConvention.DEFAULT;
        CallContext callContext = getCallContext(resultType, parameterTypes, callingConvention, true);
        LocalVariableAllocator localVariableAllocator = new LocalVariableAllocator(parameterTypes);


        Class[] javaParameterTypes = new Class[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            javaParameterTypes[i] = parameterTypes[i].getDeclaredType();
        }

        SkinnyMethodAdapter mv = new SkinnyMethodAdapter(builder.getClassVisitor(), ACC_PUBLIC | ACC_FINAL,
                closureMethod.getName(),
                sig(resultType.getDeclaredType(), javaParameterTypes), null, null);
        mv.start();
        // Retrieve the static 'ffi' Invoker instance
        mv.getstatic(p(AbstractClosurePointer.class), "ffi", ci(com.kenai.jffi.Invoker.class));

        // retrieve the call context and function address
        mv.aload(0);
        mv.getfield(builder.getClassNamePath(), builder.getCallContextFieldName(callContext), ci(CallContext.class));

        mv.aload(0);
        mv.getfield(p(AbstractClosurePointer.class), "functionAddress", ci(long.class));

        final BaseMethodGenerator[] generators = {
                new FastIntMethodGenerator(),
                new FastLongMethodGenerator(),
                new FastNumericMethodGenerator(),
                new BufferMethodGenerator()
        };

        for (BaseMethodGenerator generator : generators) {
            if (generator.isSupported(resultType, parameterTypes, callingConvention)) {
                generator.generate(builder, mv, localVariableAllocator, callContext, resultType, parameterTypes, false);
            }
        }

        mv.visitMaxs(100, 10 + localVariableAllocator.getSpaceUsed());
        mv.visitEnd();
    }


}
