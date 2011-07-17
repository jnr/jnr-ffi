package jnr.ffi.provider.jffi;

import com.kenai.jffi.CallingConvention;
import com.kenai.jffi.Function;
import com.kenai.jffi.Platform;
import jnr.ffi.Callable;
import jnr.ffi.NativeLong;
import jnr.ffi.Pointer;
import jnr.ffi.Struct;
import org.objectweb.asm.Label;

import java.lang.annotation.Annotation;
import java.nio.Buffer;
import java.util.concurrent.atomic.AtomicLong;

import static jnr.ffi.provider.jffi.AsmUtil.*;
import static jnr.ffi.provider.jffi.CodegenUtils.*;
import static org.objectweb.asm.Opcodes.*;

/**
 *
 */
class X86MethodGenerator implements MethodGenerator {
    private final AtomicLong nextMethodID = new AtomicLong(0);
    private final StubCompiler compiler;
    private final BufferMethodGenerator bufgen;

    X86MethodGenerator(StubCompiler compiler, BufferMethodGenerator bufgen) {
        this.compiler = compiler;
        this.bufgen = bufgen;
    }

    public boolean isSupported(Signature signature) {
        final Platform platform = Platform.getPlatform();

        if (platform.getOS().equals(Platform.OS.WINDOWS)) {
            return false;
        }

        if (!platform.getCPU().equals(Platform.CPU.I386) && !platform.getCPU().equals(Platform.CPU.X86_64)) {
            return false;
        }

        if (!signature.callingConvention.equals(CallingConvention.DEFAULT)) {
            return false;
        }

        Class[] nativeParameterTypes = new Class[signature.parameterTypes.length];
        for (int i = 0; i < nativeParameterTypes.length; ++i) {
            if (!isSupportedParameter(platform, signature.parameterTypes[i], signature.parameterAnnotations[i])) {
                return false;
            }

            if (Buffer.class.isAssignableFrom(signature.parameterTypes[i])) {
                nativeParameterTypes[i] = AsmUtil.unboxedType(Pointer.class);

            } else {
                nativeParameterTypes[i] = AsmUtil.unboxedType(signature.parameterTypes[i]);
            }
        }

        return isSupportedResult(platform, signature.resultType, signature.resultAnnotations)
            && compiler.canCompile(AsmUtil.unboxedReturnType(signature.resultType), nativeParameterTypes, signature.callingConvention);
    }

    public void generate(AsmBuilder builder, String functionName, Function function, Signature signature) {

        Class[] nativeParameterTypes = new Class[signature.parameterTypes.length];
        boolean unboxing = false;
        boolean ptrCheck = false;
        for (int i = 0; i < signature.parameterTypes.length; ++i) {
            Class parameterType = signature.parameterTypes[i];
            if (Buffer.class.isAssignableFrom(parameterType)) {
                nativeParameterTypes[i] = AsmUtil.unboxedType(Pointer.class);

            } else {
                nativeParameterTypes[i] = AsmUtil.unboxedType(parameterType);
            }
            unboxing |= nativeParameterTypes[i] != parameterType;
            ptrCheck |= Pointer.class.isAssignableFrom(parameterType)
                    || Struct.class.isAssignableFrom(parameterType)
                    || Buffer.class.isAssignableFrom(parameterType);
        }

        Class nativeReturnType = AsmUtil.unboxedReturnType(signature.resultType);
        unboxing |= nativeReturnType != signature.resultType;

        String stubName = functionName + (unboxing || ptrCheck ? "$jni$" + nextMethodID.incrementAndGet() : "");

        // If unboxing of parameters is required, generate a wrapper
        if (unboxing || ptrCheck) {
            SkinnyMethodAdapter mv = new SkinnyMethodAdapter(builder.getClassVisitor().visitMethod(
                    ACC_PUBLIC | ACC_FINAL,
                    functionName, sig(signature.resultType, signature.parameterTypes), null, null));
            mv.start();
            mv.aload(0);

            Label bufferInvocationLabel = AsmLibraryLoader.emitDirectCheck(mv, signature.parameterTypes);

            // Emit the unboxing wrapper
            for (int i = 0, lvar = 1; i < signature.parameterTypes.length; ++i) {
                Class parameterType = signature.parameterTypes[i];
                lvar = AsmLibraryLoader.loadParameter(mv, parameterType, lvar);
                if (parameterType != nativeParameterTypes[i]) {

                    if (Number.class.isAssignableFrom(parameterType)) {
                        unboxNumber(mv, parameterType, nativeParameterTypes[i]);

                    } else if (Boolean.class.isAssignableFrom(parameterType)) {
                        unboxBoolean(mv, parameterType, nativeParameterTypes[i]);

                    } else if (Pointer.class.isAssignableFrom(parameterType)) {
                        unboxPointer(mv, nativeParameterTypes[i]);

                    } else if (Struct.class.isAssignableFrom(parameterType)) {
                        unboxStruct(mv, nativeParameterTypes[i]);

                    } else if (Buffer.class.isAssignableFrom(parameterType)) {
                        unboxBuffer(mv, parameterType, nativeParameterTypes[i]);

                    } else if (Enum.class.isAssignableFrom(parameterType)) {
                        unboxEnum(mv, nativeParameterTypes[i]);
                    }
                }
            }

            // invoke the compiled stub
            mv.invokevirtual(builder.getClassNamePath(), stubName, sig(nativeReturnType, nativeParameterTypes));

            // emitReturn will box the return value if needed
            AsmLibraryLoader.emitReturn(mv, signature.resultType, nativeReturnType);

            if (bufferInvocationLabel != null) {
                // If there was a non-direct pointer in the parameters, need to
                // handle it via a call to the slower buffer invocation
                mv.label(bufferInvocationLabel);

                String bufferFunctionName = functionName + "$buf$" + nextMethodID.incrementAndGet();
                // reload all the parameters
                for (int i = 0, lvar = 1; i < signature.parameterTypes.length; ++i) {
                    lvar = AsmLibraryLoader.loadParameter(mv, signature.parameterTypes[i], lvar);
                }
                mv.invokevirtual(builder.getClassNamePath(), bufferFunctionName, sig(signature.resultType, signature.parameterTypes));
                emitReturnOp(mv, signature.resultType);
                // Now generate the buffer invocation method body
                bufgen.generate(builder, bufferFunctionName, function, signature);
            }
            mv.visitMaxs(100, calculateLocalVariableSpace(signature.parameterTypes) + 10);
            mv.visitEnd();
        }

        builder.getClassVisitor().visitMethod(ACC_PUBLIC | ACC_FINAL | ACC_NATIVE,
                stubName, sig(nativeReturnType, nativeParameterTypes), null, null);


        compiler.compile(function, stubName, nativeReturnType, nativeParameterTypes,
                signature.callingConvention, !signature.ignoreError);
    }

    void attach(Class clazz) {
        compiler.attach(clazz);
    }

    private static boolean isSupportedType(Platform platform, Class type, Annotation[] annotations) {
        return Boolean.class.isAssignableFrom(type) || boolean.class == type
            || Byte.class.isAssignableFrom(type) || byte.class == type
            || Short.class.isAssignableFrom(type) || short.class == type
            || Integer.class.isAssignableFrom(type) || int.class == type
            || Long.class == type || long.class == type
            || NativeLong.class == type
            || Pointer.class.isAssignableFrom(type)
            || Struct.class.isAssignableFrom(type)
            || float.class == type || Float.class == type
            || double.class == type || Double.class == type
            ;
    }

    final static boolean isSupportedResult(Platform platform, Class type, Annotation[] annotations) {
        return isSupportedType(platform, type, annotations)
            || String.class == type
            ;
    }

    final static boolean isSupportedParameter(Platform platform, Class type, Annotation[] annotations) {
        return isSupportedType(platform, type, annotations)
            || Buffer.class.isAssignableFrom(type)
            || Callable.class.isAssignableFrom(type)
            ;
    }
}
