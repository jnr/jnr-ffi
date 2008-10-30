
package com.kenai.jaffl.provider;

/**
 * Interface for any invocable function
 */
public interface Invoker {
    Object invoke(Object[] parameters);
}
