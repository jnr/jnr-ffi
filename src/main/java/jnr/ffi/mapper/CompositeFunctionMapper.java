package jnr.ffi.mapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 *
 */
public final class CompositeFunctionMapper implements FunctionMapper {
    private final Collection<FunctionMapper> functionMappers;

    public CompositeFunctionMapper(Collection<FunctionMapper> functionMappers) {
        this.functionMappers = Collections.unmodifiableList(new ArrayList<FunctionMapper>(functionMappers));
    }

    @Override
    public String mapFunctionName(String functionName, Context context) {
        for (FunctionMapper functionMapper : functionMappers) {
            String mappedName = functionMapper.mapFunctionName(functionName, context);
            if (mappedName != functionName) {
                // A translation was explicit in this mapper.
                return mappedName;
            }
        }
        return functionName;
    }
}
