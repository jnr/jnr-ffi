package jnr.ffi.provider.jffi;

import jnr.ffi.Pointer;
import jnr.ffi.Struct;
import org.objectweb.asm.Label;

import java.lang.annotation.Annotation;
import java.nio.Buffer;

import static jnr.ffi.provider.jffi.AsmUtil.*;
import static jnr.ffi.provider.jffi.CodegenUtils.p;
import static jnr.ffi.provider.jffi.NumberUtil.narrow;
import static jnr.ffi.provider.jffi.NumberUtil.widen;

/**
 *
 */
abstract class AbstractFastNumericMethodGenerator extends BaseMethodGenerator {
    private final BufferMethodGenerator bufgen;

    public AbstractFastNumericMethodGenerator(BufferMethodGenerator bufgen) {
        this.bufgen = bufgen;
    }

    public void generate(SkinnyMethodAdapter mv, Signature signature) {
        // [ stack contains: Invoker, Function ]

        //mv = new SkinnyMethodAdapter(AsmUtil.newTraceMethodVisitor(mv));
        Label bufferInvocationLabel = AsmLibraryLoader.emitDirectCheck(mv, signature.parameterTypes);
        final Class nativeIntType = getInvokerType();

        // Load and un-box parameters
        for (int i = 0, lvar = 1; i < signature.parameterTypes.length; ++i) {
            final Class parameterType = signature.parameterTypes[i];
            lvar = AsmLibraryLoader.loadParameter(mv, parameterType, lvar);

            if (Float.class == parameterType || float.class == parameterType) {
                if (!parameterType.isPrimitive()) {
                    unboxNumber(mv, parameterType, float.class);
                }
                mv.invokestatic(Float.class, "floatToRawIntBits", int.class, float.class);
                widen(mv, int.class, nativeIntType);

            } else if (Double.class == parameterType || double.class == parameterType) {
                if (!parameterType.isPrimitive()) {
                    unboxNumber(mv, parameterType, double.class);
                }
                mv.invokestatic(Double.class, "doubleToRawLongBits", long.class, double.class);

            } else if (parameterType.isPrimitive()) {
                // widen to long or narrow to int as needed
                widen(mv, parameterType, nativeIntType);
                narrow(mv, parameterType, nativeIntType);

            } else if (Number.class.isAssignableFrom(parameterType)) {
                unboxNumber(mv, parameterType, nativeIntType);

            } else if (Boolean.class.isAssignableFrom(parameterType)) {
                unboxBoolean(mv, parameterType, nativeIntType);

            } else if (Pointer.class.isAssignableFrom(parameterType)) {
                unboxPointer(mv, nativeIntType);

            } else if (Struct.class.isAssignableFrom(parameterType)) {
                unboxStruct(mv, nativeIntType);

            } else if (Buffer.class.isAssignableFrom(parameterType)) {
                unboxBuffer(mv, parameterType, nativeIntType);

            } else {
                throw new IllegalArgumentException("unsupported numeric type " + parameterType);
            }


        }

        // stack now contains [ IntInvoker, Function, int/long args ]
        mv.invokevirtual(p(com.kenai.jffi.Invoker.class),
                getInvokerMethodName(signature.resultType, signature.resultAnnotations, signature.parameterTypes, signature.parameterAnnotations, signature.ignoreError),
                getInvokerSignature(signature.parameterTypes.length, nativeIntType));

        // Convert the result from long/int to the correct return type
        if (Float.class == signature.resultType || float.class == signature.resultType) {
            narrow(mv, nativeIntType, int.class);
            mv.invokestatic(Float.class, "intBitsToFloat", float.class, int.class);

        } else if (Double.class == signature.resultType || double.class == signature.resultType) {
            widen(mv, nativeIntType, long.class);
            mv.invokestatic(Double.class, "longBitsToDouble", double.class, long.class);

        }

        // emitReturn will box or narrow/widen the return value if needed
        AsmLibraryLoader.emitReturn(mv, signature.resultType, nativeIntType);

        if (bufferInvocationLabel != null) {
            // Now emit the alternate path for any parameters that might require it
            mv.label(bufferInvocationLabel);
            bufgen.generate(mv, signature);
        }
    }

    abstract String getInvokerMethodName(Class returnType,
            Annotation[] resultAnnotations, Class[] parameterTypes, Annotation[][] parameterAnnotations,
            boolean ignoreErrno);

    abstract String getInvokerSignature(int parameterCount, Class nativeIntType);
    abstract Class getInvokerType();
}
