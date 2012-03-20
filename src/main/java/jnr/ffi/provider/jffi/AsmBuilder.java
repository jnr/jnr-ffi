package jnr.ffi.provider.jffi;

import com.kenai.jffi.Function;
import com.kenai.jffi.ObjectParameterInfo;
import jnr.ffi.mapper.FromNativeConverter;
import jnr.ffi.mapper.ToNativeConverter;
import org.objectweb.asm.ClassVisitor;

import java.util.*;

/**
 *
 */
class AsmBuilder {
    private final String classNamePath;
    private final ClassVisitor classVisitor;

    private final List<Function> functions = new ArrayList<Function>();
    private final List<ToNativeConverter> toNativeConverters = new ArrayList<ToNativeConverter>();
    private final List<FromNativeConverter> fromNativeConverters = new ArrayList<FromNativeConverter>();
    private final List<ObjectParameterInfo> objectParameterInfo = new ArrayList<ObjectParameterInfo>();
    private final Map<ToNativeConverter, String> toNativeConverterNames = new IdentityHashMap<ToNativeConverter, String>();
    private final Map<FromNativeConverter, String> fromNativeConverterNames = new IdentityHashMap<FromNativeConverter, String>();
    private final Map<ObjectParameterInfo, String> objectParameterInfoNames = new HashMap<ObjectParameterInfo, String>();
    private final Map<Function, FunctionField> functionFieldMap = new IdentityHashMap<Function, FunctionField>();


    AsmBuilder(String classNamePath, ClassVisitor classVisitor) {
        this.classNamePath = classNamePath;
        this.classVisitor = classVisitor;
    }

    public String getClassNamePath() {
        return classNamePath;
    }

    ClassVisitor getClassVisitor() {
        return classVisitor;
    }
    private static class FunctionField {
        private final String function, callContext, functionAddress;

        private FunctionField(int idx) {
            function = "function_" + idx;
            callContext = "callContext_" + idx;
            functionAddress = "functionAddress_" + idx;
        }
    }
    private FunctionField getFunctionField(Function function) {
        FunctionField f = functionFieldMap.get(function);
        if (f == null) {
            f = new FunctionField(functions.size());
            functionFieldMap.put(function, f);
            functions.add(function);
        }

        return f;
    }

    String getFunctionFieldName(Function function) {
        return getFunctionField(function).function;
    }

    String getCallContextFieldName(Function function) {
        return getFunctionField(function).callContext;
    }

    String getFunctionAddressFieldName(Function function) {
        return getFunctionField(function).functionAddress;
    }


    String getResultConverterName(FromNativeConverter converter) {
        String name = fromNativeConverterNames.get(converter);
        if (name == null) {
            int idx = fromNativeConverters.size();
            name = "fromNativeConverter" + idx;
            fromNativeConverters.add(converter);
            fromNativeConverterNames.put(converter, name);
        }

        return name;
    }

    String getParameterConverterName(ToNativeConverter converter) {
        String name = toNativeConverterNames.get(converter);
        if (name == null) {
            int idx = toNativeConverters.size();
            name = "toNativeConverter" + idx;
            toNativeConverters.add(converter);
            toNativeConverterNames.put(converter, name);
        }

        return name;
    }

    String getObjectParameterInfoName(ObjectParameterInfo info) {
        String name = objectParameterInfoNames.get(info);
        if (name == null) {
            int idx = objectParameterInfo.size();
            name = "parameterInfo_" + idx;
            objectParameterInfo.add(info);
            objectParameterInfoNames.put(info, name);
        }

        return name;
    }

    FromNativeConverter[] getFromNativeConverterArray() {
        return fromNativeConverters.toArray(new FromNativeConverter[fromNativeConverters.size()]);
    }

    ToNativeConverter[] getToNativeConverterArray() {
        return toNativeConverters.toArray(new ToNativeConverter[toNativeConverters.size()]);
    }

    ObjectParameterInfo[] getObjectParameterInfoArray() {
        return objectParameterInfo.toArray(new ObjectParameterInfo[objectParameterInfo.size()]);
    }

    Function[] getFunctionArray() {
        return functions.toArray(new Function[functions.size()]);
    }
}
