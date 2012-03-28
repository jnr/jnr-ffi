package jnr.ffi.provider.jffi;

import com.kenai.jffi.CallContext;
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

    private final ObjectNameGenerator functionId = new ObjectNameGenerator("functionAddress");
    private final ObjectNameGenerator contextId = new ObjectNameGenerator("callContext");
    private final ObjectNameGenerator toNativeConverterId = new ObjectNameGenerator("toNativeConverter");
    private final ObjectNameGenerator fromNativeConverterId = new ObjectNameGenerator("fromNativeConverter");
    private final ObjectNameGenerator objectParameterInfoId = new ObjectNameGenerator("objectParameterInfo");

    private final Map<ToNativeConverter, ObjectField> toNativeConverters = new IdentityHashMap<ToNativeConverter, ObjectField>();
    private final Map<FromNativeConverter, ObjectField> fromNativeConverters = new IdentityHashMap<FromNativeConverter, ObjectField>();
    private final Map<ObjectParameterInfo, ObjectField> objectParameterInfo = new HashMap<ObjectParameterInfo, ObjectField>();
    private final Map<CallContext, ObjectField> callContextMap = new HashMap<CallContext, ObjectField>();
    private final Map<Long, ObjectField> functionAddresses = new HashMap<Long, ObjectField>();
    private final List<ObjectField> objectFields = new ArrayList<ObjectField>();

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


    private static final class ObjectNameGenerator {
        private final String baseName;
        private int value;
        ObjectNameGenerator(String baseName) {
            this.baseName = baseName;
            this.value = 0;
        }

        String generateName() {
            return baseName + "_" + ++value;
        }
    }

    <T> ObjectField addField(Map<T, ObjectField> map, T value, Class klass, ObjectNameGenerator objectNameGenerator) {
        ObjectField field = new ObjectField(objectNameGenerator.generateName(), value, klass);
        objectFields.add(field);
        map.put(value, field);
        return field;
    }

    <T> ObjectField getField(Map<T, ObjectField> map, T value, Class klass, ObjectNameGenerator objectNameGenerator) {
        ObjectField field = map.get(value);
        return field != null ? field : addField(map, value, klass, objectNameGenerator);
    }

    String getCallContextFieldName(Function function) {
        return getField(callContextMap, function.getCallContext(), CallContext.class, contextId).name;
    }

    String getFunctionAddressFieldName(Function function) {
        return getField(functionAddresses, function.getFunctionAddress(), long.class, functionId).name;
    }


    String getFromNativeConverterName(FromNativeConverter converter) {
        return getField(fromNativeConverters, converter, FromNativeConverter.class, fromNativeConverterId).name;
    }

    String getToNativeConverterName(ToNativeConverter converter) {
        return getField(toNativeConverters, converter, ToNativeConverter.class, toNativeConverterId).name;
    }

    String getObjectParameterInfoName(ObjectParameterInfo info) {
        return getField(objectParameterInfo, info, ObjectParameterInfo.class, objectParameterInfoId).name;
    }

    public static final class ObjectField {
        public final String name;
        public final Object value;
        public final Class klass;

        public ObjectField(String fieldName, Object fieldValue, Class fieldClass) {
            this.name = fieldName;
            this.value = fieldValue;
            this.klass = fieldClass;
        }
    }

    ObjectField[] getObjectFieldArray() {
        return objectFields.toArray(new ObjectField[objectFields.size()]);
    }
}
