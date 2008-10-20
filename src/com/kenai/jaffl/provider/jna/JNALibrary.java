/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.kenai.jaffl.provider.jna;

import com.kenai.jaffl.LibraryOption;
import com.kenai.jaffl.provider.Invoker;
import java.lang.reflect.Method;
import java.util.Map;

/**
 *
 * @author wayne
 */
final class JNALibrary implements com.kenai.jaffl.provider.Library {
    private final String libraryName;
    JNALibrary(String libraryName) {
        this.libraryName = libraryName;
    }
    public Invoker getInvoker(Method method, Map<LibraryOption, ?> options) {
        return new DefaultInvoker(com.sun.jna.NativeLibrary.getInstance(libraryName), method);
    }

    public Object libraryLock() {
        return com.sun.jna.NativeLibrary.getInstance(libraryName);
    }
    private static class DefaultInvoker implements Invoker {
        private final com.sun.jna.Function function;
        private final Class returnType;
        DefaultInvoker(com.sun.jna.NativeLibrary library, Method method) {
            function = library.getFunction(method.getName());
            returnType = method.getReturnType();
        }
        public Object invoke(Object[] parameters) {
            return function.invoke(returnType, parameters);
        }
        
    }
}
