package jnr.ffi.provider.jffi;

import jnr.ffi.Pointer;
import jnr.ffi.struct.Struct;
import org.objectweb.asm.Label;

import java.lang.annotation.Annotation;

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

    public void generate(SkinnyMethodAdapter mv, Class returnType, Annotation[] resultAnnotations,
            Class[] parameterTypes, Annotation[][] parameterAnnotations, boolean ignoreErrno) {
        // [ stack contains: Invoker, Function ]

        //mv = new SkinnyMethodAdapter(AsmUtil.newTraceMethodVisitor(mv));
        Label bufferInvocationLabel = AsmLibraryLoader.emitDirectCheck(mv, parameterTypes);
        final Class nativeIntType = getInvokerType();

        // Load and un-box parameters
        for (int i = 0, lvar = 1; i < parameterTypes.length; ++i) {
            lvar = AsmLibraryLoader.loadParameter(mv, parameterTypes[i], lvar);
            final Class parameterType = parameterTypes[i];

            if (Float.class == parameterType || float.class == parameterType) {
                mv.invokestatic(Float.class, "floatToRawIntBits", int.class, float.class);
                widen(mv, int.class, nativeIntType);

            } else if (Double.class == parameterType || double.class == parameterType) {
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

            } else {
                throw new IllegalArgumentException("unsupported numeric type " + parameterType);
            }


        }

        // stack now contains [ IntInvoker, Function, int/long args ]
        mv.invokevirtual(p(com.kenai.jffi.Invoker.class),
                getInvokerMethodName(returnType, resultAnnotations, parameterTypes, parameterAnnotations, ignoreErrno),
                getInvokerSignature(parameterTypes.length, nativeIntType));

        // Convert the result from long/int to the correct return type
        if (Float.class == returnType || float.class == returnType) {
            narrow(mv, nativeIntType, int.class);
            mv.invokestatic(Float.class, "intBitsToFloat", float.class, int.class);

        } else if (Double.class == returnType || double.class == returnType) {
            widen(mv, nativeIntType, long.class);
            mv.invokestatic(Double.class, "longBitsToDouble", double.class, long.class);

        }

        // emitReturn will box or narrow/widen the return value if needed
        AsmLibraryLoader.emitReturn(mv, returnType, nativeIntType);

        if (bufferInvocationLabel != null) {
            // Now emit the alternate path for any parameters that might require it
            mv.label(bufferInvocationLabel);
            bufgen.generate(mv, returnType, resultAnnotations, parameterTypes, parameterAnnotations, ignoreErrno);
        }
    }

    abstract String getInvokerMethodName(Class returnType,
            Annotation[] resultAnnotations, Class[] parameterTypes, Annotation[][] parameterAnnotations,
            boolean ignoreErrno);

    abstract String getInvokerSignature(int parameterCount, Class nativeIntType);
    abstract Class getInvokerType();
}
