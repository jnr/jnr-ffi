package jnr.ffi.provider.jffi;

import com.kenai.jffi.CallContext;
import com.kenai.jffi.CallingConvention;
import com.kenai.jffi.Function;
import jnr.ffi.mapper.FromNativeContext;
import jnr.ffi.mapper.FromNativeConverter;
import jnr.ffi.mapper.ToNativeContext;
import jnr.ffi.mapper.ToNativeConverter;

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
            javaParameterTypes[i] = parameterTypes[i].javaType;
        }

        SkinnyMethodAdapter mv = new SkinnyMethodAdapter(builder.getClassVisitor().visitMethod(ACC_PUBLIC | ACC_FINAL,
                functionName,
                sig(resultType.javaType, javaParameterTypes), null, null));
        mv.start();

        // Retrieve the static 'ffi' Invoker instance
        mv.getstatic(p(AbstractAsmLibraryInterface.class), "ffi", ci(com.kenai.jffi.Invoker.class));

        // retrieve the call context and function address
        mv.aload(0);
        mv.getfield(builder.getClassNamePath(), builder.getCallContextFieldName(function), ci(CallContext.class));

        mv.aload(0);
        mv.getfield(builder.getClassNamePath(), builder.getFunctionAddressFieldName(function), ci(long.class));


        generate(builder, mv, function, resultType, parameterTypes, ignoreError);

        mv.visitMaxs(100, calculateLocalVariableSpace(parameterTypes) + 10);
        mv.visitEnd();
    }

    abstract void generate(AsmBuilder builder, SkinnyMethodAdapter mv, Function function, ResultType resultType, ParameterType[] parameterTypes,
                           boolean ignoreError);

    static int loadAndConvertParameter(AsmBuilder builder, SkinnyMethodAdapter mv, int lvar,
                                ParameterType parameterType) {
        ToNativeConverter parameterConverter = parameterType.toNativeConverter;
        if (parameterConverter != null) {
            mv.aload(0);
            mv.getfield(builder.getClassNamePath(), builder.getParameterConverterName(parameterConverter), ci(ToNativeConverter.class));
        }
        lvar = AsmLibraryLoader.loadParameter(mv, parameterType.javaType, lvar);
        if (parameterConverter != null) {
            if (parameterType.javaType.isPrimitive()) {
                boxValue(mv, getBoxedClass(parameterType.javaType), parameterType.javaType, parameterType.jffiType);
            }
            mv.aconst_null();
            mv.invokeinterface(ToNativeConverter.class, "toNative",
                    Object.class, Object.class, ToNativeContext.class);
            mv.checkcast(p(parameterConverter.nativeType()));
        }

        return lvar;
    }

    static void convertAndReturnResult(AsmBuilder builder, SkinnyMethodAdapter mv, ResultType resultType, Class nativeReturnType) {
        // If there is a result converter, retrieve it and put on the stack
        FromNativeConverter resultConverter = resultType.fromNativeConverter;
        if (resultConverter != null) {
            boxValue(mv, resultConverter.nativeType(), nativeReturnType, resultType.jffiType);

            mv.aload(0);
            mv.getfield(builder.getClassNamePath(), builder.getResultConverterName(resultConverter), ci(FromNativeConverter.class));
            mv.swap();
            mv.aconst_null();
            mv.invokeinterface(FromNativeConverter.class, "fromNative",
                    Object.class, Object.class, FromNativeContext.class);
            mv.checkcast(p(resultType.javaType));
            mv.areturn();

        } else {
            AsmLibraryLoader.emitReturn(mv, resultType.javaType, nativeReturnType, resultType.jffiType);
        }
    }
}
