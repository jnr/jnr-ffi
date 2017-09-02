package jnr.ffi.struct;

import jnr.ffi.NativeType;
import jnr.ffi.Type;

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

    protected Field(Struct struct, NativeType type) {
        this(struct, type, null);
    }

    protected Field(Struct struct, NativeType type, Offset offset) {
        final Type t = struct.getRuntime().findType(type);
        registerInStructure(struct, t.size() * 8, t.alignment() * 8, offset);
    }

    protected jnr.ffi.Pointer getMemory() {
        return info.getMemory();
    }

    protected int offset() {
        return offset.intValue();
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
