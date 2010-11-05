package com.kenai.jaffl.provider.jffi;

import com.kenai.jaffl.mapper.FunctionMapper;

/**
 * An implementation of {@link FunctionMapper} that just returns the same name as input
 */
public class IdentityFunctionMapper implements FunctionMapper {
    private static final class SingletonHolder {
        public static final FunctionMapper INSTANCE = new IdentityFunctionMapper();
    }

    public static FunctionMapper getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public String mapFunctionName(String functionName, Context context) {
        return functionName;
    }
}
