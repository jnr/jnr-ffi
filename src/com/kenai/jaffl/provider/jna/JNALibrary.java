/*
 * Copyright (C) 2008 Wayne Meissner
 *
 * This file is part of jffi.
 *
 * This code is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License version 3 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * version 3 for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * version 3 along with this work.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.kenai.jaffl.provider.jna;

import com.kenai.jaffl.LibraryOption;
import com.kenai.jaffl.annotations.In;
import com.kenai.jaffl.annotations.Out;
import com.kenai.jaffl.byref.ByReference;
import com.kenai.jaffl.provider.Invoker;
import com.kenai.jaffl.provider.jna.marshallers.ByRefMarshaller;
import com.kenai.jaffl.provider.jna.marshallers.MarshalContext;
import com.kenai.jaffl.provider.jna.marshallers.Marshaller;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
final class JNALibrary implements com.kenai.jaffl.provider.Library {
    private static final String OPTION_INVOKING_METHOD = "invoking-method";
    private static final com.sun.jna.TypeMapper jnaTypeMapper = new JNATypeMapper();
    private final String libraryName;
    JNALibrary(String libraryName) {
        this.libraryName = libraryName;
    }
    public Invoker getInvoker(Method method, Map<LibraryOption, ?> options) {
        boolean needMarshal = false;
        for (Class c : method.getParameterTypes()) {
            if (ByReference.class.isAssignableFrom(c)) {
                needMarshal = true;
            }
        }
        com.sun.jna.NativeLibrary lib = com.sun.jna.NativeLibrary.getInstance(libraryName);
        return needMarshal 
                ? new MarshallingInvoker(lib, method)
                : new DefaultInvoker(lib, method);
    }

    public Object libraryLock() {
        return com.sun.jna.NativeLibrary.getInstance(libraryName);
    }

    private static abstract class BaseInvoker implements Invoker {
        final com.sun.jna.Function function;
        final Class returnType;
        final Map<String, Object> functionOptions;
        BaseInvoker(com.sun.jna.NativeLibrary library, Method method) {
            function = library.getFunction(method.getName());
            returnType = method.getReturnType();
            functionOptions = new IdentityHashMap<String, Object>(4);
            functionOptions.put(com.sun.jna.Library.OPTION_TYPE_MAPPER, jnaTypeMapper);
            functionOptions.put(OPTION_INVOKING_METHOD, method);
        }
    }
    private static class DefaultInvoker extends BaseInvoker {
        DefaultInvoker(com.sun.jna.NativeLibrary library, Method method) {
            super(library, method);
        }
        public Object invoke(Object[] parameters) {
            return function.invoke(returnType, parameters, functionOptions);
        }
    }
    private static final MarshalContext getContext(Method m, int i) {
        Annotation[] annotations = m.getParameterAnnotations()[i];
        boolean in = false;
        boolean out = false;
        for (int n = 0; n < annotations.length; ++n) {
            in = (annotations[n] instanceof In) ? true : in;
            out = (annotations[n] instanceof Out) ? true : out;
        }
        return new MarshalContext(in, out);
    }
    private static final class MarshallingInvoker extends BaseInvoker {
        private final Marshaller[] marshallers;
        private final int[] marshalIndexes;
        MarshallingInvoker(com.sun.jna.NativeLibrary library, Method method) {
            super(library, method);
            Class[] types = method.getParameterTypes();
            List<Marshaller> m = new ArrayList<Marshaller>(types.length);
            List<Integer> indexes = new ArrayList<Integer>(types.length);
            for (int i = 0; i < types.length; ++i) {
                MarshalContext ctx = getContext(method, i);
                if (ByReference.class.isAssignableFrom(types[i])) {
                    m.add(new ByRefMarshaller(ctx));
                } else {
                    continue;
                }
                indexes.add(i);
            }
            marshallers = m.toArray(new Marshaller[m.size()]);
            marshalIndexes = new int[indexes.size()];
            for (int i = 0; i < marshalIndexes.length; ++i) {
                marshalIndexes[i] = indexes.get(i);
            }
        }
        public Object invoke(Object[] parameters) {
            InvocationSession session = new InvocationSession(marshallers.length);
            for (int i = 0; i < marshalIndexes.length; ++i) {
                parameters[marshalIndexes[i]]
                        = marshallers[i].marshal(session, parameters[marshalIndexes[i]]);
            }
            Object retVal = function.invoke(returnType, parameters, functionOptions);
            session.finish();
            return retVal;
        }
    }
}
