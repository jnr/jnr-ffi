package jnr.ffi.provider.jffi;

import com.kenai.jffi.CallContext;
import com.kenai.jffi.Function;
import com.kenai.jffi.ObjectParameterInfo;
import jnr.ffi.Variable;
import jnr.ffi.mapper.FromNativeConverter;
import jnr.ffi.mapper.ToNativeConverter;
import org.objectweb.asm.ClassVisitor;

import java.lang.reflect.Modifier;
import java.util.*;

import static jnr.ffi.provider.jffi.AsmUtil.boxedType;
import static jnr.ffi.provider.jffi.AsmUtil.unboxNumber;
import static jnr.ffi.provider.jffi.CodegenUtils.ci;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;

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
    private final ObjectNameGenerator variableAccessorId = new ObjectNameGenerator("variableAccessor");
    private final ObjectNameGenerator genericObjectId = new ObjectNameGenerator("objectField");

    private final Map<ToNativeConverter, ObjectField> toNativeConverters = new IdentityHashMap<ToNativeConverter, ObjectField>();
    private final Map<FromNativeConverter, ObjectField> fromNativeConverters = new IdentityHashMap<FromNativeConverter, ObjectField>();
    private final Map<ObjectParameterInfo, ObjectField> objectParameterInfo = new HashMap<ObjectParameterInfo, ObjectField>();
    private final Map<Variable, ObjectField> variableAccessors = new HashMap<Variable, ObjectField>();
    private final Map<CallContext, ObjectField> callContextMap = new HashMap<CallContext, ObjectField>();
    private final Map<Long, ObjectField> functionAddresses = new HashMap<Long, ObjectField>();
    private final Map<Object, ObjectField> genericObjects = new IdentityHashMap<Object, ObjectField>();
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
        return getFromNativeConverterField(converter).name;
    }

    String getToNativeConverterName(ToNativeConverter converter) {
        return getToNativeConverterField(converter).name;
    }

    ObjectField getToNativeConverterField(ToNativeConverter converter) {
        Class converterClass = converter.getClass();
        return getField(toNativeConverters, converter,
                Modifier.isPublic(converterClass.getModifiers()) ? converterClass : ToNativeConverter.class,
                toNativeConverterId);
    }

    ObjectField getFromNativeConverterField(FromNativeConverter converter) {
        Class converterClass = converter.getClass();
        return getField(fromNativeConverters, converter,
                Modifier.isPublic(converterClass.getModifiers()) ? converterClass : FromNativeConverter.class,
                fromNativeConverterId);
    }


    String getObjectParameterInfoName(ObjectParameterInfo info) {
        return getField(objectParameterInfo, info, ObjectParameterInfo.class, objectParameterInfoId).name;
    }

    String getObjectFieldName(Object obj, Class klass) {
        return getField(genericObjects, obj, klass, genericObjectId).name;
    }

    String getVariableName(Variable variableAccessor) {
        return getField(variableAccessors, variableAccessor, Variable.class, variableAccessorId).name;
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

    Object[] getObjectFieldValues() {
        Object[] fieldObjects = new Object[objectFields.size()];
        int i = 0;
        for (ObjectField f : objectFields) {
            fieldObjects[i++] = f.value;
        }
        return fieldObjects;
    }

    void emitFieldInitialization(SkinnyMethodAdapter init, int objectsParameterIndex) {
        int i = 0;
        for (ObjectField f : objectFields) {
            getClassVisitor().visitField(ACC_PRIVATE | ACC_FINAL, f.name, ci(f.klass), null, null);
            init.aload(0);
            init.aload(objectsParameterIndex);
            init.pushInt(i++);
            init.aaload();

            if (f.klass.isPrimitive()) {
                Class boxedType = boxedType(f.klass);
                init.checkcast(boxedType);
                unboxNumber(init, boxedType, f.klass);
            } else {
                init.checkcast(f.klass);
            }
            init.putfield(getClassNamePath(), f.name, ci(f.klass));
        }
    }
}
