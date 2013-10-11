package jnr.ffi.mapper;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class SimpleFunctionMapper implements FunctionMapper {
    private final Map<String, String> functionNameMap;

    SimpleFunctionMapper(Map<String, String> map) {
        functionNameMap = Collections.unmodifiableMap(new HashMap<String, String>(map));
    }

    public String mapFunctionName(String functionName, Context context) {
        String nativeFunction = functionNameMap.get(functionName);
        return nativeFunction != null ? nativeFunction : functionName;
    }

}
