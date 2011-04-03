package jnr.ffi.provider.jffi;

import com.kenai.jffi.CallingConvention;

import java.lang.annotation.Annotation;

/**
 *
 */
public interface AsmInvocationGenerator {

    public boolean isSupported(Class returnType, Annotation[] resultAnnotations,
                         Class[] parameterTypes, Annotation[][] parameterAnnotations, CallingConvention convention);
    public void generate(SkinnyMethodAdapter mv, Class returnType, Annotation[] resultAnnotations,
                         Class[] parameterTypes, Annotation[][] parameterAnnotations, boolean ignoreError);
}
