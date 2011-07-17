/*
 * Copyright (C) 2011 Wayne Meissner
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

import com.kenai.jffi.CallingConvention;
import jnr.ffi.LibraryOption;
import jnr.ffi.annotations.StdCall;

import java.lang.reflect.Method;
import java.util.Map;

/**
 *
 */
class ClosureUtil {
    private ClosureUtil() {
    }

    static com.kenai.jffi.Type getNativeResultType(Method m) {
        return InvokerUtil.getNativeReturnType(m);
    }

    static com.kenai.jffi.Type getNativeParameterType(Method m, int idx) {
        return InvokerUtil.getNativeParameterType(m.getParameterTypes()[idx], m.getParameterAnnotations()[idx]);
    }

    public static final com.kenai.jffi.CallingConvention getNativeCallingConvention(Method m) {
        if (m.getAnnotation(StdCall.class) != null || m.getDeclaringClass().getAnnotation(StdCall.class) != null) {
            return CallingConvention.STDCALL;
        }

        return CallingConvention.DEFAULT;
    }

}
