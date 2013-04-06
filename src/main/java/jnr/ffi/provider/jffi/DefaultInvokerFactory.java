/*
 * Copyright (C) 2008-2012 Wayne Meissner
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

import com.kenai.jffi.Function;
import com.kenai.jffi.HeapInvocationBuffer;
import com.kenai.jffi.ObjectParameterStrategy;
import com.kenai.jffi.ObjectParameterType;
import jnr.ffi.Address;
import jnr.ffi.NativeType;
import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import jnr.ffi.mapper.*;
import jnr.ffi.provider.*;

import java.lang.annotation.Annotation;
import java.nio.*;
import java.util.Collection;

import static jnr.ffi.provider.jffi.NumberUtil.getBoxedClass;
import static jnr.ffi.provider.jffi.NumberUtil.sizeof;

final class DefaultInvokerFactory {

    final jnr.ffi.provider.Invoker createInvoker(jnr.ffi.Runtime runtime, NativeLibrary nativeLibrary, Function function, ResultType resultType, ParameterType[] parameterTypes) {

        Marshaller[] marshallers = new Marshaller[parameterTypes.length];
        for (int i = 0; i < marshallers.length; ++i) {
            marshallers[i] = getMarshaller(parameterTypes[i]);
        }

        FunctionInvoker invoker = getFunctionInvoker(resultType);
        if (resultType.getFromNativeConverter() != null) {
            invoker = new ConvertingInvoker(resultType.getFromNativeConverter(), resultType.getFromNativeContext(), invoker);
        }

        return new DefaultInvoker(runtime, nativeLibrary, function, invoker, marshallers);
    }

    private static FunctionInvoker getFunctionInvoker(ResultType resultType) {
        Class returnType = resultType.effectiveJavaType();
        if (Void.class.isAssignableFrom(returnType) || void.class == returnType) {
            return VoidInvoker.INSTANCE;
        
        } else if (Boolean.class.isAssignableFrom(returnType) || boolean.class == returnType) {
            return BooleanInvoker.INSTANCE;

        } else if (Number.class.isAssignableFrom(returnType) || returnType.isPrimitive()) {
            return new ConvertingInvoker(getNumberResultConverter(resultType), null,
                    new ConvertingInvoker(getNumberDataConverter(resultType.getNativeType()), null, getNumberFunctionInvoker(resultType.getNativeType())));

        } else if (Pointer.class.isAssignableFrom(returnType)) {
            return PointerInvoker.INSTANCE;

        } else {
            throw new IllegalArgumentException("Unknown return type: " + returnType);
        }
    }

    private static FunctionInvoker getNumberFunctionInvoker(NativeType nativeType) {
        switch (nativeType) {
            case SCHAR:
            case UCHAR:
            case SSHORT:
            case USHORT:
            case SINT:
            case UINT:
            case SLONG:
            case ULONG:
            case SLONGLONG:
            case ULONGLONG:
            case ADDRESS:
                return sizeof(nativeType) <= 4 ? IntInvoker.INSTANCE : LongInvoker.INSTANCE;

            case FLOAT:
                return Float32Invoker.INSTANCE;

            case DOUBLE:
                return Float64Invoker.INSTANCE;
        }

        throw new UnsupportedOperationException("unsupported numeric type: " + nativeType);
    }

    static Marshaller getMarshaller(ParameterType parameterType) {
        Marshaller marshaller = getMarshaller(parameterType.effectiveJavaType(), parameterType.getNativeType(), parameterType.getAnnotations());
        return parameterType.getToNativeConverter() != null
            ? new ToNativeConverterMarshaller(parameterType.getToNativeConverter(), parameterType.getToNativeContext(), marshaller)
            : marshaller;
    }

    static Marshaller getMarshaller(Class type, NativeType nativeType, Collection<Annotation> annotations) {
        if (Number.class.isAssignableFrom(type) || (type.isPrimitive() && Number.class.isAssignableFrom(getBoxedClass(type)))) {
            switch (nativeType) {
                case SCHAR:
                    return new Int8Marshaller(Signed8Converter.INSTANCE);
                case UCHAR:
                    return new Int8Marshaller(Unsigned8Converter.INSTANCE);

                case SSHORT:
                    return new Int16Marshaller(Signed16Converter.INSTANCE);
                case USHORT:
                    return new Int16Marshaller(Unsigned16Converter.INSTANCE);

                case SINT:
                    return new Int32Marshaller(Signed32Converter.INSTANCE);
                case UINT:
                    return new Int32Marshaller(Unsigned32Converter.INSTANCE);

                case SLONG:
                case ULONG:
                case ADDRESS:
                    return sizeof(nativeType) == 4 ? new Int32Marshaller(getNumberDataConverter(nativeType)): Int64Marshaller.INSTANCE;

                case SLONGLONG:
                case ULONGLONG:
                    return Int64Marshaller.INSTANCE;

                case FLOAT:
                    return Float32Marshaller.INSTANCE;

                case DOUBLE:
                    return Float64Marshaller.INSTANCE;
                default:
                    throw new IllegalArgumentException("Unsupported parameter type: " + type);
            }

        } else if (Boolean.class.isAssignableFrom(type) || boolean.class == type) {
            return BooleanMarshaller.INSTANCE;
        
        } else if (Pointer.class.isAssignableFrom(type)) {
            return new PointerMarshaller(annotations);

        } else if (ByteBuffer.class.isAssignableFrom(type)) {
            return new BufferMarshaller(ObjectParameterType.ComponentType.BYTE, annotations);
        
        } else if (ShortBuffer.class.isAssignableFrom(type)) {
            return new BufferMarshaller(ObjectParameterType.ComponentType.SHORT, annotations);
        
        } else if (IntBuffer.class.isAssignableFrom(type)) {
            return new BufferMarshaller(ObjectParameterType.ComponentType.INT, annotations);
        
        } else if (LongBuffer.class.isAssignableFrom(type)) {
            return new BufferMarshaller(ObjectParameterType.ComponentType.LONG, annotations);
        
        } else if (FloatBuffer.class.isAssignableFrom(type)) {
            return new BufferMarshaller(ObjectParameterType.ComponentType.FLOAT, annotations);
        
        } else if (DoubleBuffer.class.isAssignableFrom(type)) {
            return new BufferMarshaller(ObjectParameterType.ComponentType.DOUBLE, annotations);

        } else if (Buffer.class.isAssignableFrom(type)) {
            return new BufferMarshaller(null, annotations);
        
        } else if (type.isArray() && type.getComponentType() == byte.class) {
            return new PrimitiveArrayMarshaller(PrimitiveArrayParameterStrategy.BYTE, annotations);
        
        } else if (type.isArray() && type.getComponentType() == short.class) {
            return new PrimitiveArrayMarshaller(PrimitiveArrayParameterStrategy.SHORT, annotations);
        
        } else if (type.isArray() && type.getComponentType() == int.class) {
            return new PrimitiveArrayMarshaller(PrimitiveArrayParameterStrategy.INT, annotations);
        
        } else if (type.isArray() && type.getComponentType() == long.class) {
            return new PrimitiveArrayMarshaller(PrimitiveArrayParameterStrategy.LONG, annotations);
        
        } else if (type.isArray() && type.getComponentType() == float.class) {
            return new PrimitiveArrayMarshaller(PrimitiveArrayParameterStrategy.FLOAT, annotations);
        
        } else if (type.isArray() && type.getComponentType() == double.class) {
            return new PrimitiveArrayMarshaller(PrimitiveArrayParameterStrategy.DOUBLE, annotations);

        } else if (type.isArray() && type.getComponentType() == boolean.class) {
            return new PrimitiveArrayMarshaller(PrimitiveArrayParameterStrategy.BOOLEAN, annotations);
        
        } else {
            throw new IllegalArgumentException("Unsupported parameter type: " + type);
        }
    }

    static class DefaultInvoker implements jnr.ffi.provider.Invoker {
        protected final jnr.ffi.Runtime runtime;
        final Function function;
        final FunctionInvoker functionInvoker;
        final Marshaller[] marshallers;
        final NativeLibrary nativeLibrary;

        DefaultInvoker(jnr.ffi.Runtime runtime, NativeLibrary nativeLibrary, Function function, FunctionInvoker invoker, Marshaller[] marshallers) {
            this.runtime = runtime;
            this.nativeLibrary = nativeLibrary;
            this.function = function;
            this.functionInvoker = invoker;
            this.marshallers = marshallers;
        }

        public final Object invoke(Object self, Object[] parameters) {
            InvocationSession session = new InvocationSession();
            HeapInvocationBuffer buffer = new HeapInvocationBuffer(function.getCallContext());
            try {
                if (parameters != null) for (int i = 0; i < parameters.length; ++i) {
                    marshallers[i].marshal(session, buffer, parameters[i]);
                }

                return functionInvoker.invoke(runtime, function, buffer);
            } finally {
                session.finish();
            }
        }
    }
    
    static interface Marshaller {
        public abstract void marshal(InvocationSession session, HeapInvocationBuffer buffer, Object parameter);
    }

    static interface FunctionInvoker {
        Object invoke(Runtime runtime, Function function, HeapInvocationBuffer buffer);
    }

    static abstract class BaseInvoker implements FunctionInvoker {
        static com.kenai.jffi.Invoker invoker = com.kenai.jffi.Invoker.getInstance();
    }

    static class ConvertingInvoker extends BaseInvoker {
        private final FromNativeConverter fromNativeConverter;
        private final FromNativeContext fromNativeContext;
        private final FunctionInvoker nativeInvoker;

        public ConvertingInvoker(FromNativeConverter converter, FromNativeContext context, FunctionInvoker nativeInvoker) {
            this.fromNativeConverter = converter;
            this.fromNativeContext = context;
            this.nativeInvoker = nativeInvoker;
        }

        public final Object invoke(Runtime runtime, Function function, HeapInvocationBuffer buffer) {
            return fromNativeConverter.fromNative(nativeInvoker.invoke(runtime, function, buffer), fromNativeContext);
        }
    }

    static class VoidInvoker extends BaseInvoker {
        static FunctionInvoker INSTANCE = new VoidInvoker();
        public final Object invoke(Runtime runtime, Function function, HeapInvocationBuffer buffer) {
            invoker.invokeInt(function, buffer);
            return null;
        }
    }

    static class BooleanInvoker extends BaseInvoker {
        static FunctionInvoker INSTANCE = new BooleanInvoker();
        public final Object invoke(Runtime runtime, Function function, HeapInvocationBuffer buffer) {
            return invoker.invokeInt(function, buffer) != 0;
        }
    }

    static class IntInvoker extends BaseInvoker {
        static final FunctionInvoker INSTANCE = new IntInvoker();
        public final Object invoke(Runtime runtime, Function function, HeapInvocationBuffer buffer) {
            return invoker.invokeInt(function, buffer);
        }
    }

    static class LongInvoker extends BaseInvoker {
        static final FunctionInvoker INSTANCE = new LongInvoker();
        public final Object invoke(Runtime runtime, Function function, HeapInvocationBuffer buffer) {
            return invoker.invokeLong(function, buffer);
        }
    }

    static class Float32Invoker extends BaseInvoker {
        static final FunctionInvoker INSTANCE = new Float32Invoker();
        public final Object invoke(Runtime runtime, Function function, HeapInvocationBuffer buffer) {
            return invoker.invokeFloat(function, buffer);
        }
    }
    static class Float64Invoker extends BaseInvoker {
        static final FunctionInvoker INSTANCE = new Float64Invoker();
        public final Object invoke(Runtime runtime, Function function, HeapInvocationBuffer buffer) {
            return invoker.invokeDouble(function, buffer);
        }
    }

    static class PointerInvoker extends BaseInvoker {
        static final FunctionInvoker INSTANCE = new PointerInvoker();
        public final Object invoke(Runtime runtime, Function function, HeapInvocationBuffer buffer) {
            return MemoryUtil.newPointer(runtime, invoker.invokeAddress(function, buffer));
        }
    }

    /* ---------------------------------------------------------------------- */
    static class BooleanMarshaller implements Marshaller {
        static final Marshaller INSTANCE = new BooleanMarshaller();
        public void marshal(InvocationSession session, HeapInvocationBuffer buffer, Object parameter) {
            buffer.putInt(((Boolean) parameter).booleanValue() ? 1 : 0);
        }
    }

    static class Int8Marshaller implements Marshaller {
        private final ToNativeConverter<Number, Number> toNativeConverter;

        Int8Marshaller(ToNativeConverter<Number, Number> toNativeConverter) {
            this.toNativeConverter = toNativeConverter;
        }

        public void marshal(InvocationSession session, HeapInvocationBuffer buffer, Object parameter) {
            buffer.putByte(toNativeConverter.toNative((Number) parameter, null).intValue());
        }
    }

    static class Int16Marshaller implements Marshaller {
        private final ToNativeConverter<Number, Number> toNativeConverter;

        Int16Marshaller(ToNativeConverter<Number, Number> toNativeConverter) {
            this.toNativeConverter = toNativeConverter;
        }

        public void marshal(InvocationSession session, HeapInvocationBuffer buffer, Object parameter) {
            buffer.putShort(toNativeConverter.toNative((Number) parameter, null).intValue());
        }
    }
    
    static class Int32Marshaller implements Marshaller {
        private final ToNativeConverter<Number, Number> toNativeConverter;

        Int32Marshaller(ToNativeConverter<Number, Number> toNativeConverter) {
            this.toNativeConverter = toNativeConverter;
        }

        public void marshal(InvocationSession session, HeapInvocationBuffer buffer, Object parameter) {
            buffer.putInt(toNativeConverter.toNative((Number) parameter, null).intValue());
        }
    }
    
    static class Int64Marshaller implements Marshaller {
        static final Marshaller INSTANCE = new Int64Marshaller();
        public void marshal(InvocationSession session, HeapInvocationBuffer buffer, Object parameter) {
            buffer.putLong(((Number) parameter).longValue());
        }
    }
    
    static class Float32Marshaller implements Marshaller {
        static final Marshaller INSTANCE = new Float32Marshaller();
        public void marshal(InvocationSession session, HeapInvocationBuffer buffer, Object parameter) {
            buffer.putFloat(((Number) parameter).floatValue());
        }
    }
    static class Float64Marshaller implements Marshaller {
        static final Marshaller INSTANCE = new Float64Marshaller();
        public void marshal(InvocationSession session, HeapInvocationBuffer buffer, Object parameter) {
            buffer.putDouble(((Number) parameter).doubleValue());
        }
    }
    static class PointerMarshaller implements Marshaller {
        private final int flags;

        PointerMarshaller(Collection<Annotation> annotations) {
            this.flags = AsmUtil.getNativeArrayFlags(annotations);
        }

        public void marshal(InvocationSession session, HeapInvocationBuffer buffer, Object parameter) {
            buffer.putObject(parameter, AsmRuntime.pointerParameterStrategy((Pointer) parameter), flags);
        }
    }

    static class PrimitiveArrayMarshaller implements Marshaller {
        private final PrimitiveArrayParameterStrategy strategy;
        private final int flags;

        protected PrimitiveArrayMarshaller(PrimitiveArrayParameterStrategy strategy, Collection<Annotation> annotations) {
            this.strategy = strategy;
            this.flags = AsmUtil.getNativeArrayFlags(annotations);
        }

        public final void marshal(InvocationSession session, HeapInvocationBuffer buffer, Object parameter) {
            buffer.putObject(parameter, parameter != null ? strategy : NullObjectParameterStrategy.NULL, flags);
        }
    }

    static class BufferMarshaller implements Marshaller {
        private final ObjectParameterType.ComponentType componentType;
        private final int flags;

        BufferMarshaller(ObjectParameterType.ComponentType componentType, Collection<Annotation> annotations) {
            this.componentType = componentType;
            this.flags = AsmUtil.getNativeArrayFlags(annotations);
        }

        public final void marshal(InvocationSession session, HeapInvocationBuffer buffer, Object parameter) {
            ObjectParameterStrategy strategy = componentType != null
                    ? AsmRuntime.bufferParameterStrategy((Buffer) parameter, componentType)
                    : AsmRuntime.pointerParameterStrategy((Buffer) parameter);
            buffer.putObject(parameter, strategy, flags);
        }
    }

    static class ToNativeConverterMarshaller implements Marshaller {
        private final ToNativeConverter converter;
        private final ToNativeContext context;
        private final Marshaller marshaller;
        private final boolean isPostInvokeRequired;

        public ToNativeConverterMarshaller(ToNativeConverter toNativeConverter, ToNativeContext toNativeContext, Marshaller marshaller) {
            this.converter = toNativeConverter;
            this.context = toNativeContext;
            this.marshaller = marshaller;
            this.isPostInvokeRequired = converter instanceof ToNativeConverter.PostInvocation;
        }

        @Override
        public void marshal(InvocationSession session, HeapInvocationBuffer buffer, final Object parameter) {
            final Object nativeValue = converter.toNative(parameter, context);
            marshaller.marshal(session, buffer, nativeValue);

            if (isPostInvokeRequired) {
                session.addPostInvoke(new InvocationSession.PostInvoke() {
                    @Override
                    public void postInvoke() {
                        ((ToNativeConverter.PostInvocation) converter).postInvoke(parameter, nativeValue, context);
                    }
                });
            } else {
                // hold on to the native value until the entire call session is complete
                session.keepAlive(nativeValue);
            }
        }
        
    }

    private static boolean isUnsigned(NativeType nativeType) {
        switch (nativeType) {
            case UCHAR:
            case USHORT:
            case UINT:
            case ULONG:
                return true;

            default:
                return false;
        }
    }

    static DataConverter<Number, Number> getNumberDataConverter(NativeType nativeType) {
        switch (nativeType) {
            case SCHAR:
                return DefaultInvokerFactory.Signed8Converter.INSTANCE;

            case UCHAR:
                return DefaultInvokerFactory.Unsigned8Converter.INSTANCE;

            case SSHORT:
                return DefaultInvokerFactory.Signed16Converter.INSTANCE;

            case USHORT:
                return DefaultInvokerFactory.Unsigned16Converter.INSTANCE;

            case SINT:
                return DefaultInvokerFactory.Signed32Converter.INSTANCE;

            case UINT:
                return DefaultInvokerFactory.Unsigned32Converter.INSTANCE;

            case SLONG:
                return sizeof(nativeType) == 4 ? DefaultInvokerFactory.Signed32Converter.INSTANCE : DefaultInvokerFactory.LongLongConverter.INSTANCE;

            case ULONG:
            case ADDRESS:
                return sizeof(nativeType) == 4 ? DefaultInvokerFactory.Unsigned32Converter.INSTANCE : DefaultInvokerFactory.LongLongConverter.INSTANCE;

            case SLONGLONG:
            case ULONGLONG:
                return DefaultInvokerFactory.LongLongConverter.INSTANCE;

            case FLOAT:
                return DefaultInvokerFactory.FloatConverter.INSTANCE;

            case DOUBLE:
                return DefaultInvokerFactory.DoubleConverter.INSTANCE;

        }
        throw new UnsupportedOperationException("cannot convert " + nativeType);
    }

    static abstract class NumberDataConverter implements DataConverter<Number, Number> {
        @Override
        public final Class<Number> nativeType() {
            return Number.class;
        }
    }

    static final class Signed8Converter extends NumberDataConverter {
        static final NumberDataConverter INSTANCE = new Signed8Converter();
        @Override
        public Number fromNative(Number nativeValue, FromNativeContext context) {
            return nativeValue.byteValue();
        }

        @Override
        public Number toNative(Number value, ToNativeContext context) {
            return value.byteValue();
        }
    }

    static final class Unsigned8Converter extends NumberDataConverter {
        static final NumberDataConverter INSTANCE = new Unsigned8Converter();
        @Override
        public Number fromNative(Number nativeValue, FromNativeContext context) {
            int value = nativeValue.byteValue();
            return value < 0 ? ((value & 0x7f) + 0x80) : value;
        }

        @Override
        public Number toNative(Number value, ToNativeContext context) {
            return value.intValue() & 0xffff;
        }
    }

    static final class Signed16Converter extends NumberDataConverter {
        static final NumberDataConverter INSTANCE = new Signed16Converter();
        @Override
        public Number fromNative(Number nativeValue, FromNativeContext context) {
            return nativeValue.shortValue();
        }

        @Override
        public Number toNative(Number value, ToNativeContext context) {
            return value.shortValue();
        }
    }

    static final class Unsigned16Converter extends NumberDataConverter {
        static final NumberDataConverter INSTANCE = new Unsigned16Converter();
        @Override
        public Number fromNative(Number nativeValue, FromNativeContext context) {
            int value = nativeValue.shortValue();
            return value < 0 ? ((value & 0x7fff) + 0x8000) : value;
        }

        @Override
        public Number toNative(Number value, ToNativeContext context) {
            return value.intValue() & 0xffff;
        }
    }

    static final class Signed32Converter extends NumberDataConverter {
        static final NumberDataConverter INSTANCE = new Signed32Converter();
        @Override
        public Number fromNative(Number nativeValue, FromNativeContext context) {
            return nativeValue.intValue();
        }

        @Override
        public Number toNative(Number value, ToNativeContext context) {
            return value.intValue();
        }
    }

    static final class Unsigned32Converter extends NumberDataConverter {
        static final NumberDataConverter INSTANCE = new Unsigned32Converter();
        @Override
        public Number fromNative(Number nativeValue, FromNativeContext context) {
            long value = nativeValue.intValue();
            return value < 0 ? ((value & 0x7fffffffL) + 0x80000000L) : value;
        }

        @Override
        public Number toNative(Number value, ToNativeContext context) {
            return value.longValue() & 0xffffffffL;
        }
    }

    static final class LongLongConverter extends NumberDataConverter {
        static final NumberDataConverter INSTANCE = new LongLongConverter();
        @Override
        public Number fromNative(Number nativeValue, FromNativeContext context) {
            return nativeValue.longValue();
        }

        @Override
        public Number toNative(Number value, ToNativeContext context) {
            return value.longValue();
        }
    }

    static final class FloatConverter extends NumberDataConverter {
        static final NumberDataConverter INSTANCE = new FloatConverter();
        @Override
        public Number fromNative(Number nativeValue, FromNativeContext context) {
            return nativeValue.floatValue();
        }

        @Override
        public Number toNative(Number value, ToNativeContext context) {
            return value.floatValue();
        }
    }

    static final class DoubleConverter extends NumberDataConverter {
        static final NumberDataConverter INSTANCE = new DoubleConverter();
        @Override
        public Number fromNative(Number nativeValue, FromNativeContext context) {
            return nativeValue.doubleValue();
        }

        @Override
        public Number toNative(Number value, ToNativeContext context) {
            return value.doubleValue();
        }
    }

    static final class BooleanConverter implements DataConverter<Boolean, Number> {
        static final DataConverter<Boolean, Number> INSTANCE = new BooleanConverter();
        @Override
        public Boolean fromNative(Number nativeValue, FromNativeContext context) {
            return (nativeValue.intValue() & 0x1) != 0;
        }

        @Override
        public Number toNative(Boolean value, ToNativeContext context) {
            return value ? 1 : 0;
        }

        @Override
        public Class<Number> nativeType() {
            return Number.class;
        }
    }

    static interface ResultConverter<J, N> extends FromNativeConverter<J, N> {
        J fromNative(N value, FromNativeContext fromNativeContext);
    }

    static ResultConverter<? extends Number, Number> getNumberResultConverter(jnr.ffi.provider.FromNativeType fromNativeType) {
        if (Byte.class == fromNativeType.effectiveJavaType() || byte.class == fromNativeType.effectiveJavaType()) {
            return ByteResultConverter.INSTANCE;

        } else if (Short.class == fromNativeType.effectiveJavaType() || short.class == fromNativeType.effectiveJavaType()) {
            return ShortResultConverter.INSTANCE;

        } else if (Integer.class == fromNativeType.effectiveJavaType() || int.class == fromNativeType.effectiveJavaType()) {
            return IntegerResultConverter.INSTANCE;

        } else if (Long.class == fromNativeType.effectiveJavaType() || long.class == fromNativeType.effectiveJavaType()) {
            return LongResultConverter.INSTANCE;

        } else if (Float.class == fromNativeType.effectiveJavaType() || float.class == fromNativeType.effectiveJavaType()) {
            return FloatResultConverter.INSTANCE;

        } else if (Double.class == fromNativeType.effectiveJavaType() || double.class == fromNativeType.effectiveJavaType()) {
            return DoubleResultConverter.INSTANCE;

        } else if (Address.class == fromNativeType.effectiveJavaType()) {
            return AddressResultConverter.INSTANCE;

        } else {
            throw new UnsupportedOperationException("cannot convert to " + fromNativeType.effectiveJavaType());
        }
    }


    static abstract class AbstractNumberResultConverter<T> implements ResultConverter<T, Number> {
        @Override
        public final Class<Number> nativeType() {
            return Number.class;
        }
    }

    static final class ByteResultConverter extends AbstractNumberResultConverter<Byte> {
        static final ResultConverter<? extends Number, Number> INSTANCE = new ByteResultConverter();
        @Override
        public Byte fromNative(Number value, FromNativeContext fromNativeContext) {
            return value.byteValue();
        }
    }

    static final class ShortResultConverter extends AbstractNumberResultConverter<Short> {
        static final ResultConverter<? extends Number, Number> INSTANCE = new ShortResultConverter();
        @Override
        public Short fromNative(Number value, FromNativeContext fromNativeContext) {
            return value.shortValue();
        }
    }

    static final class IntegerResultConverter extends AbstractNumberResultConverter<Integer> {
        static final ResultConverter<? extends Number, Number> INSTANCE = new IntegerResultConverter();
        @Override
        public Integer fromNative(Number value, FromNativeContext fromNativeContext) {
            return value.intValue();
        }
    }

    static final class LongResultConverter extends AbstractNumberResultConverter<Long> {
        static final ResultConverter<? extends Number, Number> INSTANCE = new LongResultConverter();
        @Override
        public Long fromNative(Number value, FromNativeContext fromNativeContext) {
            return value.longValue();
        }
    }

    static final class FloatResultConverter extends AbstractNumberResultConverter<Float> {
        static final ResultConverter<? extends Number, Number> INSTANCE = new FloatResultConverter();
        @Override
        public Float fromNative(Number value, FromNativeContext fromNativeContext) {
            return value.floatValue();
        }
    }

    static final class DoubleResultConverter extends AbstractNumberResultConverter<Double> {
        static final ResultConverter<? extends Number, Number> INSTANCE = new DoubleResultConverter();
        @Override
        public Double fromNative(Number value, FromNativeContext fromNativeContext) {
            return value.doubleValue();
        }
    }

    static final class AddressResultConverter extends AbstractNumberResultConverter<Address> {
        static final ResultConverter<? extends Number, Number> INSTANCE = new AddressResultConverter();
        @Override
        public Address fromNative(Number value, FromNativeContext fromNativeContext) {
            return Address.valueOf(value.longValue());
        }
    }
}
