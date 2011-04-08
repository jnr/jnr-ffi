package jnr.ffi.provider.jffi;

import com.kenai.jffi.CallingConvention;
import com.kenai.jffi.Function;
import org.objectweb.asm.ClassVisitor;

import java.lang.annotation.Annotation;

/**
 *
 */
public interface MethodGenerator {

    public boolean isSupported(Signature signature);
    public void generate(AsmBuilder builder, String functionName, Function function, Signature signature);
}
