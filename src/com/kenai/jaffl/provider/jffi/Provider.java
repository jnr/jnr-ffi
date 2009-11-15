
package com.kenai.jaffl.provider.jffi;

import com.kenai.jaffl.LibraryOption;
import com.kenai.jaffl.provider.MemoryManager;
import com.kenai.jaffl.provider.NativeType;
import com.kenai.jffi.LastError;
import java.util.Map;


public class Provider extends com.kenai.jaffl.FFIProvider {
    private final MemoryManager memoryManager = new com.kenai.jaffl.provider.jffi.MemoryManager();
    @Override
    public MemoryManager getMemoryManager() {
        return memoryManager;
    }

    @Override
    public <T> T loadLibrary(String libraryName, Class<T> interfaceClass, Map<LibraryOption, ?> libraryOptions) {
        return loadLibrary(new Library(libraryName), interfaceClass, libraryOptions);
    }

    @Override
    public <T> T loadLibrary(Class<T> interfaceClass, Map<LibraryOption, ?> libraryOptions, String... libraryNames) {
        return loadLibrary(new Library(libraryNames), interfaceClass, libraryOptions);
    }

    private <T> T loadLibrary(Library library, Class<T> interfaceClass, Map<LibraryOption, ?> libraryOptions) {
        try {
            if (AsmLibraryLoader.getInstance().isInterfaceSupported(interfaceClass, libraryOptions)) {
                return AsmLibraryLoader.getInstance().loadLibrary(library, interfaceClass, libraryOptions);
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
        
        return ProxyLibraryLoader.getInstance().loadLibrary(library, interfaceClass, libraryOptions);
    }
    
    @Override
    public int getLastError() {
        return LastError.getInstance().get();
    }

    @Override
    public void setLastError(int error) {
        LastError.getInstance().set(error);
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

    private static final class BadType implements com.kenai.jaffl.Type {
        private final NativeType type;

        public BadType(NativeType type) {
            this.type = type;
        }

        public int alignment() {
            throw new RuntimeException("invalid type: " + type);
        }

        public int size() {
            throw new RuntimeException("invalid type: " + type);
        }
    }
    
    @Override
    public com.kenai.jaffl.Type getType(NativeType type) {
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
