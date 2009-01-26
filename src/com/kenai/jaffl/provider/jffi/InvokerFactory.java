
package com.kenai.jaffl.provider.jffi;

import com.kenai.jaffl.LibraryOption;
import com.kenai.jaffl.provider.Invoker;
import java.lang.reflect.Method;
import java.util.Map;

public interface InvokerFactory {
    Invoker createInvoker(Method method, com.kenai.jaffl.provider.Library library, Map<LibraryOption, ?> options);
    boolean isMethodSupported(Method method);
}
