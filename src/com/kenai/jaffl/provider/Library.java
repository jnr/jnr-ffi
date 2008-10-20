/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.kenai.jaffl.provider;

import com.kenai.jaffl.LibraryOption;
import java.lang.reflect.Method;
import java.util.Map;

/**
 *
 * @author wayne
 */
public interface Library {
    Invoker getInvoker(Method method, Map<LibraryOption, ?> options);
    Object libraryLock();
}
