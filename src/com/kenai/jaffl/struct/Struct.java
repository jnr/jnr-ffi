/*
 * Some of the design and code of this class is from the javolution project.
 *
 * Copyright (C) 2006 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */

package com.kenai.jaffl.struct;

import com.kenai.jaffl.ParameterFlags;
import com.kenai.jaffl.Runtime;
import com.kenai.jaffl.Type;
import com.kenai.jaffl.NativeType;
import com.kenai.jaffl.util.EnumMapper;
import java.lang.reflect.Constructor;
import java.nio.charset.Charset;

/**
 * Representation of C structures in java.
 * 
 * <b>Note:</b> This class is not threadsafe.
 */
public abstract class Struct /*implements Marshallable */{

    static final class Info {
        private final Runtime runtime;
        private com.kenai.jaffl.Pointer memory = null;
        Struct enclosing = null;
        int offset = 0; // offset within enclosing Struct
        
        int size = 0;
        int minAlign = 1;
        boolean isUnion = false;
        boolean resetIndex = false;

        public Info(Runtime runtime) {
            this.runtime = runtime;
        }

        public final com.kenai.jaffl.Pointer getMemory(int flags) {
            return enclosing != null ? enclosing.__info.getMemory(flags) : memory != null ? memory : (memory = allocateMemory(flags));
        }

        public final com.kenai.jaffl.Pointer getMemory() {
            return getMemory(ParameterFlags.TRANSIENT);
        }

        final boolean isDirect() {
            return (enclosing != null && enclosing.__info.isDirect()) || (memory != null && memory.isDirect());
        }

        final int size() {
            return size;
        }

        final int getMinimumAlignment() {
            return minAlign;
        }

        private final com.kenai.jaffl.Pointer allocateMemory(int flags) {
            if (ParameterFlags.isDirect(flags)) {
                return runtime.getMemoryManager().allocateDirect(size(), true);
            } else {
                return runtime.getMemoryManager().allocate(size());
            }
        }
        /*
        public final Marshaller.Session marshal(Marshaller marshaller, MarshalContext context) {
            final int flags = context.getFlags();
            if (io == null) {
                allocateMemory(flags);
            } else if (!ParameterFlags.isTransient(flags) && !io.isDirect()) {
                // Switching from heap memory to native memory
                final ByteBuffer old = buffer;
                allocateMemory(flags);
                if (ParameterFlags.isIn(flags)) {
                    io.put(0, old.array(), old.arrayOffset(), old.capacity());
                }
            }
            if (memory != null) {
                marshaller.add(memory);
            } else {
                marshaller.add(buffer, context);
            }
            return Marshaller.EMPTY_SESSION;
        }
        */
        public final void useMemory(com.kenai.jaffl.Pointer io) {
            this.memory = io;
        }
        
        protected final int addField(int sizeBits, int alignBits, Offset offset) {
            this.size = Math.max(this.size, offset.intValue() + (sizeBits >> 3));
            this.minAlign = Math.max(this.minAlign, alignBits >> 3);
            return offset.intValue();
        }
        
        protected final int addField(int sizeBits, int alignBits) {
            final int mask = (alignBits >> 3) - 1;
            int off = resetIndex ? 0 : this.size;
            if ((off & mask) != 0) {
                off = (off & ~mask) + (alignBits >> 3);
            }
            this.size = Math.max(this.size, off + (sizeBits >> 3));
            this.minAlign = Math.max(this.minAlign, alignBits >> 3);
            return off;
        }

        
    }
    final Info __info;

    /**
     * Creates a new <tt>Struct</tt>.
     */
    protected Struct() {
        this(Runtime.getDefault());
    }

    protected Struct(Runtime runtime) {
        this.__info = new Info(runtime);
    }

    /**
     * Creates a new <tt>Struct</tt>.
     * 
     * @param isUnion if this Struct is a Union
     */
    Struct(final boolean isUnion) {
        this(Runtime.getDefault(), isUnion);
    }

    /**
     * Creates a new <tt>Struct</tt>.
     *
     * @param isUnion if this Struct is a Union
     */
    Struct(Runtime runtime, final boolean isUnion) {
        this(runtime);
        __info.resetIndex = isUnion;
    }

    public final Runtime getRuntime() {
        return __info.runtime;
    }

    /**
     * Uses the specified memory address as the backing store for this structure.
     *
     * @param address the native memory area.
     */
    public void useMemory(com.kenai.jaffl.Pointer address) {
        __info.useMemory(address);
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
        sb.append(getClass().getSimpleName() + " { \n");
        final java.lang.String fieldPrefix = "    ";
        for (java.lang.reflect.Field field : fields) {
            try {
                sb.append(fieldPrefix);
                sb.append(field.getName()).append(" = ");
                sb.append(field.get(this).toString());
                sb.append("\n");
            } catch (Throwable ex) {
                throw new RuntimeException(ex);
            }
        }
        sb.append("}\n");
        return sb.toString();
    }
    
    public static final class Offset extends java.lang.Number {
        private final int offset;
        public Offset(int offset) {
            this.offset = offset;
        }
        @Override
        public int intValue() {
            return offset;
        }
        @Override
        public long longValue() {
            return offset;
        }
        @Override
        public float floatValue() {
            return offset;
        }
        @Override
        public double doubleValue() {
            return offset;
        }
    }

    /**
     * Interface all Struct members must implement.
     */
    protected interface Member {
        /**
         * Gets the <tt>Struct</tt> this <tt>Member</tt> is a member of.
         * 
         * @return a <tt>Struct</tt>.
         */
        Struct struct();

        /**
         * Gets the memory object used to store this {@code Member}
         * 
         * @return a {@code Pointer}
         */
        com.kenai.jaffl.Pointer getMemory();
        
        /**
         * Gets the offset within the structure for this field.
         */
        long offset();
    }

    /**
     * Starts an array construction session
     */
    protected final void arrayBegin() {
        __info.resetIndex = false;
    }
    
    /**
     * Ends an array construction session
     */
    protected final void arrayEnd() {
        __info.resetIndex = __info.isUnion;
    }
    
    /**
     * Creates an array of <tt>Member</tt> instances.
     * 
     * @param <T> The type of the <tt>Member</tt> subclass to create.
     * @param array the array to store the instances in
     * @return the array that was passed in
     */
    @SuppressWarnings("unchecked")
    protected <T extends Member> T[] array(T[] array) {
        arrayBegin();
        try {
            Class<?> arrayClass = array.getClass().getComponentType();
            Constructor<?> ctor = arrayClass.getDeclaredConstructor(new Class[] { arrayClass.getEnclosingClass() });
            Object[] parameters = { Struct.this  };
            for (int i = 0; i < array.length; ++i) {
                array[i] = (T) ctor.newInstance(parameters);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);    
        }
        arrayEnd();
        return array;
    }
    
    /**
     * Creates an array of <tt>Signed8</tt> instances.
     * 
     * @param array the array to store the instances in
     * @return the array that was passed in
     */
    protected final Signed8[] array(Signed8[] array) {
        arrayBegin();
        for (int i = 0; i < array.length; ++i) {
            array[i] = new Signed8();
        }
        arrayEnd();
        return array;
    }
    
    /**
     * Creates an array of <tt>Unsigned8</tt> instances.
     * 
     * @param array the array to store the instances in
     * @return the array that was passed in
     */
    protected final Unsigned8[] array(Unsigned8[] array) {
        arrayBegin();
        for (int i = 0; i < array.length; ++i) {
            array[i] = new Unsigned8();
        }
        arrayEnd();
        return array;
    }
    
    /**
     * Creates an array of <tt>Signed16</tt> instances.
     * 
     * @param array the array to store the instances in
     * @return the array that was passed in
     */
    protected final Signed16[] array(Signed16[] array) {
        arrayBegin();
        for (int i = 0; i < array.length; ++i) {
            array[i] = new Signed16();
        }
        arrayEnd();
        return array;
    }
    
    /**
     * Creates an array of <tt>Unsigned16</tt> instances.
     * 
     * @param array the array to store the instances in
     * @return the array that was passed in
     */
    protected final Unsigned16[] array(Unsigned16[] array) {
        arrayBegin();
        for (int i = 0; i < array.length; ++i) {
            array[i] = new Unsigned16();
        }
        arrayEnd();
        return array;
    }
    
    /**
     * Creates an array of <tt>Signed32</tt> instances.
     * 
     * @param array the array to store the instances in
     * @return the array that was passed in
     */
    protected final Signed32[] array(Signed32[] array) {
        arrayBegin();
        for (int i = 0; i < array.length; ++i) {
            array[i] = new Signed32();
        }
        arrayEnd();
        return array;
    }
    
    /**
     * Creates an array of <tt>Unsigned32</tt> instances.
     * 
     * @param array the array to store the instances in
     * @return the array that was passed in
     */
    protected final Unsigned32[] array(Unsigned32[] array) {
        arrayBegin();
        for (int i = 0; i < array.length; ++i) {
            array[i] = new Unsigned32();
        }
        arrayEnd();
        return array;
    }

    /**
     * Creates an array of <tt>Signed64</tt> instances.
     * 
     * @param array the array to store the instances in
     * @return the array that was passed in
     */
    protected final Signed64[] array(Signed64[] array) {
        arrayBegin();
        for (int i = 0; i < array.length; ++i) {
            array[i] = new Signed64();
        }
        arrayEnd();
        return array;
    }
    
    /**
     * Creates an array of <tt>Unsigned64</tt> instances.
     * 
     * @param array the array to store the instances in
     * @return the array that was passed in
     */
    protected final Unsigned64[] array(Unsigned64[] array) {
        arrayBegin();
        for (int i = 0; i < array.length; ++i) {
            array[i] = new Unsigned64();
        }
        arrayEnd();
        return array;
    }
    
    /**
     * Creates an array of <tt>SignedLong</tt> instances.
     * 
     * @param array the array to store the instances in
     * @return the array that was passed in
     */
    protected final SignedLong[] array(SignedLong[] array) {
        arrayBegin();
        for (int i = 0; i < array.length; ++i) {
            array[i] = new SignedLong();
        }
        arrayEnd();
        return array;
    }
    
    /**
     * Creates an array of <tt>UnsignedLong</tt> instances.
     * 
     * @param array the array to store the instances in
     * @return the array that was passed in
     */
    protected final UnsignedLong[] array(UnsignedLong[] array) {
        arrayBegin();
        for (int i = 0; i < array.length; ++i) {
            array[i] = new UnsignedLong();
        }
        arrayEnd();
        return array;
    }
    
    /**
     * Creates an array of <tt>Float</tt> instances.
     * 
     * @param array the array to store the instances in
     * @return the array that was passed in
     */
    protected final Float[] array(Float[] array) {
        arrayBegin();
        for (int i = 0; i < array.length; ++i) {
            array[i] = new Float();
        }
        arrayEnd();
        return array;
    }
    
    /**
     * Creates an array of <tt>Double</tt> instances.
     * 
     * @param array the array to store the instances in
     * @return the array that was passed in
     */
    protected final Double[] array(Double[] array) {
        arrayBegin();
        for (int i = 0; i < array.length; ++i) {
            array[i] = new Double();
        }
        arrayEnd();
        return array;
    }
    
    /**
     * Creates an array of <tt>Address</tt> instances.
     * 
     * @param array the array to store the instances in
     * @return the array that was passed in
     */
    protected final Address[] array(Address[] array) {
        arrayBegin();
        for (int i = 0; i < array.length; ++i) {
            array[i] = new Address();
        }
        arrayEnd();
        return array;
    }
    
    /**
     * Creates an array of <tt>Pointer</tt> instances.
     * 
     * @param array the array to store the instances in
     * @return the array that was passed in
     */
    protected final Pointer[] array(Pointer[] array) {
        arrayBegin();
        for (int i = 0; i < array.length; ++i) {
            array[i] = new Pointer();
        }
        arrayEnd();
        return array;
    }

    protected final <T extends Struct> T inner(Struct struct) {
        int salign = struct.__info.getMinimumAlignment();
        int off = salign + ((__info.size - 1) & ~(salign - 1));
        struct.__info.enclosing = this;
        struct.__info.offset = off;
        __info.size = off + struct.__info.size;

        return (T) struct;
    }
    
    /**
     * Base implementation of Member
     */
    protected abstract class AbstractMember implements Member {
        private final int offset;
        protected AbstractMember(int size) {
            this(size, size);
        }
        protected AbstractMember(int size, int align, Offset offset) {
            this.offset = __info.addField(size, align, offset);
        }
        protected AbstractMember(int size, int align) {
            this.offset = __info.addField(size, align);
        }
        
        public final com.kenai.jaffl.Pointer getMemory() {
            return __info.getMemory();
        }
        
        /**
         * Gets the <tt>Struct</tt> this <tt>Member</tt> is a member of.
         * 
         * @return a <tt>Struct</tt>.
         */
        public final Struct struct() {
            return Struct.this;
        }
        
        /**
         * Gets the offset within the structure for this field.
         */
        public final long offset() {
            return offset + __info.offset;
        }
    }
    
    /**
     * Base class for all Number structure fields.
     */
    protected abstract class NumberField implements Member {
        /**
         * Offset from the start of the <tt>Struct</tt> memory this field is located at.
         */
        private final int offset;
  
        protected NumberField(NativeType type) {
            Type t = getRuntime().findType(type);
            this.offset = __info.addField(t.size() * 8, t.alignment() * 8);
        }

        protected NumberField(NativeType type, Offset offset) {
            Type t = getRuntime().findType(type);
            this.offset = __info.addField(t.size() * 8, t.alignment() * 8, offset);
        }
        
        public final com.kenai.jaffl.Pointer getMemory() {
            return __info.getMemory();
        }
        
        
        /**
         * Gets the <tt>Struct</tt> this <tt>Member</tt> is in.
         * 
         * @return a <tt>Struct</tt>.
         */
        public final Struct struct() {
            return Struct.this;
        }
        
        /**
         * Gets the offset within the structure for this field.
         */
        public final long offset() {
            return offset + __info.offset;
        }
        
        /**
         * Sets the field to a new value.
         * 
         * @param value The new value.
         */
        public abstract void set(java.lang.Number value);

        /**
         * Returns an {@code float} representation of this <tt>Number</tt>.
         * 
         * @return an {@code float} value for this <tt>Number</tt>.
         */
        public double doubleValue() {
            return (double) longValue();
        }
        
        /**
         * Returns an {@code float} representation of this <tt>Number</tt>.
         * 
         * @return an {@code float} value for this <tt>Number</tt>.
         */
        public float floatValue() {
            return (float) intValue();
        }

        /**
         * Returns a {@code byte} representation of this <tt>Number</tt>.
         *
         * @return a {@code byte} value for this <tt>Number</tt>.
         */
        public byte byteValue() {
            return (byte) intValue();
        }

        /**
         * Returns a {@code short} representation of this <tt>Number</tt>.
         *
         * @return a {@code short} value for this <tt>Number</tt>.
         */
        public short shortValue() {
            return (short) intValue();
        }

        /**
         * Returns a {@code int} representation of this <tt>Number</tt>.
         *
         * @return a {@code int} value for this <tt>Number</tt>.
         */
        public abstract int intValue();

        /**
         * Returns a {@code long} representation of this <tt>Number</tt>.
         * 
         * @return a {@code long} value for this <tt>Number</tt>.
         */
        public long longValue() {
            return intValue();
        }
        
        /**
         * Returns a string representation of this <code>Address</code>.
         *
         * @return a string representation of this <code>Address</code>.
         */
        @Override
        public java.lang.String toString() {
            return java.lang.Integer.toString(intValue(), 10);
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
        public final byte get() {
            return getMemory().getByte(offset());
        }
        
        /**
         * Sets the value for this field.
         * 
         * @param value the 8 bit value to set.
         */
        public final void set(byte value) {
            getMemory().putByte(offset(), value);
        }

        public void set(java.lang.Number value) {
            getMemory().putByte(offset(), value.byteValue());
        }

        /**
         * Returns a java byte representation of this field.
         * 
         * @return a java byte value for this field.
         */
        @Override
        public final byte byteValue() {
            return get();
        }
        
        /**
         * Returns a java short representation of this field.
         * 
         * @return a java short value for this field.
         */
        @Override
        public final short shortValue() {
            return get();
        }
        
        /**
         * Returns a java int representation of this field.
         * 
         * @return a java int value for this field.
         */
        @Override
        public final int intValue() {
            return get();
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
        public final short get() {
            short value = getMemory().getByte(offset());
            return value < 0 ? (short) ((value & 0x7F) + 0x80) : value;
        }
        
        /**
         * Sets the value for this field.
         * 
         * @param value the 8 bit value to set.
         */
        public final void set(short value) {
            getMemory().putByte(offset(), (byte) value);
        }

        public void set(java.lang.Number value) {
            getMemory().putByte(offset(), value.byteValue());
        }

        /**
         * Returns a java short representation of this field.
         * 
         * @return a java short value for this field.
         */
        @Override
        public final short shortValue() {
            return get();
        }
        
        /**
         * Returns a java int representation of this field.
         * 
         * @return a java int value for this field.
         */
        @Override
        public final int intValue() {
            return get();
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
        public final short get() {
            return getMemory().getShort(offset());
        }
        
        /**
         * Sets the value for this field.
         * 
         * @param value the 16 bit value to set.
         */
        public final void set(short value) {
            getMemory().putShort(offset(), value);
        }

        public void set(java.lang.Number value) {
            getMemory().putShort(offset(), value.shortValue());
        }

        /**
         * Returns a java short representation of this field.
         * 
         * @return a java short value for this field.
         */
        @Override
        public final short shortValue() {
            return get();
        }
        
        /**
         * Returns a java int representation of this field.
         * 
         * @return a java int value for this field.
         */
        @Override
        public final int intValue() {
            return get();
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
        public final int get() {
            int value = getMemory().getShort(offset());
            return value < 0 ? (int)((value & 0x7FFF) + 0x8000) : value;
        }
        
        /**
         * Sets the value for this field.
         * 
         * @param value the 16 bit unsigned value to set.
         */
        public final void set(int value) {
            getMemory().putShort(offset(), (short) value);
        }

        public void set(Number value) {
            getMemory().putShort(offset(), value.shortValue());
        }

        /**
         * Returns a java int representation of this field.
         * 
         * @return a java int value for this field.
         */
        @Override
        public final int intValue() {
            return get();
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
        public final int get() {
            return getMemory().getInt(offset());
        }
        
        /**
         * Sets the value for this field.
         * 
         * @param value the 32 bit value to set.
         */
        public final void set(int value) {
            getMemory().putInt(offset(), value);
        }

        public void set(java.lang.Number value) {
            getMemory().putInt(offset(), value.intValue());
        }

        /**
         * Returns a java int representation of this field.
         * 
         * @return a java int value for this field.
         */
        @Override
        public final int intValue() {
            return get();
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
        public final long get() {
            long value = getMemory().getInt(offset());
            return value < 0 ? (long)((value & 0x7FFFFFFFL) + 0x80000000L) : value;
        }
        
        /**
         * Sets the value for this field.
         * 
         * @param value the 32 bit unsigned value to set.
         */
        public final void set(long value) {
            getMemory().putInt(offset(), (int) value);
        }

        public void set(java.lang.Number value) {
            getMemory().putInt(offset(), value.intValue());
        }

        /**
         * Returns a java int representation of this field.
         * 
         * @return a java int value for this field.
         */
        @Override
        public final int intValue() {
            return (int) get();
        }
        
        /**
         * Returns a java long representation of this field.
         * 
         * @return a java long value for this field.
         */
        @Override
        public final long longValue() {
            return get();
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
        public final long get() {
            return getMemory().getLong(offset());
        }
        
        /**
         * Sets the value for this field.
         * 
         * @param value the 64 bit value to set.
         */
        public final void set(long value) {
            getMemory().putLong(offset(), value);
        }

        public void set(java.lang.Number value) {
            getMemory().putLong(offset(), value.longValue());
        }

        /**
         * Returns a java int representation of this field.
         * 
         * @return a java int value for this field.
         */
        @Override
        public final int intValue() {
            return (int) get();
        }
        
        /**
         * Returns a java long representation of this field.
         * 
         * @return a java long value for this field.
         */
        @Override
        public final long longValue() {
            return get();
        }
        
        /**
         * Returns a string representation of this field.
         *
         * @return a string representation of this field.
         */
        @Override
        public final java.lang.String toString() {
            return java.lang.Long.toString(get());
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
        public final long get() {
            return getMemory().getLong(offset());
        }
        
        /**
         * Sets the value for this field.
         * 
         * @param value the 64 bit value to set.
         */
        public final void set(long value) {
            getMemory().putLong(offset(), value);
        }

        public void set(java.lang.Number value) {
            getMemory().putLong(offset(), value.longValue());
        }

        /**
         * Returns a java int representation of this field.
         * 
         * @return a java int value for this field.
         */
        @Override
        public final int intValue() {
            return (int) get();
        }
        
        /**
         * Returns a java long representation of this field.
         * 
         * @return a java long value for this field.
         */
        @Override
        public final long longValue() {
            return get();
        }
        
        /**
         * Returns a string representation of this field.
         *
         * @return a string representation of this field.
         */
        @Override
        public final java.lang.String toString() {
            return java.lang.Long.toString(get());
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
        public final long get() {
            return getMemory().getNativeLong(offset());
        }
        
        /**
         * Sets the value for this field.
         * 
         * @param value the 32/64 bit value to set.
         */
        public final void set(long value) {
            getMemory().putNativeLong(offset(), value);
        }

        public void set(java.lang.Number value) {
            getMemory().putNativeLong(offset(), value.longValue());
        }

        /**
         * Returns a java int representation of this field.
         * 
         * @return a java int value for this field.
         */
        @Override
        public final int intValue() {
            return (int) get();
        }
        
        /**
         * Returns a java long representation of this field.
         * 
         * @return a java long value for this field.
         */
        @Override
        public final long longValue() {
            return get();
        }
        
        /**
         * Returns a string representation of this field.
         *
         * @return a string representation of this field.
         */
        @Override
        public final java.lang.String toString() {
            return java.lang.Long.toString(get());
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
        public final long get() {
            long value = getMemory().getNativeLong(offset());
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
        public final void set(long value) {
            getMemory().putNativeLong(offset(), value);
        }

        public void set(java.lang.Number value) {
            getMemory().putNativeLong(offset(), value.longValue());
        }

        /**
         * Returns a java int representation of this field.
         * 
         * @return a java int value for this field.
         */
        @Override
        public final int intValue() {
            return (int) get();
        }
        
        /**
         * Returns a java long representation of this field.
         * 
         * @return a java long value for this field.
         */
        @Override
        public final long longValue() {
            return get();
        }
        
        /**
         * Returns a string representation of this field.
         *
         * @return a string representation of this field.
         */
        @Override
        public final java.lang.String toString() {
            return java.lang.Long.toString(get());
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
        
        public final float get() {
            return getMemory().getFloat(offset());
        }
        public final void set(float value) {
            getMemory().putFloat(offset(), value);
        }
        public void set(java.lang.Number value) {
            getMemory().putFloat(offset(), value.floatValue());
        }
        
        @Override
        public final int intValue() {
            return (int) get();
        }

        @Override
        public final double doubleValue() {
            return get();
        }

        @Override
        public final float floatValue() {
            return get();
        }

        @Override
        public final long longValue() {
            return (long) get();
        }
        @Override
        public final java.lang.String toString() {
            return java.lang.String.valueOf(get());
        }
    }

    public final class Double extends NumberField {
        public Double() {
            super(NativeType.DOUBLE);
        }
        public Double(Offset offset) {
            super(NativeType.DOUBLE, offset);
        }
        public final double get() {
            return getMemory().getDouble(offset());
        }
        public final void set(double value) {
            getMemory().putDouble(offset(), value);
        }
        public void set(java.lang.Number value) {
            getMemory().putDouble(offset(), value.doubleValue());
        }
        
        @Override
        public final int intValue() {
            return (int) get();
        }
        
        @Override
        public final long longValue() {
            return (long) get();
        }
        @Override
        public final float floatValue() {
            return (float) get();
        }
        
        @Override
        public final double doubleValue() {
            return get();
        }
        @Override
        public final java.lang.String toString() {
            return java.lang.String.valueOf(get());
        }
    }
    
    /**
     * Represents a native memory address.
     */
    public class Address extends NumberField {
        
        /**
         * Creates a new <tt>Address</tt> field.
         */
        public Address() {
            super(NativeType.ADDRESS);
        }
        public Address(Offset offset) {
            super(NativeType.ADDRESS, offset);
        }
        
        /**
         * Gets the {@link com.kenai.jffi.Address} value from the native memory.
         * 
         * @return a {@link com.kenai.jffi.Address}.
         */
        public final com.kenai.jaffl.Address get() {
            return com.kenai.jaffl.Address.valueOf(getMemory().getAddress(offset()));
        }
        
        /**
         * Puts a {@link com.kenai.jffi.Address} value into the native memory.
         */
        public final void set(com.kenai.jaffl.Address value) {
            getMemory().putAddress(offset(), value != null ? value.nativeAddress() : 0);
        }

        public void set(java.lang.Number value) {
            getMemory().putAddress(offset(), value.longValue());
        }
        /**
         * Returns an integer representation of this address.
         * 
         * @return an integer value for this address.
         */
        @Override
        public final int intValue() {
            return get().intValue();
        }
        
        /**
         * Returns an {@code long} representation of this address.
         * 
         * @return an {@code long} value for this address.
         */
        @Override
        public final long longValue() {
            return get().longValue();
        }
        
        /**
         * Returns a string representation of this <code>Address</code>.
         *
         * @return a string representation of this <code>Address</code>.
         */
        @Override
        public final java.lang.String toString() {
            return get().toString();
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
         * Gets the {@link com.kenai.jffi.Address} value from the native memory.
         * 
         * @return a {@link com.kenai.jffi.Address}.
         */
        public final com.kenai.jaffl.Pointer get() {
            return getMemory().getPointer(offset());
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
         * Puts a {@link com.kenai.jffi.Address} value into the native memory.
         */
        public final void set(com.kenai.jaffl.Pointer value) {
            getMemory().putPointer(offset(), value);
        }

        public void set(java.lang.Number value) {
            getMemory().putAddress(offset(), value.longValue());
        }
        /**
         * Returns an integer representation of this <code>Pointer</code>.
         * 
         * @return an integer value for this <code>Pointer</code>.
         */
        @Override
        public final int intValue() {
            return (int) getMemory().getAddress(offset());
        }
        
        /**
         * Returns an {@code long} representation of this <code>Pointer</code>.
         * 
         * @return an {@code long} value for this <code>Pointer</code>.
         */
        @Override
        public final long longValue() {
            return getMemory().getAddress(offset());
        }
        
        /**
         * Returns a string representation of this <code>Pointer</code>.
         *
         * @return a string representation of this <code>Pointer</code>.
         */
        @Override
        public final java.lang.String toString() {
            return get().toString();
        }
    }
    
    /**
     * Base for all the Enum fields.
     * 
     * @param <E> the type of {@link java.lang.Enum}
     */
    protected abstract class EnumField<E> extends NumberField {
        protected final Class<E> enumClass;

        /**
         * Constructs a new Enum field.
         * 
         * @param type the native type of the enum.
         * @param enumClass the Enum class.
         */
        public EnumField(NativeType type, Class<E> enumClass) {
            super(type);
            this.enumClass = enumClass;
        }
        
        /**
         * Gets a java Enum value representing the native integer value.
         * 
         * @return a java Enum value.
         */
        public abstract E get();
        
        /**
         * Returns a string representation of this field.
         *
         * @return a string representation of this field.
         */
        @Override
        public final java.lang.String toString() {
            return get().toString();
        }
    }
    /**
     * An 8 bit enum field.
     * 
     * @param <E> the {@link java.lang.Enum} to translate to/from.
     */
    public class Enum8<E extends java.lang.Enum<E>> extends EnumField<E> {
        /**
         * Creates a new 8 bit enum field.
         * 
         * @param enumClass the class of the {@link java.lang.Enum}.
         */
        public Enum8(Class<E> enumClass) {
            super(NativeType.SCHAR, enumClass);
        }
        
        /**
         * Gets a java Enum value representing the native integer value.
         * 
         * @return a java Enum value.
         */
        public final E get() {
            return EnumMapper.getInstance().valueOf(intValue(), enumClass);
        }
        
        /**
         * Sets the native integer value using a java Enum value.
         * 
         * @param value the java <tt>Enum</tt> value.
         */
        public final void set(E value) {
            getMemory().putByte(offset(), (byte) EnumMapper.getInstance().intValue(value));
        }

        public void set(java.lang.Number value) {
            getMemory().putByte(offset(), value.byteValue());
        }
        /**
         * Returns an integer representation of this enum field.
         * 
         * @return an integer value for this enum field.
         */
        @Override
        public final int intValue() {
            return getMemory().getByte(offset());
        }
    }

    public class Enum16<E extends java.lang.Enum<E>> extends EnumField<E> {
        public Enum16(Class<E> enumClass) {
            super(NativeType.SSHORT, enumClass);
        }
        public final E get() {
            return EnumMapper.getInstance().valueOf(intValue(), enumClass);
        }
        public final void set(E value) {
            getMemory().putShort(offset(), (short) EnumMapper.getInstance().intValue(value));
        }
        public void set(java.lang.Number value) {
            getMemory().putShort(offset(), value.shortValue());
        }
        @Override
        public final int intValue() {
            return getMemory().getShort(offset());
        }
    }

    public class Enum32<E extends java.lang.Enum<E>> extends EnumField<E> {
        public Enum32(Class<E> enumClass) {
            super(NativeType.SINT, enumClass);
        }
        public final E get() {
            return EnumMapper.getInstance().valueOf(intValue(), enumClass);
        }
        public final void set(E value) {
            getMemory().putInt(offset(), EnumMapper.getInstance().intValue(value));
        }
        public void set(java.lang.Number value) {
            getMemory().putInt(offset(), value.intValue());
        }
        @Override
        public final int intValue() {
            return getMemory().getInt(offset());
        }
    }
    
    public class Enum64<E extends java.lang.Enum<E>> extends EnumField<E> {
        public Enum64(Class<E> enumClass) {
            super(NativeType.SLONGLONG, enumClass);
        }
        public final E get() {
            return EnumMapper.getInstance().valueOf(intValue(), enumClass);
        }
        public final void set(E value) {
            getMemory().putLong(offset(), EnumMapper.getInstance().intValue(value));
        }
        public void set(java.lang.Number value) {
            getMemory().putLong(offset(), value.longValue());
        }
        @Override
        public final int intValue() {
            return (int) longValue();
        }
        @Override
        public final long longValue() {
            return getMemory().getLong(offset());
        }
    }

    public class EnumLong<E extends java.lang.Enum<E>> extends EnumField<E> {
        public EnumLong(Class<E> enumClass) {
            super(NativeType.SLONG, enumClass);
        }
        
        public final E get() {
            return EnumMapper.getInstance().valueOf(intValue(), enumClass);
        }
        public final void set(E value) {
            getMemory().putNativeLong(offset(), EnumMapper.getInstance().intValue(value));
        }
        public void set(java.lang.Number value) {
            getMemory().putNativeLong(offset(), value.longValue());
        }

        @Override
        public final int intValue() {
            return (int) longValue();
        }

        @Override
        public final long longValue() {
            return getMemory().getNativeLong(offset());
        }
    }
    
    public class Enum<T extends java.lang.Enum<T>> extends Enum32<T> {
        public Enum(Class<T> enumClass) {
            super(enumClass);
        }
    }
    
    abstract public class String extends AbstractMember {
        protected final Charset charset;
        protected final int length;
        
        protected String(int size, int align, int length, Charset cs) {
            super(size, align);
            this.length = length;
            this.charset = cs;
        }
        protected String(int size, int align, Offset offset, int length, Charset cs) {
            super(size, align, offset);
            this.length = length;
            this.charset = cs;
        }
        public final int length() {
            return length;
        }

        protected abstract com.kenai.jaffl.Pointer getStringMemory();
        public abstract java.lang.String get();
        public abstract void set(java.lang.String value);

        @Override
        public final java.lang.String toString() {
            return get();
        }
    }

    public class UTFString extends String {
        public UTFString(int length, Charset cs) {
            super(length * 8, 8, length, cs); // FIXME: This won't work for non-ASCII
         
        }
        protected com.kenai.jaffl.Pointer getStringMemory() {
            return getMemory().slice(offset(), length());
        }

        public final java.lang.String get() {
            return getStringMemory().getString(0, length, charset);
        }

        public final void set(java.lang.String value) {
            getStringMemory().putString(0, value, length, charset);
        }
    }

    public class UTF8String extends UTFString {
        public UTF8String(int size) {
            super(size, Charset.forName("UTF-8"));
        }
    }

    public class AsciiString extends UTFString {
        public AsciiString(int size) {
            super(size, Charset.forName("ASCII"));
        }
    }

    public class UTFStringRef extends String {
        private com.kenai.jaffl.Pointer valueHolder;

        public UTFStringRef(int length, Charset cs) {
            super(getRuntime().findType(NativeType.ADDRESS).size() * 8, getRuntime().findType(NativeType.ADDRESS).alignment() * 8,
                    length, cs);
        }

        public UTFStringRef(Charset cs) {
            this(Integer.MAX_VALUE, cs);
        }

        protected com.kenai.jaffl.Pointer getStringMemory() {
            return getMemory().getPointer(offset(), length());
        }

        public final java.lang.String get() {
            com.kenai.jaffl.Pointer ptr = getStringMemory();
            return ptr != null ? ptr.getString(0, length, charset) : null;
        }

        public final void set(java.lang.String value) {
            if (value != null) {
                valueHolder = getRuntime().getMemoryManager().allocateDirect(length() * 4);
                valueHolder.putString(0, value, length() * 4, charset);
                getMemory().putPointer(offset(), valueHolder);
            
            } else {
                this.valueHolder = null;
                getMemory().putAddress(offset(), 0);
            }
        }
    }

    public class UTF8StringRef extends UTFStringRef {
        public UTF8StringRef(int size) {
            super(size, Charset.forName("UTF-8"));
        }
        public UTF8StringRef() {
            super(Integer.MAX_VALUE, Charset.forName("UTF-8"));
        }
    }
    public class AsciiStringRef extends UTFStringRef {
        public AsciiStringRef(int size) {
            super(size, Charset.forName("ASCII"));
        }
        public AsciiStringRef() {
            super(Integer.MAX_VALUE, Charset.forName("ASCII"));
        }
    }

    /**
     * Specialized padding fields for structs.  Use this instead of arrays of other
     * members for more efficient struct construction.
     */
    public final class Padding extends AbstractMember {
        Padding(Type type, int length) {
            super(type.size() * 8 * length, type.alignment() * 8);
        }
        Padding(NativeType type, int length) {
            super(getRuntime().findType(type).size() * 8 * length, getRuntime().findType(type).alignment() * 8);
        }
    }
    /*
    public final Marshaller.Session marshal(Marshaller marshaller, MarshalContext context) {
        return __info.marshal(marshaller, context);
    }
    */
}
