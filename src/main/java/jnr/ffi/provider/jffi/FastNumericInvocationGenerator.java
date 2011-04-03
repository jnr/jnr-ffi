package jnr.ffi.provider.jffi;

import com.kenai.jffi.CallingConvention;
import com.kenai.jffi.Function;
import com.kenai.jffi.Platform;
import jnr.ffi.NativeLong;
import jnr.ffi.Pointer;
import jnr.ffi.struct.Struct;
import org.objectweb.asm.Label;

import java.lang.annotation.Annotation;

import static jnr.ffi.provider.jffi.AsmUtil.*;
import static jnr.ffi.provider.jffi.CodegenUtils.ci;
import static jnr.ffi.provider.jffi.CodegenUtils.p;
import static jnr.ffi.provider.jffi.NumberUtil.isLong64;
import static jnr.ffi.provider.jffi.NumberUtil.widen;

/**
 *
 */
public class FastNumericInvocationGenerator extends BaseMethodGenerator {
    private final BufferInvocationGenerator bufgen;

    public FastNumericInvocationGenerator(BufferInvocationGenerator bufgen) {
        this.bufgen = bufgen;
    }

    static final boolean FAST_NUMERIC_AVAILABLE = isFastNumericAvailable();

    public boolean isSupported(Class returnType, Annotation[] resultAnnotations, Class[] parameterTypes, Annotation[][] parameterAnnotations,
                               CallingConvention convention) {
        return convention == CallingConvention.DEFAULT &&
                isFastNumericMethod(returnType, resultAnnotations, parameterTypes, parameterAnnotations);
    }

    public void generate(SkinnyMethodAdapter mv, Class returnType, Annotation[] resultAnnotations, Class[] parameterTypes, Annotation[][] parameterAnnotations, boolean ignoreError) {
        generateFastNumericInvocation(mv, returnType, resultAnnotations, parameterTypes, parameterAnnotations, ignoreError);
    }

    private void generateFastNumericInvocation(SkinnyMethodAdapter mv, Class returnType,
                                                    Annotation[] resultAnnotations, Class[] parameterTypes, Annotation[][] parameterAnnotations, boolean ignoreErrno) {
        // [ stack contains: Invoker, Function ]

        Label bufferInvocationLabel = AsmLibraryLoader.emitDirectCheck(mv, parameterTypes);
        Class nativeIntType = AsmLibraryLoader.getMinimumIntType(returnType, resultAnnotations, parameterTypes, parameterAnnotations);

        // Emit fast-numeric invocation
        for (int i = 0, lvar = 1; i < parameterTypes.length; ++i) {
            lvar = AsmLibraryLoader.loadParameter(mv, parameterTypes[i], lvar);

            if (parameterTypes[i].isPrimitive()) {
                // widen to long if needed
                widen(mv, parameterTypes[i], nativeIntType);

            } else if (Number.class.isAssignableFrom(parameterTypes[i])) {
                unboxNumber(mv, parameterTypes[i], nativeIntType);

            } else if (Boolean.class.isAssignableFrom(parameterTypes[i])) {
                unboxBoolean(mv, parameterTypes[i], nativeIntType);

            } else if (Enum.class.isAssignableFrom(parameterTypes[i])) {
                unboxEnum(mv, nativeIntType);

            } else if (Pointer.class.isAssignableFrom(parameterTypes[i])) {
                unboxPointer(mv, nativeIntType);

            } else if (Struct.class.isAssignableFrom(parameterTypes[i])) {
                unboxStruct(mv, nativeIntType);

            }

            if (Float.class == parameterTypes[i] || float.class == parameterTypes[i]) {
                mv.invokestatic(Float.class, "floatToRawIntBits", int.class, float.class);
                mv.i2l();

            } else if (Double.class == parameterTypes[i] || double.class == parameterTypes[i]) {
                mv.invokestatic(Double.class, "doubleToRawLongBits", long.class, double.class);

            }
        }

        // stack now contains [ IntInvoker, Function, int args ]
        mv.invokevirtual(p(com.kenai.jffi.Invoker.class),
                getFastNumericInvokerMethodName(returnType, resultAnnotations,
                        parameterTypes, parameterAnnotations, ignoreErrno),
                getFastNumericInvokerSignature(parameterTypes.length, nativeIntType));

        // Convert the result from long to the correct return type
        if (Float.class == returnType || float.class == returnType) {
            mv.l2i();
            mv.invokestatic(Float.class, "intBitsToFloat", float.class, int.class);

        } else if (Double.class == returnType || double.class == returnType) {
            mv.invokestatic(Double.class, "longBitsToDouble", double.class, long.class);
        }

        AsmLibraryLoader.emitReturn(mv, returnType, nativeIntType);

        if (bufferInvocationLabel != null) {
            // Now emit the alternate path for any parameters that might require it
            mv.label(bufferInvocationLabel);
            bufgen.generate(mv, returnType, resultAnnotations, parameterTypes, parameterAnnotations, ignoreErrno);
        }
    }

    static final String getFastNumericInvokerMethodName(Class returnType,
            Annotation[] resultAnnotations, Class[] parameterTypes, Annotation[][] parameterAnnotations,
            boolean ignoreErrno) {

        StringBuilder sb = new StringBuilder("invoke");

        boolean p32 = false, p64 = false, n = false;
        for (int i = 0; i < parameterTypes.length; ++i) {
            final Class t = parameterTypes[i];
            if (AsmLibraryLoader.isInt32Param(t, parameterAnnotations[i])) {
                p32 = true;

            } else if (isLong64(t, parameterAnnotations[i])
                    || (Platform.getPlatform().addressSize() == 64 && AsmLibraryLoader.isPointerParam(t))) {
                p64 = true;

            } else {
                n = true;
                break;
            }
        }

        n |= !AsmLibraryLoader.isFastIntegerResult(returnType, resultAnnotations);

        final boolean r32 = AsmLibraryLoader.isInt32Result(returnType, resultAnnotations);
        char numericType;

        if (r32 && !p64 && !n) {
            // all 32 bit integer params with a 32bit integer result - use the int path
            numericType = 'I';

        } else if (!r32 && !n && (!p32 || Platform.getPlatform().addressSize() == 64)) {
            // A call that is 64bit result with all 64bit params, or it is a 64bit
            // machine where 32bit params will promote, then use the fast-long invoker
            numericType = 'L';

        } else {
            // Default to the numeric path, which is a bit slower, but can handle anything
            numericType = 'N';
        }

        if (ignoreErrno && numericType == 'I') {
            sb.append("NoErrno");
        }

        if (parameterTypes.length < 1) {
            sb.append("V");
        } else for (int i = 0; i < parameterTypes.length; ++i) {
            sb.append(numericType);
        }

        return sb.append("r").append(numericType).toString();
    }

    static final String getFastNumericInvokerSignature(int parameterCount, Class nativeIntType) {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(ci(Function.class));
        final char iType = nativeIntType == int.class ? 'I' : 'J';
        for (int i = 0; i < parameterCount; ++i) {
            sb.append(iType);
        }
        sb.append(")").append(iType);

        return sb.toString();
    }

    final static boolean isFastNumericResult(Class type, Annotation[] annotations) {
        return AsmLibraryLoader.isFastIntegerResult(type, annotations)
                || Long.class.isAssignableFrom(type) || long.class == type
                || NativeLong.class.isAssignableFrom(type)
                || AsmLibraryLoader.isPointerResult(type)
                || float.class == type || Float.class == type
                || double.class == type || Double.class == type;
    }

    final static boolean isFastNumericParam(Class type, Annotation[] annotations) {
        return AsmLibraryLoader.isFastIntegerParam(type, annotations)
                || Long.class.isAssignableFrom(type) || long.class == type
                || NativeLong.class.isAssignableFrom(type)
                || Pointer.class.isAssignableFrom(type)
                || Struct.class.isAssignableFrom(type)
                || float.class == type || Float.class == type
                || double.class == type || Double.class == type;
    }

    final static boolean isFastNumericAvailable() {
        try {
            com.kenai.jffi.Invoker.class.getDeclaredMethod("invokeNNNNNNrN", Function.class, long.class, long.class, long.class, long.class, long.class, long.class);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    final static boolean isFastNumericMethod(Class returnType, Annotation[] resultAnnotations,
            Class[] parameterTypes, Annotation[][] parameterAnnotations) {

        if (!FAST_NUMERIC_AVAILABLE || parameterTypes.length > 6) {
            return false;
        }

        if (!isFastNumericResult(returnType, resultAnnotations)) {
            return false;
        }

        for (int i = 0; i < parameterTypes.length; ++i) {
            if (!isFastNumericParam(parameterTypes[i], parameterAnnotations[i])) {
                return false;
            }
        }

        return Platform.getPlatform().getCPU() == Platform.CPU.I386
                || Platform.getPlatform().getCPU() == Platform.CPU.X86_64;
    }
}
