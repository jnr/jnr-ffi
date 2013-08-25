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
import jnr.ffi.Variable;
import jnr.ffi.mapper.*;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;

import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static jnr.ffi.provider.jffi.AsmUtil.*;
import static jnr.ffi.provider.jffi.CodegenUtils.*;
import static jnr.ffi.provider.jffi.InvokerUtil.hasAnnotation;
import static org.objectweb.asm.Opcodes.*;

/**
 * Generate global variable accessors
 */
public class VariableAccessorGenerator {
    private final AtomicLong nextClassID = new AtomicLong(0);
    private final jnr.ffi.Runtime runtime;
    static final Map<NativeType, PointerOp> pointerOperations;

    public VariableAccessorGenerator(jnr.ffi.Runtime runtime) {
        this.runtime = runtime;
    }

    public void generate(AsmBuilder builder, Class interfaceClass, String variableName, long address,
                         Class javaType, Collection<Annotation> annotations,
                         SignatureTypeMapper typeMapper, AsmClassLoader classLoader) {

        if (!NativeLibraryLoader.ASM_ENABLED) {
            throw new UnsupportedOperationException("asm bytecode generation not supported");
        }

        SimpleNativeContext context = new SimpleNativeContext(builder.getRuntime(), annotations);
        SignatureType signatureType = DefaultSignatureType.create(javaType, (FromNativeContext) context);
        jnr.ffi.mapper.FromNativeType fromNativeType = typeMapper.getFromNativeType(signatureType, context);
        FromNativeConverter fromNativeConverter = fromNativeType != null ? fromNativeType.getFromNativeConverter() : null;
        jnr.ffi.mapper.ToNativeType toNativeType = typeMapper.getToNativeType(signatureType, context);
        ToNativeConverter toNativeConverter = toNativeType != null ? toNativeType.getToNativeConverter() : null;

        Variable variableAccessor = buildVariableAccessor(builder.getRuntime(), address, interfaceClass, javaType, annotations,
                toNativeConverter, fromNativeConverter, classLoader);
        SkinnyMethodAdapter mv = new SkinnyMethodAdapter(builder.getClassVisitor(), ACC_PUBLIC | ACC_FINAL,
                variableName, sig(Variable.class), null, null);
        mv.start();
        mv.aload(0);
        mv.getfield(builder.getClassNamePath(), builder.getVariableName(variableAccessor), ci(Variable.class));
        mv.areturn();
        mv.visitMaxs(10, 10);
        mv.visitEnd();
    }

    Variable buildVariableAccessor(jnr.ffi.Runtime runtime, long address, Class interfaceClass, Class javaType, Collection<Annotation> annotations,
                                   ToNativeConverter toNativeConverter, FromNativeConverter fromNativeConverter,
                                   AsmClassLoader classLoader) {
        boolean debug = AsmLibraryLoader.DEBUG && !hasAnnotation(annotations, NoTrace.class);
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        ClassVisitor cv = debug ? AsmUtil.newCheckClassAdapter(cw) : cw;

        AsmBuilder builder = new AsmBuilder(runtime, p(interfaceClass) + "$VariableAccessor$$" + nextClassID.getAndIncrement(), cv, classLoader);
        cv.visit(V1_6, ACC_PUBLIC | ACC_FINAL, builder.getClassNamePath(), null, p(Object.class),
                new String[] { p(Variable.class) });

        SkinnyMethodAdapter set = new SkinnyMethodAdapter(builder.getClassVisitor(), ACC_PUBLIC | ACC_FINAL, "set",
                sig(void.class, Object.class),
                null, null);

        Class boxedType = toNativeConverter != null ? toNativeConverter.nativeType() : javaType;
        NativeType nativeType = Types.getType(runtime, boxedType, annotations).getNativeType();
        jnr.ffi.provider.ToNativeType toNativeType = new jnr.ffi.provider.ToNativeType(javaType, nativeType, annotations, toNativeConverter, null);
        jnr.ffi.provider.FromNativeType fromNativeType = new jnr.ffi.provider.FromNativeType(javaType, nativeType, annotations, fromNativeConverter, null);
        PointerOp pointerOp = pointerOperations.get(nativeType);
        if (pointerOp == null) {
            throw new IllegalArgumentException("global variable type not supported: " + javaType);
        }

        set.start();
        set.aload(0);
        Pointer pointer = DirectMemoryIO.wrap(runtime, address);
        set.getfield(builder.getClassNamePath(), builder.getObjectFieldName(pointer, Pointer.class), ci(Pointer.class));
        set.lconst_0();

        set.aload(1);
        set.checkcast(javaType);
        emitToNativeConversion(builder, set, toNativeType);

        ToNativeOp toNativeOp = ToNativeOp.get(toNativeType);
        if (toNativeOp != null && toNativeOp.isPrimitive()) {
            toNativeOp.emitPrimitive(set, pointerOp.nativeIntClass, toNativeType.getNativeType());

        } else if (Pointer.class.isAssignableFrom(toNativeType.effectiveJavaType())) {
            pointerOp = POINTER_OP_POINTER;

        } else {
            throw new IllegalArgumentException("global variable type not supported: " + javaType);
        }

        pointerOp.put(set);

        set.voidreturn();
        set.visitMaxs(10, 10);
        set.visitEnd();

        SkinnyMethodAdapter get = new SkinnyMethodAdapter(builder.getClassVisitor(), ACC_PUBLIC | ACC_FINAL, "get",
                sig(Object.class),
                null, null);

        get.start();
        get.aload(0);
        get.getfield(builder.getClassNamePath(), builder.getObjectFieldName(pointer, Pointer.class), ci(Pointer.class));
        get.lconst_0();
        pointerOp.get(get);
        emitFromNativeConversion(builder, get, fromNativeType, pointerOp.nativeIntClass);
        get.areturn();
        get.visitMaxs(10, 10);
        get.visitEnd();

        SkinnyMethodAdapter init = new SkinnyMethodAdapter(cv, ACC_PUBLIC, "<init>",
                sig(void.class, Object[].class),
                null, null);
        init.start();
        init.aload(0);
        init.invokespecial(p(Object.class), "<init>", sig(void.class));

        builder.emitFieldInitialization(init, 1);

        init.voidreturn();
        init.visitMaxs(10, 10);
        init.visitEnd();

        cv.visitEnd();

        try {
            byte[] bytes = cw.toByteArray();
            if (debug) {
                ClassVisitor trace = AsmUtil.newTraceClassVisitor(new PrintWriter(System.err));
                new ClassReader(bytes).accept(trace, 0);
            }

            Class<Variable> implClass = classLoader.defineClass(builder.getClassNamePath().replace("/", "."), bytes);
            Constructor<Variable> cons = implClass.getDeclaredConstructor(Object[].class);
            return cons.newInstance(new Object[] { builder.getObjectFieldValues() });
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    static {
        Map<NativeType, PointerOp> ops = new EnumMap<NativeType, PointerOp>(NativeType.class);
        op(ops, NativeType.SCHAR, "Byte", byte.class);
        op(ops, NativeType.UCHAR, "Byte", byte.class);
        op(ops, NativeType.SSHORT, "Short", short.class);
        op(ops, NativeType.USHORT, "Short", short.class);
        op(ops, NativeType.SINT, "Int", int.class);
        op(ops, NativeType.UINT, "Int", int.class);
        op(ops, NativeType.SLONG, "Long", long.class);
        op(ops, NativeType.ULONG, "Long", long.class);
        op(ops, NativeType.SLONGLONG, "LongLong", long.class);
        op(ops, NativeType.ULONGLONG, "LongLong", long.class);
        op(ops, NativeType.FLOAT, "Float", float.class);
        op(ops, NativeType.DOUBLE, "Double", double.class);
        op(ops, NativeType.ADDRESS, "Address", long.class);

        pointerOperations = Collections.unmodifiableMap(ops);
    }

    private static void op(Map<NativeType, PointerOp> ops, NativeType type, String name, Class nativeIntType) {
        ops.put(type, new PointerOp(name, nativeIntType));
    }

    private static final PointerOp POINTER_OP_POINTER = new PointerOp("Pointer", Pointer.class);

    private static final class PointerOp {
        private final String getMethodName;
        private final String putMethodName;
        final Class nativeIntClass;

        private PointerOp(String name, Class nativeIntClass) {
            this.getMethodName = "get" + name;
            this.putMethodName = "put" + name;
            this.nativeIntClass = nativeIntClass;
        }

        void put(SkinnyMethodAdapter mv) {
            mv.invokevirtual(Pointer.class, putMethodName, void.class, long.class, nativeIntClass);
        }

        void get(SkinnyMethodAdapter mv) {
            mv.invokevirtual(Pointer.class, getMethodName, nativeIntClass, long.class);
        }
    }
}
