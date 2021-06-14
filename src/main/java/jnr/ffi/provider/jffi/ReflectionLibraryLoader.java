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

import jnr.ffi.CallingConvention;
import jnr.ffi.LibraryOption;
import jnr.ffi.Runtime;
import jnr.ffi.Variable;
import jnr.ffi.annotations.Synchronized;
import jnr.ffi.mapper.CompositeTypeMapper;
import jnr.ffi.mapper.FunctionMapper;
import jnr.ffi.mapper.SignatureTypeMapper;
import jnr.ffi.provider.IdentityFunctionMapper;
import jnr.ffi.provider.InterfaceScanner;
import jnr.ffi.provider.Invoker;
import jnr.ffi.provider.LoadedLibrary;
import jnr.ffi.provider.NativeFunction;
import jnr.ffi.provider.NativeInvocationHandler;
import jnr.ffi.provider.NativeVariable;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static jnr.ffi.provider.jffi.InvokerUtil.getCallingConvention;
import static jnr.ffi.util.Annotations.sortedAnnotationCollection;

/**
 *
 */
class ReflectionLibraryLoader extends LibraryLoader {

    @Override
    <T> T loadLibrary(NativeLibrary library, Class<T> interfaceClass, MethodHandles.Lookup lookup, Map<LibraryOption, ?> libraryOptions, boolean failImmediately) {
        Map<Method, Invoker> invokers = new LazyLoader<T>(library, interfaceClass, libraryOptions);

        if (failImmediately) {
            SignatureTypeMapper typeMapper = getSignatureTypeMapper(libraryOptions);
            CallingConvention libraryCallingConvention = getCallingConvention(interfaceClass, libraryOptions);
            InterfaceScanner scanner = new InterfaceScanner(interfaceClass, typeMapper, libraryCallingConvention);

            // force all functions to bind
            for (NativeFunction function : scanner.functions()) {
                if (function.getMethod().isDefault()) continue;
                invokers.get(function.getMethod());
            }

            // force all variables to bind
            for (NativeVariable variable : scanner.variables()) {
                invokers.get(variable.getMethod());
            }
        }

        return interfaceClass.cast(Proxy.newProxyInstance(interfaceClass.getClassLoader(),
                new Class[]{ interfaceClass, LoadedLibrary.class }, new NativeInvocationHandler(invokers, lookup)));
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

    private static final class GetRuntimeInvoker implements Invoker {
        private final jnr.ffi.Runtime runtime;

        private GetRuntimeInvoker(Runtime runtime) {
            this.runtime = runtime;
        }

        @Override
        public Object invoke(Object self, Object[] parameters) {
            return runtime;
        }
    }

    private static final class LazyLoader<T> extends AbstractMap<Method, Invoker> {
        private final DefaultInvokerFactory invokerFactory;
        private final jnr.ffi.Runtime runtime = NativeRuntime.getInstance();
        private final AsmClassLoader classLoader = new AsmClassLoader();
        private final SignatureTypeMapper typeMapper;
        private final FunctionMapper functionMapper;
        private final jnr.ffi.CallingConvention libraryCallingConvention;

        private final boolean libraryIsSynchronized;

        private final NativeLibrary library;
        @SuppressWarnings("unused")
        private final Class<T> interfaceClass;
        @SuppressWarnings("unused")
        private final Map<LibraryOption, ?> libraryOptions;

        private LazyLoader(NativeLibrary library, Class<T> interfaceClass, Map<LibraryOption, ?> libraryOptions) {
            this.library = library;
            this.interfaceClass = interfaceClass;
            this.libraryOptions = libraryOptions;

            this.functionMapper = libraryOptions.containsKey(LibraryOption.FunctionMapper)
                    ? (FunctionMapper) libraryOptions.get(LibraryOption.FunctionMapper) : IdentityFunctionMapper.getInstance();

            SignatureTypeMapper typeMapper = getSignatureTypeMapper(libraryOptions);
            CompositeTypeMapper closureTypeMapper = newClosureTypeMapper(classLoader, typeMapper);

            this.typeMapper = newCompositeTypeMapper(runtime, classLoader, typeMapper, closureTypeMapper);

            libraryCallingConvention = getCallingConvention(interfaceClass, libraryOptions);
            libraryIsSynchronized = interfaceClass.isAnnotationPresent(Synchronized.class);
            invokerFactory = new DefaultInvokerFactory(runtime, library, this.typeMapper, functionMapper, libraryCallingConvention, libraryOptions, libraryIsSynchronized);
        }

        @Override
        public Set<Entry<Method, Invoker>> entrySet() {
            throw new UnsupportedOperationException("not implemented");
        }

        @Override
        public synchronized Invoker get(Object key) {

            if (!(key instanceof Method)) {
                throw new IllegalArgumentException("key not instance of Method");
            }

            Method method = (Method) key;
            if (Variable.class.isAssignableFrom(method.getReturnType())) {
                return getVariableAccessor(method);

            } else if (method.getName().equals("getRuntime") && method.getReturnType().isAssignableFrom(NativeRuntime.class)) {
                return new GetRuntimeInvoker(runtime);
            } else {
                return invokerFactory.createInvoker(method);
            }
        }

        private Invoker getVariableAccessor(Method method) {
            Collection<Annotation> annotations = sortedAnnotationCollection(method.getAnnotations());

            String functionName = functionMapper.mapFunctionName(method.getName(), new NativeFunctionMapperContext(library, annotations));
            long symbolAddress = library.getSymbolAddress(functionName);
            if (symbolAddress == 0L) {
                return new FunctionNotFoundInvoker(method, functionName);
            }
            Variable variable = ReflectionVariableAccessorGenerator.createVariableAccessor(runtime, method, symbolAddress,
                    typeMapper, annotations);
            return new VariableAcccessorInvoker(variable);
        }


        private static final class VariableAcccessorInvoker implements Invoker {
            private final Variable variable;

            private VariableAcccessorInvoker(Variable variable) {
                this.variable = variable;
            }

            @Override
            public Object invoke(Object self, Object[] parameters) {
                return variable;
            }
        }
    }
}
