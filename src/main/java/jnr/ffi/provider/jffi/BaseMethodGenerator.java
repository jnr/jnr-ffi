package jnr.ffi.provider.jffi;

import com.kenai.jffi.CallContext;
import com.kenai.jffi.Function;
import jnr.ffi.mapper.*;

import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Collections;

import static jnr.ffi.provider.jffi.AsmUtil.*;
import static jnr.ffi.provider.jffi.CodegenUtils.*;
import static jnr.ffi.provider.jffi.NumberUtil.getBoxedClass;
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
        mv.getfield(builder.getClassNamePath(), builder.getCallContextFieldName(function), ci(CallContext.class));

        mv.aload(0);
        mv.getfield(builder.getClassNamePath(), builder.getFunctionAddressFieldName(function), ci(long.class));

        LocalVariableAllocator localVariableAllocator = new LocalVariableAllocator(parameterTypes);

        generate(builder, mv, localVariableAllocator, function, resultType, parameterTypes, ignoreError);

        mv.visitMaxs(100, localVariableAllocator.getSpaceUsed());
        mv.visitEnd();
    }

    abstract void generate(AsmBuilder builder, SkinnyMethodAdapter mv, LocalVariableAllocator localVariableAllocator, Function function, ResultType resultType, ParameterType[] parameterTypes,
                           boolean ignoreError);

    static void loadAndConvertParameter(AsmBuilder builder, SkinnyMethodAdapter mv, LocalVariable parameter,
                                       ToNativeType parameterType) {
        AsmUtil.load(mv, parameterType.getDeclaredType(), parameter);
        emitToNativeConversion(builder, mv, parameterType);
    }

    static void emitPostInvoke(AsmBuilder builder, SkinnyMethodAdapter mv, ParameterType[] parameterTypes,
                               LocalVariable[] parameters, LocalVariable[] converted) {
        for (int i = 0; i < converted.length; ++i) {
            if (converted[i] != null) {
                mv.aload(0);
                AsmBuilder.ObjectField toNativeConverterField = builder.getToNativeConverterField(parameterTypes[i].toNativeConverter);
                mv.getfield(builder.getClassNamePath(), toNativeConverterField.name, ci(toNativeConverterField.klass));
                if (!ToNativeConverter.PostInvocation.class.isAssignableFrom(toNativeConverterField.klass)) {
                    mv.checkcast(ToNativeConverter.PostInvocation.class);
                }
                mv.aload(parameters[i]);
                mv.aload(converted[i]);
                if (parameterTypes[i].toNativeContext != null) {
                    getfield(mv, builder, builder.getToNativeContextField(parameterTypes[i].toNativeContext));
                } else {
                    mv.aconst_null();
                }
                mv.invokeinterface(ToNativeConverter.PostInvocation.class, "postInvoke", void.class,
                        Object.class, Object.class, ToNativeContext.class);
            }
        }
    }
}
