package jnr.ffi.provider.jffi;

import com.kenai.jffi.*;
import jnr.ffi.NativeType;
import jnr.ffi.Pointer;
import org.objectweb.asm.Label;

import java.lang.annotation.Annotation;
import java.nio.*;
import java.util.*;

import static jnr.ffi.provider.jffi.AsmUtil.*;
import static jnr.ffi.provider.jffi.CodegenUtils.ci;
import static jnr.ffi.provider.jffi.CodegenUtils.p;
import static jnr.ffi.provider.jffi.NumberUtil.*;

/**
 *
 */
abstract class AbstractFastNumericMethodGenerator extends BaseMethodGenerator {

    public void generate(final AsmBuilder builder, final SkinnyMethodAdapter mv, LocalVariableAllocator localVariableAllocator, CallContext callContext, final ResultType resultType, final ParameterType[] parameterTypes,
                         boolean ignoreError) {
        // [ stack contains: Invoker, Function ]
         final Class nativeIntType = getInvokerType();
        final LocalVariable objCount = localVariableAllocator.allocate(int.class);
        final LocalVariable[] parameters = AsmUtil.getParameterVariables(parameterTypes);
        LocalVariable[] strategies = new LocalVariable[parameterTypes.length];
        final LocalVariable[] converted = new LocalVariable[parameterTypes.length];
        int pointerCount = 0;

        // Load, convert, and un-box parameters
        for (int i = 0; i < parameterTypes.length; ++i) {
            converted[i] = loadAndConvertParameter(builder, mv, localVariableAllocator, parameters[i], parameterTypes[i]);

            Class javaParameterType = parameterTypes[i].effectiveJavaType();
            ToNativeOp op = ToNativeOp.get(parameterTypes[i]);
            if (op != null && op.isPrimitive()) {
                op.emitPrimitive(mv, getInvokerType(), parameterTypes[i].nativeType);

            } else if (hasPointerParameterStrategy(javaParameterType)) {

                // Initialize the objectCount local var
                if (pointerCount++ < 1) {
                    mv.pushInt(0);
                    mv.istore(objCount);
                }

                emitPointerParameterStrategyLookup(mv, javaParameterType, parameterTypes[i].annotations());

                mv.astore(strategies[i] = localVariableAllocator.allocate(ObjectParameterStrategy.class));
                mv.aload(strategies[i]);

                mv.getfield(p(PointerParameterStrategy.class), "objectCount", ci(int.class));
                mv.iload(objCount);
                mv.iadd();
                mv.istore(objCount);

                if (javaParameterType.isArray() && javaParameterType.getComponentType().isPrimitive()) {
                    // heap objects are always address == 0, no need to call getAddress()
                    if (int.class == nativeIntType) mv.iconst_0(); else mv.lconst_0();
                } else {
                    // Get the native address (will return zero for heap objects)
                    mv.aload(strategies[i]);
                    mv.aload(converted[i]);
                    mv.invokevirtual(PointerParameterStrategy.class, "address", long.class, Object.class);
                    narrow(mv, long.class, nativeIntType);
                }

            } else {
                throw new IllegalArgumentException("unsupported parameter type " + parameterTypes[i].getDeclaredType());
            }
        }

        // stack now contains [ IntInvoker, Function, int/long args ]
        Label hasObjects = new Label();
        Label convertResult = new Label();
        if (pointerCount > 0) {
            mv.iload(objCount);
            mv.ifne(hasObjects);
        }
        mv.invokevirtual(p(com.kenai.jffi.Invoker.class),
                getInvokerMethodName(resultType, parameterTypes, ignoreError),
                getInvokerSignature(parameterTypes.length, nativeIntType));

        if (pointerCount > 0) mv.label(convertResult);

        Class javaReturnType = resultType.effectiveJavaType();
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
        final Class unboxedResultType = unboxedReturnType(javaReturnType);
        convertPrimitive(mv, nativeReturnType, unboxedResultType, resultType.nativeType);
        emitEpilogue(builder, mv, resultType, parameterTypes, parameters, converted, null);

        /* --  method returns above - below is an alternative path -- */

        // Now implement heap object support
        if (pointerCount > 0) {
            mv.label(hasObjects);

            if (int.class == nativeIntType) {
                // For int invoker, need to convert all the int args to long
                LocalVariable[] tmp = new LocalVariable[parameterTypes.length];
                for (int i = parameterTypes.length - 1; i > 0; i--) {
                    tmp[i] = localVariableAllocator.allocate(int.class);
                    mv.istore(tmp[i]);
                }
                if (parameterTypes.length > 0) mv.i2l();
                // Now reload them back onto the parameter stack, and widen to long
                for (int i = 1; i < parameterTypes.length; i++) {
                    mv.iload(tmp[i]);
                    mv.i2l();
                }
            }

            mv.iload(objCount);
            // Need to load all the converters onto the stack
            for (int i = 0; i < parameterTypes.length; i++) {
                if (strategies[i] != null) {
                    mv.aload(converted[i]);
                    mv.aload(strategies[i]);
                    mv.aload(0);

                    ObjectParameterInfo info = ObjectParameterInfo.create(i,
                            AsmUtil.getNativeArrayFlags(parameterTypes[i].annotations()));

                    mv.getfield(builder.getClassNamePath(), builder.getObjectParameterInfoName(info),
                            ci(ObjectParameterInfo.class));
                }
            }
            mv.invokevirtual(p(com.kenai.jffi.Invoker.class),
                    getObjectParameterMethodName(parameterTypes.length),
                    getObjectParameterMethodSignature(parameterTypes.length, pointerCount));
            narrow(mv, long.class, nativeIntType);
            mv.go_to(convertResult);
        }
    }


    @SuppressWarnings("unchecked")
    static final Set<Class> pointerTypes = Collections.unmodifiableSet(new LinkedHashSet<Class>(Arrays.asList(
        Pointer.class,
        ByteBuffer.class, CharBuffer.class, ShortBuffer.class, IntBuffer.class, LongBuffer.class, FloatBuffer.class, DoubleBuffer.class,
        Buffer.class, // make sure Buffer is after the more specific (faster execution path) checks
        CharSequence.class,
        byte[].class, short[].class, char[].class, int[].class, long[].class, float[].class, double[].class, boolean[].class
    )));

    @SuppressWarnings("unchecked")
    static boolean hasPointerParameterStrategy(Class javaType) {
        for (Class c : pointerTypes) {
            if (c.isAssignableFrom(javaType)) {
                // FIXME: replace special handling for 32/64bit arrays with more generic elsewhere
                return !(LongBuffer.class.isAssignableFrom(javaType) || long[].class == javaType) || sizeof(NativeType.SLONG) == 8;
            }
        }

        return true;

    }

    static void emitPointerParameterStrategyLookup(SkinnyMethodAdapter mv, Class javaParameterType, Collection<Annotation> annotations) {
        boolean converted = false;
        for (Class c : pointerTypes) {
            if (c.isAssignableFrom(javaParameterType)) {
                mv.invokestatic(AsmRuntime.class, "pointerParameterStrategy", PointerParameterStrategy.class, c);
                converted = true;
                break;
            }
        }

        if (!converted) {
            throw new RuntimeException("no conversion strategy for " + javaParameterType);
        }
    }

    static String getObjectParameterMethodName(int parameterCount) {
        return "invokeN" + parameterCount;
    }

    static String getObjectParameterMethodSignature(int parameterCount, int pointerCount) {
        StringBuilder sb = new StringBuilder();
        sb.append('(').append(ci(CallContext.class)).append(ci(long.class));
        for (int i = 0; i < parameterCount; i++) {
            sb.append('J');
        }
        sb.append('I'); // objCount
        for (int n = 0; n < pointerCount; n++) {
            sb.append(ci(Object.class));
            sb.append(ci(ObjectParameterStrategy.class));
            sb.append(ci(ObjectParameterInfo.class));
        }
        sb.append(")J");
        return sb.toString();
    }

    abstract String getInvokerMethodName(ResultType resultType, ParameterType[] parameterTypes,
                                         boolean ignoreErrno);

    abstract String getInvokerSignature(int parameterCount, Class nativeIntType);
    abstract Class getInvokerType();
}
