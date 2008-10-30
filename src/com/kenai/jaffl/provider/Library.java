
package com.kenai.jaffl.provider;

import com.kenai.jaffl.LibraryOption;
import java.lang.reflect.Method;
import java.util.Map;

/**
 *
 */
public interface Library {
    Invoker getInvoker(Method method, Map<LibraryOption, ?> options);
    Object libraryLock();
}
