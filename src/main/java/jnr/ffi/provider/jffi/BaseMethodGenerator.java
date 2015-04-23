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
import com.kenai.jffi.Function;
import jnr.ffi.mapper.*;
import jnr.ffi.provider.*;

import static jnr.ffi.provider.jffi.AsmUtil.*;
import static jnr.ffi.provider.jffi.CodegenUtils.*;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;

/**
 *
 */
abstract class BaseMethodGenerator implements MethodGenerator {

    public void generate(AsmBuilder builder, String functionName, Function function,
                         ResultType resultType, ParameterType[] parameterTypes, boolean ignoreError) {
        Class[] javaParameterTypes = new Class[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            javaParameterTypes[i] = parameterTypes[i].getDeclaredType();
        }

        SkinnyMethodAdapter mv = new SkinnyMethodAdapter(builder.getClassVisitor(), ACC_PUBLIC | ACC_FINAL,
                functionName,
                sig(resultType.getDeclaredType(), javaParameterTypes), null, null);
        mv.start();

        // Retrieve the static 'ffi' Invoker instance
        mv.getstatic(p(AbstractAsmLibraryInterface.class), "ffi", ci(com.kenai.jffi.Invoker.class));

        // retrieve the call context and function address
        mv.aload(0);
        mv.getfield(builder.getClassNamePath(), builder.getCallContextFieldName(function.getCallContext()), ci(CallContext.class));

        mv.aload(0);
        mv.getfield(builder.getClassNamePath(), builder.getFunctionAddressFieldName(function), ci(long.class));

        LocalVariableAllocator localVariableAllocator = new LocalVariableAllocator(parameterTypes);

        generate(builder, mv, localVariableAllocator, function.getCallContext(), resultType, parameterTypes, ignoreError);

        mv.visitMaxs(100, localVariableAllocator.getSpaceUsed());
        mv.visitEnd();
    }

    abstract void generate(AsmBuilder builder, SkinnyMethodAdapter mv, LocalVariableAllocator localVariableAllocator, CallContext callContext, ResultType resultType, ParameterType[] parameterTypes,
                           boolean ignoreError);

    static LocalVariable loadAndConvertParameter(AsmBuilder builder, SkinnyMethodAdapter mv,
                                                 LocalVariableAllocator localVariableAllocator,
                                                 LocalVariable parameter, jnr.ffi.provider.ToNativeType parameterType) {
        AsmUtil.load(mv, parameterType.getDeclaredType(), parameter);
        emitToNativeConversion(builder, mv, parameterType);

        if (parameterType.getToNativeConverter() != null) {
            LocalVariable converted = localVariableAllocator.allocate(parameterType.getToNativeConverter().nativeType());
            mv.astore(converted);
            mv.aload(converted);
            return converted;
        }

        return parameter;
    }

    static boolean isPostInvokeRequired(ParameterType[] parameterTypes) {
        for (ParameterType parameterType : parameterTypes) {
            if (parameterType.getToNativeConverter() instanceof ToNativeConverter.PostInvocation) {
                return true;
            }
        }

        return false;
    }

    static void emitEpilogue(final AsmBuilder builder, final SkinnyMethodAdapter mv, final ResultType resultType,
                           final ParameterType[] parameterTypes,
                      final LocalVariable[] parameters, final LocalVariable[] converted, final Runnable sessionCleanup) {
        final Class unboxedResultType = unboxedReturnType(resultType.effectiveJavaType());
        if (isPostInvokeRequired(parameterTypes) || sessionCleanup != null) {
            tryfinally(mv, new Runnable() {
                        public void run() {
                            emitFromNativeConversion(builder, mv, resultType, unboxedResultType);
                            // ensure there is always at least one instruction inside the try {} block
                            mv.nop();
                        }
                    },
                    new Runnable() {
                        public void run() {
                            emitPostInvoke(builder, mv, parameterTypes, parameters, converted);
                            if (sessionCleanup != null) {
                                sessionCleanup.run();
                            }
                        }
                    }
            );
        } else {
            emitFromNativeConversion(builder, mv, resultType, unboxedResultType);
        }
        emitReturnOp(mv, resultType.getDeclaredType());
    }

    static void emitPostInvoke(AsmBuilder builder, final SkinnyMethodAdapter mv, ParameterType[] parameterTypes,
                               LocalVariable[] parameters, LocalVariable[] converted) {
        for (int i = 0; i < converted.length; ++i) {
            if (converted[i] != null && parameterTypes[i].getToNativeConverter() instanceof ToNativeConverter.PostInvocation) {
                mv.aload(0);
                AsmBuilder.ObjectField toNativeConverterField = builder.getToNativeConverterField(parameterTypes[i].getToNativeConverter());
                mv.getfield(builder.getClassNamePath(), toNativeConverterField.name, ci(toNativeConverterField.klass));
                if (!ToNativeConverter.PostInvocation.class.isAssignableFrom(toNativeConverterField.klass)) {
                    mv.checkcast(ToNativeConverter.PostInvocation.class);
                }
                mv.aload(parameters[i]);
                mv.aload(converted[i]);
                if (parameterTypes[i].getToNativeContext() != null) {
                    getfield(mv, builder, builder.getToNativeContextField(parameterTypes[i].getToNativeContext()));
                } else {
                    mv.aconst_null();
                }

                mv.invokestatic(AsmRuntime.class, "postInvoke", void.class,
                        ToNativeConverter.PostInvocation.class, Object.class, Object.class, ToNativeContext.class);
            }
        }
    }
}
