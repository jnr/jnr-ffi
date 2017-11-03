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

import static jnr.ffi.provider.jffi.InvokerUtil.getCallContext;
import static jnr.ffi.provider.jffi.InvokerUtil.getParameterTypes;
import static jnr.ffi.provider.jffi.InvokerUtil.getResultType;
import static jnr.ffi.provider.jffi.NumberUtil.getBoxedClass;
import static jnr.ffi.provider.jffi.NumberUtil.sizeof;
import static jnr.ffi.util.Annotations.sortedAnnotationCollection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import jnr.ffi.Address;
import jnr.ffi.CallingConvention;
import jnr.ffi.LibraryLoader;
import jnr.ffi.LibraryOption;
import jnr.ffi.NativeType;
import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import jnr.ffi.annotations.Meta;
import jnr.ffi.annotations.StdCall;
import jnr.ffi.annotations.Synchronized;
import jnr.ffi.mapper.DataConverter;
import jnr.ffi.mapper.DefaultSignatureType;
import jnr.ffi.mapper.FromNativeContext;
import jnr.ffi.mapper.FromNativeConverter;
import jnr.ffi.mapper.FunctionMapper;
import jnr.ffi.mapper.MethodResultContext;
import jnr.ffi.mapper.SignatureType;
import jnr.ffi.mapper.SignatureTypeMapper;
import jnr.ffi.mapper.ToNativeContext;
import jnr.ffi.mapper.ToNativeConverter;
import jnr.ffi.mapper.ToNativeType;
import jnr.ffi.provider.InvocationSession;
import jnr.ffi.provider.Invoker;
import jnr.ffi.provider.NativeFunction;
import jnr.ffi.provider.ParameterType;
import jnr.ffi.provider.ResultType;
import jnr.ffi.provider.SigType;
import jnr.ffi.util.AnnotationProxy;

import com.kenai.jffi.Function;
import com.kenai.jffi.HeapInvocationBuffer;
import com.kenai.jffi.ObjectParameterStrategy;
import com.kenai.jffi.ObjectParameterType;

final class DefaultInvokerFactory {
    private final Runtime runtime;
    private final NativeLibrary library;
    private final SignatureTypeMapper typeMapper;
    private final FunctionMapper functionMapper;
    private final jnr.ffi.CallingConvention libraryCallingConvention;
    private final boolean libraryIsSynchronized;
    private final Map<LibraryOption, ?> libraryOptions;

    public DefaultInvokerFactory(
            Runtime runtime,
            NativeLibrary library,
            SignatureTypeMapper typeMapper,
            FunctionMapper functionMapper,
            CallingConvention libraryCallingConvention,
            Map<LibraryOption, ?> libraryOptions,
            boolean libraryIsSynchronized) {
        super();
        this.runtime = runtime;
        this.library = library;
        this.typeMapper = typeMapper;
        this.functionMapper = functionMapper;
        this.libraryCallingConvention = libraryCallingConvention;
        this.libraryIsSynchronized = libraryIsSynchronized;
        this.libraryOptions = libraryOptions;
    }

    public Invoker createInvoker(Method method) {
        Collection<Annotation> annotations = sortedAnnotationCollection(method.getAnnotations());
        String functionName = functionMapper.mapFunctionName(method.getName(), new NativeFunctionMapperContext(library, annotations));
        long functionAddress = library.getSymbolAddress(functionName);
        if (functionAddress == 0L) {
            return new FunctionNotFoundInvoker(method, functionName);
        }

        FromNativeContext resultContext = new MethodResultContext(NativeRuntime.getInstance(), method);
        SignatureType signatureType = DefaultSignatureType.create(method.getReturnType(), resultContext);
        ResultType resultType = getResultType(runtime, method.getReturnType(),
                resultContext.getAnnotations(), typeMapper.getFromNativeType(signatureType, resultContext),
                resultContext);
        
        FunctionInvoker functionInvoker = getFunctionInvoker(resultType);
        if (resultType.getFromNativeConverter() != null) {
            functionInvoker = new ConvertingInvoker(resultType.getFromNativeConverter(), resultType.getFromNativeContext(), functionInvoker);
        }
        
        ParameterType[] parameterTypes = getParameterTypes(runtime, typeMapper, method);
        //Allow individual methods to set the calling convention to stdcall
        CallingConvention callingConvention = method.isAnnotationPresent(StdCall.class)
                ? CallingConvention.STDCALL : libraryCallingConvention;

        boolean saveError = LibraryLoader.saveError(libraryOptions, NativeFunction.hasSaveError(method), NativeFunction.hasIgnoreError(method));
        
        Invoker invoker;
        if (method.isVarArgs()) {
            invoker = new VariadicInvoker(runtime, functionInvoker, typeMapper, parameterTypes, functionAddress, resultType, saveError, callingConvention);
        } else {
            Function function = new Function(functionAddress,
                    getCallContext(resultType, parameterTypes, callingConvention, saveError));

            Marshaller[] marshallers = new Marshaller[parameterTypes.length];
            for (int i = 0; i < marshallers.length; ++i) {
                marshallers[i] = getMarshaller(parameterTypes[i]);
            }

            return new DefaultInvoker(runtime, library, function, functionInvoker, marshallers);
        }

        //
        // If either the method or the library is specified as requiring
        // synchronization, then wrap the raw invoker in a synchronized proxy
        //
        return libraryIsSynchronized || method.isAnnotationPresent(Synchronized.class)
                ? new SynchronizedInvoker(invoker) : invoker;
    }

    private static FunctionInvoker getFunctionInvoker(ResultType resultType) {
        Class returnType = resultType.effectiveJavaType();
        if (Void.class.isAssignableFrom(returnType) || void.class == returnType) {
            return VoidInvoker.INSTANCE;
        
        } else if (Boolean.class.isAssignableFrom(returnType) || boolean.class == returnType) {
            return BooleanInvoker.INSTANCE;

        } else if (Character.class.isAssignableFrom(returnType) || char.class == returnType) {
            return CharacterInvoker.INSTANCE;

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
        
        } else if (Character.class.isAssignableFrom(type) || char.class == type) {
            return CharacterMarshaller.INSTANCE;
        
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

    static class VariadicInvoker implements jnr.ffi.provider.Invoker {
        private final jnr.ffi.Runtime runtime;
        private final FunctionInvoker functionInvoker;
        private final SignatureTypeMapper typeMapper;
        private final ParameterType[] fixedParameterTypes;
        private final long functionAddress;
        private final SigType resultType;
        private final boolean requiresErrno;
        private final CallingConvention callingConvention;

        VariadicInvoker(Runtime runtime,
                FunctionInvoker functionInvoker, SignatureTypeMapper typeMapper,
                ParameterType[] fixedParameterTypes, long functionAddress,
                SigType resultType, boolean requiresErrno,
                CallingConvention callingConvention) {
            super();
            this.runtime = runtime;
            this.functionInvoker = functionInvoker;
            this.typeMapper = typeMapper;
            this.fixedParameterTypes = fixedParameterTypes;
            this.functionAddress = functionAddress;
            this.resultType = resultType;
            this.requiresErrno = requiresErrno;
            this.callingConvention = callingConvention;
        }

        public final Object invoke(Object self, Object[] parameters) {
            Object[] varParam = (Object[])parameters[parameters.length - 1];
            ParameterType[] argTypes = new ParameterType[fixedParameterTypes.length + varParam.length];
            System.arraycopy(fixedParameterTypes, 0, argTypes, 0, fixedParameterTypes.length - 1);

            Object[] variableArgs = new Object[varParam.length + 1];
            int variableArgsCount = 0;
            List<Class<? extends Annotation>> paramAnnotations = new ArrayList<Class<? extends Annotation>>();

            for (Object arg : varParam) {
                if (arg instanceof Class && Annotation.class.isAssignableFrom((Class)arg)) {
                    paramAnnotations.add((Class)arg);
                } else {
                    Class<?> argClass;
                    ToNativeConverter<?, ?> toNativeConverter = null;
                    Collection<Annotation> annos = getAnnotations(paramAnnotations);
                    paramAnnotations.clear();
                    ToNativeContext toNativeContext = new SimpleNativeContext(runtime, annos);

                    if (arg != null) {
                        ToNativeType toNativeType = typeMapper.getToNativeType(DefaultSignatureType.create(arg.getClass(), toNativeContext), toNativeContext);
                        toNativeConverter = toNativeType == null ? null : toNativeType.getToNativeConverter();
                        argClass = toNativeConverter == null ? arg.getClass() : toNativeConverter.nativeType();
                        variableArgs[variableArgsCount] = arg;
                    } else {
                        argClass = Pointer.class;
                        variableArgs[variableArgsCount] = arg;
                    }

                    argTypes[fixedParameterTypes.length + variableArgsCount - 1] = new ParameterType(
                            argClass, 
                            Types.getType(runtime, argClass, annos).getNativeType(), 
                            annos, 
                            toNativeConverter, 
                            new SimpleNativeContext(runtime, annos));
                    variableArgsCount++;
                }
            }
            
            //Add one extra vararg of NULL to meet the common convention of ending
            //varargs with a NULL.  Functions that get a length from the fixed arguments
            //will ignore the extra, and funtions that expect the extra NULL will get it.
            //This matches what JNA does.
            argTypes[fixedParameterTypes.length + variableArgsCount - 1] = new ParameterType(
                    Pointer.class, 
                    Types.getType(runtime, Pointer.class, Collections.<Annotation>emptyList()).getNativeType(), 
                    Collections.<Annotation>emptyList(), 
                    null, 
                    new SimpleNativeContext(runtime, Collections.<Annotation>emptyList()));
            variableArgs[variableArgsCount] = null;
            variableArgsCount++;

            Function function = new Function(functionAddress,
                    getCallContext(resultType, argTypes, variableArgsCount + fixedParameterTypes.length - 1, callingConvention, requiresErrno));
            HeapInvocationBuffer buffer = new HeapInvocationBuffer(function.getCallContext());

            InvocationSession session = new InvocationSession();
            try {
                if (parameters != null) for (int i = 0; i < parameters.length - 1; ++i) {
                    getMarshaller(argTypes[i]).marshal(session, buffer, parameters[i]);
                }
                
                for (int i = 0; i < variableArgsCount; ++i) {
                    getMarshaller(argTypes[i + fixedParameterTypes.length - 1]).marshal(session, buffer, variableArgs[i]);
                }

                return functionInvoker.invoke(runtime, function, buffer);
            } finally {
                session.finish();
            }
        }
        
        private static Collection<Annotation> getAnnotations(Collection<Class<? extends Annotation>> klasses) {
            List<Annotation> ret = new ArrayList<Annotation>();
            for (Class<? extends Annotation> klass : klasses) {
                if (klass.getAnnotation(Meta.class) != null) {
                    for (Annotation anno : klass.getAnnotations()) {
                        if (anno.annotationType().getName().startsWith("java") 
                                || Meta.class.equals(anno.annotationType())) {
                            continue;
                        }
                        ret.add(anno);
                    }
                } else {
                    ret.add(AnnotationProxy.newProxy(klass));
                }
            }
            return ret;
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

    private static final class SynchronizedInvoker implements Invoker {
        private final Invoker invoker;
        public SynchronizedInvoker(Invoker invoker) {
            this.invoker = invoker;
        }

        @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
        public Object invoke(Object self, Object[] parameters) {
            synchronized (self) {
                return invoker.invoke(self, parameters);
            }
        }
    }

    private static final class FunctionNotFoundInvoker implements Invoker {
        private final Method method;
        private final String functionName;

        private FunctionNotFoundInvoker(Method method, String functionName) {
            this.method = method;
            this.functionName = functionName;
        }

        @Override
        public Object invoke(Object self, Object[] parameters) {
            throw new UnsatisfiedLinkError(String.format("native method '%s' not found for method %s", functionName,  method));
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
            if (function.getReturnType().size() == 8) {
                return invoker.invokeLong(function, buffer) != 0;
            }
            return invoker.invokeInt(function, buffer) != 0;
        }
    }

    static class CharacterInvoker extends BaseInvoker {
        static FunctionInvoker INSTANCE = new CharacterInvoker();
        public final Object invoke(Runtime runtime, Function function, HeapInvocationBuffer buffer) {
            return (char) invoker.invokeInt(function, buffer);
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

    static class CharacterMarshaller implements Marshaller {
        static final CharacterMarshaller INSTANCE = new CharacterMarshaller();
        public void marshal(InvocationSession session, HeapInvocationBuffer buffer, Object parameter) {
            buffer.putInt(((Character) parameter).charValue());
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
            return value.byteValue() & 0xff;
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
            return nativeValue.longValue() != 0;
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
