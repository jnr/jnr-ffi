/*
 * Copyright (C) 2011 Wayne Meissner
 *
 * This file is part of the JNR project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jnr.ffi.provider.jffi;

import com.kenai.jffi.CallContext;
import com.kenai.jffi.Function;
import com.kenai.jffi.ObjectParameterInfo;
import jnr.ffi.*;
import jnr.ffi.Runtime;
import jnr.ffi.mapper.FromNativeContext;
import jnr.ffi.mapper.FromNativeConverter;
import jnr.ffi.mapper.ToNativeContext;
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
    private final jnr.ffi.Runtime runtime;
    private final String classNamePath;
    private final ClassVisitor classVisitor;
    private final AsmClassLoader classLoader;

    private final ObjectNameGenerator functionId = new ObjectNameGenerator("functionAddress");
    private final ObjectNameGenerator contextId = new ObjectNameGenerator("callContext");
    private final ObjectNameGenerator toNativeConverterId = new ObjectNameGenerator("toNativeConverter");
    private final ObjectNameGenerator toNativeContextId = new ObjectNameGenerator("toNativeContext");
    private final ObjectNameGenerator fromNativeConverterId = new ObjectNameGenerator("fromNativeConverter");
    private final ObjectNameGenerator fromNativeContextId = new ObjectNameGenerator("fromNativeContext");
    private final ObjectNameGenerator objectParameterInfoId = new ObjectNameGenerator("objectParameterInfo");
    private final ObjectNameGenerator variableAccessorId = new ObjectNameGenerator("variableAccessor");
    private final ObjectNameGenerator genericObjectId = new ObjectNameGenerator("objectField");

    private final Map<ToNativeConverter, ObjectField> toNativeConverters = new IdentityHashMap<ToNativeConverter, ObjectField>();
    private final Map<ToNativeContext, ObjectField> toNativeContexts = new IdentityHashMap<ToNativeContext, ObjectField>();
    private final Map<FromNativeConverter, ObjectField> fromNativeConverters = new IdentityHashMap<FromNativeConverter, ObjectField>();
    private final Map<FromNativeContext, ObjectField> fromNativeContexts = new IdentityHashMap<FromNativeContext, ObjectField>();
    private final Map<ObjectParameterInfo, ObjectField> objectParameterInfo = new HashMap<ObjectParameterInfo, ObjectField>();
    private final Map<Variable, ObjectField> variableAccessors = new HashMap<Variable, ObjectField>();
    private final Map<CallContext, ObjectField> callContextMap = new HashMap<CallContext, ObjectField>();
    private final Map<Long, ObjectField> functionAddresses = new HashMap<Long, ObjectField>();
    private final Map<Object, ObjectField> genericObjects = new IdentityHashMap<Object, ObjectField>();
    private final List<ObjectField> objectFields = new ArrayList<ObjectField>();

    AsmBuilder(jnr.ffi.Runtime runtime, String classNamePath, ClassVisitor classVisitor, AsmClassLoader classLoader) {
        this.runtime = runtime;
        this.classNamePath = classNamePath;
        this.classVisitor = classVisitor;
        this.classLoader = classLoader;
    }

    public String getClassNamePath() {
        return classNamePath;
    }

    ClassVisitor getClassVisitor() {
        return classVisitor;
    }

    public AsmClassLoader getClassLoader() {
        return classLoader;
    }

    public jnr.ffi.Runtime getRuntime() {
        return runtime;
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

    String getCallContextFieldName(CallContext callContext) {
        return getField(callContextMap, callContext, CallContext.class, contextId).name;
    }

    String getFunctionAddressFieldName(Function function) {
        return getField(functionAddresses, function.getFunctionAddress(), long.class, functionId).name;
    }

    ObjectField getRuntimeField() {
        return getObjectField(runtime, runtime.getClass());
    }

    String getFromNativeConverterName(FromNativeConverter converter) {
        return getFromNativeConverterField(converter).name;
    }

    String getToNativeConverterName(ToNativeConverter converter) {
        return getToNativeConverterField(converter).name;
    }

    private static Class nearestClass(Object obj, Class defaultClass) {
        return Modifier.isPublic(obj.getClass().getModifiers()) ? obj.getClass() : defaultClass;
    }

    ObjectField getToNativeConverterField(ToNativeConverter converter) {
        return getField(toNativeConverters, converter, nearestClass(converter, ToNativeConverter.class), toNativeConverterId);
    }

    ObjectField getFromNativeConverterField(FromNativeConverter converter) {
        return getField(fromNativeConverters, converter, nearestClass(converter, FromNativeConverter.class), fromNativeConverterId);
    }

    ObjectField getToNativeContextField(ToNativeContext context) {
        return getField(toNativeContexts, context, nearestClass(context, ToNativeContext.class), toNativeContextId);
    }

    ObjectField getFromNativeContextField(FromNativeContext context) {
        return getField(fromNativeContexts, context, nearestClass(context, FromNativeContext.class), fromNativeContextId);
    }


    String getObjectParameterInfoName(ObjectParameterInfo info) {
        return getField(objectParameterInfo, info, ObjectParameterInfo.class, objectParameterInfoId).name;
    }

    String getObjectFieldName(Object obj, Class klass) {
        return getField(genericObjects, obj, klass, genericObjectId).name;
    }

    ObjectField getObjectField(Object obj, Class klass) {
        return getField(genericObjects, obj, klass, genericObjectId);
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
