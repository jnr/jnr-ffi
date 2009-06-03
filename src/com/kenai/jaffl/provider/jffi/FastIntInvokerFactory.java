
package com.kenai.jaffl.provider.jffi;

import com.kenai.jaffl.LibraryOption;
import com.kenai.jaffl.NativeLong;
import com.kenai.jaffl.Platform;
import com.kenai.jaffl.Pointer;
import com.kenai.jaffl.annotations.IgnoreError;
import com.kenai.jaffl.annotations.SaveError;
import com.kenai.jaffl.util.EnumMapper;
import com.kenai.jffi.CallingConvention;
import com.kenai.jffi.Function;
import com.kenai.jffi.Type;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;


class FastIntInvokerFactory implements InvokerFactory {
    private final static class SingletonHolder {
        static InvokerFactory INSTANCE = new FastIntInvokerFactory();
    }
    public static final InvokerFactory getInstance() {
        return SingletonHolder.INSTANCE;
    }
    public com.kenai.jaffl.provider.Invoker createInvoker(Method method, com.kenai.jaffl.provider.Library library, Map<LibraryOption, ?> options) {
        final long address = ((Library) library).getNativeLibrary().getSymbolAddress(method.getName());

        Class returnType = method.getReturnType();
        Class[] paramTypes = method.getParameterTypes();
        Type[] nativeParamTypes = new Type[paramTypes.length];
        for (int i = 0; i < nativeParamTypes.length; ++i) {
            nativeParamTypes[i] = InvokerUtil.getNativeParameterType(paramTypes[i]);
        }
        
        Function function = new Function(address, InvokerUtil.getNativeReturnType(method), 
                nativeParamTypes, CallingConvention.DEFAULT, InvokerUtil.requiresErrno(method));

        FastIntFunctionInvoker functionInvoker;
        switch (paramTypes.length) {
            case 0:
                functionInvoker = FunctionInvokerVrI.INSTANCE;
                break;
            case 1:
                functionInvoker = FunctionInvokerIrI.INSTANCE;
                break;
            case 2:
                functionInvoker = FunctionInvokerIIrI.INSTANCE;
                break;
            case 3:
                functionInvoker = FunctionInvokerIIIrI.INSTANCE;
                break;
            default:
                throw new IllegalArgumentException("Parameter limit exceeded");
        }
        if (Void.class.isAssignableFrom(returnType) || void.class == returnType) {
            return new VoidInvoker(function, functionInvoker);
        } else if (Boolean.class.isAssignableFrom(returnType) || boolean.class == returnType) {
            return new BooleanInvoker(function, functionInvoker);
        } else if (Byte.class.isAssignableFrom(returnType) || byte.class == returnType) {
            return new ByteInvoker(function, functionInvoker);
        } else if (Short.class.isAssignableFrom(returnType) || short.class == returnType) {
            return new ShortInvoker(function, functionInvoker);
        } else if (Integer.class.isAssignableFrom(returnType) || int.class == returnType) {
            return new IntInvoker(function, functionInvoker);
        } else if (NativeLong.class.isAssignableFrom(returnType)) {
            return new NativeLongInvoker(function, functionInvoker);
        } else if (Enum.class.isAssignableFrom(returnType)) {
            return new EnumInvoker(function, functionInvoker, returnType);
        } else if (Pointer.class.isAssignableFrom(returnType)) {
            return new PointerInvoker(function, functionInvoker);
        } else {
            throw new IllegalArgumentException("Unknown return type: " + returnType);
        }
    }
    public final boolean isMethodSupported(Method method) {
        try {
            if (!isFastIntResult(method.getReturnType())) {
                return false;
            }            
            
            Class[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length > 3) {
                return false;
            }

            for (int i = 0; i < parameterTypes.length; ++i) {
                if (!isFastIntParam(parameterTypes[i])) {
                    return false;
                }
            }
            
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
    final static boolean isFastIntResult(Class type) {
        return Void.class.isAssignableFrom(type) || void.class == type
                || Boolean.class.isAssignableFrom(type) || boolean.class == type
                || Enum.class.isAssignableFrom(type)
                || Byte.class.isAssignableFrom(type) || byte.class == type
                || Short.class.isAssignableFrom(type) || short.class == type
                || Integer.class.isAssignableFrom(type) || int.class == type
                || (NativeLong.class.isAssignableFrom(type) && Platform.getPlatform().longSize() == 32)
                || (Pointer.class.isAssignableFrom(type) && Platform.getPlatform().addressSize() == 32)
                ;
    }
    final static boolean isFastIntParam(Class type) {
        return Boolean.class.isAssignableFrom(type) || boolean.class == type
                || Byte.class.isAssignableFrom(type) || byte.class == type
                || Short.class.isAssignableFrom(type) || short.class == type
                || Integer.class.isAssignableFrom(type) || int.class == type
                || (NativeLong.class.isAssignableFrom(type) && Platform.getPlatform().longSize() == 32)
                ;
    }
    
    static abstract class FastIntFunctionInvoker {
        static final com.kenai.jffi.Invoker ffi = com.kenai.jffi.Invoker.getInstance();
        abstract int invoke(Function function, Object[] args);
    }
    static interface ResultConverter {
        Object fromNative(int value);
    }
    static abstract class BaseInvoker implements com.kenai.jaffl.provider.Invoker {
        final Function function;
        final FastIntFunctionInvoker functionInvoker;
        BaseInvoker(Function function, FastIntFunctionInvoker invoker) {
            this.function = function;
            this.functionInvoker = invoker;
        }
    }
    static final class VoidInvoker extends BaseInvoker {
        public VoidInvoker(Function function, FastIntFunctionInvoker invoker) {
            super(function, invoker);
        }

        public final Object invoke(Object[] parameters) {
            functionInvoker.invoke(function, parameters);
            return null;
        }
    }
    static final class ByteInvoker extends BaseInvoker {
        public ByteInvoker(Function function, FastIntFunctionInvoker invoker) {
            super(function, invoker);
        }

        public final Object invoke(Object[] parameters) {
            return Byte.valueOf((byte) functionInvoker.invoke(function, parameters));
        }
    }
    static final class ShortInvoker extends BaseInvoker {
        public ShortInvoker(Function function, FastIntFunctionInvoker invoker) {
            super(function, invoker);
        }

        public final Object invoke(Object[] parameters) {
            return Short.valueOf((short) functionInvoker.invoke(function, parameters));
        }
    }
    static final class IntInvoker extends BaseInvoker {
        public IntInvoker(Function function, FastIntFunctionInvoker invoker) {
            super(function, invoker);
        }

        public final Object invoke(Object[] parameters) {
            return Integer.valueOf(functionInvoker.invoke(function, parameters));
        }
    }
    static final class NativeLongInvoker extends BaseInvoker {
        public NativeLongInvoker(Function function, FastIntFunctionInvoker invoker) {
            super(function, invoker);
        }

        public final Object invoke(Object[] parameters) {
            return NativeLong.valueOf(functionInvoker.invoke(function, parameters));
        }
    }
    static final class BooleanInvoker extends BaseInvoker {
        public BooleanInvoker(Function function, FastIntFunctionInvoker invoker) {
            super(function, invoker);
        }

        public final Object invoke(Object[] parameters) {
            return Boolean.valueOf(functionInvoker.invoke(function, parameters) != 0);
        }
    }
    static final class PointerInvoker extends BaseInvoker {
        public PointerInvoker(Function function, FastIntFunctionInvoker invoker) {
            super(function, invoker);
        }

        public final Object invoke(Object[] parameters) {
            return new JFFIPointer((long) functionInvoker.invoke(function, parameters) & 0xffffffffL);
        }
    }
    static final class EnumInvoker extends BaseInvoker {
        private final Class enumClass;
        private EnumInvoker(Function function, FastIntFunctionInvoker invoker, Class enumClass) {
            super(function, invoker);
            this.enumClass = enumClass;
        }
        @SuppressWarnings("unchecked")
        public final Object invoke(Object[] parameters) {
            return EnumMapper.getInstance().valueOf(functionInvoker.invoke(function, parameters), enumClass);
        }
    }
    static final class FunctionInvokerVrI extends FastIntFunctionInvoker {
        static final FastIntFunctionInvoker INSTANCE = new FunctionInvokerVrI();
        public int invoke(Function function, Object[] args) {
            return ffi.invokeVrI(function);
        }
    }
    static final class FunctionInvokerIrI extends FastIntFunctionInvoker {
        static final FastIntFunctionInvoker INSTANCE = new FunctionInvokerIrI();
        public final int invoke(Function function, Object[] args) {
            return ffi.invokeIrI(function, ((Number) args[0]).intValue());
        }
    }
    static final class FunctionInvokerIIrI extends FastIntFunctionInvoker {
        static final FastIntFunctionInvoker INSTANCE = new FunctionInvokerIIrI();
        public final int invoke(Function function, Object[] args) {
            return ffi.invokeIIrI(function,
                    ((Number) args[0]).intValue(),
                    ((Number) args[1]).intValue());
        }
    }
    private static final class FunctionInvokerIIIrI extends FastIntFunctionInvoker {
        static final FastIntFunctionInvoker INSTANCE = new FunctionInvokerIIIrI();
        public final int invoke(Function function, Object[] args) {
            return ffi.invokeIIIrI(function, ((Number) args[0]).intValue(),
                    ((Number) args[1]).intValue(), ((Number) args[2]).intValue());
        }
    }
}
