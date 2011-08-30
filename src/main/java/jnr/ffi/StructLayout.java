package jnr.ffi;

import java.lang.reflect.Constructor;

import static jnr.ffi.Struct.Offset;

/**
 *
 */
public class StructLayout implements Type {
    private final Runtime runtime;
    private final boolean isUnion = false;
    private boolean resetIndex = false;
    StructLayout enclosing = null;
    int offset = 0;
    int size = 0;
    int alignment = 1;
    int paddedSize = 0;

    /**
     * Creates a new <tt>StructLayout</tt>.
     */
    protected StructLayout(Runtime runtime) {
        this.runtime = runtime;
    }

    public final Runtime getRuntime() {
        return this.runtime;
    }

    public final int size() {
        return paddedSize;
    }

    public final int alignment() {
        return alignment;
    }

    /**
     * Returns a human readable {@link java.lang.String} representation of the structure.
     *
     * @return a <tt>String representation of this structure.
     */
    @Override
    public java.lang.String toString() {
        StringBuilder sb = new StringBuilder();
        java.lang.reflect.Field[] fields = getClass().getDeclaredFields();
        sb.append(getClass().getSimpleName()).append(" { \n");
        final java.lang.String fieldPrefix = "    ";
        for (java.lang.reflect.Field field : fields) {
            try {
                sb.append(fieldPrefix).append('\n');
            } catch (Throwable ex) {
                throw new RuntimeException(ex);
            }
        }
        sb.append("}\n");

        return sb.toString();
    }

    private static int align(int offset, int alignment) {
        return (offset + alignment - 1) & ~(alignment - 1);
    }

    protected final int addField(int fieldSize, int fieldAlign, int offset) {
        this.size = Math.max(this.size, offset + fieldSize);
        this.alignment = Math.max(this.alignment, fieldAlign);
        this.paddedSize = align(this.size, this.alignment);

        return offset;
    }

    protected final int addField(int fieldSize, int fieldAlign, Offset offset) {
        return addField(fieldSize, fieldAlign, offset.intValue());
    }

    protected final int addField(int fieldSize, int fieldAlign) {
        return addField(fieldSize, fieldAlign, resetIndex ? 0 : align(this.size, fieldAlign));
    }

    protected final int addField(Type t) {
        return addField(t.size(), t.alignment());
    }

    protected final int addField(Type t, int offset) {
        return addField(t.size(), t.alignment(), offset);
    }

    protected final int addField(Type t, Offset offset) {
        return addField(t, offset.intValue());
    }

    /**
     * Interface all Struct members must implement.
     */
    protected interface Field {
        /**
         * Gets the <tt>StructLayout</tt> this <tt>Field</tt> belongs to.
         *
         * @return a <tt>Struct</tt>.
         */
        StructLayout enclosing();

        /**
         * Gets the offset within the structure for this field.
         */
        long offset();
    }

    /**
     * Starts an array construction session
     */
    protected final void arrayBegin() {
        resetIndex = false;
    }

    /**
     * Ends an array construction session
     */
    protected final void arrayEnd() {
        resetIndex = isUnion;
    }

    /**
     * Creates an array of <tt>Member</tt> instances.
     *
     * @param <T> The type of the <tt>Member</tt> subclass to create.
     * @param array the array to store the instances in
     * @return the array that was passed in
     */
    @SuppressWarnings("unchecked")
    protected <T extends Field> T[] array(T[] array) {
        arrayBegin();
        try {
            Class<?> arrayClass = array.getClass().getComponentType();
            Constructor<?> ctor = arrayClass.getDeclaredConstructor(new Class[] { arrayClass.getEnclosingClass() });
            Object[] parameters = { StructLayout.this  };
            for (int i = 0; i < array.length; ++i) {
                array[i] = (T) ctor.newInstance(parameters);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        arrayEnd();
        return array;
    }

    protected final <T extends StructLayout> T inner(T structLayout) {
        structLayout.offset = align(this.size, structLayout.alignment);
        structLayout.enclosing = this;
        this.size = structLayout.offset + structLayout.size;
        this.paddedSize = align(this.size, structLayout.alignment());

        return structLayout;
    }

/**
     * Base implementation of Member
     */
    protected abstract class AbstractField implements Field {
        private final int offset;

        protected AbstractField(int size, int align, Offset offset) {
            this.offset = addField(size, align, offset.intValue());
        }
        protected AbstractField(int size, int align) {
            this.offset = addField(size, align);
        }

        protected AbstractField(NativeType type) {
            this.offset = addField(getRuntime().findType(type));
        }

        protected AbstractField(NativeType type, Offset offset) {
            this.offset = addField(getRuntime().findType(type), offset);
        }

        /**
         * Gets the <tt>Struct</tt> this <tt>Member</tt> is a member of.
         *
         * @return a <tt>Struct</tt>.
         */
        public final StructLayout enclosing() {
            return StructLayout.this;
        }

        /**
         * Gets the offset within the structure for this field.
         */
        public final long offset() {
            return offset + StructLayout.this.offset;
        }
    }



    /**
     * Base class for Boolean fields
     */
    protected abstract class AbstractBoolean extends AbstractField {
        protected AbstractBoolean(NativeType type) {
            super(type);
        }

        /**
         * Gets the value for this field.
         *
         * @return a boolean.
         */
        public abstract boolean get(jnr.ffi.Pointer ptr);

        /**
         * Sets the field to a new value.
         *
         * @param value The new value.
         */
        public abstract void set(jnr.ffi.Pointer ptr, boolean value);

        /**
         * Returns a string representation of this <code>Boolean</code>.
         *
         * @return a string representation of this <code>Boolean</code>.
         */
        public java.lang.String toString(jnr.ffi.Pointer ptr) {
            return java.lang.Boolean.toString(get(ptr));
        }
    }


    /**
     * A normal C boolean - 1 byte in size
     */
    protected final class Boolean extends AbstractBoolean {
        protected Boolean() {
            super(NativeType.SCHAR);
        }

        public final boolean get(jnr.ffi.Pointer ptr) {
            return (ptr.getByte(offset()) & 0x1) != 0;
        }

        public final void set(jnr.ffi.Pointer ptr, boolean value) {
            ptr.putByte(offset(), (byte) (value ? 1 : 0));
        }
    }

    /**
     * A Windows BOOL - 4 bytes
     */
    protected final class WBOOL extends AbstractBoolean {
        protected WBOOL() {
            super(NativeType.SINT);
        }

        public final boolean get(jnr.ffi.Pointer ptr) {
            return (ptr.getInt(offset()) & 0x1) != 0;
        }

        public final void set(jnr.ffi.Pointer ptr, boolean value) {
            ptr.putInt(offset(), value ? 1 : 0);
        }
    }

    /**
     * Base class for all Number structure fields.
     */
    protected abstract class NumberField extends AbstractField {
        protected NumberField(NativeType type) {
            super(type);
        }

        protected NumberField(NativeType type, Offset offset) {
            super(type, offset);
        }

        /**
         * Sets the field to a new value.
         *
         * @param value The new value.
         */
        public abstract void set(jnr.ffi.Pointer ptr, java.lang.Number value);

        /**
         * Returns an {@code float} representation of this <tt>Number</tt>.
         *
         * @return an {@code float} value for this <tt>Number</tt>.
         */
        public double doubleValue(jnr.ffi.Pointer ptr) {
            return (double) longValue(ptr);
        }

        /**
         * Returns an {@code float} representation of this <tt>Number</tt>.
         *
         * @return an {@code float} value for this <tt>Number</tt>.
         */
        public float floatValue(jnr.ffi.Pointer ptr) {
            return (float) intValue(ptr);
        }

        /**
         * Returns a {@code byte} representation of this <tt>Number</tt>.
         *
         * @return a {@code byte} value for this <tt>Number</tt>.
         */
        public byte byteValue(jnr.ffi.Pointer ptr) {
            return (byte) intValue(ptr);
        }

        /**
         * Returns a {@code short} representation of this <tt>Number</tt>.
         *
         * @return a {@code short} value for this <tt>Number</tt>.
         */
        public short shortValue(jnr.ffi.Pointer ptr) {
            return (short) intValue(ptr);
        }

        /**
         * Returns a {@code int} representation of this <tt>Number</tt>.
         *
         * @return a {@code int} value for this <tt>Number</tt>.
         */
        public abstract int intValue(jnr.ffi.Pointer ptr);

        /**
         * Returns a {@code long} representation of this <tt>Number</tt>.
         *
         * @return a {@code long} value for this <tt>Number</tt>.
         */
        public long longValue(jnr.ffi.Pointer ptr) {
            return intValue(ptr);
        }

        /**
         * Returns a string representation of this <code>Number</code>.
         *
         * @return a string representation of this <code>Number</code>.
         */
        public java.lang.String toString(jnr.ffi.Pointer ptr) {
            return java.lang.Integer.toString(intValue(ptr), 10);
        }
    }


    /**
     * An 8 bit signed integer
     */
    public class Signed8 extends NumberField {
        /**
         * Creates a new 8 bit integer field.
         */
        public Signed8() {
            super(NativeType.SCHAR);
        }

        /**
         * Creates a new 8 bit integer field at a specific offset
         *
         * @param offset The offset within the memory area
         */
        public Signed8(Offset offset) {
            super(NativeType.SCHAR, offset);
        }

        /**
         * Gets the value for this field.
         *
         * @return a byte.
         */
        public final byte get(jnr.ffi.Pointer ptr) {
            return ptr.getByte(offset());
        }

        /**
         * Sets the value for this field.
         *
         * @param ptr The memory to set the value in.
         * @param value the 8 bit value to set.
         */
        public final void set(jnr.ffi.Pointer ptr, byte value) {
            ptr.putByte(offset(), value);
        }

        public void set(jnr.ffi.Pointer ptr, java.lang.Number value) {
            ptr.putByte(offset(), value.byteValue());
        }

        /**
         * Returns a java byte representation of this field.
         *
         * @return a java byte value for this field.
         */
        @Override
        public final byte byteValue(jnr.ffi.Pointer ptr) {
            return get(ptr);
        }

        /**
         * Returns a java short representation of this field.
         *
         * @return a java short value for this field.
         */
        @Override
        public final short shortValue(jnr.ffi.Pointer ptr) {
            return get(ptr);
        }

        /**
         * Returns a java int representation of this field.
         *
         * @return a java int value for this field.
         */
        @Override
        public final int intValue(jnr.ffi.Pointer ptr) {
            return get(ptr);
        }
    }

    /**
     * An 8 bit unsigned integer
     */
    public class Unsigned8 extends NumberField {
        /**
         * Creates a new 8 bit unsigned integer field.
         */
        public Unsigned8() {
            super(NativeType.UCHAR);
        }

        /**
         * Creates a new 8 bit unsigned integer field at a specific offset
         *
         * @param offset The offset within the memory area for this field.
         */
        public Unsigned8(Offset offset) {
            super(NativeType.UCHAR, offset);
        }

        /**
         * Gets the value for this field.
         *
         * @return a byte.
         */
        public final short get(jnr.ffi.Pointer ptr) {
            short value = ptr.getByte(offset());
            return value < 0 ? (short) ((value & 0x7F) + 0x80) : value;
        }

        /**
         * Sets the value for this field.
         *
         * @param value the 8 bit value to set.
         */
        public final void set(jnr.ffi.Pointer ptr, short value) {
            ptr.putByte(offset(), (byte) value);
        }

        public void set(jnr.ffi.Pointer ptr, java.lang.Number value) {
            ptr.putByte(offset(), value.byteValue());
        }

        /**
         * Returns a java short representation of this field.
         *
         * @return a java short value for this field.
         */
        @Override
        public final short shortValue(jnr.ffi.Pointer ptr) {
            return get(ptr);
        }

        /**
         * Returns a java int representation of this field.
         *
         * @return a java int value for this field.
         */
        @Override
        public final int intValue(jnr.ffi.Pointer ptr) {
            return get(ptr);
        }
    }

    /**
     * A 16 bit signed integer field.
     */
    public class Signed16 extends NumberField {
        /**
         * Creates a new 16 bit integer field.
         */
        public Signed16() {
            super(NativeType.SSHORT);
        }

        /**
         * Creates a new 16 bit signed integer field at a specific offset
         *
         * @param offset The offset within the memory area for this field.
         */
        public Signed16(Offset offset) {
            super(NativeType.SSHORT, offset);
        }

        /**
         * Gets the value for this field.
         *
         * @return a short.
         */
        public final short get(jnr.ffi.Pointer ptr) {
            return ptr.getShort(offset());
        }

        /**
         * Sets the value for this field.
         *
         * @param value the 16 bit value to set.
         */
        public final void set(jnr.ffi.Pointer ptr, short value) {
            ptr.putShort(offset(), value);
        }

        public void set(jnr.ffi.Pointer ptr, java.lang.Number value) {
            ptr.putShort(offset(), value.shortValue());
        }

        /**
         * Returns a java short representation of this field.
         *
         * @return a java short value for this field.
         */
        @Override
        public final short shortValue(jnr.ffi.Pointer ptr) {
            return get(ptr);
        }

        /**
         * Returns a java int representation of this field.
         *
         * @return a java int value for this field.
         */
        @Override
        public final int intValue(jnr.ffi.Pointer ptr) {
            return get(ptr);
        }
    }

    /**
     * A 16 bit signed integer field.
     */
    public class Unsigned16 extends NumberField {
        /**
         * Creates a new 16 bit integer field.
         */
        public Unsigned16() {
            super(NativeType.USHORT);
        }

        /**
         * Creates a new 16 bit unsigned integer field at a specific offset
         *
         * @param offset The offset within the memory area for this field.
         */
        public Unsigned16(Offset offset) {
            super(NativeType.USHORT, offset);
        }

        /**
         * Gets the value for this field.
         *
         * @return a short.
         */
        public final int get(jnr.ffi.Pointer ptr) {
            int value = ptr.getShort(offset());
            return value < 0 ? (int)((value & 0x7FFF) + 0x8000) : value;
        }

        /**
         * Sets the value for this field.
         *
         * @param value the 16 bit unsigned value to set.
         */
        public final void set(jnr.ffi.Pointer ptr, int value) {
            ptr.putShort(offset(), (short) value);
        }

        public void set(jnr.ffi.Pointer ptr, Number value) {
            ptr.putShort(offset(), value.shortValue());
        }

        /**
         * Returns a java int representation of this field.
         *
         * @return a java int value for this field.
         */
        @Override
        public final int intValue(jnr.ffi.Pointer ptr) {
            return get(ptr);
        }
    }

    /**
     * A 32 bit signed integer field.
     */
    public class Signed32 extends NumberField {
        /**
         * Creates a new 32 bit integer field.
         */
        public Signed32() {
            super(NativeType.SINT);
        }

        /**
         * Creates a new 32 bit signed integer field at a specific offset
         *
         * @param offset The offset within the memory area for this field.
         */
        public Signed32(Offset offset) {
            super(NativeType.SINT, offset);
        }

        /**
         * Gets the value for this field.
         *
         * @return a int.
         */
        public final int get(jnr.ffi.Pointer ptr) {
            return ptr.getInt(offset());
        }

        /**
         * Sets the value for this field.
         *
         * @param value the 32 bit value to set.
         */
        public final void set(jnr.ffi.Pointer ptr, int value) {
            ptr.putInt(offset(), value);
        }

        public void set(jnr.ffi.Pointer ptr, java.lang.Number value) {
            ptr.putInt(offset(), value.intValue());
        }

        /**
         * Returns a java int representation of this field.
         *
         * @return a java int value for this field.
         */
        @Override
        public final int intValue(jnr.ffi.Pointer ptr) {
            return get(ptr);
        }
    }

    /**
     * A 32 bit signed integer field.
     */
    public class Unsigned32 extends NumberField {
        /**
         * Creates a new 32 bit integer field.
         */
        public Unsigned32() {
            super(NativeType.UINT);
        }

        /**
         * Creates a new 32 bit unsigned integer field at a specific offset
         *
         * @param offset The offset within the memory area for this field.
         */
        public Unsigned32(Offset offset) {
            super(NativeType.SINT, offset);
        }

        /**
         * Gets the value for this field.
         *
         * @return a long.
         */
        public final long get(jnr.ffi.Pointer ptr) {
            long value = ptr.getInt(offset());
            return value < 0 ? (long)((value & 0x7FFFFFFFL) + 0x80000000L) : value;
        }

        /**
         * Sets the value for this field.
         *
         * @param value the 32 bit unsigned value to set.
         */
        public final void set(jnr.ffi.Pointer ptr, long value) {
            ptr.putInt(offset(), (int) value);
        }

        public void set(jnr.ffi.Pointer ptr, java.lang.Number value) {
            ptr.putInt(offset(), value.intValue());
        }

        /**
         * Returns a java int representation of this field.
         *
         * @return a java int value for this field.
         */
        @Override
        public final int intValue(jnr.ffi.Pointer ptr) {
            return (int) get(ptr);
        }

        /**
         * Returns a java long representation of this field.
         *
         * @return a java long value for this field.
         */
        @Override
        public final long longValue(jnr.ffi.Pointer ptr) {
            return get(ptr);
        }
    }

    /**
     * A 64 bit signed integer field.
     */
    public class Signed64 extends NumberField {
        /**
         * Creates a new 64 bit integer field.
         */
        public Signed64() {
            super(NativeType.SLONGLONG);
        }

        /**
         * Creates a new 64 bit signed integer field at a specific offset
         *
         * @param offset The offset within the memory area for this field.
         */
        public Signed64(Offset offset) {
            super(NativeType.SLONGLONG, offset);
        }

        /**
         * Gets the value for this field.
         *
         * @return a long.
         */
        public final long get(jnr.ffi.Pointer ptr) {
            return ptr.getLong(offset());
        }

        /**
         * Sets the value for this field.
         *
         * @param value the 64 bit value to set.
         */
        public final void set(jnr.ffi.Pointer ptr, long value) {
            ptr.putLong(offset(), value);
        }

        public void set(jnr.ffi.Pointer ptr, java.lang.Number value) {
            ptr.putLong(offset(), value.longValue());
        }

        /**
         * Returns a java int representation of this field.
         *
         * @return a java int value for this field.
         */
        @Override
        public final int intValue(jnr.ffi.Pointer ptr) {
            return (int) get(ptr);
        }

        /**
         * Returns a java long representation of this field.
         *
         * @return a java long value for this field.
         */
        @Override
        public final long longValue(jnr.ffi.Pointer ptr) {
            return get(ptr);
        }

        /**
         * Returns a string representation of this field.
         *
         * @return a string representation of this field.
         */
        @Override
        public final java.lang.String toString(jnr.ffi.Pointer ptr) {
            return java.lang.Long.toString(get(ptr));
        }
    }

    /**
     * A 64 bit unsigned integer field.
     */
    public class Unsigned64 extends NumberField {
        /**
         * Creates a new 64 bit integer field.
         */
        public Unsigned64() {
            super(NativeType.ULONGLONG);
        }

        /**
         * Creates a new 64 bit unsigned integer field at a specific offset
         *
         * @param offset The offset within the memory area for this field.
         */
        public Unsigned64(Offset offset) {
            super(NativeType.ULONGLONG, offset);
        }

        /**
         * Gets the value for this field.
         *
         * @return a long.
         */
        public final long get(jnr.ffi.Pointer ptr) {
            return ptr.getLong(offset());
        }

        /**
         * Sets the value for this field.
         *
         * @param value the 64 bit value to set.
         */
        public final void set(jnr.ffi.Pointer ptr, long value) {
            ptr.putLong(offset(), value);
        }

        public void set(jnr.ffi.Pointer ptr, java.lang.Number value) {
            ptr.putLong(offset(), value.longValue());
        }

        /**
         * Returns a java int representation of this field.
         *
         * @return a java int value for this field.
         */
        @Override
        public final int intValue(jnr.ffi.Pointer ptr) {
            return (int) get(ptr);
        }

        /**
         * Returns a java long representation of this field.
         *
         * @return a java long value for this field.
         */
        @Override
        public final long longValue(jnr.ffi.Pointer ptr) {
            return get(ptr);
        }

        /**
         * Returns a string representation of this field.
         *
         * @return a string representation of this field.
         */
        @Override
        public final java.lang.String toString(jnr.ffi.Pointer ptr) {
            return java.lang.Long.toString(get(ptr));
        }
    }

    /**
     * A native long integer field.
     */
    public class SignedLong extends NumberField {
        /**
         * Creates a new native long field.
         */
        public SignedLong() {
            super(NativeType.SLONG);
        }

        /**
         * Creates a new signed native long field at a specific offset
         *
         * @param offset The offset within the memory area for this field.
         */
        public SignedLong(Offset offset) {
            super(NativeType.SLONG, offset);
        }

        /**
         * Gets the value for this field.
         *
         * @return a long.
         */
        public final long get(jnr.ffi.Pointer ptr) {
            return ptr.getNativeLong(offset());
        }

        /**
         * Sets the value for this field.
         *
         * @param value the 32/64 bit value to set.
         */
        public final void set(jnr.ffi.Pointer ptr, long value) {
            ptr.putNativeLong(offset(), value);
        }

        public void set(jnr.ffi.Pointer ptr, java.lang.Number value) {
            ptr.putNativeLong(offset(), value.longValue());
        }

        /**
         * Returns a java int representation of this field.
         *
         * @return a java int value for this field.
         */
        @Override
        public final int intValue(jnr.ffi.Pointer ptr) {
            return (int) get(ptr);
        }

        /**
         * Returns a java long representation of this field.
         *
         * @return a java long value for this field.
         */
        @Override
        public final long longValue(jnr.ffi.Pointer ptr) {
            return get(ptr);
        }

        /**
         * Returns a string representation of this field.
         *
         * @return a string representation of this field.
         */
        @Override
        public final java.lang.String toString(jnr.ffi.Pointer ptr) {
            return java.lang.Long.toString(get(ptr));
        }
    }

    /**
     * A native long integer field.
     */
    public class UnsignedLong extends NumberField {

        /**
         * Creates a new native long field.
         */
        public UnsignedLong() {
            super(NativeType.ULONG);
        }

        /**
         * Creates a new unsigned native long field at a specific offset
         *
         * @param offset The offset within the memory area for this field.
         */
        public UnsignedLong(Offset offset) {
            super(NativeType.ULONG, offset);
        }

        /**
         * Gets the value for this field.
         *
         * @return a int.
         */
        public final long get(jnr.ffi.Pointer ptr) {
            long value = ptr.getNativeLong(offset());
            final long mask = getRuntime().findType(NativeType.SLONG).size() == 32 ? 0xffffffffL : 0xffffffffffffffffL;
            return value < 0
                    ? (long) ((value & mask) + mask + 1)
                    : value;
        }

        /**
         * Sets the value for this field.
         *
         * @param value the 32/64 bit value to set.
         */
        public final void set(jnr.ffi.Pointer ptr, long value) {
            ptr.putNativeLong(offset(), value);
        }

        public void set(jnr.ffi.Pointer ptr, java.lang.Number value) {
            ptr.putNativeLong(offset(), value.longValue());
        }

        /**
         * Returns a java int representation of this field.
         *
         * @return a java int value for this field.
         */
        @Override
        public final int intValue(jnr.ffi.Pointer ptr) {
            return (int) get(ptr);
        }

        /**
         * Returns a java long representation of this field.
         *
         * @return a java long value for this field.
         */
        @Override
        public final long longValue(jnr.ffi.Pointer ptr) {
            return get(ptr);
        }

        /**
         * Returns a string representation of this field.
         *
         * @return a string representation of this field.
         */
        @Override
        public final java.lang.String toString(jnr.ffi.Pointer ptr) {
            return java.lang.Long.toString(get(ptr));
        }
    }

    public class Float extends NumberField {
        public Float() {
            super(NativeType.FLOAT);
        }
        /**
         * Creates a new float field at a specific offset
         *
         * @param offset The offset within the memory area for this field.
         */
        public Float(Offset offset) {
            super(NativeType.FLOAT, offset);
        }

        public final float get(jnr.ffi.Pointer ptr) {
            return ptr.getFloat(offset());
        }
        public final void set(jnr.ffi.Pointer ptr, float value) {
            ptr.putFloat(offset(), value);
        }
        public void set(jnr.ffi.Pointer ptr, java.lang.Number value) {
            ptr.putFloat(offset(), value.floatValue());
        }

        @Override
        public final int intValue(jnr.ffi.Pointer ptr) {
            return (int) get(ptr);
        }

        @Override
        public final double doubleValue(jnr.ffi.Pointer ptr) {
            return get(ptr);
        }

        @Override
        public final float floatValue(jnr.ffi.Pointer ptr) {
            return get(ptr);
        }

        @Override
        public final long longValue(jnr.ffi.Pointer ptr) {
            return (long) get(ptr);
        }
        @Override
        public final java.lang.String toString(jnr.ffi.Pointer ptr) {
            return java.lang.String.valueOf(get(ptr));
        }
    }

    public final class Double extends NumberField {
        public Double() {
            super(NativeType.DOUBLE);
        }
        public Double(Offset offset) {
            super(NativeType.DOUBLE, offset);
        }
        public final double get(jnr.ffi.Pointer ptr) {
            return ptr.getDouble(offset());
        }
        public final void set(jnr.ffi.Pointer ptr, double value) {
            ptr.putDouble(offset(), value);
        }
        public void set(jnr.ffi.Pointer ptr, java.lang.Number value) {
            ptr.putDouble(offset(), value.doubleValue());
        }

        @Override
        public final int intValue(jnr.ffi.Pointer ptr) {
            return (int) get(ptr);
        }

        @Override
        public final long longValue(jnr.ffi.Pointer ptr) {
            return (long) get(ptr);
        }
        @Override
        public final float floatValue(jnr.ffi.Pointer ptr) {
            return (float) get(ptr);
        }

        @Override
        public final double doubleValue(jnr.ffi.Pointer ptr) {
            return get(ptr);
        }
        @Override
        public final java.lang.String toString(jnr.ffi.Pointer ptr) {
            return java.lang.String.valueOf(get(ptr));
        }
    }

    /**
     * Represents a native memory address.
     */
    public class Pointer extends NumberField {
        /**
         * Creates a new <tt>Address</tt> field.
         */
        public Pointer() {
            super(NativeType.ADDRESS);
        }

        public Pointer(Offset offset) {
            super(NativeType.ADDRESS, offset);
        }

        /**
         * Gets the {@link jnr.ffi.Pointer} value from the native memory.
         *
         * @return a {@link jnr.ffi.Pointer}.
         */
        public final jnr.ffi.Pointer get(jnr.ffi.Pointer ptr) {
            return ptr.getPointer(offset());
        }

        /**
         * Gets the size of a Pointer in bits
         *
         * @return the size of the Pointer
         */
        public final int size() {
            return getRuntime().findType(NativeType.ADDRESS).size() * 8;
        }

        /**
         * Sets a {@link jnr.ffi.Pointer} value in the native memory.
         */
        public final void set(jnr.ffi.Pointer ptr, jnr.ffi.Pointer value) {
            ptr.putPointer(offset(), value);
        }

        public void set(jnr.ffi.Pointer ptr, java.lang.Number value) {
            ptr.putAddress(offset(), value.longValue());
        }

        /**
         * Returns an integer representation of this <code>Pointer</code>.
         *
         * @return an integer value for this <code>Pointer</code>.
         */
        @Override
        public final int intValue(jnr.ffi.Pointer ptr) {
            return (int) ptr.getAddress(offset());
        }

        /**
         * Returns an {@code long} representation of this <code>Pointer</code>.
         *
         * @return an {@code long} value for this <code>Pointer</code>.
         */
        @Override
        public final long longValue(jnr.ffi.Pointer ptr) {
            return ptr.getAddress(offset());
        }

        /**
         * Returns a string representation of this <code>Pointer</code>.
         *
         * @return a string representation of this <code>Pointer</code>.
         */
        @Override
        public final java.lang.String toString(jnr.ffi.Pointer ptr) {
            return get(ptr).toString();
        }
    }

    /**
     * Specialized padding fields for structs.  Use this instead of arrays of other
     * members for more efficient struct construction.
     */
    protected final class Padding extends AbstractField {
        public Padding(Type type, int length) {
            super(type.size() * length, type.alignment());
        }
        public Padding(NativeType type, int length) {
            this(getRuntime().findType(type), length);
        }
    }

}
