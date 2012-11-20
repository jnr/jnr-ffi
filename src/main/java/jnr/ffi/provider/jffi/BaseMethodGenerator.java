package jnr.ffi.provider.jffi;

import com.kenai.jffi.CallContext;
import com.kenai.jffi.Function;
import jnr.ffi.mapper.*;

import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Collections;

import static jnr.ffi.provider.jffi.AsmUtil.*;
import static jnr.ffi.provider.jffi.CodegenUtils.*;
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

        SkinnyMethodAdapter mv = new SkinnyMethodAdapter(builder.getClassVisitor(), ACC_PUBLIC | ACC_FINAL,
                functionName,
                sig(resultType.getDeclaredType(), javaParameterTypes), null, null);
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
            Method toNativeMethod = getToNativeMethod(parameterType);
            mv.aload(0);
            AsmBuilder.ObjectField toNativeConverterField = builder.getToNativeConverterField(parameterConverter);
            mv.getfield(builder.getClassNamePath(), toNativeConverterField.name, ci(toNativeConverterField.klass));

            if (!toNativeMethod.getDeclaringClass().equals(toNativeConverterField.klass)) {
                mv.checkcast(toNativeMethod.getDeclaringClass());
            }

            AsmUtil.load(mv, parameterType.getDeclaredType(), parameter);

            if (parameterType.getDeclaredType().isPrimitive()) {
                boxValue(mv, getBoxedClass(parameterType.getDeclaredType()), parameterType.getDeclaredType());
            }

            if (!toNativeMethod.getParameterTypes()[0].isAssignableFrom(getBoxedClass(parameterType.getDeclaredType()))) {
                mv.checkcast(toNativeMethod.getParameterTypes()[0]);
            }
            mv.aconst_null();
            if (toNativeMethod.getDeclaringClass().isInterface()) {
                mv.invokeinterface(toNativeMethod.getDeclaringClass(), toNativeMethod.getName(),
                        toNativeMethod.getReturnType(), toNativeMethod.getParameterTypes());
            } else {
                mv.invokevirtual(toNativeMethod.getDeclaringClass(), toNativeMethod.getName(),
                    toNativeMethod.getReturnType(), toNativeMethod.getParameterTypes());
            }
            if (!parameterConverter.nativeType().isAssignableFrom(toNativeMethod.getReturnType())) {
                mv.checkcast(p(parameterConverter.nativeType()));
            }
        } else {
            AsmUtil.load(mv, parameterType.getDeclaredType(), parameter);
        }
    }

    static Method getToNativeMethod(ParameterType toNativeType) {
        ToNativeConverter toNativeConverter = toNativeType.toNativeConverter;
        if (toNativeConverter == null) {
            return null;
        }

        try {
            Class<? extends ToNativeConverter> toNativeConverterClass = toNativeConverter.getClass();
            if (Modifier.isPublic(toNativeConverterClass.getModifiers())) {
                for (Method method : toNativeConverterClass.getMethods()) {
                    if (!method.getName().equals("toNative")) continue;
                    Class[] methodParameterTypes = method.getParameterTypes();
                    if (toNativeConverter.nativeType().isAssignableFrom(method.getReturnType())
                            && methodParameterTypes.length == 2
                            && methodParameterTypes[0].isAssignableFrom(toNativeType.getDeclaredType())
                            && methodParameterTypes[1] == ToNativeContext.class
                            && methodIsAccessible(method)) {
                        return method;
                    }
                }
            }
            Method method = toNativeConverterClass.getMethod("toNative", Object.class, ToNativeContext.class);
            return methodIsAccessible(method)
                    ? method : ToNativeConverter.class.getDeclaredMethod("toNative", Object.class, ToNativeContext.class);

        } catch (NoSuchMethodException nsme) {
            try {
                return ToNativeConverter.class.getDeclaredMethod("toNative", Object.class, ToNativeContext.class);
            } catch (NoSuchMethodException nsme2) {
                throw new RuntimeException("internal error. " + ToNativeConverter.class + " has no toNative() method");
            }
        }
    }

    static Method getFromNativeMethod(ResultType fromNativeType) {
        FromNativeConverter fromNativeConverter = fromNativeType.fromNativeConverter;
        if (fromNativeConverter == null) {
            return null;
        }

        try {
            Class<? extends FromNativeConverter> fromNativeConverterClass = fromNativeConverter.getClass();
            if (Modifier.isPublic(fromNativeConverterClass.getModifiers())) {
                for (Method method : fromNativeConverterClass.getMethods()) {
                    if (!method.getName().equals("fromNative")) continue;
                    Class[] methodParameterTypes = method.getParameterTypes();
                    if (fromNativeType.getDeclaredType().isAssignableFrom(method.getReturnType())
                            && methodParameterTypes.length == 2
                            && methodParameterTypes[0].isAssignableFrom(fromNativeConverter.nativeType())
                            && methodParameterTypes[1] == FromNativeContext.class
                            && methodIsAccessible(method)) {
                        return method;
                    }
                }
            }
            Method method = fromNativeConverterClass.getMethod("fromNative", Object.class, FromNativeContext.class);
            return methodIsAccessible(method)
                    ? method : FromNativeConverter.class.getDeclaredMethod("fromNative", Object.class, FromNativeContext.class);

        } catch (NoSuchMethodException nsme) {
            try {
                return FromNativeConverter.class.getDeclaredMethod("fromNative", Object.class, FromNativeContext.class);
            } catch (NoSuchMethodException nsme2) {
                throw new RuntimeException("internal error. " + FromNativeConverter.class + " has no fromNative() method");
            }
        }
    }

    static boolean methodIsAccessible(Method method) {
        return Modifier.isPublic(method.getModifiers()) && Modifier.isPublic(method.getDeclaringClass().getModifiers());
    }

    static void convertAndReturnResult(AsmBuilder builder, SkinnyMethodAdapter mv, ResultType resultType, Class nativeResultClass) {
        // If there is a result converter, retrieve it and put on the stack
        FromNativeConverter resultConverter = resultType.fromNativeConverter;
        if (resultConverter != null) {
            boxValue(mv, resultConverter.nativeType(), nativeResultClass);

            mv.aload(0);
            Method fromNativeMethod = getFromNativeMethod(resultType);
            AsmBuilder.ObjectField fromNativeConverterField = builder.getFromNativeConverterField(resultConverter);
            mv.getfield(builder.getClassNamePath(), fromNativeConverterField.name, ci(fromNativeConverterField.klass));
            mv.swap();
            mv.aconst_null();
            if (fromNativeMethod.getDeclaringClass().isInterface()) {
                mv.invokeinterface(fromNativeMethod.getDeclaringClass(), fromNativeMethod.getName(),
                        fromNativeMethod.getReturnType(), fromNativeMethod.getParameterTypes());
            } else {
                mv.invokevirtual(fromNativeMethod.getDeclaringClass(), fromNativeMethod.getName(),
                        fromNativeMethod.getReturnType(), fromNativeMethod.getParameterTypes());
            }

            if (resultType.getDeclaredType().isPrimitive()) {
                // The actual return type is a primitive, but there was a converter for it - extract the primitive value
                Class boxedType = getBoxedClass(resultType.getDeclaredType());
                mv.checkcast(p(boxedType));
                unboxNumber(mv, boxedType, resultType.getDeclaredType(), resultType.nativeType);
                emitReturnOp(mv, resultType.getDeclaredType());

            } else {
                mv.checkcast(p(resultType.getDeclaredType()));
                mv.areturn();
            }

        } else {
            AsmUtil.emitReturn(mv, resultType.getDeclaredType(), nativeResultClass);
        }
    }

    static void emitPostInvoke(AsmBuilder builder, SkinnyMethodAdapter mv, ParameterType[] parameterTypes,
                               LocalVariable[] parameters, LocalVariable[] converted) {
        for (int i = 0; i < converted.length; ++i) {
            if (converted[i] != null) {
                mv.aload(0);
                AsmBuilder.ObjectField toNativeConverterField = builder.getToNativeConverterField(parameterTypes[i].toNativeConverter);
                mv.getfield(builder.getClassNamePath(), toNativeConverterField.name, ci(toNativeConverterField.klass));
                mv.checkcast(ToNativeConverter.PostInvocation.class);
                mv.aload(parameters[i]);
                mv.aload(converted[i]);
                mv.aconst_null();
                mv.invokeinterface(ToNativeConverter.PostInvocation.class, "postInvoke", void.class,
                        Object.class, Object.class, ToNativeContext.class);
            }
        }
    }
}
