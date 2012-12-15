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

import jnr.ffi.*;
import jnr.ffi.Runtime;
import jnr.ffi.mapper.DefaultTypeMapper;
import jnr.ffi.mapper.SignatureTypeMapperAdapter;
import jnr.ffi.provider.AbstractRuntime;
import jnr.ffi.provider.BadType;
import jnr.ffi.provider.DefaultObjectReferenceManager;

import java.lang.reflect.Field;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public final class NativeRuntime extends AbstractRuntime {
    private final NativeMemoryManager mm = new NativeMemoryManager(this);
    private final NativeClosureManager closureManager = new NativeClosureManager(this,
            new SignatureTypeMapperAdapter(new DefaultTypeMapper()),
            new AsmClassLoader(ClassLoader.getSystemClassLoader()));
    private final Type[] aliases;

    public static NativeRuntime getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private static final class SingletonHolder {
        public static final NativeRuntime INSTANCE = new NativeRuntime();
    }

    private NativeRuntime() {
        super(ByteOrder.nativeOrder(), buildTypeMap());
        NativeType[] nativeAliases = buildNativeTypeAliases();
        EnumSet<TypeAlias> typeAliasSet = EnumSet.allOf(TypeAlias.class);
        aliases = new Type[typeAliasSet.size()];

        for (TypeAlias alias : typeAliasSet) {
            if (nativeAliases.length > alias.ordinal() && nativeAliases[alias.ordinal()] != NativeType.VOID) {
                aliases[alias.ordinal()] = findType(nativeAliases[alias.ordinal()]);
            } else {
                aliases[alias.ordinal()] = new BadType(alias.name());
            }
        }
    }

    private static EnumMap<NativeType, Type> buildTypeMap() {
        EnumMap<NativeType, Type> typeMap = new EnumMap<NativeType, Type>(NativeType.class);
        EnumSet<NativeType> nativeTypes = EnumSet.allOf(NativeType.class);

        for (NativeType t : nativeTypes) {
            typeMap.put(t, jafflType(t));
        }

        return typeMap;
    }

    private static NativeType[] buildNativeTypeAliases() {
        Platform platform = Platform.getNativePlatform();
        Package pkg = NativeRuntime.class.getPackage();
        String cpu = platform.getCPU().toString();
        String os = platform.getOS().toString();
        EnumSet<TypeAlias> typeAliases = EnumSet.allOf(TypeAlias.class);
        NativeType[] aliases = {};
        Class cls;
        try {
            cls = Class.forName(pkg.getName() + ".platform." + cpu + "." + os + ".TypeAliases");
            Field aliasesField = cls.getField("ALIASES");
            Map aliasMap = Map.class.cast(aliasesField.get(cls));
            aliases = new NativeType[typeAliases.size()];
            for (TypeAlias t : typeAliases) {
                aliases[t.ordinal()] = (NativeType) aliasMap.get(t);
                if (aliases[t.ordinal()] == null) {
                    aliases[t.ordinal()] = NativeType.VOID;
                }
            }
        } catch (ClassNotFoundException cne) {
            Logger.getLogger(NativeRuntime.class.getName()).log(Level.SEVERE, "failed to load type aliases: " + cne);
        } catch (NoSuchFieldException nsfe) {
            Logger.getLogger(NativeRuntime.class.getName()).log(Level.SEVERE, "failed to load type aliases: " + nsfe);
        } catch (IllegalAccessException iae) {
            Logger.getLogger(NativeRuntime.class.getName()).log(Level.SEVERE, "failed to load type aliases: " + iae);
        }

        return aliases;
    }

    @Override
    public Type findType(TypeAlias type) {
        return aliases[type.ordinal()];
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

    @Override
    public boolean isCompatible(Runtime other) {
        return other instanceof NativeRuntime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NativeRuntime that = (NativeRuntime) o;

        return Arrays.equals(aliases, that.aliases) && closureManager.equals(that.closureManager) && mm.equals(that.mm);
    }

    @Override
    public int hashCode() {
        int result = mm.hashCode();
        result = 31 * result + closureManager.hashCode();
        result = 31 * result + Arrays.hashCode(aliases);
        return result;
    }

    private static final class TypeDelegate extends jnr.ffi.Type {
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
    
    private static jnr.ffi.Type jafflType(NativeType type) {
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
                return new BadType(type.toString());
        }
    }
}
