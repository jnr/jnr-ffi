
package com.kenai.jaffl.mapper;

import com.kenai.jaffl.Library;


public interface FunctionMapper {
    static interface Context {
        Library getLibrary();
    }
    public String mapFunctionName(String functionName, Context context);
}
