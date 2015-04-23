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

import com.kenai.jffi.ObjectParameterType;

import java.nio.Buffer;
import java.util.EnumSet;

/**
 *
 */
final class HeapBufferParameterStrategy extends ParameterStrategy {

    public HeapBufferParameterStrategy(ObjectParameterType.ComponentType componentType) {
        super(HEAP, ObjectParameterType.create(ObjectParameterType.ARRAY, componentType));
    }

    @Override
    public long address(Object o) {
        return 0;
    }

    @Override
    public Object object(Object o) {
        return ((Buffer) o).array();
    }

    @Override
    public int offset(Object o) {
        Buffer buffer = (Buffer) o;
        return buffer.arrayOffset() + buffer.position();
    }

    @Override
    public int length(Object o) {
        return ((Buffer) o).remaining();
    }


    private static final HeapBufferParameterStrategy[] heapBufferStrategies;
    static {
        EnumSet<ObjectParameterType.ComponentType> componentTypes = EnumSet.allOf(ObjectParameterType.ComponentType.class);
        heapBufferStrategies = new HeapBufferParameterStrategy[componentTypes.size()];
        for (ObjectParameterType.ComponentType componentType : componentTypes) {
            heapBufferStrategies[componentType.ordinal()] = new HeapBufferParameterStrategy(componentType);
        }
    }

    static HeapBufferParameterStrategy get(ObjectParameterType.ComponentType componentType) {
        return heapBufferStrategies[componentType.ordinal()];
    }

}
