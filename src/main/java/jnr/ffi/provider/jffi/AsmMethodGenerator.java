package jnr.ffi.provider.jffi;

import com.kenai.jffi.CallingConvention;
import com.kenai.jffi.Function;
import org.objectweb.asm.ClassVisitor;

import java.lang.annotation.Annotation;

/**
 *
 */
public interface AsmMethodGenerator {

    public boolean isSupported(Class returnType, Annotation[] resultAnnotations,
                         Class[] parameterTypes, Annotation[][] parameterAnnotations, CallingConvention convention);
    public void generate(Function function,
                         ClassVisitor cv, String className, String functionName, String functionFieldName,
                         Class returnType, Annotation[] resultAnnotations,
                         Class[] parameterTypes, Annotation[][] parameterAnnotations, CallingConvention convention, boolean ignoreError);
}
