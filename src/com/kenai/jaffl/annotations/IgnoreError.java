
package com.kenai.jaffl.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Tags a library method as not needing any error codes as returned
 * by errno on unix, or GetLastError on windows be saved.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface IgnoreError {

}
