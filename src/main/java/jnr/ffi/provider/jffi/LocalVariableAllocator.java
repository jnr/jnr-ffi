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

import jnr.ffi.provider.SigType;

/**
 *
 */
class LocalVariableAllocator {
    private int nextIndex;

    LocalVariableAllocator(SigType[] parameterTypes) {
        this.nextIndex = AsmUtil.calculateLocalVariableSpace(parameterTypes) + 1;
    }

    LocalVariableAllocator(Class... parameterTypes) {
        this.nextIndex = AsmUtil.calculateLocalVariableSpace(parameterTypes) + 1;
    }

    LocalVariableAllocator(int nextIndex) {
        this.nextIndex = nextIndex;
    }

    LocalVariable allocate(Class type) {
        LocalVariable var = new LocalVariable(type, nextIndex);
        this.nextIndex += AsmUtil.calculateLocalVariableSpace(type);
        return var;
    }

    int getSpaceUsed() {
        return nextIndex;
    }
}
