
package com.kenai.jaffl.mapper;

import com.kenai.jaffl.Library;


/**
 *
 * @author wayne
 */
public interface FunctionMapper {
    static interface Context {
        Library getLibrary();
    }
    public String mapFunctionName(String functionName, Context context);
}
