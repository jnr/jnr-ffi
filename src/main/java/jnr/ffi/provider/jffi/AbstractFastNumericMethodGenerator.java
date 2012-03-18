package jnr.ffi.provider.jffi;

import jnr.ffi.Pointer;
import jnr.ffi.Struct;
import jnr.ffi.mapper.FromNativeConverter;
import jnr.ffi.mapper.ToNativeConverter;
import org.objectweb.asm.Label;

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

    private void emitNumericParameter(SkinnyMethodAdapter mv, final Class javaType, com.kenai.jffi.Type jffiType) {
        final Class nativeIntType = getInvokerType();

        if (Float.class == javaType || float.class == javaType) {
            if (!javaType.isPrimitive()) {
                unboxNumber(mv, javaType, float.class);
            }
            mv.invokestatic(Float.class, "floatToRawIntBits", int.class, float.class);
            widen(mv, int.class, nativeIntType);

        } else if (Double.class == javaType || double.class == javaType) {
            if (!javaType.isPrimitive()) {
                unboxNumber(mv, javaType, double.class);
            }
            mv.invokestatic(Double.class, "doubleToRawLongBits", long.class, double.class);

        } else if (javaType.isPrimitive()) {
            NumberUtil.convertPrimitive(mv, javaType, nativeIntType, jffiType);

        } else if (Number.class.isAssignableFrom(javaType)) {
            unboxNumber(mv, javaType, nativeIntType, jffiType);

        } else if (Boolean.class.isAssignableFrom(javaType)) {
            unboxBoolean(mv, javaType, nativeIntType);

        } else if (Pointer.class.isAssignableFrom(javaType)) {
            unboxPointer(mv, nativeIntType);

        } else if (Struct.class.isAssignableFrom(javaType)) {
            unboxStruct(mv, nativeIntType);

        } else if (Buffer.class.isAssignableFrom(javaType)) {
            unboxBuffer(mv, javaType, nativeIntType);

        } else {
            throw new IllegalArgumentException("unsupported numeric type " + javaType);
        }
    }

    public void generate(AsmBuilder builder, SkinnyMethodAdapter mv, ResultType resultType, ParameterType[] parameterTypes,
                         boolean ignoreError) {
// [ stack contains: Invoker, Function ]

        mv = new SkinnyMethodAdapter(AsmUtil.newTraceMethodVisitor(mv));
        Label bufferInvocationLabel = AsmLibraryLoader.emitDirectCheck(mv, parameterTypes);
        final Class nativeIntType = getInvokerType();

        // Load, convert, and un-box parameters
        for (int i = 0, lvar = 1; i < parameterTypes.length; ++i) {
            ParameterType parameterType = parameterTypes[i];

            lvar = loadAndConvertParameter(builder, mv, lvar, parameterType);
            ToNativeConverter parameterConverter = parameterTypes[i].toNativeConverter;
            Class javaParameterType = parameterConverter != null
                    ? parameterConverter.nativeType() : parameterTypes[i].javaType;
            emitNumericParameter(mv, javaParameterType, parameterType.jffiType);
        }

        // stack now contains [ IntInvoker, Function, int/long args ]
        mv.invokevirtual(p(com.kenai.jffi.Invoker.class),
                getInvokerMethodName(resultType, parameterTypes, ignoreError),
                getInvokerSignature(parameterTypes.length, nativeIntType));

        FromNativeConverter resultConverter = resultType.fromNativeConverter;
        Class javaReturnType = resultConverter != null
                ? resultConverter.nativeType() : resultType.javaType;
        Class nativeReturnType = nativeIntType;

        // Convert the result from long/int to the correct return type
        if (Float.class == javaReturnType || float.class == javaReturnType) {
            narrow(mv, nativeIntType, int.class);
            mv.invokestatic(Float.class, "intBitsToFloat", float.class, int.class);
            nativeReturnType = float.class;

        } else if (Double.class == javaReturnType || double.class == javaReturnType) {
            widen(mv, nativeIntType, long.class);
            mv.invokestatic(Double.class, "longBitsToDouble", double.class, long.class);
            nativeReturnType = double.class;

        }

        // box and/or narrow/widen the return value if needed
        convertAndReturnResult(builder, mv, resultType, nativeReturnType);

        if (bufferInvocationLabel != null) {
            // Now emit the alternate path for any parameters that might require it
            mv.label(bufferInvocationLabel);
            bufgen.generateBufferInvocation(builder, mv, resultType, parameterTypes);
        }
    }

    abstract String getInvokerMethodName(ResultType resultType, ParameterType[] parameterTypes,
                                         boolean ignoreErrno);

    abstract String getInvokerSignature(int parameterCount, Class nativeIntType);
    abstract Class getInvokerType();

    static boolean getBooleanProperty(String propertyName, boolean defaultValue) {
        return Boolean.valueOf(System.getProperty(propertyName, Boolean.valueOf(defaultValue).toString()));
    }
}
