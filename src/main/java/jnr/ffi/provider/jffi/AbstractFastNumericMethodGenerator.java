package jnr.ffi.provider.jffi;

import com.kenai.jffi.*;
import jnr.ffi.NativeType;
import jnr.ffi.Pointer;
import jnr.ffi.Struct;
import jnr.ffi.mapper.ToNativeConverter;
import jnr.ffi.provider.ParameterFlags;
import org.objectweb.asm.Label;

import java.lang.annotation.Annotation;
import java.nio.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import static jnr.ffi.provider.jffi.AsmUtil.*;
import static jnr.ffi.provider.jffi.CodegenUtils.ci;
import static jnr.ffi.provider.jffi.CodegenUtils.p;
import static jnr.ffi.provider.jffi.NumberUtil.*;

/**
 *
 */
abstract class AbstractFastNumericMethodGenerator extends BaseMethodGenerator {
    private final BufferMethodGenerator bufgen;

    public AbstractFastNumericMethodGenerator(BufferMethodGenerator bufgen) {
        this.bufgen = bufgen;
    }

    private void emitNumericParameter(SkinnyMethodAdapter mv, final Class javaType, NativeType nativeType) {
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
            NumberUtil.convertPrimitive(mv, javaType, nativeIntType, nativeType);

        } else if (Number.class.isAssignableFrom(javaType)) {
            unboxNumber(mv, javaType, nativeIntType, nativeType);

        } else if (Boolean.class.isAssignableFrom(javaType)) {
            unboxBoolean(mv, javaType, nativeIntType);

        } else {
            throw new IllegalArgumentException("unsupported numeric type " + javaType);
        }
    }

    public void generate(AsmBuilder builder, SkinnyMethodAdapter mv, LocalVariableAllocator localVariableAllocator, Function function, ResultType resultType, ParameterType[] parameterTypes,
                         boolean ignoreError) {
// [ stack contains: Invoker, Function ]
         final Class nativeIntType = getInvokerType();
        final LocalVariable objCount = localVariableAllocator.allocate(int.class);
        LocalVariable[] parameters = AsmUtil.getParameterVariables(parameterTypes);
        LocalVariable[] pointers = new LocalVariable[parameterTypes.length];
        LocalVariable[] strategies = new LocalVariable[parameterTypes.length];
        LocalVariable[] converted = new LocalVariable[parameterTypes.length];
        ObjectParameterInfo[] objectParameterInfo = new ObjectParameterInfo[parameterTypes.length];
        int pointerCount = 0;

        // Load, convert, and un-box parameters
        for (int i = 0; i < parameterTypes.length; ++i) {
            loadAndConvertParameter(builder, mv, parameters[i], parameterTypes[i]);
            if (parameterTypes[i].toNativeConverter instanceof ToNativeConverter.PostInvocation) {
                mv.dup();
                mv.astore(converted[i] = localVariableAllocator.allocate(Object.class));
            }

            Class javaParameterType = parameterTypes[i].effectiveJavaType();

            if (Pointer.class.isAssignableFrom(javaParameterType) && isDelegate(parameterTypes[i])) {
                // delegates are always direct, so handle without the strategy processing
                unboxPointer(mv, nativeIntType);

            } else if (Pointer.class.isAssignableFrom(javaParameterType)
                    || Struct.class.isAssignableFrom(javaParameterType)
                    || String.class == javaParameterType
                    || CharSequence.class == javaParameterType
                    || ByteBuffer.class.isAssignableFrom(javaParameterType)
                    || ShortBuffer.class.isAssignableFrom(javaParameterType)
                    || IntBuffer.class.isAssignableFrom(javaParameterType)
                    || (LongBuffer.class.isAssignableFrom(javaParameterType) && sizeof(NativeType.SLONG) == 8)
                    || FloatBuffer.class.isAssignableFrom(javaParameterType)
                    || DoubleBuffer.class.isAssignableFrom(javaParameterType)
                    || byte[].class == javaParameterType
                    || short[].class == javaParameterType
                    || int[].class == javaParameterType
                    || (long[].class == javaParameterType && sizeof(NativeType.SLONG) == 8)
                    || float[].class == javaParameterType
                    || double[].class == javaParameterType
                    ) {

                // Initialize the objectCount local var
                if (pointerCount++ < 1) {
                    mv.pushInt(0);
                    mv.istore(objCount);
                }

                if (parameterTypes[i].toNativeConverter != null) {
                    // Save the current pointer parameter (result of type conversion above)
                    pointers[i] = localVariableAllocator.allocate(Object.class);
                    mv.astore(pointers[i]);
                    mv.aload(pointers[i]);
                } else {
                    // avoid the save/load of an extra local var if no parameter conversion took place
                    pointers[i] = parameters[i];
                }

                emitPointerParameterStrategyLookup(mv, javaParameterType, parameterTypes[i].annotations);

                strategies[i] = localVariableAllocator.allocate(ObjectParameterStrategy.class);
                mv.astore(strategies[i]);
                mv.aload(strategies[i]);

                mv.getfield(p(PointerParameterStrategy.class), "objectCount", ci(int.class));
                mv.iload(objCount);
                mv.iadd();
                mv.istore(objCount);

                objectParameterInfo[i] = ObjectParameterInfo.create(i, AsmUtil.getNativeArrayFlags(parameterTypes[i].annotations));

                if (CharSequence.class.isAssignableFrom(javaParameterType)
                    || javaParameterType.isArray() && javaParameterType.getComponentType().isPrimitive()) {
                    // heap objects are always address == 0, no need to call getAddress()
                    if (int.class == nativeIntType) mv.iconst_0(); else mv.lconst_0();
                } else {
                    // Get the native address (will return zero for heap objects)
                    mv.aload(strategies[i]);
                    mv.aload(pointers[i]);
                    mv.invokevirtual(PointerParameterStrategy.class, "address", long.class, Object.class);
                    narrow(mv, long.class, nativeIntType);
                }

            } else {
                emitNumericParameter(mv, javaParameterType, parameterTypes[i].nativeType);
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

        // Update any converted parameters that require it
        emitPostInvoke(builder, mv, parameterTypes, parameters, converted);

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
        Class unboxedResultType = unboxedReturnType(javaReturnType);
        convertPrimitive(mv, nativeReturnType, unboxedResultType, resultType.nativeType);
        convertAndReturnResult(builder, mv, resultType, unboxedResultType);

        /* --  method returns above - below is an alternative path -- */

        // Now implement heap object support
        if (pointerCount > 0) {
            final int MAX_OBJECT_COUNT = 3;
            mv.label(hasObjects);
            Label bufferInvocation = new Label();

            if (pointerCount > MAX_OBJECT_COUNT) {
                mv.iload(objCount);
                mv.pushInt(MAX_OBJECT_COUNT);
                mv.if_icmpgt(bufferInvocation);
            }

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

            if (pointerCount <= MAX_OBJECT_COUNT) {
                mv.iload(objCount);
                // Need to load all the converters onto the stack
                for (int i = 0; i < parameterTypes.length; i++) {
                    if (pointers[i] != null) {
                        mv.aload(pointers[i]);
                        mv.aload(strategies[i]);
                        mv.aload(0);

                        ObjectParameterInfo info = ObjectParameterInfo.create(i,
                                AsmUtil.getNativeArrayFlags(parameterTypes[i].annotations));

                        mv.getfield(builder.getClassNamePath(), builder.getObjectParameterInfoName(info),
                                ci(ObjectParameterInfo.class));
                    }
                }
                mv.invokevirtual(p(com.kenai.jffi.Invoker.class),
                        getObjectParameterMethodName(parameterTypes.length),
                        getObjectParameterMethodSignature(parameterTypes.length, pointerCount));
                narrow(mv, long.class, nativeIntType);

            } else {

                emitMultiplePointerInvoke(builder, mv, localVariableAllocator, parameterTypes, nativeIntType, objCount, pointers, strategies, objectParameterInfo, pointerCount, convertResult, MAX_OBJECT_COUNT);
                mv.label(bufferInvocation);
                emitBufferInvocation(builder, mv, localVariableAllocator, function, resultType, parameterTypes, nativeIntType,
                        objCount, pointers, strategies);
            }
            mv.go_to(convertResult);
        }
    }

    private void emitMultiplePointerInvoke(AsmBuilder builder, SkinnyMethodAdapter mv,
                                           LocalVariableAllocator localVariableAllocator, ParameterType[] parameterTypes,
                                           Class nativeIntType, LocalVariable objCount,
                                           LocalVariable[] pointers, LocalVariable[] strategies,
                                           ObjectParameterInfo[] objectParameterInfo, int pointerCount,
                                           Label convertResult, int MAX_OBJECT_COUNT) {
        int[] pointerIndexes = new int[pointerCount];
        for (int i = 0, j = 0; i < parameterTypes.length; i++) {
            if (strategies[i] != null) {
                pointerIndexes[j++] = i;
            }
        }

        int maxPointerCount = Math.min(MAX_OBJECT_COUNT, pointerCount);
        LocalVariable[] s = new LocalVariable[maxPointerCount];
        LocalVariable[] p = new LocalVariable[maxPointerCount];
        LocalVariable[] o = new LocalVariable[maxPointerCount];

        for (int i = 0; i < maxPointerCount; i++) {
            s[i] = strategies[pointerIndexes[i]];
            p[i] = pointers[pointerIndexes[i]];
            o[i] = localVariableAllocator.allocate(Object.class); mv.aconst_null(); mv.astore(o[i]);
        }


        Label[] prev = null, next = new Label[pointerIndexes.length];
        Label done = new Label();
        for (int i = 0; i < maxPointerCount; i++) {
            for (int pointerIndex = i; pointerIndex < pointerIndexes.length; pointerIndex++) {
                Label testNextPointer = new Label();

                if (prev != null && prev[pointerIndex] != null) mv.label(prev[pointerIndex]);
                if (i > 0) {
                    mv.iload(objCount);
                    mv.pushInt(i + 1);
                    mv.if_icmplt(done);
                }

                int parameterIndex = pointerIndexes[pointerIndex];

                mv.aload(strategies[parameterIndex]);
                mv.invokevirtual(ObjectParameterStrategy.class, "isDirect", boolean.class);
                mv.iftrue(testNextPointer);

                if (!p[i].equals(pointers[parameterIndex])) {
                    mv.aload(pointers[parameterIndex]);
                    mv.astore(p[i]);
                }

                if (!s[i].equals(strategies[parameterIndex])) {
                    mv.aload(strategies[parameterIndex]);
                    mv.astore(s[i]);
                }

                // Store the object info for this pointer param
                mv.aload(0);
                mv.getfield(builder.getClassNamePath(),
                        builder.getObjectParameterInfoName(objectParameterInfo[parameterIndex]),
                        ci(ObjectParameterInfo.class));

                mv.astore(o[i]);

                if ((i < maxPointerCount - 1 && pointerIndex < pointerIndexes.length - 1)) {
                    next[pointerIndex + 1] = new Label();
                    mv.go_to(next[pointerIndex + 1]);

                } else {
                    mv.go_to(done);
                }
                mv.label(testNextPointer);
            }
            prev = next;
            next = new Label[pointerIndexes.length];
        }
        mv.label(done);

        // Now load the extra args onto the stack
        mv.iload(objCount);

        for (int i = 0; i < maxPointerCount; i++) {
            Label moreObjects = new Label();
            mv.aload(p[i]);
            mv.aload(s[i]);
            mv.aload(o[i]);

            boolean isLast = (i >= maxPointerCount - 1);
            if (!isLast) {
                mv.iload(objCount);
                mv.pushInt(i + 1);
                mv.if_icmpgt(moreObjects);
            }

            mv.invokevirtual(p(Invoker.class),
                    getObjectParameterMethodName(parameterTypes.length),
                    getObjectParameterMethodSignature(parameterTypes.length, i + 1));
            narrow(mv, long.class, nativeIntType);
            mv.go_to(convertResult);
            if (!isLast) mv.label(moreObjects);
        }
    }

    private void emitBufferInvocation(AsmBuilder builder, SkinnyMethodAdapter mv, LocalVariableAllocator localVariableAllocator, Function function, ResultType resultType, ParameterType[] parameterTypes, Class nativeIntType, LocalVariable objCount, LocalVariable[] pointers, LocalVariable[] strategies) {
        // Save the converted parameters
        LocalVariable[] saved = new LocalVariable[parameterTypes.length];
        for (int i = parameterTypes.length - 1; i >= 0; i--) {
            if (strategies[i] == null) {
                saved[i] = localVariableAllocator.allocate(nativeIntType);
                store(mv, nativeIntType, saved[i]);
            } else {
                // No need to save the address arg, since it will be re-fetched via the strategy
                if (long.class == nativeIntType) mv.pop2(); else mv.pop();
            }
        }

        // Create a new InvocationBuffer
        mv.aload(0);
        mv.getfield(builder.getClassNamePath(), builder.getCallContextFieldName(function), ci(CallContext.class));
        mv.iload(objCount);
        mv.invokestatic(AsmRuntime.class, "newHeapInvocationBuffer", HeapInvocationBuffer.class, CallContext.class, int.class);

        // Now add each of the parameters to the invocation buffer
        for (int i = 0; i < parameterTypes.length; i++) {
            mv.dup();

            if (strategies[i] != null) {
                mv.aload(pointers[i]);
                mv.aload(strategies[i]);
                mv.pushInt(AsmUtil.getNativeArrayFlags(parameterTypes[i].annotations));
                mv.invokevirtual(HeapInvocationBuffer.class, "putObject", void.class, Object.class, ObjectParameterStrategy.class, int.class);

            } else {
                load(mv, nativeIntType, saved[i]);
                switch (parameterTypes[i].nativeType) {
                    case SCHAR:
                    case UCHAR:
                        narrow(mv, nativeIntType, int.class);
                        mv.invokevirtual(HeapInvocationBuffer.class, "putByte", void.class, int.class);
                        break;

                    case SSHORT:
                    case USHORT:
                        narrow(mv, nativeIntType, int.class);
                        mv.invokevirtual(HeapInvocationBuffer.class, "putShort", void.class, int.class);
                        break;

                    default:
                        if (sizeof(parameterTypes[i].nativeType) < 8) {
                            narrow(mv, nativeIntType, int.class);
                            mv.invokevirtual(HeapInvocationBuffer.class, "putInt", void.class, int.class);
                        } else {
                            mv.invokevirtual(HeapInvocationBuffer.class, "putLong", void.class, long.class);
                        }
                }
            }

        }


        if (resultType.nativeType == NativeType.VOID || sizeof(resultType.nativeType) < 8) {
            mv.invokevirtual(Invoker.class, "invokeInt", int.class, CallContext.class, long.class, HeapInvocationBuffer.class);
            widen(mv, int.class, nativeIntType);
        } else {
            mv.invokevirtual(Invoker.class, "invokeLong", long.class, CallContext.class, long.class, HeapInvocationBuffer.class);
        }
    }

    @SuppressWarnings("unchecked")
    static final Set<Class> pointerTypes = Collections.unmodifiableSet(new LinkedHashSet<Class>(Arrays.asList(
        Pointer.class,
        CharSequence.class,
        ByteBuffer.class, ShortBuffer.class, IntBuffer.class, LongBuffer.class, FloatBuffer.class, DoubleBuffer.class,
        byte[].class, short[].class, char[].class, int[].class, long[].class, float[].class, double[].class, boolean[].class
    )));

    static void emitPointerParameterStrategyLookup(SkinnyMethodAdapter mv, Class javaParameterType, Annotation[] annotations) {
        boolean converted = false;
        for (Class c : pointerTypes) {
            if (c.isAssignableFrom(javaParameterType)) {
                mv.invokestatic(AsmRuntime.class, "pointerParameterStrategy", PointerParameterStrategy.class, c);
                converted = true;
            }
        }
        if (converted) {
            return;
        }

        if (Struct.class.isAssignableFrom(javaParameterType)) {
            if (ParameterFlags.isDirect(AsmUtil.getParameterFlags(annotations))) {
                // Force the struct to be passed using direct backing memory
                mv.invokestatic(AsmRuntime.class, "directStructParameterStrategy", PointerParameterStrategy.class, Struct.class);

            } else {
                mv.invokestatic(AsmRuntime.class, "structParameterStrategy", PointerParameterStrategy.class, Struct.class);
            }

        } else {
            throw new RuntimeException("no strategy for " + javaParameterType);
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

    static boolean getBooleanProperty(String propertyName, boolean defaultValue) {
        return Boolean.valueOf(System.getProperty(propertyName, Boolean.valueOf(defaultValue).toString()));
    }
}
