package jnr.ffi.provider.jffi;

import com.kenai.jffi.Function;
import jnr.ffi.mapper.FromNativeConverter;
import jnr.ffi.mapper.ToNativeConverter;
import org.objectweb.asm.ClassVisitor;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
class AsmBuilder {
    private final String classNamePath;
    private final ClassVisitor classVisitor;

    private final List<ToNativeConverter> toNativeConverters = new ArrayList<ToNativeConverter>();
    private final List<FromNativeConverter> fromNativeConverters = new ArrayList<FromNativeConverter>();
    private final Map<ToNativeConverter, String> toNativeConverterNames = new IdentityHashMap<ToNativeConverter, String>();
    private final Map<FromNativeConverter, String> fromNativeConverterNames = new IdentityHashMap<FromNativeConverter, String>();
    private final Map<Function, String> functionFieldNames = new IdentityHashMap<Function, String>();


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

    void addFunctionField(Function function, String functionFieldName) {
        functionFieldNames.put(function, functionFieldName);
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

    String getFunctionFieldName(Function function) {
        String name = functionFieldNames.get(function);
        if (name == null) {
            throw new IllegalStateException("no function name registered for " + function);
        }

        return name;
    }

    FromNativeConverter[] getFromNativeConverterArray() {
        return fromNativeConverters.toArray(new FromNativeConverter[fromNativeConverters.size()]);
    }

    ToNativeConverter[] getToNativeConverterArray() {
        return toNativeConverters.toArray(new ToNativeConverter[toNativeConverters.size()]);
    }
}
