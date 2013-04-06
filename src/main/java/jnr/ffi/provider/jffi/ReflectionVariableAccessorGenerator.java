/*
 * Copyright (C) 2012 Wayne Meissner
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

import jnr.ffi.NativeType;
import jnr.ffi.Pointer;
import jnr.ffi.Variable;
import jnr.ffi.mapper.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;

import static jnr.ffi.provider.jffi.DefaultInvokerFactory.getNumberDataConverter;
import static jnr.ffi.provider.jffi.DefaultInvokerFactory.getNumberResultConverter;
import static jnr.ffi.provider.jffi.NumberUtil.sizeof;

/**
 *
 */
class ReflectionVariableAccessorGenerator {
    static Variable createVariableAccessor(jnr.ffi.Runtime runtime, Method method, long symbolAddress, SignatureTypeMapper typeMapper, Collection<Annotation> annotations) {

        java.lang.reflect.Type variableType = ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0];
        if (!(variableType instanceof Class)) {
            throw new IllegalArgumentException("unsupported variable class: " + variableType);
        }

        Class javaType = (Class) variableType;

        SimpleNativeContext context = new SimpleNativeContext(runtime, annotations);
        SignatureType signatureType = DefaultSignatureType.create(javaType, (FromNativeContext) context);
        jnr.ffi.mapper.FromNativeType mappedFromNativeType = typeMapper.getFromNativeType(signatureType, context);
        FromNativeConverter fromNativeConverter = mappedFromNativeType != null ? mappedFromNativeType.getFromNativeConverter() : null;
        jnr.ffi.mapper.ToNativeType mappedToNativeType = typeMapper.getToNativeType(signatureType, context);
        ToNativeConverter toNativeConverter = mappedToNativeType != null ? mappedToNativeType.getToNativeConverter() : null;


        Class boxedType = toNativeConverter != null ? toNativeConverter.nativeType() : javaType;
        NativeType nativeType = Types.getType(runtime, boxedType, annotations).getNativeType();
        jnr.ffi.provider.ToNativeType toNativeType = new jnr.ffi.provider.ToNativeType(javaType, nativeType, annotations, toNativeConverter, null);
        jnr.ffi.provider.FromNativeType fromNativeType = new jnr.ffi.provider.FromNativeType(javaType, nativeType, annotations, fromNativeConverter, null);
        Variable variable;
        Pointer memory = MemoryUtil.newPointer(runtime, symbolAddress);
        variable = getNativeVariableAccessor(memory, toNativeType, fromNativeType);
        return toNativeType.getToNativeConverter() != null
                ? getConvertingVariable(variable, toNativeType.getToNativeConverter(), fromNativeType.getFromNativeConverter())
                : variable;
    }

    static Variable getConvertingVariable(Variable nativeVariable, ToNativeConverter toNativeConverter, FromNativeConverter fromNativeConverter) {
        if ((toNativeConverter != null && fromNativeConverter == null) || toNativeConverter == null && fromNativeConverter != null) {
            throw new UnsupportedOperationException("convertible types must have both a ToNativeConverter and a FromNativeConverter");
        }
        return new ConvertingVariable(nativeVariable, toNativeConverter, fromNativeConverter);
    }

    static Variable getNativeVariableAccessor(Pointer memory, jnr.ffi.provider.ToNativeType toNativeType, jnr.ffi.provider.FromNativeType fromNativeType) {
        if (Pointer.class == toNativeType.effectiveJavaType()) {
            return new PointerVariable(memory);

        } else if (Number.class.isAssignableFrom(toNativeType.effectiveJavaType())) {
            return new NumberVariable(memory, getPointerOp(toNativeType.getNativeType()),
                    getNumberDataConverter(toNativeType.getNativeType()), getNumberResultConverter(fromNativeType));

        } else {
            throw new UnsupportedOperationException("unsupported variable type: " + toNativeType.effectiveJavaType());
        }
    }

    private static PointerOp<Number> getPointerOp(NativeType nativeType) {
        switch (nativeType) {
            case SCHAR:
            case UCHAR:
                return Int8PointerOp.INSTANCE;

            case SSHORT:
            case USHORT:
                return Int16PointerOp.INSTANCE;

            case SINT:
            case UINT:
                return Int32PointerOp.INSTANCE;

            case SLONGLONG:
            case ULONGLONG:
                return Int64PointerOp.INSTANCE;

            case SLONG:
            case ULONG:
            case ADDRESS:
                return sizeof(nativeType) == 4 ? Int32PointerOp.INSTANCE : Int64PointerOp.INSTANCE;

            case FLOAT:
                return FloatPointerOp.INSTANCE;

            case DOUBLE:
                return DoublePointerOp.INSTANCE;

        }
        throw new UnsupportedOperationException("cannot convert " + nativeType);
    }

    private static abstract class AbstractVariable<T> implements Variable<T> {
        protected final Pointer memory;

        protected AbstractVariable(Pointer memory) {
            this.memory = memory;
        }
    }

    private static final class ConvertingVariable implements Variable {
        private final Variable variable;
        private final ToNativeConverter toNativeConverter;
        private final FromNativeConverter fromNativeConverter;

        private ConvertingVariable(Variable variable, ToNativeConverter toNativeConverter, FromNativeConverter fromNativeConverter) {
            this.variable = variable;
            this.toNativeConverter = toNativeConverter;
            this.fromNativeConverter = fromNativeConverter;
        }

        @Override
        public Object get() {
            return fromNativeConverter.fromNative(variable.get(), null);
        }

        @Override
        public void set(Object value) {
            variable.set(toNativeConverter.toNative(value, null));
        }
    }


    private static final class NumberVariable extends AbstractVariable<Number> {
        private final DataConverter<Number, Number> dataConverter;
        private final DefaultInvokerFactory.ResultConverter<? extends Number, Number> resultConverter;
        private final PointerOp<Number> pointerOp;
        private NumberVariable(Pointer memory, PointerOp<Number> pointerOp,
                               DataConverter<Number, Number> dataConverter,
                               DefaultInvokerFactory.ResultConverter<? extends Number, Number> resultConverter) {
            super(memory);
            this.pointerOp = pointerOp;
            this.dataConverter = dataConverter;
            this.resultConverter = resultConverter;
        }

        @Override
        public Number get() {
            return resultConverter.fromNative(dataConverter.fromNative(pointerOp.get(memory), null), null);
        }

        @Override
        public void set(Number value) {
            pointerOp.put(memory, dataConverter.toNative(value, null));
        }
    }


    private static final class PointerVariable extends AbstractVariable<Pointer> {
        private PointerVariable(Pointer memory) {
            super(memory);
        }

        public Pointer get() {
            return memory.getPointer(0);
        }

        public void set(Pointer value) {
            if (value != null) memory.putPointer(0, value); else memory.putAddress(0, 0L);
        }
    }


    private static interface PointerOp<T> {
        public T get(Pointer memory);
        public void put(Pointer memory, T value);
    }

    private static final class Int8PointerOp implements PointerOp<Number> {
        static final PointerOp<Number> INSTANCE = new Int8PointerOp();
        @Override
        public Number get(Pointer memory) {
            return memory.getByte(0);
        }

        @Override
        public void put(Pointer memory, Number value) {
            memory.putByte(0, value.byteValue());
        }
    }

    private static final class Int16PointerOp implements PointerOp<Number> {
        static final PointerOp<Number> INSTANCE = new Int16PointerOp();
        @Override
        public Number get(Pointer memory) {
            return memory.getShort(0);
        }

        @Override
        public void put(Pointer memory, Number value) {
            memory.putShort(0, value.shortValue());
        }
    }

    private static final class Int32PointerOp implements PointerOp<Number> {
        static final PointerOp<Number> INSTANCE = new Int32PointerOp();
        @Override
        public Number get(Pointer memory) {
            return memory.getInt(0);
        }

        @Override
        public void put(Pointer memory, Number value) {
            memory.putInt(0, value.intValue());
        }
    }

    private static final class Int64PointerOp implements PointerOp<Number> {
        static final PointerOp<Number> INSTANCE = new Int64PointerOp();
        @Override
        public Number get(Pointer memory) {
            return memory.getLongLong(0);
        }

        @Override
        public void put(Pointer memory, Number value) {
            memory.putLongLong(0, value.longValue());
        }
    }

    private static final class FloatPointerOp implements PointerOp<Number> {
        static final PointerOp<Number> INSTANCE = new FloatPointerOp();
        @Override
        public Number get(Pointer memory) {
            return memory.getFloat(0);
        }

        @Override
        public void put(Pointer memory, Number value) {
            memory.putFloat(0, value.floatValue());
        }
    }


    private static final class DoublePointerOp implements PointerOp<Number> {
        static final PointerOp<Number> INSTANCE = new DoublePointerOp();
        @Override
        public Number get(Pointer memory) {
            return memory.getFloat(0);
        }

        @Override
        public void put(Pointer memory, Number value) {
            memory.putFloat(0, value.floatValue());
        }
    }
}
