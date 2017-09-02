package jnr.ffi.struct;

import jnr.ffi.NativeType;
import jnr.ffi.Type;
import jnr.ffi.TypeAlias;

/**
 * Structure field
 */
public abstract class Field {

    private Struct.Info info;
    private Offset offset;

    protected Field(Struct struct, int size, int align, Offset offset) {
        registerInStructure(struct, size, align, offset);
    }

    protected Field(Struct struct, int size, int align) {
        this(struct, size, align, null);
    }

    protected Field(Struct struct, NativeType nativeType) {
        this(struct, nativeType, null);
    }

    protected Field(Struct struct, NativeType nativeType, Offset offset) {
        final Type type = struct.getRuntime().findType(nativeType);
        registerInStructure(struct, type, offset);
    }

    protected Field(Struct struct, TypeAlias typeAlias) {
        this(struct, typeAlias, null);
    }

    protected Field(Struct struct, TypeAlias typeAlias, Offset offset) {
        Type type = struct.getRuntime().findType(typeAlias);
        registerInStructure(struct, type, offset);
    }

    private void registerInStructure(Struct struct, Type type, Offset offset) {
        registerInStructure(struct, type.size() * 8, type.alignment() * 8, offset);
    }

    public final jnr.ffi.Pointer getMemory() {
        return info.getMemory();
    }

    protected long offset() {
        return offset.intValue() + info.getOffset();
    }

    private void registerInStructure(Struct struct, int size, int alignment, Offset offset) {
        info = struct.__info;
        if (offset == null) {
            int offsetAfterAllocation = struct.__info.addField(size, alignment);
            this.offset = new Offset(offsetAfterAllocation);
        } else {
            struct.__info.addField(size, alignment, offset);
        }

    }
}
