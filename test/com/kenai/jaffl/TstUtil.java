/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.kenai.jaffl;

import java.util.Collections;
import java.util.Map;

/**
 *
 * @author wayne
 */
public final class TstUtil {
    private TstUtil() {}
    public static final String getTestLibraryName() {
        return "test";
    }
    public static <T> T loadTestLib(Class<T> interfaceClass) {
        final Map<LibraryOption, ?> options = Collections.emptyMap();
        return Library.loadLibrary(getTestLibraryName(), interfaceClass, options);
    }
}
