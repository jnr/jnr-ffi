package jnr.ffi.provider.jffi;

import com.kenai.jffi.CallingConvention;
import com.kenai.jffi.Function;
import jnr.ffi.Pointer;
import jnr.ffi.struct.Struct;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

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
    private final StubCompiler compiler = StubCompiler.newCompiler();
    private final AtomicLong nextMethodID = new AtomicLong(0);
    private final BufferMethodGenerator bufgen;

    X86MethodGenerator(BufferMethodGenerator bufgen) {
        this.bufgen = bufgen;
    }

    public boolean isSupported(Class returnType, Annotation[] resultAnnotations,
                               Class[] parameterTypes, Annotation[][] parameterAnnotations,
                               CallingConvention convention) {
        return canCompile(compiler, returnType, parameterTypes, convention);
    }


    public void generate(SkinnyMethodAdapter mv, Class returnType, Annotation[] resultAnnotations, Class[] parameterTypes, Annotation[][] parameterAnnotations, boolean ignoreError) {
        throw new NotImplementedException();
    }

    public void generate(Function function,
                         ClassVisitor cv, String className, String functionName, String functionFieldName,
                         Class returnType, Annotation[] resultAnnotations,
                         Class[] parameterTypes, Annotation[][] parameterAnnotations, CallingConvention convention, boolean ignoreError) {
        compile(function, cv, className, functionName, functionFieldName, returnType,
                resultAnnotations, parameterTypes, parameterAnnotations, convention, ignoreError);
    }


    static boolean canCompile(StubCompiler compiler, Class returnType, Class[] parameterTypes,
                              CallingConvention convention) {

        Class[] nativeParameterTypes = new Class[parameterTypes.length];
        for (int i = 0; i < nativeParameterTypes.length; ++i) {
            if (Buffer.class.isAssignableFrom(parameterTypes[i])) {
                nativeParameterTypes[i] = AsmUtil.unboxedType(Pointer.class);

            } else {
                nativeParameterTypes[i] = AsmUtil.unboxedType(parameterTypes[i]);
            }
        }

        return compiler.canCompile(AsmUtil.unboxedReturnType(returnType), nativeParameterTypes, convention);
    }

    private void compile(Function function,
                        ClassVisitor cv, String className, String functionName, String functionFieldName,
                        Class returnType, Annotation[] resultAnnotations, Class[] parameterTypes, Annotation[][] parameterAnnotations,
                        CallingConvention convention, boolean ignoreErrno) {

        Class[] nativeParameterTypes = new Class[parameterTypes.length];
        boolean unboxing = false;
        boolean ptrCheck = false;
        for (int i = 0; i < nativeParameterTypes.length; ++i) {
            if (Buffer.class.isAssignableFrom(parameterTypes[i])) {
                nativeParameterTypes[i] = AsmUtil.unboxedType(Pointer.class);

            } else {
                nativeParameterTypes[i] = AsmUtil.unboxedType(parameterTypes[i]);
            }
            unboxing |= nativeParameterTypes[i] != parameterTypes[i];
            ptrCheck |= Pointer.class.isAssignableFrom(parameterTypes[i])
                    || Struct.class.isAssignableFrom(parameterTypes[i])
                    || Buffer.class.isAssignableFrom(parameterTypes[i]);
        }

        Class nativeReturnType = AsmUtil.unboxedReturnType(returnType);
        unboxing |= nativeReturnType != returnType;

        String stubName = functionName + (unboxing || ptrCheck ? "$jni$" + nextMethodID.incrementAndGet() : "");

        // If unboxing of parameters is required, generate a wrapper
        if (unboxing || ptrCheck) {
            SkinnyMethodAdapter mv = new SkinnyMethodAdapter(cv.visitMethod(ACC_PUBLIC | ACC_FINAL,
                    functionName, sig(returnType, parameterTypes), null, null));
            mv.start();
            mv.aload(0);

            Label bufferInvocationLabel = AsmLibraryLoader.emitDirectCheck(mv, parameterTypes);

            // Emit the unboxing wrapper
            for (int i = 0, lvar = 1; i < parameterTypes.length; ++i) {
                lvar = AsmLibraryLoader.loadParameter(mv, parameterTypes[i], lvar);
                if (parameterTypes[i] != nativeParameterTypes[i]) {

                    if (Number.class.isAssignableFrom(parameterTypes[i])) {
                        unboxNumber(mv, parameterTypes[i], nativeParameterTypes[i]);

                    } else if (Boolean.class.isAssignableFrom(parameterTypes[i])) {
                        unboxBoolean(mv, parameterTypes[i], nativeParameterTypes[i]);

                    } else if (Pointer.class.isAssignableFrom(parameterTypes[i])) {
                        unboxPointer(mv, nativeParameterTypes[i]);

                    } else if (Struct.class.isAssignableFrom(parameterTypes[i])) {
                        unboxStruct(mv, nativeParameterTypes[i]);

                    } else if (Buffer.class.isAssignableFrom(parameterTypes[i])) {
                        unboxBuffer(mv, parameterTypes[i], nativeParameterTypes[i]);

                    } else if (Enum.class.isAssignableFrom(parameterTypes[i])) {
                        unboxEnum(mv, nativeParameterTypes[i]);
                    }
                }
            }

            // invoke the compiled stub
            mv.invokevirtual(className, stubName, sig(nativeReturnType, nativeParameterTypes));

            // emitReturn will box the return value if needed
            AsmLibraryLoader.emitReturn(mv, returnType, nativeReturnType);

            String bufInvoke = null;
            if (bufferInvocationLabel != null) {
                // If there was a non-direct pointer in the parameters, need to
                // handle it via a call to the slow buffer invocation
                mv.label(bufferInvocationLabel);

                bufInvoke = functionName + "$buf$" + nextMethodID.incrementAndGet();
                // reload all the parameters
                for (int i = 0, lvar = 1; i < parameterTypes.length; ++i) {
                    lvar = AsmLibraryLoader.loadParameter(mv, parameterTypes[i], lvar);
                }
                mv.invokevirtual(className, bufInvoke, sig(returnType, parameterTypes));
                emitReturnOp(mv, returnType);
            }
            mv.visitMaxs(100, calculateLocalVariableSpace(parameterTypes) + 10);
            mv.visitEnd();

            if (bufInvoke != null) {
                SkinnyMethodAdapter bi = new SkinnyMethodAdapter(cv.visitMethod(ACC_PUBLIC | ACC_FINAL,
                        bufInvoke, sig(returnType, parameterTypes), null, null));
                bi.start();

                // Retrieve the static 'ffi' Invoker instance
                bi.getstatic(p(AsmLibraryLoader.AbstractNativeInterface.class), "ffi", ci(com.kenai.jffi.Invoker.class));

                // retrieve this.function
                bi.aload(0);
                bi.getfield(className, functionFieldName, ci(Function.class));

                BufferMethodGenerator.generateBufferInvocation(bi, returnType, resultAnnotations, parameterTypes, parameterAnnotations);
                bi.visitMaxs(100, calculateLocalVariableSpace(parameterTypes) + 10);
                bi.visitEnd();
            }
        }

        cv.visitMethod(ACC_PUBLIC | ACC_FINAL | ACC_NATIVE,
                stubName, sig(nativeReturnType, nativeParameterTypes), null, null);


        compiler.compile(function, stubName, nativeReturnType, nativeParameterTypes,
                convention, !ignoreErrno);
    }

    void attach(Class clazz) {
        compiler.attach(clazz);
    }

}
