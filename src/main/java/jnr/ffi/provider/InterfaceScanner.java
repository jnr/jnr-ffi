/*
 * Copyright (C) 2013 Wayne Meissner
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

package jnr.ffi.provider;

import jnr.ffi.CallingConvention;
import jnr.ffi.Variable;
import jnr.ffi.annotations.StdCall;
import jnr.ffi.mapper.SignatureTypeMapper;

import java.lang.reflect.Method;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;

public class InterfaceScanner {
    private final Class interfaceClass;
    private final SignatureTypeMapper typeMapper;
    private final CallingConvention callingConvention;
    private final Method[] methods;

    public InterfaceScanner(Class interfaceClass, SignatureTypeMapper typeMapper, CallingConvention callingConvention) {
        this.interfaceClass = interfaceClass;
        this.typeMapper = typeMapper;
        this.methods = interfaceClass.getMethods();
        this.callingConvention = interfaceClass.isAnnotationPresent(StdCall.class) ? CallingConvention.STDCALL : callingConvention; 
    }

    public Collection<NativeFunction> functions() {
        return new AbstractCollection<NativeFunction>() {
            @Override
            public Iterator<NativeFunction> iterator() {
                return new FunctionsIterator(methods);
            }

            @Override
            public int size() {
                return 0;
            }
        };
    }

    public Collection<NativeVariable> variables() {
        return new AbstractCollection<NativeVariable>() {
            @Override
            public Iterator<NativeVariable> iterator() {
                return new VariablesIterator(methods);
            }

            @Override
            public int size() {
                return 0;
            }
        };
    }
    
    private final class FunctionsIterator implements Iterator<NativeFunction> {
        private final java.lang.reflect.Method[] methods;
        private int nextIndex;

        private FunctionsIterator(Method[] methods) {
            this.methods = methods;
            this.nextIndex = 0;
        }

        @Override
        public boolean hasNext() {
            for (; nextIndex < methods.length; nextIndex++) {
                if (!Variable.class.isAssignableFrom(methods[nextIndex].getReturnType())) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public NativeFunction next() {
            // Allow individual methods to set the calling convention to stdcall
            CallingConvention callingConvention = methods[nextIndex].isAnnotationPresent(StdCall.class)
                    ? CallingConvention.STDCALL : InterfaceScanner.this.callingConvention;
            return new NativeFunction(methods[nextIndex++], callingConvention);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private final class VariablesIterator implements Iterator<NativeVariable> {
        private final java.lang.reflect.Method[] methods;
        private int nextIndex;

        private VariablesIterator(Method[] methods) {
            this.methods = methods;
            this.nextIndex = 0;
        }

        @Override
        public boolean hasNext() {
            for (; nextIndex < methods.length; nextIndex++) {
                if (Variable.class == methods[nextIndex].getReturnType()) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public NativeVariable next() {
            return new NativeVariable(methods[nextIndex++]);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
