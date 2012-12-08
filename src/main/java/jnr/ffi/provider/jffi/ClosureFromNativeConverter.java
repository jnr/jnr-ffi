package jnr.ffi.provider.jffi;

import com.kenai.jffi.CallContext;
import com.kenai.jffi.CallingConvention;
import jnr.ffi.Pointer;
import jnr.ffi.annotations.StdCall;
import jnr.ffi.mapper.*;
import jnr.ffi.provider.InAccessibleMemoryIO;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicLong;

import static jnr.ffi.provider.jffi.ClosureUtil.getDelegateMethod;
import static jnr.ffi.provider.jffi.CodegenUtils.*;
import static jnr.ffi.provider.jffi.InvokerUtil.*;
import static org.objectweb.asm.Opcodes.*;

/**
 *
 */
abstract public class ClosureFromNativeConverter implements FromNativeConverter<Object, Pointer> {
    @Override
    public Class<Pointer> nativeType() {
        return Pointer.class;
    }

    public static FromNativeConverter<?, Pointer> getInstance(SignatureType type, AsmClassLoader classLoader, SignatureTypeMapper typeMapper) {
        return newClosureConverter(classLoader, type.getDeclaredType(), typeMapper);
    }

    public static final class ProxyConverter extends ClosureFromNativeConverter {
        private final Constructor closureConstructor;
        private final Object[] initFields;

        public ProxyConverter(Constructor closureConstructor, Object[] initFields) {
            this.closureConstructor = closureConstructor;
            this.initFields = initFields.clone();
        }

        @Override
        public Object fromNative(Pointer nativeValue, FromNativeContext context) {
            try {
                return closureConstructor.newInstance(NativeRuntime.getInstance(), nativeValue.address(), initFields);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }
    }

    public static abstract class AbstractClosurePointer extends InAccessibleMemoryIO {
        public static final com.kenai.jffi.Invoker ffi = com.kenai.jffi.Invoker.getInstance();
        protected final long functionAddress;

        protected AbstractClosurePointer(jnr.ffi.Runtime runtime, long functionAddress) {
            super(runtime);
            this.functionAddress = functionAddress;
        }

        @Override
        public final boolean isDirect() {
            return true;
        }

        @Override
        public final long address() {
            return functionAddress;
        }

        @Override
        public final long size() {
            return 0;
        }
    }


    private static final AtomicLong nextClassID = new AtomicLong(0);
    private static FromNativeConverter newClosureConverter(AsmClassLoader classLoader, Class closureClass,
                                                                        SignatureTypeMapper typeMapper) {

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        ClassVisitor cv = AsmLibraryLoader.DEBUG ? AsmUtil.newCheckClassAdapter(cw) : cw;

        final String className = p(closureClass) + "$jnr$fromNativeConverter$" + nextClassID.getAndIncrement();
        AsmBuilder builder = new AsmBuilder(className, cv);

        cv.visit(V1_5, ACC_PUBLIC | ACC_FINAL, className, null, p(AbstractClosurePointer.class),
                new String[] { p(closureClass) } );

        cv.visitAnnotation(ci(FromNativeConverter.NoContext.class), true);

        generateInvocation(builder, closureClass, typeMapper);

        // Create the constructor to set the instance fields
        SkinnyMethodAdapter init = new SkinnyMethodAdapter(cv, ACC_PUBLIC, "<init>",
                sig(void.class, NativeRuntime.class, long.class, Object[].class), null, null);
        init.start();
        // Invoke the super class constructor as super(functionAddress)
        init.aload(0);
        init.aload(1);
        init.lload(2);
        init.invokespecial(p(AbstractClosurePointer.class), "<init>", sig(void.class, jnr.ffi.Runtime.class, long.class));
        builder.emitFieldInitialization(init, 4);
        init.voidreturn();
        init.visitMaxs(10, 10);
        init.visitEnd();

        Class implClass = loadClass(classLoader, className, cw);
        try {
            return new ProxyConverter(implClass.getConstructor(NativeRuntime.class, long.class, Object[].class), builder.getObjectFieldValues());
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    private static Class loadClass(AsmClassLoader classLoader, String className, ClassWriter cw) {
        try {
            byte[] bytes = cw.toByteArray();
            if (AsmLibraryLoader.DEBUG) {
                ClassVisitor trace = AsmUtil.newTraceClassVisitor(new PrintWriter(System.err));
                new ClassReader(bytes).accept(trace, 0);
            }

            return classLoader.defineClass(className.replace("/", "."), bytes);
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void generateInvocation(AsmBuilder builder, Class closureClass, SignatureTypeMapper typeMapper) {
        NativeRuntime runtime = NativeRuntime.getInstance();
        Method closureMethod = getDelegateMethod(closureClass);

        FromNativeContext resultContext = new MethodResultContext(closureMethod);
        SignatureType signatureType = DefaultSignatureType.create(closureMethod.getReturnType(), resultContext);
        ResultType resultType = getResultType(runtime, closureMethod.getReturnType(),
                resultContext.getAnnotations(), typeMapper.getFromNativeConverter(signatureType, resultContext),
                resultContext);

        ParameterType[] parameterTypes = getParameterTypes(runtime, typeMapper, closureMethod);

        // Allow individual methods to set the calling convention to stdcall
        CallingConvention callingConvention = closureClass.isAnnotationPresent(StdCall.class)
                ? CallingConvention.STDCALL : CallingConvention.DEFAULT;
        CallContext callContext = getCallContext(resultType, parameterTypes, callingConvention, true);
        LocalVariableAllocator localVariableAllocator = new LocalVariableAllocator(parameterTypes);


        Class[] javaParameterTypes = new Class[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            javaParameterTypes[i] = parameterTypes[i].getDeclaredType();
        }

        SkinnyMethodAdapter mv = new SkinnyMethodAdapter(builder.getClassVisitor(), ACC_PUBLIC | ACC_FINAL,
                closureMethod.getName(),
                sig(resultType.getDeclaredType(), javaParameterTypes), null, null);
        mv.start();
        // Retrieve the static 'ffi' Invoker instance
        mv.getstatic(p(AbstractClosurePointer.class), "ffi", ci(com.kenai.jffi.Invoker.class));

        // retrieve the call context and function address
        mv.aload(0);
        mv.getfield(builder.getClassNamePath(), builder.getCallContextFieldName(callContext), ci(CallContext.class));

        mv.aload(0);
        mv.getfield(p(AbstractClosurePointer.class), "functionAddress", ci(long.class));

        BufferMethodGenerator generator = new BufferMethodGenerator();
        generator.generateBufferInvocation(builder, mv, localVariableAllocator, callContext, resultType, parameterTypes);

        mv.visitMaxs(100, 10 + localVariableAllocator.getSpaceUsed());
        mv.visitEnd();
    }


}
