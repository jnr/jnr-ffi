package jnr.ffi.provider.jffi;

import com.kenai.jffi.CallingConvention;
import com.kenai.jffi.Function;

/**
 *
 */
class NotImplMethodGenerator implements MethodGenerator {
    public boolean isSupported(ResultType resultType, ParameterType[] parameterTypes, CallingConvention callingConvention) {
        return false;
    }

    public void generate(AsmBuilder builder, String functionName, Function function, ResultType resultType, ParameterType[] parameterTypes, boolean ignoreError) {
        throw new UnsupportedOperationException("not supported");
    }
}
