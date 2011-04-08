package jnr.ffi.provider.jffi;

import com.kenai.jffi.Function;

import static jnr.ffi.provider.jffi.AsmUtil.calculateLocalVariableSpace;
import static jnr.ffi.provider.jffi.CodegenUtils.ci;
import static jnr.ffi.provider.jffi.CodegenUtils.p;
import static jnr.ffi.provider.jffi.CodegenUtils.sig;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;

/**
 *
 */
abstract class BaseMethodGenerator implements MethodGenerator {

    public void generate(AsmBuilder builder, String functionName, Function function, Signature signature) {
        SkinnyMethodAdapter mv = new SkinnyMethodAdapter(builder.getClassVisitor().visitMethod(ACC_PUBLIC | ACC_FINAL,
                functionName,
                sig(signature.resultType, signature.parameterTypes), null, null));
        mv.start();

        // Retrieve the static 'ffi' Invoker instance
        mv.getstatic(p(AbstractAsmLibraryInterface.class), "ffi", ci(com.kenai.jffi.Invoker.class));

        // retrieve this.function
        mv.aload(0);
        mv.getfield(builder.getClassNamePath(), builder.getFunctionFieldName(function), ci(Function.class));

        generate(mv, signature);

        mv.visitMaxs(100, calculateLocalVariableSpace(signature.parameterTypes) + 10);
        mv.visitEnd();
    }

    abstract void generate(SkinnyMethodAdapter mv, Signature signature);
}
