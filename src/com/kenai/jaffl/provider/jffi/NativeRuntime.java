
package com.kenai.jaffl.provider.jffi;

import com.kenai.jaffl.NativeType;
import com.kenai.jaffl.Type;
import com.kenai.jaffl.provider.BadType;
import java.nio.ByteOrder;

/**
 *
 */
public final class NativeRuntime extends com.kenai.jaffl.Runtime {
    private final NativeMemoryManager mm = new NativeMemoryManager();

    public static final NativeRuntime getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private static final class SingletonHolder {
        public static final NativeRuntime INSTANCE = new NativeRuntime();
    }

    private NativeRuntime() {
        super(ByteOrder.nativeOrder(), buildTypeArray());
    }

    private static Type[] buildTypeArray() {
        NativeType[] nativeTypes = NativeType.values();

        Type[] t = new Type[nativeTypes.length];
        for (int i = 0; i < nativeTypes.length; ++i) {
            t[i] = jafflType(nativeTypes[i]);
        }

        return t;
    }

    public final NativeMemoryManager getMemoryManager() {
        return mm;
    }
    
    @Override
    public int getLastError() {
        return com.kenai.jffi.LastError.getInstance().get();
    }


    @Override
    public void setLastError(int error) {
        com.kenai.jffi.LastError.getInstance().set(error);
    }

    private static final class TypeDelegate implements com.kenai.jaffl.Type {
        private final com.kenai.jffi.Type type;

        public TypeDelegate(com.kenai.jffi.Type type) {
            this.type = type;
        }

        public int alignment() {
            return type.alignment();
        }

        public int size() {
            return type.size();
        }
    }
    
    private static final com.kenai.jaffl.Type jafflType(NativeType type) {
        switch (type) {
            case VOID:
                return new TypeDelegate(com.kenai.jffi.Type.VOID);
            case SCHAR:
                return new TypeDelegate(com.kenai.jffi.Type.SCHAR);
            case UCHAR:
                return new TypeDelegate(com.kenai.jffi.Type.UCHAR);
            case SSHORT:
                return new TypeDelegate(com.kenai.jffi.Type.SSHORT);
            case USHORT:
                return new TypeDelegate(com.kenai.jffi.Type.USHORT);
            case SINT:
                return new TypeDelegate(com.kenai.jffi.Type.SINT);
            case UINT:
                return new TypeDelegate(com.kenai.jffi.Type.UINT);
            case SLONG:
                return new TypeDelegate(com.kenai.jffi.Type.SLONG);
            case ULONG:
                return new TypeDelegate(com.kenai.jffi.Type.ULONG);
            case SLONGLONG:
                return new TypeDelegate(com.kenai.jffi.Type.SINT64);
            case ULONGLONG:
                return new TypeDelegate(com.kenai.jffi.Type.UINT64);
            case FLOAT:
                return new TypeDelegate(com.kenai.jffi.Type.FLOAT);
            case DOUBLE:
                return new TypeDelegate(com.kenai.jffi.Type.DOUBLE);
            case ADDRESS:
                return new TypeDelegate(com.kenai.jffi.Type.POINTER);
            default:
                return new BadType(type);
        }
    }
}
