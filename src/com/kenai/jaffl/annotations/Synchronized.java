
package com.kenai.jaffl.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Indicates that a library or a library method requires all calls to be
 * synchronized.
 * 
 * i.e. calls from multiple threads will synchronize on a monitor object,
 * then call the native method.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Synchronized {

}
