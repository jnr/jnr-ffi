
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
import com.kenai.jaffl.provider.AbstractArrayMemoryIO;
import com.kenai.jaffl.provider.InvocationSession;
import com.kenai.jaffl.provider.Invoker;
import com.kenai.jaffl.struct.Struct;
import com.kenai.jaffl.struct.StructUtil;
import com.kenai.jaffl.util.EnumMapper;
import com.kenai.jffi.Function;
import com.kenai.jffi.HeapInvocationBuffer;
import com.kenai.jffi.InvocationBuffer;
import com.kenai.jffi.Type;
import java.lang.annotation.Annotation;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.reflect.Method;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.util.Map;

class DefaultInvokerFactory {
    public Invoker createInvoker(Method method, com.kenai.jaffl.provider.Library library, Map<LibraryOption, ?> options) {
        final long address = ((Library) library).getNativeLibrary().getSymbolAddress(method.getName());

        Marshaller[] marshallers = new Marshaller[method.getParameterTypes().length];
        Type[] paramTypes = new Type[marshallers.length];
        for (int i = 0; i < marshallers.length; ++i) {
            marshallers[i] = getMarshaller(method, i);
            paramTypes[i] = getNativeParameterType(method, i);
        }
        Class returnType = method.getReturnType();

        Function function = new Function(address, getNativeReturnType(method), paramTypes);
        FunctionInvoker invoker = null;
        if (Void.class.isAssignableFrom(returnType) || void.class == returnType) {
            invoker = VoidInvoker.INSTANCE;
        } else if (Boolean.class.isAssignableFrom(returnType) || boolean.class == returnType) {
            invoker = BooleanInvoker.INSTANCE;
        } else if (Enum.class.isAssignableFrom(returnType)) {
            invoker = new EnumInvoker(returnType);
        } else if (Byte.class.isAssignableFrom(returnType) || byte.class == returnType) {
            invoker = Int8Invoker.INSTANCE;
        } else if (Short.class.isAssignableFrom(returnType) || short.class == returnType) {
            invoker = Int16Invoker.INSTANCE;
        } else if (Integer.class.isAssignableFrom(returnType) || int.class == returnType) {
            invoker = Int32Invoker.INSTANCE;
        } else if (Long.class.isAssignableFrom(returnType) || long.class == returnType) {
            invoker = Int64Invoker.INSTANCE;
        } else if (NativeLong.class.isAssignableFrom(returnType)) {
            invoker = Platform.getPlatform().longSize() == 32
                ? NativeLong32Invoker.INSTANCE : NativeLong64Invoker.INSTANCE;
        } else if (Float.class.isAssignableFrom(returnType) || float.class == returnType) {
            invoker = Float32Invoker.INSTANCE;
        } else if (Double.class.isAssignableFrom(returnType) || double.class == returnType) {
            invoker = Float64Invoker.INSTANCE;
        } else if (Pointer.class.isAssignableFrom(returnType)) {
            invoker = PointerInvoker.INSTANCE;
        } else if (Struct.class.isAssignableFrom(returnType)) {
            invoker = new StructInvoker(returnType);
        } else {
            throw new IllegalArgumentException("Unknown return type: " + returnType);
        }
        boolean sessionRequired = false;
        for (Marshaller m : marshallers) {
            if (m.isSessionRequired()) {
                sessionRequired = true;
                break;
            }
        }
        return sessionRequired
                ? new SessionInvoker(function, invoker, marshallers)
                : new DefaultInvoker(function, invoker, marshallers);
    }
    private static final Type getNativeReturnType(Method method) {
        Class type = method.getReturnType();
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
        } else {
            throw new IllegalArgumentException("Unsupported return type: " + type);
        }
    }
    private static final Type getNativeParameterType(Method method, int paramIndex) {
        Class type = method.getParameterTypes()[paramIndex];
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
        } else if (Struct.class.isAssignableFrom(type)) {
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
    static final int getNativeFlags(int flags) {
        int nflags = 0;
        nflags |= ParameterFlags.isIn(flags) ? com.kenai.jffi.ParameterFlags.IN : 0;
        nflags |= ParameterFlags.isOut(flags) ? com.kenai.jffi.ParameterFlags.OUT : 0;
        nflags |= ParameterFlags.isNulTerminate(flags) ? com.kenai.jffi.ParameterFlags.NULTERMINATE : 0;
        return nflags;
    }
    private static final Marshaller getMarshaller(Method method, int paramIndex) {
        Class type = method.getParameterTypes()[paramIndex];
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
            return new StringBuilderMarshaller(getInOutFlags(method, paramIndex));
        } else if (StringBuilder.class.isAssignableFrom(type)) {
            return new StringBuilderMarshaller(getInOutFlags(method, paramIndex));
        } else if (CharSequence.class.isAssignableFrom(type)) {
            return CharSequenceMarshaller.INSTANCE;
        } else if (ByReference.class.isAssignableFrom(type)) {
            return new ByReferenceMarshaller(getInOutFlags(method, paramIndex));
        } else if (Struct.class.isAssignableFrom(type)) {
            return new StructMarshaller(getInOutFlags(method, paramIndex));
        } else if (ByteBuffer.class.isAssignableFrom(type)) {
            return new ByteBufferMarshaller(getInOutFlags(method, paramIndex));
        } else if (ShortBuffer.class.isAssignableFrom(type)) {
            return new ShortBufferMarshaller(getInOutFlags(method, paramIndex));
        } else if (IntBuffer.class.isAssignableFrom(type)) {
            return new IntBufferMarshaller(getInOutFlags(method, paramIndex));
        } else if (LongBuffer.class.isAssignableFrom(type)) {
            return new LongBufferMarshaller(getInOutFlags(method, paramIndex));
        } else if (FloatBuffer.class.isAssignableFrom(type)) {
            return new FloatBufferMarshaller(getInOutFlags(method, paramIndex));
        } else if (DoubleBuffer.class.isAssignableFrom(type)) {
            return new DoubleBufferMarshaller(getInOutFlags(method, paramIndex));
        } else if (type.isArray() && type.getComponentType() == byte.class) {
            return new ByteArrayMarshaller(getInOutFlags(method.getParameterAnnotations()[paramIndex]));
        } else if (type.isArray() && type.getComponentType() == short.class) {
            return new ShortArrayMarshaller(getInOutFlags(method.getParameterAnnotations()[paramIndex]));
        } else if (type.isArray() && type.getComponentType() == int.class) {
            return new IntArrayMarshaller(getInOutFlags(method.getParameterAnnotations()[paramIndex]));
        } else if (type.isArray() && type.getComponentType() == long.class) {
            return new LongArrayMarshaller(getInOutFlags(method.getParameterAnnotations()[paramIndex]));
        } else if (type.isArray() && type.getComponentType() == float.class) {
            return new FloatArrayMarshaller(getInOutFlags(method.getParameterAnnotations()[paramIndex]));
        } else if (type.isArray() && type.getComponentType() == double.class) {
            return new DoubleArrayMarshaller(getInOutFlags(method.getParameterAnnotations()[paramIndex]));
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
    private static final class StringIO {
        private final static Charset defaultCharset = Charset.defaultCharset();
        public final CharsetEncoder encoder = defaultCharset.newEncoder();
        public final CharsetDecoder decoder = defaultCharset.newDecoder();
        public final int nulByteCount = Math.round(encoder.maxBytesPerChar());
        public StringIO() {
            encoder.onMalformedInput(CodingErrorAction.REPLACE)
                .onUnmappableCharacter(CodingErrorAction.REPLACE);
            decoder.onMalformedInput(CodingErrorAction.REPLACE)
                .onUnmappableCharacter(CodingErrorAction.REPLACE);
        }
        public final void nulTerminate(ByteBuffer buf) {
            // NUL terminate the string
            int nulSize = nulByteCount;
            while (nulSize >= 4) {
                buf.putInt(0);
                nulSize -= 4;
            }
            if (nulSize >= 2) {
                buf.putShort((short) 0);
                nulSize -= 2;
            }
            if (nulSize >= 1) {
                buf.put((byte) 0);
            }
        }
    }
    private static final ThreadLocal<Reference<StringIO>> threadLocalStringIO
            = new ThreadLocal<Reference<StringIO>>();
    private static final StringIO getStringIO() {
        Reference<StringIO> ref = threadLocalStringIO.get();
        StringIO io = ref != null ? ref.get() : null;
        if (io == null) {
            io = new StringIO();
            threadLocalStringIO.set(new SoftReference<StringIO>(io));
        }
        return io;
    }
    private static final ByteBuffer copyinString(final CharSequence value, final int minSize, int flags) {
        final StringIO io = getStringIO();
        final CharsetEncoder encoder = io.encoder;

        // Calculate the raw byte size required (with allowance for NUL termination)
        final int len = (int) (((float)Math.max(minSize, value.length()) + 1) * encoder.maxBytesPerChar());
        final ByteBuffer buf = ByteBuffer.allocate(len);
        if (ParameterFlags.isIn(flags)) {
            encoder.reset();
            //
            // Copy the string to native memory
            //
            encoder.encode(CharBuffer.wrap(value), buf, true);
            encoder.flush(buf);
            io.nulTerminate(buf);
            buf.rewind();
        }
        return buf;
    }
    
    private static final CharSequence copyoutString(final ByteBuffer buf, final int maxSize) {
        // Find the NUL terminator and limit to that, so the
        // StringBuffer/StringBuilder does not have superfluous NUL chars
        int end = indexOf(buf, (byte) 0);
        if (end < 0 || end > maxSize) {
            end = maxSize;
        }
        buf.rewind().limit(end);
        try {
            return getStringIO().decoder.reset().decode(buf);
        } catch (CharacterCodingException ex) {
            throw new Error("Illegal character data in native string", ex);
        }
    }
    public final static int indexOf(ByteBuffer buf, byte value) {
        for (int offset = 0; offset > -1; ++offset) {
            if (buf.get(offset) == value) {
                return offset;
            }
        }
        return -1;
    }
    static final class CharSequenceMarshaller extends BaseMarshaller {
        static final Marshaller INSTANCE = new CharSequenceMarshaller();
        static final int FLAGS = com.kenai.jffi.ParameterFlags.IN | com.kenai.jffi.ParameterFlags.NULTERMINATE;
        public void marshal(InvocationBuffer buffer, Object parameter) {
            if (parameter == null) {
                buffer.putAddress(0L);
            } else {
                CharSequence cs = (CharSequence) parameter;
                ByteBuffer buf = copyinString(cs, cs.length(), ParameterFlags.IN);
                buffer.putArray(buf.array(), buf.arrayOffset(), buf.remaining(), FLAGS);
            }
        }
    }
    static final class StringBuilderMarshaller extends BaseMarshaller {
        private final int nflags, inout;
        public StringBuilderMarshaller(int inout) {
            this.inout = inout;
            this.nflags = getNativeFlags(inout | (ParameterFlags.isIn(inout) ? ParameterFlags.NULTERMINATE : 0));
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
                final ByteBuffer buf = copyinString(sb, sb.capacity(), inout);
                buffer.putArray(buf.array(), buf.arrayOffset(), buf.remaining(), nflags);
                //
                // Copy the string back out if its an OUT parameter
                //
                if (ParameterFlags.isOut(inout)) {
                    session.addPostInvoke(new InvocationSession.PostInvoke() {

                        public void postInvoke() {
                            sb.delete(0, sb.length()).append(copyoutString(buf, sb.capacity()));
                        }
                    });
                }
            } else if (parameter instanceof StringBuffer) {
                final StringBuffer sb = (StringBuffer) parameter;
                final ByteBuffer buf = copyinString(sb, sb.capacity(), inout);
                buffer.putArray(buf.array(), buf.arrayOffset(), buf.limit(), nflags);
                //
                // Copy the string back out if its an OUT parameter
                //
                if (ParameterFlags.isOut(inout)) {
                    session.addPostInvoke(new InvocationSession.PostInvoke() {

                        public void postInvoke() {
                            sb.delete(0, sb.length()).append(copyoutString(buf, sb.capacity()));
                        }
                    });
                }
            }
        }
    }
    static final class ByteArrayMarshaller extends BaseMarshaller {
        private final int flags;
        public ByteArrayMarshaller(int flags) {
            this.flags = getNativeFlags(flags | (ParameterFlags.isIn(flags) ? ParameterFlags.NULTERMINATE : 0));
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
            this.flags = getNativeFlags(flags);
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
            this.flags = getNativeFlags(flags);
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
            this.flags = getNativeFlags(flags);
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
            this.flags = getNativeFlags(flags);
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
            this.flags = getNativeFlags(flags);
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
            this.flags = getNativeFlags(flags | (ParameterFlags.isIn(flags) ? ParameterFlags.NULTERMINATE : 0));
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
            this.flags = getNativeFlags(flags);
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
            this.flags = getNativeFlags(flags);
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
            this.flags = getNativeFlags(flags);
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
            this.flags = getNativeFlags(flags);
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
            this.flags = getNativeFlags(flags);
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
            this.flags = getNativeFlags(flags);
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
                ref.marshal(buf);
                buffer.putArray(buf.array(), buf.arrayOffset() + buf.position(), buf.remaining(), flags);
                session.addPostInvoke(new InvocationSession.PostInvoke() {
                    public void postInvoke() {
                        ref.unmarshal(buf);
                    }
                });
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
            this.nflags = getNativeFlags(flags);
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
}
