
package com.kenai.jaffl.provider.jffi;

import com.kenai.jaffl.LibraryOption;
import com.kenai.jaffl.MemoryIO;
import com.kenai.jaffl.NativeLong;
import com.kenai.jaffl.ParameterFlags;
import com.kenai.jaffl.Platform;
import com.kenai.jaffl.Pointer;
import com.kenai.jaffl.annotations.In;
import com.kenai.jaffl.annotations.Out;
import com.kenai.jaffl.byref.ByReference;
import com.kenai.jaffl.mapper.FromNativeContext;
import com.kenai.jaffl.mapper.FromNativeConverter;
import com.kenai.jaffl.mapper.MethodResultContext;
import com.kenai.jaffl.mapper.ToNativeContext;
import com.kenai.jaffl.mapper.ToNativeConverter;
import com.kenai.jaffl.mapper.TypeMapper;
import com.kenai.jaffl.provider.AbstractArrayMemoryIO;
import com.kenai.jaffl.provider.DelegatingMemoryIO;
import com.kenai.jaffl.provider.InvocationSession;
import com.kenai.jaffl.provider.Invoker;
import com.kenai.jaffl.provider.StringIO;
import com.kenai.jaffl.struct.Struct;
import com.kenai.jaffl.struct.StructUtil;
import com.kenai.jaffl.util.EnumMapper;
import com.kenai.jffi.CallingConvention;
import com.kenai.jffi.Function;
import com.kenai.jffi.HeapInvocationBuffer;
import com.kenai.jffi.InvocationBuffer;
import com.kenai.jffi.Type;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
    public final Invoker createInvoker(Method method, com.kenai.jaffl.provider.Library library, Map<LibraryOption, ?> options) {
        final long address = ((Library) library).getNativeLibrary().getSymbolAddress(method.getName());

        TypeMapper typeMapper = (TypeMapper) options.get(LibraryOption.TypeMapper);
        Marshaller[] marshallers = new Marshaller[method.getParameterTypes().length];
        Type[] paramTypes = new Type[marshallers.length];

        for (int i = 0; i < marshallers.length; ++i) {
            marshallers[i] = getMarshaller(method, i, typeMapper);
            paramTypes[i] = getNativeParameterType(method, i, typeMapper);
        }

        Class returnType = method.getReturnType();
        FromNativeConverter resultConverter = typeMapper != null
                ? typeMapper.getFromNativeConverter(returnType)
                : null;
        if (resultConverter != null) {
            returnType = resultConverter.nativeType();
        }
        Function function = new Function(address, getNativeReturnType(returnType),
                paramTypes, CallingConvention.DEFAULT, InvokerUtil.requiresErrno(method));
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
        ToNativeConverter converter = mapper != null ? mapper.getToNativeConverter(type) : null;
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
    static final int getInOutFlags(Method method, int paramIndex) {
        return getInOutFlags(method.getParameterAnnotations()[paramIndex]);
    }
    static final int getInOutFlags(Annotation[] annotations) {
        int flags = 0;
        for (int i = 0; i < annotations.length; ++i) {
            flags |= (annotations[i] instanceof In) ? ParameterFlags.IN : 0;
            flags |= (annotations[i] instanceof Out) ? ParameterFlags.OUT : 0;
        }
        return flags != 0 ? flags : (ParameterFlags.IN | ParameterFlags.OUT);
    }
    static final int getNativeArrayFlags(int flags) {
        int nflags = 0;
        nflags |= ParameterFlags.isIn(flags) ? com.kenai.jffi.ArrayFlags.IN : 0;
        nflags |= ParameterFlags.isOut(flags) ? com.kenai.jffi.ArrayFlags.OUT : 0;
        nflags |= ParameterFlags.isNulTerminate(flags) ? com.kenai.jffi.ArrayFlags.NULTERMINATE : 0;
        return nflags;
    }
    private static final Marshaller getMarshaller(Method method, int paramIndex, TypeMapper mapper) {
        Class type = method.getParameterTypes()[paramIndex];
        ToNativeConverter converter = mapper != null ? mapper.getToNativeConverter(type) : null;
        if (converter != null) {
            return new ToNativeConverterMarshaller(converter, 
                    getMarshaller(converter.nativeType(), method.getParameterAnnotations()[paramIndex]));
        } else {
            return getMarshaller(method, paramIndex);
        }
    }

    private static final Marshaller getMarshaller(Method method, int paramIndex) {
        return getMarshaller(method.getParameterTypes()[paramIndex],
                method.getParameterAnnotations()[paramIndex]);
    }

    private static final Marshaller getMarshaller(Class type, Annotation[] annotations) {
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
            return new StringBuilderMarshaller(getInOutFlags(annotations));
        } else if (StringBuilder.class.isAssignableFrom(type)) {
            return new StringBuilderMarshaller(getInOutFlags(annotations));
        } else if (CharSequence.class.isAssignableFrom(type)) {
            return CharSequenceMarshaller.INSTANCE;
        } else if (ByReference.class.isAssignableFrom(type)) {
            return new ByReferenceMarshaller(getInOutFlags(annotations));
        } else if (Struct.class.isAssignableFrom(type)) {
            return new StructMarshaller(getInOutFlags(annotations));
        } else if (ByteBuffer.class.isAssignableFrom(type)) {
            return new ByteBufferMarshaller(getInOutFlags(annotations));
        } else if (ShortBuffer.class.isAssignableFrom(type)) {
            return new ShortBufferMarshaller(getInOutFlags(annotations));
        } else if (IntBuffer.class.isAssignableFrom(type)) {
            return new IntBufferMarshaller(getInOutFlags(annotations));
        } else if (LongBuffer.class.isAssignableFrom(type)) {
            return new LongBufferMarshaller(getInOutFlags(annotations));
        } else if (FloatBuffer.class.isAssignableFrom(type)) {
            return new FloatBufferMarshaller(getInOutFlags(annotations));
        } else if (DoubleBuffer.class.isAssignableFrom(type)) {
            return new DoubleBufferMarshaller(getInOutFlags(annotations));
        } else if (type.isArray() && type.getComponentType() == byte.class) {
            return new ByteArrayMarshaller(getInOutFlags(annotations));
        } else if (type.isArray() && type.getComponentType() == short.class) {
            return new ShortArrayMarshaller(getInOutFlags(annotations));
        } else if (type.isArray() && type.getComponentType() == int.class) {
            return new IntArrayMarshaller(getInOutFlags(annotations));
        } else if (type.isArray() && type.getComponentType() == long.class) {
            return new LongArrayMarshaller(getInOutFlags(annotations));
        } else if (type.isArray() && type.getComponentType() == float.class) {
            return new FloatArrayMarshaller(getInOutFlags(annotations));
        } else if (type.isArray() && type.getComponentType() == double.class) {
            return new DoubleArrayMarshaller(getInOutFlags(annotations));
        } else if (type.isArray() && Struct.class.isAssignableFrom(type.getComponentType())) {
            return new StructArrayMarshaller(getInOutFlags(annotations));
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
            final long ptr = invoker.invokeAddress(function, buffer);
            return ptr != 0 ? new JFFIPointer(ptr) : null;
        }
    }
    static final class StructInvoker extends BaseInvoker {
        private final Class structClass;
        public StructInvoker(Class structClass) {
            this.structClass = structClass;
        }
        
        public final Object invoke(Function function, HeapInvocationBuffer buffer) {
            final long ptr = invoker.invokeAddress(function, buffer);
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
        com.kenai.jffi.MemoryIO IO = com.kenai.jffi.MemoryIO.getInstance();
        static final FunctionInvoker INSTANCE = new StringInvoker();
        public final Object invoke(Function function, HeapInvocationBuffer buffer) {
            final long ptr = invoker.invokeAddress(function, buffer);
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
            buffer.putAddress(((JFFIPointer) parameter).address);
        }
    }
    
    static final class CharSequenceMarshaller extends BaseMarshaller {
        static final Marshaller INSTANCE = new CharSequenceMarshaller();
        static final int FLAGS = com.kenai.jffi.ArrayFlags.IN | com.kenai.jffi.ArrayFlags.NULTERMINATE;
        public void marshal(InvocationBuffer buffer, Object parameter) {
            if (parameter == null) {
                buffer.putAddress(0L);
            } else {
                CharSequence cs = (CharSequence) parameter;
                ByteBuffer buf = StringIO.getStringIO().toNative(cs, cs.length(), true);
                buffer.putArray(buf.array(), buf.arrayOffset(), buf.remaining(), FLAGS);
            }
        }
    }
    static final class StringBuilderMarshaller extends BaseMarshaller {
        private final int nflags, inout;
        public StringBuilderMarshaller(int inout) {
            this.inout = inout;
            this.nflags = getNativeArrayFlags(inout | (ParameterFlags.isIn(inout) ? ParameterFlags.NULTERMINATE : 0));
        }

        @Override
        public final boolean isSessionRequired() {
            return true;
        }

        public void marshal(InvocationBuffer buffer, Object parameter) {
            throw new UnsupportedOperationException("Cannot marshal StringBuilder without session");
        }
        @Override
        public void marshal(InvocationSession session, InvocationBuffer buffer, Object parameter) {
            if (parameter == null) {
                buffer.putAddress(0L);
            } else if (parameter instanceof StringBuilder) {
                final StringBuilder sb = (StringBuilder) parameter;
                final StringIO io = StringIO.getStringIO();
                final ByteBuffer buf = io.toNative(sb, sb.capacity(), ParameterFlags.isIn(inout));
                buffer.putArray(buf.array(), buf.arrayOffset(), buf.remaining(), nflags);
                //
                // Copy the string back out if its an OUT parameter
                //
                if (ParameterFlags.isOut(inout)) {
                    session.addPostInvoke(new InvocationSession.PostInvoke() {

                        public void postInvoke() {
                            sb.delete(0, sb.length()).append(io.fromNative(buf, sb.capacity()));
                        }
                    });
                }
            } else if (parameter instanceof StringBuffer) {
                final StringBuffer sb = (StringBuffer) parameter;
                final StringIO io = StringIO.getStringIO();
                final ByteBuffer buf = io.toNative(sb, sb.capacity(), ParameterFlags.isIn(inout));
                buffer.putArray(buf.array(), buf.arrayOffset(), buf.limit(), nflags);
                //
                // Copy the string back out if its an OUT parameter
                //
                if (ParameterFlags.isOut(inout)) {
                    session.addPostInvoke(new InvocationSession.PostInvoke() {

                        public void postInvoke() {
                            sb.delete(0, sb.length()).append(io.fromNative(buf, sb.capacity()));
                        }
                    });
                }
            }
        }
    }
    static final class ByteArrayMarshaller extends BaseMarshaller {
        private final int flags;
        public ByteArrayMarshaller(int flags) {
            this.flags = getNativeArrayFlags(flags | (ParameterFlags.isIn(flags) ? ParameterFlags.NULTERMINATE : 0));
        }

        public final void marshal(InvocationBuffer buffer, Object parameter) {
            if (parameter == null) {
                buffer.putAddress(0L);
            } else {
                byte[] array = byte[].class.cast(parameter);
                buffer.putArray(array, 0, array.length, flags);
            }
        }
    }
    static final class ShortArrayMarshaller extends BaseMarshaller {
        private final int flags;
        public ShortArrayMarshaller(int flags) {
            this.flags = getNativeArrayFlags(flags);
        }

        public final void marshal(InvocationBuffer buffer, Object parameter) {
            if (parameter == null) {
                buffer.putAddress(0L);
            } else {
                short[] array = short[].class.cast(parameter);
                buffer.putArray(array, 0, array.length, flags);
            }
        }
    }
    static final class IntArrayMarshaller extends BaseMarshaller {
        private final int flags;
        public IntArrayMarshaller(int flags) {
            this.flags = getNativeArrayFlags(flags);
        }

        public final void marshal(InvocationBuffer buffer, Object parameter) {
            if (parameter == null) {
                buffer.putAddress(0L);
            } else {
                int[] array = int[].class.cast(parameter);
                buffer.putArray(array, 0, array.length, flags);
            }
        }
    }
    static final class LongArrayMarshaller extends BaseMarshaller {
        private final int flags;
        public LongArrayMarshaller(int flags) {
            this.flags = getNativeArrayFlags(flags);
        }

        public final void marshal(InvocationBuffer buffer, Object parameter) {
            if (parameter == null) {
                buffer.putAddress(0L);
            } else {
                long[] array = long[].class.cast(parameter);
                buffer.putArray(array, 0, array.length, flags);
            }
        }
    }
    static final class FloatArrayMarshaller extends BaseMarshaller {
        private final int flags;
        public FloatArrayMarshaller(int flags) {
            this.flags = getNativeArrayFlags(flags);
        }

        public final void marshal(InvocationBuffer buffer, Object parameter) {
            if (parameter == null) {
                buffer.putAddress(0L);
            } else {
                float[] array = float[].class.cast(parameter);
                buffer.putArray(array, 0, array.length, flags);
            }
        }
    }
    static final class DoubleArrayMarshaller extends BaseMarshaller {
        private final int flags;
        public DoubleArrayMarshaller(int flags) {
            this.flags = getNativeArrayFlags(flags);
        }

        public final void marshal(InvocationBuffer buffer, Object parameter) {
            if (parameter == null) {
                buffer.putAddress(0L);
            } else {
                double[] array = double[].class.cast(parameter);
                buffer.putArray(array, 0, array.length, flags);
            }
        }
    }
    static final class ByteBufferMarshaller extends BaseMarshaller {
        private final int flags;
        public ByteBufferMarshaller(int flags) {
            this.flags = getNativeArrayFlags(flags | (ParameterFlags.isIn(flags) ? ParameterFlags.NULTERMINATE : 0));
        }

        public final void marshal(InvocationBuffer buffer, Object parameter) {
            ByteBuffer buf = (ByteBuffer) parameter;
            if (parameter == null) {
                buffer.putAddress(0L);
            } else if (buf.hasArray()) {
                buffer.putArray(buf.array(), buf.arrayOffset() + buf.position(), buf.remaining(), flags);
            } else {
                buffer.putDirectBuffer(buf, buf.position(), buf.remaining());
            }
        }
    }
    static final class ShortBufferMarshaller extends BaseMarshaller {
        private final int flags;
        public ShortBufferMarshaller(int flags) {
            this.flags = getNativeArrayFlags(flags);
        }

        public final void marshal(InvocationBuffer buffer, Object parameter) {
            ShortBuffer buf = (ShortBuffer) parameter;
            if (parameter == null) {
                buffer.putAddress(0L);
            } else if (buf.hasArray()) {
                buffer.putArray(buf.array(), buf.arrayOffset() + buf.position(), buf.remaining(), flags);
            } else {
                buffer.putDirectBuffer(buf, buf.position() << 1, buf.remaining() << 1);
            }
        }
    }
    static final class IntBufferMarshaller extends BaseMarshaller {
        private final int flags;
        public IntBufferMarshaller(int flags) {
            this.flags = getNativeArrayFlags(flags);
        }

        public final void marshal(InvocationBuffer buffer, Object parameter) {
            IntBuffer buf = (IntBuffer) parameter;
            if (parameter == null) {
                buffer.putAddress(0L);
            } else if (buf.hasArray()) {
                buffer.putArray(buf.array(), buf.arrayOffset() + buf.position(), buf.remaining(), flags);
            } else {
                buffer.putDirectBuffer(buf, buf.position() << 2, buf.remaining() << 2);
            }
        }
    }
    static final class LongBufferMarshaller extends BaseMarshaller {
        private final int flags;
        public LongBufferMarshaller(int flags) {
            this.flags = getNativeArrayFlags(flags);
        }

        public final void marshal(InvocationBuffer buffer, Object parameter) {
            LongBuffer buf = (LongBuffer) parameter;
            if (parameter == null) {
                buffer.putAddress(0L);
            } else if (buf.hasArray()) {
                buffer.putArray(buf.array(), buf.arrayOffset() + buf.position(), buf.remaining(), flags);
            } else {
                buffer.putDirectBuffer(buf, buf.position() << 3, buf.remaining() << 3);
            }
        }
    }
    static final class FloatBufferMarshaller extends BaseMarshaller {
        private final int flags;
        public FloatBufferMarshaller(int flags) {
            this.flags = getNativeArrayFlags(flags);
        }

        public final void marshal(InvocationBuffer buffer, Object parameter) {
            FloatBuffer buf = (FloatBuffer) parameter;
            if (parameter == null) {
                buffer.putAddress(0L);
            } else if (buf.hasArray()) {
                buffer.putArray(buf.array(), buf.arrayOffset() + buf.position(), buf.remaining(), flags);
            } else {
                buffer.putDirectBuffer(buf, buf.position() << 2, buf.remaining() << 2);
            }
        }
    }
    static final class DoubleBufferMarshaller extends BaseMarshaller {
        private final int flags;
        public DoubleBufferMarshaller(int flags) {
            this.flags = getNativeArrayFlags(flags);
        }

        public final void marshal(InvocationBuffer buffer, Object parameter) {
            DoubleBuffer buf = (DoubleBuffer) parameter;
            if (parameter == null) {
                buffer.putAddress(0L);
            } else if (buf.hasArray()) {
                buffer.putArray(buf.array(), buf.arrayOffset() + buf.position(), buf.remaining(), flags);
            } else {
                buffer.putDirectBuffer(buf, buf.position() << 3, buf.remaining() << 3);
            }
        }
    }
    static final class ByReferenceMarshaller extends BaseMarshaller {
        private final int flags;
        public ByReferenceMarshaller(int flags) {
            this.flags = getNativeArrayFlags(flags);
        }

        @Override
        public final boolean isSessionRequired() {
            return true;
        }

        @Override
        public final void marshal(InvocationSession session, InvocationBuffer buffer, Object parameter) {
            if (parameter == null) {
                buffer.putAddress(0L);
            } else {
                final ByReference ref = (ByReference) parameter;
                final ByteBuffer buf = ByteBuffer.allocate(ref.nativeSize()).order(ByteOrder.nativeOrder());
                buf.clear();
                if (com.kenai.jffi.ArrayFlags.isIn(flags)) {
                    ref.marshal(buf);
                }
                buffer.putArray(buf.array(), buf.arrayOffset() + buf.position(), buf.remaining(), flags);
                if (com.kenai.jffi.ArrayFlags.isOut(flags)) {
                    session.addPostInvoke(new InvocationSession.PostInvoke() {
                        public void postInvoke() {
                            ref.unmarshal(buf);
                        }
                    });
                }
            }
        }

        public void marshal(InvocationBuffer buffer, Object parameter) {
            throw new UnsupportedOperationException("Cannot marshal ByReference without session");
        }
    }
    static final class StructMarshaller extends BaseMarshaller {
        private final int nflags, flags;
        public StructMarshaller(int flags) {
            this.flags = flags;
            this.nflags = getNativeArrayFlags(flags);
        }

        public final void marshal(InvocationBuffer buffer, Object parameter) {
            if (parameter == null) {
                buffer.putAddress(0L);
            } else {
                Struct s = (Struct) parameter;
                MemoryIO io = StructUtil.getMemoryIO(s, flags);
                if (io instanceof AbstractArrayMemoryIO) {
                    AbstractArrayMemoryIO aio = (AbstractArrayMemoryIO) io;
                    buffer.putArray(aio.array(), aio.offset(), aio.length(), nflags);
                } else if (io instanceof DirectMemory) {
                    buffer.putAddress(((DirectMemory) io).getAddress());
                }
            }
        }
    }
    static final class StructArrayMarshaller extends BaseMarshaller {
        private final int nflags, flags;
        public StructArrayMarshaller(int flags) {
            this.flags = flags;
            this.nflags = getNativeArrayFlags(flags);
        }
        
        public final void marshal(InvocationBuffer buffer, Object parameter) {
            if (parameter == null) {
                buffer.putAddress(0L);
            } else {
                Struct[] array = Struct[].class.cast(parameter);
                MemoryIO io = StructUtil.getMemoryIO(array[0], flags);
                if (!(io instanceof DelegatingMemoryIO)) {
                    throw new RuntimeException("Struct array must be backed by contiguous array");
                }
                io = ((DelegatingMemoryIO) io).getDelegatedMemoryIO();
                if (io instanceof AbstractArrayMemoryIO) {
                    AbstractArrayMemoryIO aio = (AbstractArrayMemoryIO) io;
                    buffer.putArray(aio.array(), aio.offset(), aio.length(), nflags);
                } else if (io instanceof DirectMemory) {
                    buffer.putAddress(((DirectMemory) io).getAddress());
                }
            }
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
