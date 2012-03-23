package jnr.ffi.provider.jffi;

import com.kenai.jffi.CallContext;
import com.kenai.jffi.Function;
import jnr.ffi.mapper.*;

import static jnr.ffi.provider.jffi.AsmUtil.boxValue;
import static jnr.ffi.provider.jffi.AsmUtil.calculateLocalVariableSpace;
import static jnr.ffi.provider.jffi.CodegenUtils.ci;
import static jnr.ffi.provider.jffi.CodegenUtils.p;
import static jnr.ffi.provider.jffi.CodegenUtils.sig;
import static jnr.ffi.provider.jffi.NumberUtil.getBoxedClass;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;

/**
 *
 */
abstract class BaseMethodGenerator implements MethodGenerator {

    public void generate(AsmBuilder builder, String functionName, Function function, Signature signature) {
        ResultType resultType = InvokerUtil.getResultType(NativeRuntime.getInstance(),
                signature.resultType, signature.resultAnnotations, null);
        ParameterType[] parameterTypes = new ParameterType[signature.parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            parameterTypes[i] = InvokerUtil.getParameterType(NativeRuntime.getInstance(),
                    signature.parameterTypes[i], signature.parameterAnnotations[i], null);
        }

        generate(builder, functionName, function, resultType, parameterTypes, signature.ignoreError);
    }

    public void generate(AsmBuilder builder, String functionName, Function function,
                         ResultType resultType, ParameterType[] parameterTypes, boolean ignoreError) {
        Class[] javaParameterTypes = new Class[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            javaParameterTypes[i] = parameterTypes[i].getDeclaredType();
        }

        SkinnyMethodAdapter mv = new SkinnyMethodAdapter(builder.getClassVisitor().visitMethod(ACC_PUBLIC | ACC_FINAL,
                functionName,
                sig(resultType.getDeclaredType(), javaParameterTypes), null, null));
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
                                       ParameterType parameterType) {
        ToNativeConverter parameterConverter = parameterType.toNativeConverter;
        if (parameterConverter != null) {
            mv.aload(0);
            mv.getfield(builder.getClassNamePath(), builder.getParameterConverterName(parameterConverter), ci(ToNativeConverter.class));
        }
        AsmUtil.loadParameter(mv, parameterType.getDeclaredType(), parameter);
        if (parameterConverter != null) {
            if (parameterType.getDeclaredType().isPrimitive()) {
                boxValue(mv, getBoxedClass(parameterType.getDeclaredType()), parameterType.getDeclaredType());
            }
            mv.aconst_null();
            mv.invokeinterface(ToNativeConverter.class, "toNative",
                    Object.class, Object.class, ToNativeContext.class);
            mv.checkcast(p(parameterConverter.nativeType()));
        }
    }

    static void convertAndReturnResult(AsmBuilder builder, SkinnyMethodAdapter mv, ResultType resultType, Class nativeResultClass) {
        // If there is a result converter, retrieve it and put on the stack
        FromNativeConverter resultConverter = resultType.fromNativeConverter;
        if (resultConverter != null) {
            boxValue(mv, resultConverter.nativeType(), nativeResultClass);

            mv.aload(0);
            mv.getfield(builder.getClassNamePath(), builder.getResultConverterName(resultConverter), ci(FromNativeConverter.class));
            mv.swap();
            mv.aconst_null();
            mv.invokeinterface(FromNativeConverter.class, "fromNative",
                    Object.class, Object.class, FromNativeContext.class);
            mv.checkcast(p(resultType.getDeclaredType()));
            mv.areturn();

        } else {
            AsmUtil.emitReturn(mv, resultType.getDeclaredType(), nativeResultClass);
        }
    }

    static void emitPostInvoke(AsmBuilder builder, SkinnyMethodAdapter mv, ParameterType[] parameterTypes,
                               LocalVariable[] parameters, LocalVariable[] converted) {
        for (int i = 0; i < converted.length; ++i) {
            if (converted[i] != null) {
                mv.aload(0);
                mv.getfield(builder.getClassNamePath(), builder.getParameterConverterName(parameterTypes[i].toNativeConverter),
                        ci(ToNativeConverter.class));
                mv.checkcast(PostInvocation.class);
                mv.aload(parameters[i]);
                mv.aload(converted[i]);
                mv.aconst_null();
                mv.invokeinterface(PostInvocation.class, "postInvoke", void.class,
                        Object.class, Object.class, ToNativeContext.class);
            }
        }
    }
}
