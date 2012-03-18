package jnr.ffi.provider.jffi;

import com.kenai.jffi.CallingConvention;
import com.kenai.jffi.Function;

/**
 *
 */
public interface MethodGenerator {

    public void generate(AsmBuilder builder, String functionName, Function function, Signature signature);
    public boolean isSupported(ResultType resultType, ParameterType[] parameterTypes, CallingConvention callingConvention);
    public void generate(AsmBuilder builder, String functionName, Function function,
                         ResultType resultType, ParameterType[] parameterTypes, boolean ignoreError);
}
