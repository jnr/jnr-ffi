package jnr.ffi.provider.jffi;

import com.kenai.jffi.CallingConvention;
import com.kenai.jffi.Function;
import org.objectweb.asm.ClassVisitor;

import java.lang.annotation.Annotation;

import static jnr.ffi.provider.jffi.AsmUtil.calculateLocalVariableSpace;
import static jnr.ffi.provider.jffi.CodegenUtils.ci;
import static jnr.ffi.provider.jffi.CodegenUtils.p;
import static jnr.ffi.provider.jffi.CodegenUtils.sig;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;

/**
 *
 */
abstract class BaseMethodGenerator implements AsmInvocationGenerator {
    public final void generate(Function function,
                             ClassVisitor cv, String className, String functionName, String functionFieldName,
                             Class returnType, Annotation[] resultAnnotations,
                             Class[] parameterTypes, Annotation[][] parameterAnnotations, CallingConvention convention, boolean ignoreError) {
        SkinnyMethodAdapter mv = new SkinnyMethodAdapter(cv.visitMethod(ACC_PUBLIC | ACC_FINAL, functionName,
                sig(returnType, parameterTypes), null, null));
        mv.start();

        // Retrieve the static 'ffi' Invoker instance
        mv.getstatic(p(AsmLibraryLoader.AbstractNativeInterface.class), "ffi", ci(com.kenai.jffi.Invoker.class));

        // retrieve this.function
        mv.aload(0);
        mv.getfield(className, functionFieldName, ci(Function.class));

        generate(mv, returnType, resultAnnotations, parameterTypes, parameterAnnotations, ignoreError);

        mv.visitMaxs(100, calculateLocalVariableSpace(parameterTypes) + 10);
        mv.visitEnd();
    }
    abstract void generate(SkinnyMethodAdapter mv, Class returnType, Annotation[] resultAnnotations,
                         Class[] parameterTypes, Annotation[][] parameterAnnotations, boolean ignoreError);
}
