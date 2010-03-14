
package com.kenai.jaffl.provider.jffi;

import com.kenai.jaffl.Address;
import com.kenai.jaffl.LibraryOption;
import com.kenai.jaffl.NativeLong;
import com.kenai.jaffl.provider.ParameterFlags;
import com.kenai.jaffl.Pointer;
import com.kenai.jaffl.annotations.StdCall;
import com.kenai.jaffl.byref.ByReference;
import com.kenai.jaffl.mapper.FromNativeContext;
import com.kenai.jaffl.mapper.FromNativeConverter;
import com.kenai.jaffl.mapper.FunctionMapper;
import com.kenai.jaffl.mapper.MethodResultContext;
import com.kenai.jaffl.mapper.ToNativeContext;
import com.kenai.jaffl.mapper.ToNativeConverter;
import com.kenai.jaffl.mapper.TypeMapper;
import com.kenai.jaffl.provider.InvocationSession;
import com.kenai.jaffl.provider.Invoker;
import com.kenai.jaffl.provider.StringIO;
import com.kenai.jaffl.struct.Struct;
import com.kenai.jaffl.util.EnumMapper;
import com.kenai.jffi.Function;
import com.kenai.jffi.HeapInvocationBuffer;
import com.kenai.jffi.InvocationBuffer;
import com.kenai.jffi.Platform;
import com.kenai.jffi.Type;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.Map;

final class DefaultInvokerFactory implements InvokerFactory {
    private final static class SingletonHolder {
        static InvokerFactory INSTANCE = new DefaultInvokerFactory();
    }
    public static final InvokerFactory getInstance() {
        return SingletonHolder.INSTANCE;
    }
    public final boolean isMethodSupported(Method method) {
        return true; // The default factory supports everything
    }

    private static final InvokeAddress invokeAddress =
            Platform.getPlatform().addressSize() == 32 ? InvokeAddress32.INSTANCE : InvokeAddress64.INSTANCE;

    public final Invoker createInvoker(Method method, com.kenai.jaffl.provider.Library library, Map<LibraryOption, ?> options) {
        FunctionMapper functionMapper = options.containsKey(LibraryOption.FunctionMapper)
                ? (FunctionMapper) options.get(LibraryOption.FunctionMapper) : IdentityFunctionMapper.INSTANCE;
        final long address = ((NativeLibrary) library).findSymbolAddress(functionMapper.mapFunctionName(method.getName(), null));

        TypeMapper typeMapper = options.containsKey(LibraryOption.TypeMapper)
                ? (TypeMapper) options.get(LibraryOption.TypeMapper) : NullTypeMapper.INSTANCE;

        com.kenai.jffi.CallingConvention convention = method.getAnnotation(StdCall.class) != null
                ? com.kenai.jffi.CallingConvention.STDCALL : InvokerUtil.getCallingConvention(options);

        Marshaller[] marshallers = new Marshaller[method.getParameterTypes().length];
        Type[] paramTypes = new Type[marshallers.length];

        for (int i = 0; i < marshallers.length; ++i) {
            marshallers[i] = getMarshaller(method, i, typeMapper);
            paramTypes[i] = getNativeParameterType(method, i, typeMapper);
        }

        Class returnType = method.getReturnType();
        FromNativeConverter resultConverter = typeMapper.getFromNativeConverter(returnType);
        if (resultConverter != null) {
            returnType = resultConverter.nativeType();
        }
        Function function = new Function(address, getNativeReturnType(returnType),
                paramTypes, convention, InvokerUtil.requiresErrno(method));
        FunctionInvoker invoker = getFunctionInvoker(returnType);
        if (resultConverter != null) {
            MethodResultContext context = new MethodResultContext(method);
            invoker = new ConvertingInvoker(resultConverter, context, invoker);
        }
        return isSessionRequired(marshallers)
                ? new SessionInvoker(function, invoker, marshallers)
                : new DefaultInvoker(function, invoker, marshallers);
    }
    private static final boolean isSessionRequired(Marshaller[] marshallers) {
        for (Marshaller m : marshallers) {
            if (m.isSessionRequired()) {
                return true;
            }
        }
        return false;
    }
    private static final FunctionInvoker getFunctionInvoker(Class returnType) {
        if (Void.class.isAssignableFrom(returnType) || void.class == returnType) {
            return VoidInvoker.INSTANCE;
        } else if (Boolean.class.isAssignableFrom(returnType) || boolean.class == returnType) {
            return BooleanInvoker.INSTANCE;
        } else if (Enum.class.isAssignableFrom(returnType)) {
            return new EnumInvoker(returnType);
        } else if (Byte.class.isAssignableFrom(returnType) || byte.class == returnType) {
            return Int8Invoker.INSTANCE;
        } else if (Short.class.isAssignableFrom(returnType) || short.class == returnType) {
            return Int16Invoker.INSTANCE;
        } else if (Integer.class.isAssignableFrom(returnType) || int.class == returnType) {
            return Int32Invoker.INSTANCE;
        } else if (Long.class.isAssignableFrom(returnType) || long.class == returnType) {
            return Int64Invoker.INSTANCE;
        } else if (NativeLong.class.isAssignableFrom(returnType)) {
            return Platform.getPlatform().longSize() == 32
                ? NativeLong32Invoker.INSTANCE : NativeLong64Invoker.INSTANCE;
        } else if (Float.class.isAssignableFrom(returnType) || float.class == returnType) {
            return Float32Invoker.INSTANCE;
        } else if (Double.class.isAssignableFrom(returnType) || double.class == returnType) {
            return Float64Invoker.INSTANCE;
        } else if (Pointer.class.isAssignableFrom(returnType)) {
            return PointerInvoker.INSTANCE;
        } else if (Address.class.isAssignableFrom(returnType)) {
            return AddressInvoker.INSTANCE;
        } else if (Struct.class.isAssignableFrom(returnType)) {
            return new StructInvoker(returnType);
        } else if (String.class.isAssignableFrom(returnType)) {
            return StringInvoker.INSTANCE;
        } else {
            throw new IllegalArgumentException("Unknown return type: " + returnType);
        }
    }
    private static final Type getNativeReturnType(Class type) {
        if (Void.class.isAssignableFrom(type) || void.class == type) {
            return Type.VOID;
        } else if (Boolean.class.isAssignableFrom(type) || boolean.class == type) {
            return Type.SINT32;
        } else if (Byte.class.isAssignableFrom(type) || byte.class == type) {
            return Type.SINT8;
        } else if (Short.class.isAssignableFrom(type) || short.class == type) {
            return Type.SINT16;
        } else if (Integer.class.isAssignableFrom(type) || int.class == type) {
            return Type.SINT32;
        } else if (Long.class.isAssignableFrom(type) || long.class == type) {
            return Type.SINT64;
        } else if (NativeLong.class.isAssignableFrom(type)) {
            return Platform.getPlatform().longSize() == 32 ? Type.SINT32: Type.SINT64;
        } else if (Float.class.isAssignableFrom(type) || float.class == type) {
            return Type.FLOAT;
        } else if (Double.class.isAssignableFrom(type) || double.class == type) {
            return Type.DOUBLE;
        } else if (Enum.class.isAssignableFrom(type)) {
            return Type.SINT32;
        } else if (Pointer.class.isAssignableFrom(type)) {
            return Type.POINTER;
        } else if (Address.class.isAssignableFrom(type)) {
            return Type.POINTER;
        } else if (Struct.class.isAssignableFrom(type)) {
            return Type.POINTER;
        } else if (String.class.isAssignableFrom(type)) {
            return Type.POINTER;
        } else {
            throw new IllegalArgumentException("Unsupported return type: " + type);
        }
    }

    private static final Type getNativeParameterType(Method method, int paramIndex, TypeMapper mapper) {
        Class type = method.getParameterTypes()[paramIndex];
        ToNativeConverter converter = mapper.getToNativeConverter(type);
        return getNativeParameterType(converter != null ? converter.nativeType() : type);
    }

    private static final Type getNativeParameterType(Class type) {
        if (Byte.class.isAssignableFrom(type) || byte.class == type) {
            return Type.SINT8;
        } else if (Short.class.isAssignableFrom(type) || short.class == type) {
            return Type.SINT16;
        } else if (Integer.class.isAssignableFrom(type) || int.class == type) {
            return Type.SINT32;
        } else if (Long.class.isAssignableFrom(type) || long.class == type) {
            return Type.SINT64;
        } else if (NativeLong.class.isAssignableFrom(type)) {
            return Platform.getPlatform().longSize() == 32 ? Type.SINT32: Type.SINT64;
        } else if (Float.class.isAssignableFrom(type) || float.class == type) {
            return Type.FLOAT;
        } else if (Double.class.isAssignableFrom(type) || double.class == type) {
            return Type.DOUBLE;
        } else if (Boolean.class.isAssignableFrom(type) || boolean.class == type) {
            return Type.SINT32;
        } else if (Enum.class.isAssignableFrom(type)) {
            return Type.SINT32;
        } else if (Pointer.class.isAssignableFrom(type)) {
            return Type.POINTER;
        } else if (Struct.class.isAssignableFrom(type) || type.isArray() && Struct.class.isAssignableFrom(type.getComponentType())) {
            return Type.POINTER;
        } else if (Buffer.class.isAssignableFrom(type)) {
            return Type.POINTER;
        } else if (CharSequence.class.isAssignableFrom(type)) {
            return Type.POINTER;
        } else if (ByReference.class.isAssignableFrom(type)) {
            return Type.POINTER;
        } else if (type.isArray()) {
            return Type.POINTER;
        } else {
            throw new IllegalArgumentException("Unsupported parameter type: " + type);
        }
    }
    static final int getParameterFlags(Method method, int paramIndex) {
        return getParameterFlags(method.getParameterAnnotations()[paramIndex]);
    }

    static final int getParameterFlags(Annotation[] annotations) {
        return ParameterFlags.parse(annotations);
    }

    static final int getNativeArrayFlags(int flags) {
        int nflags = 0;
        nflags |= ParameterFlags.isIn(flags) ? com.kenai.jffi.ArrayFlags.IN : 0;
        nflags |= ParameterFlags.isOut(flags) ? com.kenai.jffi.ArrayFlags.OUT : 0;
        nflags |= ParameterFlags.isNulTerminate(flags) ? com.kenai.jffi.ArrayFlags.NULTERMINATE : 0;
        return nflags;
    }

    static final int getNativeArrayFlags(Annotation[] annotations) {
        return getNativeArrayFlags(getParameterFlags(annotations));
    }

    static final Marshaller getMarshaller(Method method, int paramIndex, TypeMapper mapper) {
        Class type = method.getParameterTypes()[paramIndex];
        ToNativeConverter converter = mapper != null ? mapper.getToNativeConverter(type) : null;
        if (converter != null) {
            return new ToNativeConverterMarshaller(converter, 
                    getMarshaller(converter.nativeType(), method.getParameterAnnotations()[paramIndex]));
        } else {
            return getMarshaller(method, paramIndex);
        }
    }

    static final Marshaller getMarshaller(Method method, int paramIndex) {
        return getMarshaller(method.getParameterTypes()[paramIndex],
                method.getParameterAnnotations()[paramIndex]);
    }

    static final Marshaller getMarshaller(Class type, Annotation[] annotations) {
        if (Byte.class.isAssignableFrom(type) || byte.class == type) {
            return Int8Marshaller.INSTANCE;
        } else if (Short.class.isAssignableFrom(type) || short.class == type) {
            return Int16Marshaller.INSTANCE;
        } else if (Integer.class.isAssignableFrom(type) || int.class == type) {
            return Int32Marshaller.INSTANCE;
        } else if (Long.class.isAssignableFrom(type) || long.class == type) {
            return Int64Marshaller.INSTANCE;
        } else if (NativeLong.class.isAssignableFrom(type)) {
            return Platform.getPlatform().longSize() == 32
                    ? Int32Marshaller.INSTANCE : Int64Marshaller.INSTANCE;
        } else if (Float.class.isAssignableFrom(type) || float.class == type) {
            return Float32Marshaller.INSTANCE;
        } else if (Double.class.isAssignableFrom(type) || double.class == type) {
            return Float64Marshaller.INSTANCE;
        } else if (Boolean.class.isAssignableFrom(type) || boolean.class == type) {
            return BooleanMarshaller.INSTANCE;
        } else if (Enum.class.isAssignableFrom(type)) {
            return EnumMarshaller.INSTANCE;
        } else if (Pointer.class.isAssignableFrom(type)) {
            return PointerMarshaller.INSTANCE;
        } else if (StringBuffer.class.isAssignableFrom(type)) {
            return new StringBufferMarshaller(getParameterFlags(annotations));
        } else if (StringBuilder.class.isAssignableFrom(type)) {
            return new StringBuilderMarshaller(getParameterFlags(annotations));
        } else if (CharSequence.class.isAssignableFrom(type)) {
            return CharSequenceMarshaller.INSTANCE;
        } else if (ByReference.class.isAssignableFrom(type)) {
            return new ByReferenceMarshaller(getParameterFlags(annotations));
        } else if (Struct.class.isAssignableFrom(type)) {
            return new StructMarshaller(getParameterFlags(annotations));
        } else if (ByteBuffer.class.isAssignableFrom(type)) {
            return new ByteBufferMarshaller(getParameterFlags(annotations));
        } else if (ShortBuffer.class.isAssignableFrom(type)) {
            return new ShortBufferMarshaller(getParameterFlags(annotations));
        } else if (IntBuffer.class.isAssignableFrom(type)) {
            return new IntBufferMarshaller(getParameterFlags(annotations));
        } else if (LongBuffer.class.isAssignableFrom(type)) {
            return new LongBufferMarshaller(getParameterFlags(annotations));
        } else if (FloatBuffer.class.isAssignableFrom(type)) {
            return new FloatBufferMarshaller(getParameterFlags(annotations));
        } else if (DoubleBuffer.class.isAssignableFrom(type)) {
            return new DoubleBufferMarshaller(getParameterFlags(annotations));
        } else if (type.isArray() && type.getComponentType() == byte.class) {
            return new ByteArrayMarshaller(getParameterFlags(annotations));
        } else if (type.isArray() && type.getComponentType() == short.class) {
            return new ShortArrayMarshaller(getParameterFlags(annotations));
        } else if (type.isArray() && type.getComponentType() == int.class) {
            return new IntArrayMarshaller(getParameterFlags(annotations));
        } else if (type.isArray() && type.getComponentType() == long.class) {
            return new LongArrayMarshaller(getParameterFlags(annotations));
        } else if (type.isArray() && type.getComponentType() == float.class) {
            return new FloatArrayMarshaller(getParameterFlags(annotations));
        } else if (type.isArray() && type.getComponentType() == double.class) {
            return new DoubleArrayMarshaller(getParameterFlags(annotations));
        } else if (type.isArray() && Struct.class.isAssignableFrom(type.getComponentType())) {
            return new StructArrayMarshaller(getParameterFlags(annotations));
        } else {
            throw new IllegalArgumentException("Unsupported parameter type: " + type);
        }
    }
    static final class SessionInvoker implements com.kenai.jaffl.provider.Invoker {
        static final com.kenai.jffi.Invoker invoker = com.kenai.jffi.Invoker.getInstance();
        final Function function;
        final FunctionInvoker functionInvoker;
        final Marshaller[] marshallers;
        SessionInvoker(Function function, FunctionInvoker invoker, Marshaller[] marshallers) {
            this.function = function;
            this.functionInvoker = invoker;
            this.marshallers = marshallers;
        }
        final HeapInvocationBuffer marshal(InvocationSession session, Object[] parameters) {
            HeapInvocationBuffer buffer = new HeapInvocationBuffer(function);
            for (int i = 0; i < parameters.length; ++i) {
                marshallers[i].marshal(session, buffer, parameters[i]);
            }
            return buffer;
        }

        public final Object invoke(Object[] parameters) {
            InvocationSession session = new InvocationSession();
            Object retVal = functionInvoker.invoke(function, marshal(session, parameters));
            session.finish();
            return retVal;
        }
    }
    static final class DefaultInvoker implements com.kenai.jaffl.provider.Invoker {
        
        final Function function;
        final FunctionInvoker functionInvoker;
        final Marshaller[] marshallers;
        DefaultInvoker(Function function, FunctionInvoker invoker, Marshaller[] marshallers) {
            this.function = function;
            this.functionInvoker = invoker;
            this.marshallers = marshallers;
        }
        final HeapInvocationBuffer marshal(Object[] parameters) {
            HeapInvocationBuffer buffer = new HeapInvocationBuffer(function);
            for (int i = 0; i < parameters.length; ++i) {
                marshallers[i].marshal(buffer, parameters[i]);
            }
            return buffer;
        }

        public final Object invoke(Object[] parameters) {
            return functionInvoker.invoke(function, marshal(parameters));
        }

    }
    
    static interface Marshaller {
        abstract boolean isSessionRequired();
        abstract void marshal(InvocationSession session, InvocationBuffer buffer, Object parameter);
        abstract void marshal(InvocationBuffer buffer, Object parameter);
    }

    static interface FunctionInvoker {
        Object invoke(Function function, HeapInvocationBuffer buffer);
    }

    static abstract class BaseMarshaller implements Marshaller {
        public boolean isSessionRequired() { return false; }
        public void marshal(InvocationSession session, InvocationBuffer buffer, Object parameter) {
            marshal(buffer, parameter);
        }
    }

    static abstract class BaseInvoker implements FunctionInvoker {
        static final com.kenai.jffi.Invoker invoker = com.kenai.jffi.Invoker.getInstance();
    }

    static final class ConvertingInvoker extends BaseInvoker {
        private final FromNativeConverter converter;
        private final FromNativeContext context;
        private final FunctionInvoker nativeInvoker;

        public ConvertingInvoker(FromNativeConverter converter, FromNativeContext context, FunctionInvoker nativeInvoker) {
            this.converter = converter;
            this.context = context;
            this.nativeInvoker = nativeInvoker;
        }

        public final Object invoke(Function function, HeapInvocationBuffer buffer) {
            return converter.fromNative(nativeInvoker.invoke(function, buffer), context);
        }
    }
    static final class VoidInvoker extends BaseInvoker {
        static final FunctionInvoker INSTANCE = new VoidInvoker();
        public final Object invoke(Function function, HeapInvocationBuffer buffer) {
            invoker.invokeInt(function, buffer);
            return null;
        }
    }
    static final class BooleanInvoker extends BaseInvoker {
        static final FunctionInvoker INSTANCE = new BooleanInvoker();
        public final Object invoke(Function function, HeapInvocationBuffer buffer) {
            return Boolean.valueOf(invoker.invokeInt(function, buffer) != 0);
        }
    }
    static final class EnumInvoker extends BaseInvoker {
        private final Class enumClass;
        private EnumInvoker(Class enumClass) {
            this.enumClass = enumClass;
        }
        @SuppressWarnings("unchecked")
        public final Object invoke(Function function, HeapInvocationBuffer buffer) {
            return EnumMapper.getInstance().valueOf(invoker.invokeInt(function, buffer), enumClass);
        }
    }
    static final class Int8Invoker extends BaseInvoker {
        static final FunctionInvoker INSTANCE = new Int8Invoker();
        public final Object invoke(Function function, HeapInvocationBuffer buffer) {
            return Byte.valueOf((byte) invoker.invokeInt(function, buffer));
        }
    }
    static final class Int16Invoker extends BaseInvoker {
        static final FunctionInvoker INSTANCE = new Int16Invoker();
        public final Object invoke(Function function, HeapInvocationBuffer buffer) {
            return Short.valueOf((short) invoker.invokeInt(function, buffer));
        }
    }
    static final class Int32Invoker extends BaseInvoker {
        static final FunctionInvoker INSTANCE = new Int32Invoker();
        public final Object invoke(Function function, HeapInvocationBuffer buffer) {
            return Integer.valueOf(invoker.invokeInt(function, buffer));
        }
    }
    static final class Int64Invoker extends BaseInvoker {
        static final FunctionInvoker INSTANCE = new Int64Invoker();
        public final Object invoke(Function function, HeapInvocationBuffer buffer) {
            return Long.valueOf(invoker.invokeLong(function, buffer));
        }
    }
    static final class NativeLong32Invoker extends BaseInvoker {
        static final FunctionInvoker INSTANCE = new NativeLong32Invoker();
        public final Object invoke(Function function, HeapInvocationBuffer buffer) {
            return NativeLong.valueOf(invoker.invokeInt(function, buffer));
        }
    }
    static final class NativeLong64Invoker extends BaseInvoker {
        static final FunctionInvoker INSTANCE = new NativeLong64Invoker();
        public final Object invoke(Function function, HeapInvocationBuffer buffer) {
            return NativeLong.valueOf(invoker.invokeLong(function, buffer));
        }
    }
    static final class Float32Invoker extends BaseInvoker {
        static final FunctionInvoker INSTANCE = new Float32Invoker();
        public final Object invoke(Function function, HeapInvocationBuffer buffer) {
            return invoker.invokeFloat(function, buffer);
        }
    }
    static final class Float64Invoker extends BaseInvoker {
        static final FunctionInvoker INSTANCE = new Float64Invoker();
        public final Object invoke(Function function, HeapInvocationBuffer buffer) {
            return invoker.invokeDouble(function, buffer);
        }
    }

    static final class PointerInvoker extends BaseInvoker {
        static final FunctionInvoker INSTANCE = new PointerInvoker();
        public final Object invoke(Function function, HeapInvocationBuffer buffer) {
            return MemoryUtil.newPointer(invokeAddress.invoke(function, buffer));
        }
    }

    static final class AddressInvoker extends BaseInvoker {
        static final FunctionInvoker INSTANCE = new AddressInvoker();
        public final Object invoke(Function function, HeapInvocationBuffer buffer) {
            return Address.valueOf(invokeAddress.invoke(function, buffer));
        }
    }

    private static abstract class InvokeAddress {
        static final com.kenai.jffi.Invoker invoker = com.kenai.jffi.Invoker.getInstance();
        public abstract long invoke(Function function, HeapInvocationBuffer buffer);
    }

    private static final class InvokeAddress32 extends InvokeAddress {
        public static final InvokeAddress INSTANCE = new InvokeAddress32();

        public final long invoke(Function function, HeapInvocationBuffer buffer) {
            return invoker.invokeInt(function, buffer);
        }
    }

    private static final class InvokeAddress64 extends InvokeAddress {
        public static final InvokeAddress INSTANCE = new InvokeAddress64();

        public final long invoke(Function function, HeapInvocationBuffer buffer) {
            return invoker.invokeInt(function, buffer);
        }
    }


    static final class StructInvoker extends BaseInvoker {
        private final Class structClass;

        public StructInvoker(Class structClass) {
            this.structClass = structClass;
        }
        
        public final Object invoke(Function function, HeapInvocationBuffer buffer) {
            final long ptr = invokeAddress.invoke(function, buffer);
            if (ptr == 0L) {
                return null;
            }
            try {
                Struct s = (Struct) structClass.newInstance();
                s.useMemory(new DirectMemoryIO(ptr));
                return s;
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }
    }

    static final class StringInvoker extends BaseInvoker {
        static final FunctionInvoker INSTANCE = new StringInvoker();
        private static final com.kenai.jffi.MemoryIO IO = com.kenai.jffi.MemoryIO.getInstance();

        public final Object invoke(Function function, HeapInvocationBuffer buffer) {
            final long ptr = invokeAddress.invoke(function, buffer);
            if (ptr == 0) {
                return null;
            }
            final ByteBuffer buf = ByteBuffer.wrap(IO.getZeroTerminatedByteArray(ptr));

            return StringIO.getStringIO().fromNative(buf).toString();
        }
    }

    /* ---------------------------------------------------------------------- */
    static final class BooleanMarshaller extends BaseMarshaller {
        static final Marshaller INSTANCE = new BooleanMarshaller();
        public void marshal(InvocationBuffer buffer, Object parameter) {
            buffer.putInt(((Boolean) parameter).booleanValue() ? 1 : 0);
        }
    }
    static final class EnumMarshaller extends BaseMarshaller {
        static final Marshaller INSTANCE = new EnumMarshaller();
        public void marshal(InvocationBuffer buffer, Object parameter) {
            buffer.putInt(EnumMapper.getInstance().intValue((Enum) parameter));
        }
    }
    static final class Int8Marshaller extends BaseMarshaller {
        static final Marshaller INSTANCE = new Int8Marshaller();
        public void marshal(InvocationBuffer buffer, Object parameter) {
            buffer.putByte(((Number) parameter).intValue());
        }
    }
    static final class Int16Marshaller extends BaseMarshaller {
        static final Marshaller INSTANCE = new Int16Marshaller();
        public void marshal(InvocationBuffer buffer, Object parameter) {
            buffer.putShort(((Number) parameter).intValue());
        }
    }
    static final class Int32Marshaller extends BaseMarshaller {
        static final Marshaller INSTANCE = new Int32Marshaller();
        public void marshal(InvocationBuffer buffer, Object parameter) {
            buffer.putInt(((Number) parameter).intValue());
        }
    }
    static final class Int64Marshaller extends BaseMarshaller {
        static final Marshaller INSTANCE = new Int64Marshaller();
        public void marshal(InvocationBuffer buffer, Object parameter) {
            buffer.putLong(((Number) parameter).longValue());
        }
    }
    static final class Float32Marshaller extends BaseMarshaller {
        static final Marshaller INSTANCE = new Float32Marshaller();
        public void marshal(InvocationBuffer buffer, Object parameter) {
            buffer.putFloat(((Number) parameter).floatValue());
        }
    }
    static final class Float64Marshaller extends BaseMarshaller {
        static final Marshaller INSTANCE = new Float64Marshaller();
        public void marshal(InvocationBuffer buffer, Object parameter) {
            buffer.putDouble(((Number) parameter).doubleValue());
        }
    }
    static final class PointerMarshaller extends BaseMarshaller {
        static final Marshaller INSTANCE = new PointerMarshaller();
        public void marshal(InvocationBuffer buffer, Object parameter) {
            buffer.putAddress(((Pointer) parameter).address());
        }
    }
    
    static final class CharSequenceMarshaller extends BaseMarshaller {
        static final Marshaller INSTANCE = new CharSequenceMarshaller();
        public void marshal(InvocationBuffer buffer, Object parameter) {
            AsmRuntime.marshal(buffer, (CharSequence) parameter);
        }
    }

    static abstract class SessionRequiredMarshaller extends BaseMarshaller {
        @Override
        public final boolean isSessionRequired() {
            return true;
        }

        public void marshal(InvocationBuffer buffer, Object parameter) {
            throw new UnsupportedOperationException("Cannot marshal this type without session");
        }
    }

    static final class StringBuilderMarshaller extends SessionRequiredMarshaller {
        private final int nflags, inout;
        public StringBuilderMarshaller(int inout) {
            this.inout = inout;
            this.nflags = getNativeArrayFlags(inout | (ParameterFlags.isIn(inout) ? ParameterFlags.NULTERMINATE : 0));
        }

        
        @Override
        public void marshal(InvocationSession session, InvocationBuffer buffer, Object parameter) {
            AsmRuntime.marshal(session, buffer, (StringBuilder) parameter, inout, nflags);
        }
    }

    static final class StringBufferMarshaller extends SessionRequiredMarshaller {
        private final int nflags, inout;
        public StringBufferMarshaller(int inout) {
            this.inout = inout;
            this.nflags = getNativeArrayFlags(inout | (ParameterFlags.isIn(inout) ? ParameterFlags.NULTERMINATE : 0));
        }


        @Override
        public void marshal(InvocationSession session, InvocationBuffer buffer, Object parameter) {
            AsmRuntime.marshal(session, buffer, (StringBuffer) parameter, inout, nflags);
        }
    }
            
    static final class ByteArrayMarshaller extends BaseMarshaller {
        private final int flags;
        public ByteArrayMarshaller(int flags) {
            this.flags = getNativeArrayFlags(flags | (ParameterFlags.isIn(flags) ? ParameterFlags.NULTERMINATE : 0));
        }

        public final void marshal(InvocationBuffer buffer, Object parameter) {
            AsmRuntime.marshal(buffer, byte[].class.cast(parameter), flags);
        }
    }
    static final class ShortArrayMarshaller extends BaseMarshaller {
        private final int flags;
        public ShortArrayMarshaller(int flags) {
            this.flags = getNativeArrayFlags(flags);
        }

        public final void marshal(InvocationBuffer buffer, Object parameter) {
            AsmRuntime.marshal(buffer, short[].class.cast(parameter), flags);
        }
    }
    static final class IntArrayMarshaller extends BaseMarshaller {
        private final int flags;
        public IntArrayMarshaller(int flags) {
            this.flags = getNativeArrayFlags(flags);
        }

        public final void marshal(InvocationBuffer buffer, Object parameter) {
            AsmRuntime.marshal(buffer, int[].class.cast(parameter), flags);
        }
    }

    static final class LongArrayMarshaller extends BaseMarshaller {
        private final int flags;
        public LongArrayMarshaller(int flags) {
            this.flags = getNativeArrayFlags(flags);
        }

        public final void marshal(InvocationBuffer buffer, Object parameter) {
            AsmRuntime.marshal(buffer, long[].class.cast(parameter), flags);
        }
    }

    static final class FloatArrayMarshaller extends BaseMarshaller {
        private final int flags;
        public FloatArrayMarshaller(int flags) {
            this.flags = getNativeArrayFlags(flags);
        }

        public final void marshal(InvocationBuffer buffer, Object parameter) {
            AsmRuntime.marshal(buffer, float[].class.cast(parameter), flags);
        }
    }
    
    static final class DoubleArrayMarshaller extends BaseMarshaller {
        private final int flags;
        public DoubleArrayMarshaller(int flags) {
            this.flags = getNativeArrayFlags(flags);
        }

        public final void marshal(InvocationBuffer buffer, Object parameter) {
            AsmRuntime.marshal(buffer, double[].class.cast(parameter), flags);
        }
    }

    static final class ByteBufferMarshaller extends BaseMarshaller {
        private final int flags;
        public ByteBufferMarshaller(int flags) {
            this.flags = getNativeArrayFlags(flags | (ParameterFlags.isIn(flags) ? ParameterFlags.NULTERMINATE : 0));
        }

        public final void marshal(InvocationBuffer buffer, Object parameter) {
            AsmRuntime.marshal(buffer, (ByteBuffer) parameter, flags);
        }
    }

    static final class ShortBufferMarshaller extends BaseMarshaller {
        private final int flags;
        public ShortBufferMarshaller(int flags) {
            this.flags = getNativeArrayFlags(flags);
        }

        public final void marshal(InvocationBuffer buffer, Object parameter) {
            AsmRuntime.marshal(buffer, (ShortBuffer) parameter, flags);
        }
    }

    static final class IntBufferMarshaller extends BaseMarshaller {
        private final int flags;
        public IntBufferMarshaller(int flags) {
            this.flags = getNativeArrayFlags(flags);
        }

        public final void marshal(InvocationBuffer buffer, Object parameter) {
            AsmRuntime.marshal(buffer, (IntBuffer) parameter, flags);
        }
    }

    static final class LongBufferMarshaller extends BaseMarshaller {
        private final int flags;
        public LongBufferMarshaller(int flags) {
            this.flags = getNativeArrayFlags(flags);
        }

        public final void marshal(InvocationBuffer buffer, Object parameter) {
            AsmRuntime.marshal(buffer, (LongBuffer) parameter, flags);
        }
    }

    static final class FloatBufferMarshaller extends BaseMarshaller {
        private final int flags;
        public FloatBufferMarshaller(int flags) {
            this.flags = getNativeArrayFlags(flags);
        }

        public final void marshal(InvocationBuffer buffer, Object parameter) {
            AsmRuntime.marshal(buffer, (FloatBuffer) parameter, flags);
        }
    }

    static final class DoubleBufferMarshaller extends BaseMarshaller {
        private final int flags;
        public DoubleBufferMarshaller(int flags) {
            this.flags = getNativeArrayFlags(flags);
        }

        public final void marshal(InvocationBuffer buffer, Object parameter) {
            AsmRuntime.marshal(buffer, (DoubleBuffer) parameter, flags);
        }
    }
    
    static final class ByReferenceMarshaller extends SessionRequiredMarshaller {
        private final int flags;
        public ByReferenceMarshaller(int flags) {
            this.flags = getNativeArrayFlags(flags);
        }

        @Override
        public final void marshal(InvocationSession session, InvocationBuffer buffer, Object parameter) {
            AsmRuntime.marshal(session, buffer, (ByReference) parameter, flags);
        }
    }

    static final class StructMarshaller extends BaseMarshaller {
        private final int nflags, flags;
        public StructMarshaller(int flags) {
            this.flags = flags;
            this.nflags = getNativeArrayFlags(flags);
        }

        public final void marshal(InvocationBuffer buffer, Object parameter) {
            AsmRuntime.marshal(buffer, (Struct) parameter, flags, nflags);
        }
    }

    static final class StructArrayMarshaller extends BaseMarshaller {
        private final int nflags, flags;
        public StructArrayMarshaller(int flags) {
            this.flags = flags;
            this.nflags = getNativeArrayFlags(flags);
        }
        
        public final void marshal(InvocationBuffer buffer, Object parameter) {
            AsmRuntime.marshal(buffer, Struct[].class.cast(parameter), flags, nflags);
        }
    }

    static final class ToNativeConverterMarshaller extends BaseMarshaller {
        private final ToNativeConverter converter;
        private final ToNativeContext context = null;
        private final Marshaller marshaller;

        public ToNativeConverterMarshaller(ToNativeConverter converter, Marshaller marshaller) {
            this.converter = converter;
            this.marshaller = marshaller;
        }

        public void marshal(InvocationBuffer buffer, Object parameter) {
            marshaller.marshal(buffer, converter.toNative(parameter, context));
        }

        @Override
        public boolean isSessionRequired() {
            return marshaller.isSessionRequired();
        }

        @Override
        public void marshal(InvocationSession session, InvocationBuffer buffer, Object parameter) {
            marshaller.marshal(session, buffer, converter.toNative(parameter, context));
        }
        
    }
}
