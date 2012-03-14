/*
 * Copyright (C) 2008-2010 Wayne Meissner
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

import jnr.ffi.NativeType;
import jnr.ffi.ObjectReferenceManager;
import jnr.ffi.Type;
import jnr.ffi.TypeAlias;
import jnr.ffi.provider.AbstractRuntime;
import jnr.ffi.provider.BadType;
import jnr.ffi.provider.DefaultObjectReferenceManager;

import java.nio.ByteOrder;
import java.util.EnumMap;
import java.util.EnumSet;

/**
 *
 */
public final class NativeRuntime extends AbstractRuntime {
    private final NativeMemoryManager mm = new NativeMemoryManager(this);
    private final NativeClosureManager closureManager = new NativeClosureManager(this);

    public static final NativeRuntime getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private static final class SingletonHolder {
        public static final NativeRuntime INSTANCE = new NativeRuntime();
    }

    private NativeRuntime() {
        super(ByteOrder.nativeOrder(), buildTypeMap());
    }

    private static EnumMap<NativeType, Type> buildTypeMap() {
        EnumMap<NativeType, Type> typeMap = new EnumMap<NativeType, Type>(NativeType.class);
        EnumSet<NativeType> nativeTypes = EnumSet.allOf(NativeType.class);

        for (NativeType t : nativeTypes) {
            typeMap.put(t, jafflType(t));
        }

        return typeMap;
    }

    @Override
    public Type findType(TypeAlias type) {
        throw new RuntimeException("not implemented yet");
    }

    public final NativeMemoryManager getMemoryManager() {
        return mm;
    }

    public NativeClosureManager getClosureManager() {
        return closureManager;
    }

    @Override
    public ObjectReferenceManager newObjectReferenceManager() {
        return new DefaultObjectReferenceManager(this);
    }

    @Override
    public int getLastError() {
        return com.kenai.jffi.LastError.getInstance().get();
    }


    @Override
    public void setLastError(int error) {
        com.kenai.jffi.LastError.getInstance().set(error);
    }

    private static final class TypeDelegate implements jnr.ffi.Type {
        private final com.kenai.jffi.Type type;
        private final NativeType nativeType;

        public TypeDelegate(com.kenai.jffi.Type type, NativeType nativeType) {
            this.type = type;
            this.nativeType = nativeType;
        }

        public int alignment() {
            return type.alignment();
        }

        public int size() {
            return type.size();
        }

        public NativeType getNativeType() {
            return nativeType;
        }

        public String toString() {
            return type.toString();
        }
    }
    
    private static final jnr.ffi.Type jafflType(NativeType type) {
        switch (type) {
            case VOID:
                return new TypeDelegate(com.kenai.jffi.Type.VOID, NativeType.VOID);
            case SCHAR:
                return new TypeDelegate(com.kenai.jffi.Type.SCHAR, NativeType.SCHAR);
            case UCHAR:
                return new TypeDelegate(com.kenai.jffi.Type.UCHAR, NativeType.UCHAR);
            case SSHORT:
                return new TypeDelegate(com.kenai.jffi.Type.SSHORT, NativeType.SSHORT);
            case USHORT:
                return new TypeDelegate(com.kenai.jffi.Type.USHORT, NativeType.USHORT);
            case SINT:
                return new TypeDelegate(com.kenai.jffi.Type.SINT, NativeType.SINT);
            case UINT:
                return new TypeDelegate(com.kenai.jffi.Type.UINT, NativeType.UINT);
            case SLONG:
                return new TypeDelegate(com.kenai.jffi.Type.SLONG, NativeType.SLONG);
            case ULONG:
                return new TypeDelegate(com.kenai.jffi.Type.ULONG, NativeType.ULONG);
            case SLONGLONG:
                return new TypeDelegate(com.kenai.jffi.Type.SINT64, NativeType.SLONGLONG);
            case ULONGLONG:
                return new TypeDelegate(com.kenai.jffi.Type.UINT64, NativeType.ULONGLONG);
            case FLOAT:
                return new TypeDelegate(com.kenai.jffi.Type.FLOAT, NativeType.FLOAT);
            case DOUBLE:
                return new TypeDelegate(com.kenai.jffi.Type.DOUBLE, NativeType.DOUBLE);
            case ADDRESS:
                return new TypeDelegate(com.kenai.jffi.Type.POINTER, NativeType.ADDRESS);
            default:
                return new BadType(type);
        }
    }
}
