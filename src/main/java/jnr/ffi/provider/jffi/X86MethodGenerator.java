package jnr.ffi.provider.jffi;

import com.kenai.jffi.*;
import jnr.ffi.Address;
import jnr.ffi.NativeType;
import jnr.ffi.Pointer;
import jnr.ffi.mapper.ToNativeConverter;
import org.objectweb.asm.Label;

import java.util.concurrent.atomic.AtomicLong;

import static jnr.ffi.provider.jffi.AbstractFastNumericMethodGenerator.emitPointerParameterStrategyLookup;
import static jnr.ffi.provider.jffi.AsmUtil.*;
import static jnr.ffi.provider.jffi.BaseMethodGenerator.emitEpilogue;
import static jnr.ffi.provider.jffi.BaseMethodGenerator.emitPostInvoke;
import static jnr.ffi.provider.jffi.BaseMethodGenerator.loadAndConvertParameter;
import static jnr.ffi.provider.jffi.CodegenUtils.*;
import static jnr.ffi.provider.jffi.NumberUtil.*;
import static org.objectweb.asm.Opcodes.*;

/**
 *
 */
class X86MethodGenerator implements MethodGenerator {
    private final AtomicLong nextMethodID = new AtomicLong(0);
    private final StubCompiler compiler;

    X86MethodGenerator(StubCompiler compiler) {
        this.compiler = compiler;
    }

    public boolean isSupported(ResultType resultType, ParameterType[] parameterTypes, CallingConvention callingConvention) {
        if (!Boolean.valueOf(System.getProperty("jnr.ffi.x86asm.enabled", "true"))) {
            return false;
        }

        final Platform platform = Platform.getPlatform();

        if (platform.getOS().equals(Platform.OS.WINDOWS)) {
            return false;
        }

        if (!platform.getCPU().equals(Platform.CPU.I386) && !platform.getCPU().equals(Platform.CPU.X86_64)) {
            return false;
        }

        if (!callingConvention.equals(CallingConvention.DEFAULT)) {
            return false;
        }

        int objectCount = 0;
        for (int i = 0; i < parameterTypes.length; ++i) {
            if (!isSupportedParameter(parameterTypes[i])) {
                return false;
            }

            if (isSupportedObjectParameterType(parameterTypes[i])) {
                objectCount++;
            }
        }

        if (objectCount > 0) {
            if (parameterTypes.length > 4 || objectCount > 3) {
                return false;
            }
        }

        return isSupportedResult(resultType)
                && compiler.canCompile(resultType, parameterTypes, callingConvention);
    }

    public void generate(AsmBuilder builder, String functionName, Function function,
                         ResultType resultType, ParameterType[] parameterTypes, boolean ignoreError) {

        Class[] nativeParameterTypes = new Class[parameterTypes.length];
        boolean wrapperNeeded = false;

        for (int i = 0; i < parameterTypes.length; ++i) {
            wrapperNeeded |= parameterTypes[i].toNativeConverter != null || !parameterTypes[i].effectiveJavaType().isPrimitive();
            if (!parameterTypes[i].effectiveJavaType().isPrimitive()) {
                nativeParameterTypes[i] = getNativeClass(parameterTypes[i].nativeType);
            } else {
                nativeParameterTypes[i] = parameterTypes[i].effectiveJavaType();
            }
        }

        Class nativeReturnType;
        wrapperNeeded |= resultType.fromNativeConverter != null || !resultType.effectiveJavaType().isPrimitive();
        if (resultType.effectiveJavaType().isPrimitive()) {
            nativeReturnType = resultType.effectiveJavaType();
        } else {
            nativeReturnType = getNativeClass(resultType.nativeType);
        }

        String stubName = functionName + (wrapperNeeded ? "$jni$" + nextMethodID.incrementAndGet() : "");

        builder.getClassVisitor().visitMethod(ACC_PUBLIC | ACC_FINAL | ACC_NATIVE | (wrapperNeeded ? ACC_STATIC : 0),
                stubName, sig(nativeReturnType, nativeParameterTypes), null, null);

        compiler.compile(function, stubName, resultType, parameterTypes, nativeReturnType, nativeParameterTypes,
                CallingConvention.DEFAULT, !ignoreError);

        // If unboxing of parameters is required, generate a wrapper
        if (wrapperNeeded) {
            generateWrapper(builder, functionName, function, resultType, parameterTypes,
                    stubName, nativeReturnType, nativeParameterTypes);
        }
    }

    private static void generateWrapper(final AsmBuilder builder, String functionName, Function function,
                                        final ResultType resultType, final ParameterType[] parameterTypes,
                                        String nativeMethodName, Class nativeReturnType, Class[] nativeParameterTypes) {
        Class[] javaParameterTypes = new Class[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            javaParameterTypes[i] = parameterTypes[i].getDeclaredType();
        }

        final SkinnyMethodAdapter mv = new SkinnyMethodAdapter(builder.getClassVisitor(),
                ACC_PUBLIC | ACC_FINAL,
                functionName, sig(resultType.getDeclaredType(), javaParameterTypes), null, null);
        mv.setMethodVisitor(AsmUtil.newTraceMethodVisitor(mv.getMethodVisitor()));
        mv.start();


        LocalVariableAllocator localVariableAllocator = new LocalVariableAllocator(parameterTypes);
        final LocalVariable objCount = localVariableAllocator.allocate(int.class);
        final LocalVariable[] parameters = AsmUtil.getParameterVariables(parameterTypes);
        LocalVariable[] pointers = new LocalVariable[parameterTypes.length];
        LocalVariable[] strategies = new LocalVariable[parameterTypes.length];
        final LocalVariable[] converted = new LocalVariable[parameterTypes.length];
        int pointerCount = 0;

        for (int i = 0; i < parameterTypes.length; ++i) {
            Class javaParameterClass = parameterTypes[i].effectiveJavaType();
            Class nativeParameterClass = nativeParameterTypes[i];

            loadAndConvertParameter(builder, mv, parameters[i], parameterTypes[i]);
            if (parameterTypes[i].toNativeConverter instanceof ToNativeConverter.PostInvocation) {
                mv.dup();
                mv.astore(converted[i] = localVariableAllocator.allocate(Object.class));
            }

            ToNativeOp toNativeOp = ToNativeOp.get(parameterTypes[i]);
            if (toNativeOp != null && toNativeOp.isPrimitive()) {
                toNativeOp.emitPrimitive(mv, nativeParameterClass, parameterTypes[i].nativeType);

            } else if (Pointer.class.isAssignableFrom(javaParameterClass)) {

                // Initialize the objectCount local var
                if (pointerCount++ < 1) {
                    mv.pushInt(0);
                    mv.istore(objCount);
                }

                strategies[i] = localVariableAllocator.allocate(ObjectParameterStrategy.class);

                if (parameterTypes[i].toNativeConverter != null) {
                    // Save the current pointer parameter (result of type conversion above)
                    pointers[i] = localVariableAllocator.allocate(Object.class);
                    mv.astore(pointers[i]);
                    mv.aload(pointers[i]);
                } else {
                    // avoid the save/load of an extra local var if no parameter conversion took place
                    pointers[i] = parameters[i];
                }

                emitPointerParameterStrategyLookup(mv, javaParameterClass, parameterTypes[i].annotations());

                mv.astore(strategies[i]);
                mv.aload(strategies[i]);

                mv.getfield(p(PointerParameterStrategy.class), "objectCount", ci(int.class));
                mv.iload(objCount);
                mv.iadd();
                mv.istore(objCount);

                // Get the native address (will return zero for heap objects)
                mv.aload(strategies[i]);
                mv.aload(pointers[i]);
                mv.invokevirtual(PointerParameterStrategy.class, "address", long.class, Object.class);
                narrow(mv, long.class, nativeParameterClass);

            } else if (!javaParameterClass.isPrimitive()) {
                throw new IllegalArgumentException("unsupported type " + javaParameterClass);
            }
        }
        Label hasObjects = new Label();
        Label convertResult = new Label();

        // If there are any objects, jump to the fast-object path
        if (pointerCount > 0) {
            mv.iload(objCount);
            mv.ifne(hasObjects);
        }

        // invoke the compiled stub
        mv.invokestatic(builder.getClassNamePath(), nativeMethodName, sig(nativeReturnType, nativeParameterTypes));

        // If boxing is neccessary, perform conversions
        final Class unboxedResultType = unboxedReturnType(resultType.effectiveJavaType());
        convertPrimitive(mv, nativeReturnType, unboxedResultType);

        if (pointerCount > 0) {
            mv.label(convertResult);
        }
        emitEpilogue(builder, mv, resultType, parameterTypes, parameters, converted, null);

        /* --  method returns above - below is the object path, which will jump back above to return -- */

        // Now implement heap object support
        if (pointerCount > 0) {
            mv.label(hasObjects);

            // Store all the native args
            LocalVariable[] tmp = new LocalVariable[parameterTypes.length];
            for (int i = parameterTypes.length - 1; i >= 0; i--) {
                tmp[i] = localVariableAllocator.allocate(long.class);
                if (float.class == nativeParameterTypes[i]) {
                    mv.invokestatic(Float.class, "floatToRawIntBits", int.class, float.class);
                    mv.i2l();

                } else if (double.class == nativeParameterTypes[i]) {
                    mv.invokestatic(Double.class, "doubleToRawLongBits", long.class, double.class);

                } else {
                    convertPrimitive(mv, nativeParameterTypes[i], long.class, parameterTypes[i].nativeType);
                }
                mv.lstore(tmp[i]);
            }

            // Retrieve the static 'ffi' Invoker instance
            mv.getstatic(p(AbstractAsmLibraryInterface.class), "ffi", ci(com.kenai.jffi.Invoker.class));

            // retrieve the call context and function address
            mv.aload(0);
            mv.getfield(builder.getClassNamePath(), builder.getCallContextFieldName(function), ci(CallContext.class));

            mv.aload(0);
            mv.getfield(builder.getClassNamePath(), builder.getFunctionAddressFieldName(function), ci(long.class));

            // Now reload the args back onto the parameter stack
            mv.lload(tmp);

            mv.iload(objCount);
            // Need to load all the converters onto the stack
            for (int i = 0; i < parameterTypes.length; i++) {
                if (pointers[i] != null) {
                    mv.aload(pointers[i]);
                    mv.aload(strategies[i]);
                    mv.aload(0);

                    ObjectParameterInfo info = ObjectParameterInfo.create(i,
                            AsmUtil.getNativeArrayFlags(parameterTypes[i].annotations()));

                    mv.getfield(builder.getClassNamePath(), builder.getObjectParameterInfoName(info),
                            ci(ObjectParameterInfo.class));
                }
            }

            mv.invokevirtual(p(com.kenai.jffi.Invoker.class),
                    AbstractFastNumericMethodGenerator.getObjectParameterMethodName(parameterTypes.length),
                    AbstractFastNumericMethodGenerator.getObjectParameterMethodSignature(parameterTypes.length, pointerCount));

            // Convert the result from long/int to the correct return type
            if (float.class == nativeReturnType) {
                narrow(mv, long.class, int.class);
                mv.invokestatic(Float.class, "intBitsToFloat", float.class, int.class);

            } else if (double.class == nativeReturnType) {
                mv.invokestatic(Double.class, "longBitsToDouble", double.class, long.class);

            } else if (void.class == nativeReturnType) {
                mv.pop2();

            }
            convertPrimitive(mv, long.class, unboxedResultType, resultType.nativeType);

            // Jump to the main conversion/boxing code above
            mv.go_to(convertResult);
        }
        mv.visitMaxs(100, localVariableAllocator.getSpaceUsed());
        mv.visitEnd();
    }

    void attach(Class clazz) {
        compiler.attach(clazz);
    }


    private static boolean isSupportedObjectParameterType(ParameterType type) {
        return Pointer.class.isAssignableFrom(type.effectiveJavaType());
    }


    private static boolean isSupportedType(SigType type) {
        switch (type.nativeType) {
            case SCHAR:
            case UCHAR:
            case SSHORT:
            case USHORT:
            case SINT:
            case UINT:
            case SLONG:
            case ULONG:
            case SLONGLONG:
            case ULONGLONG:
            case FLOAT:
            case DOUBLE:
                return true;

            default:
                return false;
        }
    }


    static boolean isSupportedResult(ResultType resultType) {
        return isSupportedType(resultType) || void.class == resultType.effectiveJavaType()
                || resultType.nativeType == NativeType.ADDRESS
                ;
    }

    static boolean isSupportedParameter(ParameterType parameterType) {
        return isSupportedType(parameterType)
                || isSupportedObjectParameterType(parameterType)
                ;
    }

    static Class getNativeClass(NativeType nativeType) {
        switch (nativeType) {
            case SCHAR:
            case UCHAR:
            case SSHORT:
            case USHORT:
            case SINT:
            case UINT:
            case SLONG:
            case ULONG:
            case ADDRESS:
            case SLONGLONG:
            case ULONGLONG:
                // Since the asm code does any sign/zero extension, we can pass everything down as int/long
                // which potentially saves some java conversion ops
                return sizeof(nativeType) <= 4 ? int.class : long.class;

            case FLOAT:
                return float.class;

            case DOUBLE:
                return double.class;

            case VOID:
                return void.class;

            default:
                throw new IllegalArgumentException("unsupported native type: " + nativeType);
        }
    }
}
