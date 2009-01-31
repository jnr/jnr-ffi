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

import com.kenai.jaffl.MemoryIO;
import com.kenai.jaffl.ParameterFlags;
import com.kenai.jaffl.Platform;
import com.kenai.jaffl.util.EnumMapper;
import java.lang.reflect.Constructor;
import java.nio.charset.Charset;

/**
 * Representation of C structures in java.
 * 
 * <b>Note:</b> This class is not threadsafe.
 */
public abstract class Struct /*implements Marshallable */{
    /**
     * Various platform-dependent constants needed for Struct construction
     */
    protected static final class Constants {
        private static final boolean isSparc() { return Platform.getPlatform().getCPU() == Platform.CPU.SPARC; }
        /*
         * Most arches align long/double on the same size as a native long (or a pointer)
         * Sparc (32bit) requires it to be aligned on an 8 byte boundary
         */
        static final int LONG_SIZE = Platform.getPlatform().longSize();
        static final int ADDRESS_SIZE = Platform.getPlatform().addressSize();
        static final long LONG_MASK = LONG_SIZE == 32 ? 0x7FFFFFFFL : 0x7FFFFFFFFFFFFFFFL;
        static final int LONG_ALIGN = isSparc() ? 64 : ADDRESS_SIZE;
        static final int ADDRESS_ALIGN = isSparc() ? 64 : ADDRESS_SIZE;
        static final int DOUBLE_ALIGN = isSparc() ? 64 : ADDRESS_SIZE;
        static final int FLOAT_ALIGN = isSparc() ? 64 : 32;
    }
    static final class Info {
        MemoryIO io;
        int size = 0;
        int minAlign = 1;
        boolean isUnion = false;
        boolean resetIndex = false;

        public final MemoryIO getMemoryIO(int flags) {
            return io != null ? io : (io = allocateMemory(ParameterFlags.TRANSIENT));
        }
        public final MemoryIO getMemoryIO() {
            return getMemoryIO(ParameterFlags.TRANSIENT);
        }
        final int size() {
            return size;
        }
        final int getMinimumAlignment() {
            return minAlign;
        }
        private final MemoryIO allocateMemory(int flags) {
            if (ParameterFlags.isTransient(flags)) {
                return MemoryIO.allocate(size());
            } else {
                return MemoryIO.allocateDirect(size(), true);
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
        public final void useMemory(com.kenai.jaffl.MemoryIO io) {
            this.io = io;
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
    final Info __info = new Info();
    
    /**
     * Creates a new <tt>Struct</tt>.
     */
    protected Struct() {}
    
    /**
     * Creates a new <tt>Struct</tt>.
     * 
     * @param isUnion if this Struct is a Union
     */
    Struct(final boolean isUnion) {
        __info.resetIndex = isUnion;
    }

    /**
     * Uses the specified memory address as the backing store for this structure.
     *
     * @param address the native memory area.
     */
    public void useMemory(com.kenai.jaffl.MemoryIO address) {
        __info.useMemory(address);
    }

    /**
     * Uses the specified memory address as the backing store for this structure.
     *
     * @param address the native memory area.
     */
    public void useMemory(com.kenai.jaffl.Pointer address) {
        __info.useMemory(MemoryIO.wrap(address));
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
         * Gets the <tt>MemoryIO</tt> used to read/write this <tt>Member</tt>.
         * 
         * @return a <tt>MemoryIO</tt>.
         */
        MemoryIO getMemoryIO();
        
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
    
    /**
     * Base implementation of Member
     */
    protected abstract class AbstractMember implements Member {
        protected final int offset;
        protected AbstractMember(int size) {
            this(size, size);
        }
        protected AbstractMember(int size, int align, Offset offset) {
            this.offset = __info.addField(size, align, offset);
        }
        protected AbstractMember(int size, int align) {
            this.offset = __info.addField(size, align);
        }
        
        /**
         * Gets the <tt>MemoryIO</tt> used to read/write this <tt>Member</tt>.
         * 
         * @return a <tt>MemoryIO</tt>.
         */
        public final MemoryIO getMemoryIO() {
            return __info.getMemoryIO();
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
            return offset;
        }
    }
    
    /**
     * Base class for all Number structure fields.
     */
    protected abstract class NumberField extends java.lang.Number implements Member {
        /**
         * Offset from the start of the <tt>Struct</tt> memory this field is located at.
         */
        protected final int offset;
        protected NumberField(int size) {
            this(size, size);
        }
        protected NumberField(int size, Offset offset) {
            this(size, size, offset);
        }
        protected NumberField(int size, int align, Offset offset) {
            this.offset = __info.addField(size, align, offset);
        }
        protected NumberField(int size, int align) {
            this.offset = __info.addField(size, align);
        }
        
        /**
         * Gets the <tt>MemoryIO</tt> used to read/write this <tt>Member</tt>.
         * 
         * @return a <tt>MemoryIO</tt>.
         */
        public final MemoryIO getMemoryIO() {
            return __info.getMemoryIO();
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
            return offset;
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
        @Override
        public double doubleValue() {
            return (double) longValue();
        }
        
        /**
         * Returns an {@code float} representation of this <tt>Number</tt>.
         * 
         * @return an {@code float} value for this <tt>Number</tt>.
         */
        @Override
        public float floatValue() {
            return (float) intValue();
        }
        
        /**
         * Returns an {@code long} representation of this <tt>Number</tt>.
         * 
         * @return an {@code long} value for this <tt>Number</tt>.
         */
        @Override
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
            super(8);
        }

        /**
         * Creates a new 8 bit integer field at a specific offset
         *
         * @param offset The offset within the memory area
         */
        public Signed8(Offset offset) {
            super(8, offset);
        }
        
        /**
         * Gets the value for this field.
         * 
         * @return a byte.
         */
        public final byte get() {
            return getMemoryIO().getByte(offset);
        }
        
        /**
         * Sets the value for this field.
         * 
         * @param value the 8 bit value to set.
         */
        public final void set(byte value) {
            getMemoryIO().putByte(offset, value);
        }

        public void set(java.lang.Number value) {
            getMemoryIO().putByte(offset, value.byteValue());
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
            super(8);
        }

        /**
         * Creates a new 8 bit unsigned integer field at a specific offset
         *
         * @param offset The offset within the memory area for this field.
         */
        public Unsigned8(Offset offset) {
            super(8, offset);
        }
        
        /**
         * Gets the value for this field.
         * 
         * @return a byte.
         */
        public final short get() {
            short value = getMemoryIO().getByte(offset);
            return value < 0 ? (short) ((value & 0x7F) + 0x80) : value;
        }
        
        /**
         * Sets the value for this field.
         * 
         * @param value the 8 bit value to set.
         */
        public final void set(short value) {
            getMemoryIO().putByte(offset, (byte) value);
        }

        public void set(java.lang.Number value) {
            getMemoryIO().putByte(offset, value.byteValue());
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
            super(16);
        }

        /**
         * Creates a new 16 bit signed integer field at a specific offset
         *
         * @param offset The offset within the memory area for this field.
         */
        public Signed16(Offset offset) {
            super(16, offset);
        }

        /**
         * Gets the value for this field.
         * 
         * @return a short.
         */
        public final short get() {
            return getMemoryIO().getShort(offset);
        }
        
        /**
         * Sets the value for this field.
         * 
         * @param value the 16 bit value to set.
         */
        public final void set(short value) {
            getMemoryIO().putShort(offset, value);
        }

        public void set(java.lang.Number value) {
            getMemoryIO().putShort(offset, value.shortValue());
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
            super(16);
        }

        /**
         * Creates a new 16 bit unsigned integer field at a specific offset
         *
         * @param offset The offset within the memory area for this field.
         */
        public Unsigned16(Offset offset) {
            super(16, offset);
        }

        /**
         * Gets the value for this field.
         * 
         * @return a short.
         */
        public final int get() {
            int value = getMemoryIO().getShort(offset);
            return value < 0 ? (int)((value & 0x7FFF) + 0x8000) : value;
        }
        
        /**
         * Sets the value for this field.
         * 
         * @param value the 16 bit unsigned value to set.
         */
        public final void set(int value) {
            getMemoryIO().putShort(offset, (short) value);
        }

        public void set(Number value) {
            getMemoryIO().putShort(offset, value.shortValue());
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
            super(32);
        }

        /**
         * Creates a new 32 bit signed integer field at a specific offset
         *
         * @param offset The offset within the memory area for this field.
         */
        public Signed32(Offset offset) {
            super(32, offset);
        }

        /**
         * Gets the value for this field.
         * 
         * @return a int.
         */
        public final int get() {
            return getMemoryIO().getInt(offset);
        }
        
        /**
         * Sets the value for this field.
         * 
         * @param value the 32 bit value to set.
         */
        public final void set(int value) {
            getMemoryIO().putInt(offset, value);
        }

        public void set(java.lang.Number value) {
            getMemoryIO().putInt(offset, value.intValue());
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
            super(32);
        }

        /**
         * Creates a new 32 bit unsigned integer field at a specific offset
         *
         * @param offset The offset within the memory area for this field.
         */
        public Unsigned32(Offset offset) {
            super(32, offset);
        }

        /**
         * Gets the value for this field.
         * 
         * @return a long.
         */
        public final long get() {
            long value = getMemoryIO().getInt(offset);
            return value < 0 ? (long)((value & 0x7FFFFFFFL) + 0x80000000L) : value;
        }
        
        /**
         * Sets the value for this field.
         * 
         * @param value the 32 bit unsigned value to set.
         */
        public final void set(long value) {
            getMemoryIO().putInt(offset, (int) value);
        }

        public void set(java.lang.Number value) {
            getMemoryIO().putInt(offset, value.intValue());
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
            super(64, Constants.LONG_ALIGN);
        }

        /**
         * Creates a new 64 bit signed integer field at a specific offset
         *
         * @param offset The offset within the memory area for this field.
         */
        public Signed64(Offset offset) {
            super(64, Constants.LONG_ALIGN, offset);
        }

        /**
         * Gets the value for this field.
         * 
         * @return a long.
         */
        public final long get() {
            return getMemoryIO().getLong(offset);
        }
        
        /**
         * Sets the value for this field.
         * 
         * @param value the 64 bit value to set.
         */
        public final void set(long value) {
            getMemoryIO().putLong(offset, value);
        }

        public void set(java.lang.Number value) {
            getMemoryIO().putLong(offset, value.longValue());
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
            super(64, Constants.LONG_ALIGN);
        }
        
        /**
         * Creates a new 64 bit unsigned integer field at a specific offset
         *
         * @param offset The offset within the memory area for this field.
         */
        public Unsigned64(Offset offset) {
            super(64, Constants.LONG_ALIGN, offset);
        }
        
        /**
         * Gets the value for this field.
         * 
         * @return a long.
         */
        public final long get() {
            return getMemoryIO().getLong(offset);
        }
        
        /**
         * Sets the value for this field.
         * 
         * @param value the 64 bit value to set.
         */
        public final void set(long value) {
            getMemoryIO().putLong(offset, value);
        }

        public void set(java.lang.Number value) {
            getMemoryIO().putLong(offset, value.longValue());
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
            super(Constants.LONG_SIZE, Constants.LONG_ALIGN);
        }

        /**
         * Creates a new signed native long field at a specific offset
         *
         * @param offset The offset within the memory area for this field.
         */
        public SignedLong(Offset offset) {
            super(Constants.LONG_SIZE, Constants.LONG_ALIGN, offset);
        }

        /**
         * Gets the value for this field.
         * 
         * @return a long.
         */
        public final long get() {
            return getMemoryIO().getNativeLong(offset);
        }
        
        /**
         * Sets the value for this field.
         * 
         * @param value the 32/64 bit value to set.
         */
        public final void set(long value) {
            getMemoryIO().putNativeLong(offset, value);
        }

        public void set(java.lang.Number value) {
            getMemoryIO().putNativeLong(offset, value.longValue());
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
            super(Constants.LONG_SIZE, Constants.LONG_ALIGN);
        }

        /**
         * Creates a new unsigned native long field at a specific offset
         *
         * @param offset The offset within the memory area for this field.
         */
        public UnsignedLong(Offset offset) {
            super(Constants.LONG_SIZE, Constants.LONG_ALIGN, offset);
        }

        /**
         * Gets the value for this field.
         * 
         * @return a int.
         */
        public final long get() {
            long value = getMemoryIO().getNativeLong(offset);
            return value < 0 
                    ? (long) ((value & Constants.LONG_MASK) + Constants.LONG_MASK + 1) 
                    : value;
        }
        
        /**
         * Sets the value for this field.
         * 
         * @param value the 32/64 bit value to set.
         */
        public final void set(long value) {
            getMemoryIO().putNativeLong(offset, value);
        }

        public void set(java.lang.Number value) {
            getMemoryIO().putNativeLong(offset, value.longValue());
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
            super(java.lang.Float.SIZE, Constants.FLOAT_ALIGN);
        }
        /**
         * Creates a new float field at a specific offset
         *
         * @param offset The offset within the memory area for this field.
         */
        public Float(Offset offset) {
            super(java.lang.Float.SIZE, Constants.FLOAT_ALIGN, offset);
        }
        
        public final float get() {
            return getMemoryIO().getFloat(offset);
        }
        public final void set(float value) {
            getMemoryIO().putFloat(offset, value);
        }
        public void set(java.lang.Number value) {
            getMemoryIO().putFloat(offset, value.floatValue());
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
            super(java.lang.Double.SIZE, Constants.DOUBLE_ALIGN);
        }
        public Double(Offset offset) {
            super(java.lang.Double.SIZE, Constants.DOUBLE_ALIGN, offset);
        }
        public final double get() {
            return getMemoryIO().getDouble(offset);
        }
        public final void set(double value) {
            getMemoryIO().putDouble(offset, value);
        }
        public void set(java.lang.Number value) {
            getMemoryIO().putDouble(offset, value.doubleValue());
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
            super(Constants.ADDRESS_SIZE, Constants.ADDRESS_ALIGN);
        }
        public Address(Offset offset) {
            super(Constants.ADDRESS_SIZE, Constants.ADDRESS_ALIGN, offset);
        }
        
        /**
         * Gets the {@link com.googlecode.jffi.Address} value from the native memory.
         * 
         * @return a {@link com.googlecode.jffi.Address}.
         */
        public final com.kenai.jaffl.Address get() {
            long value = getMemoryIO().getAddress(offset);
            return value != 0 ? new com.kenai.jaffl.Address(value) : null;
        }
        
        /**
         * Puts a {@link jafl.Address} value into the native memory.
         */
        public final void set(com.kenai.jaffl.Address value) {
            getMemoryIO().putAddress(offset, value != null ? value.nativeAddress() : 0);
        }

        public void set(java.lang.Number value) {
            getMemoryIO().putAddress(offset, value.longValue());
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
            super(Constants.ADDRESS_SIZE, Constants.ADDRESS_ALIGN);
        }
        public Pointer(Offset offset) {
            super(Constants.ADDRESS_SIZE, Constants.ADDRESS_ALIGN, offset);
        }

        /**
         * Gets the {@link com.googlecode.jffi.Address} value from the native memory.
         * 
         * @return a {@link com.googlecode.jffi.Address}.
         */
        public final com.kenai.jaffl.Pointer get() {
            return getMemoryIO().getPointer(offset);
        }
        
        /**
         * Gets the size of a Pointer in bits
         * 
         * @return the size of the Pointer
         */
        public final int size() {
            return com.kenai.jaffl.Address.SIZE;
        }
        
        /**
         * Puts a {@link com.googlecode.jffi.Address} value into the native memory.
         */
        public final void set(com.kenai.jaffl.Pointer value) {
            getMemoryIO().putPointer(offset, value);
        }

        public void set(java.lang.Number value) {
            getMemoryIO().putAddress(offset, value.longValue());
        }
        /**
         * Returns an integer representation of this <code>Pointer</code>.
         * 
         * @return an integer value for this <code>Pointer</code>.
         */
        @Override
        public final int intValue() {
            return (int) getMemoryIO().getAddress(offset);
        }
        
        /**
         * Returns an {@code long} representation of this <code>Pointer</code>.
         * 
         * @return an {@code long} value for this <code>Pointer</code>.
         */
        @Override
        public final long longValue() {
            return getMemoryIO().getAddress(offset);
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
         * @param size the size of the native integer.
         * @param enumClass the Enum class.
         */
        public EnumField(int size, Class<E> enumClass) {
            this(size, size, enumClass);
        }
        
        /**
         * Constructs a new Enum field.
         *
         * @param size the size of the native integer.
         * * @param offset the offset from the start of the struct memory area.
         * @param enumClass the Enum class.
         */
        public EnumField(int size, Offset offset, Class<E> enumClass) {
            this(size, size, offset, enumClass);
        }
        
        /**
         * Constructs a new Enum field.
         * @param size the size of the native integer.
         * @param align the minimum alignment of the native integer
         * @param enumClass the Enum class.
         */
        public EnumField(int size, int align, Class<E> enumClass) {
            super(size, align);
            this.enumClass = enumClass;
        }
        /**
         * Constructs a new Enum field.
         * @param size the size of the native integer.
         * @param align the minimum alignment of the native integer
         * @param offset the offset from the start of the struct memory area
         * @param enumClass the Enum class.
         */
        public EnumField(int size, int align, Offset offset, Class<E> enumClass) {
            super(size, align, offset);
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
            super(8, enumClass);
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
            getMemoryIO().putByte(offset, (byte) EnumMapper.getInstance().intValue(value));
        }

        public void set(java.lang.Number value) {
            getMemoryIO().putByte(offset, value.byteValue());
        }
        /**
         * Returns an integer representation of this enum field.
         * 
         * @return an integer value for this enum field.
         */
        @Override
        public final int intValue() {
            return getMemoryIO().getInt(offset);
        }
    }
    public class Enum16<E extends java.lang.Enum<E>> extends EnumField<E> {
        public Enum16(Class<E> enumClass) {
            super(16, enumClass);
        }
        public final E get() {
            return EnumMapper.getInstance().valueOf(intValue(), enumClass);
        }
        public final void set(E value) {
            getMemoryIO().putShort(offset, (short) EnumMapper.getInstance().intValue(value));
        }
        public void set(java.lang.Number value) {
            getMemoryIO().putShort(offset, value.shortValue());
        }
        @Override
        public final int intValue() {
            return getMemoryIO().getShort(offset);
        }
    }
    public class Enum32<E extends java.lang.Enum<E>> extends EnumField<E> {
        public Enum32(Class<E> enumClass) {
            super(32, enumClass);
        }
        public final E get() {
            return EnumMapper.getInstance().valueOf(intValue(), enumClass);
        }
        public final void set(E value) {
            getMemoryIO().putInt(offset, EnumMapper.getInstance().intValue(value));
        }
        public void set(java.lang.Number value) {
            getMemoryIO().putInt(offset, value.intValue());
        }
        @Override
        public final int intValue() {
            return getMemoryIO().getInt(offset);
        }
    }
    
    public class Enum64<E extends java.lang.Enum<E>> extends EnumField<E> {
        public Enum64(Class<E> enumClass) {
            super(64, Constants.LONG_ALIGN, enumClass);
        }
        public final E get() {
            return EnumMapper.getInstance().valueOf(intValue(), enumClass);
        }
        public final void set(E value) {
            getMemoryIO().putLong(offset, EnumMapper.getInstance().intValue(value));
        }
        public void set(java.lang.Number value) {
            getMemoryIO().putLong(offset, value.longValue());
        }
        @Override
        public final int intValue() {
            return (int) longValue();
        }
        @Override
        public final long longValue() {
            return getMemoryIO().getLong(offset);
        }
    }
    public class EnumLong<E extends java.lang.Enum<E>> extends EnumField<E> {
        public EnumLong(Class<E> enumClass) {
            super(Constants.LONG_SIZE, Constants.LONG_ALIGN, enumClass);
        }
        
        public final E get() {
            return EnumMapper.getInstance().valueOf(intValue(), enumClass);
        }
        public final void set(E value) {
            getMemoryIO().putNativeLong(offset, EnumMapper.getInstance().intValue(value));
        }
        public void set(java.lang.Number value) {
            getMemoryIO().putNativeLong(offset, value.longValue());
        }

        @Override
        public final int intValue() {
            return (int) longValue();
        }

        @Override
        public final long longValue() {
            return getMemoryIO().getNativeLong(offset);
        }
    }
    
    public class Enum<T extends java.lang.Enum<T>> extends Enum32<T> {
        public Enum(Class<T> enumClass) {
            super(enumClass);
        }
    }
    
    abstract public class String extends AbstractMember {
        private final Charset charset;
        private final int length;
        
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
        protected abstract MemoryIO getStringMemory();
        public final java.lang.String get() {
            return getStringMemory().getString(0, length, charset);
        }
        public final void set(java.lang.String value) {
            getStringMemory().putString(0, value, length, charset);
        }
        @Override
        public final java.lang.String toString() {
            return get();
        }
    }
    public class UTFString extends String {
        public UTFString(int length, Charset cs) {
            super(length * 8, 8, length, cs); // FIXME: This won't work for non-ASCII
         
        }
        protected MemoryIO getStringMemory() {
            return getMemoryIO().slice(offset, length());
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
        public UTFStringRef(int length, Charset cs) {
            super(Constants.ADDRESS_SIZE, Constants.ADDRESS_SIZE, length, cs);
        }
        public UTFStringRef(Charset cs) {
            this(Integer.MAX_VALUE, cs);
        }
        protected MemoryIO getStringMemory() {
            return getMemoryIO().getMemoryIO(offset, length());
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
    /*
    public final Marshaller.Session marshal(Marshaller marshaller, MarshalContext context) {
        return __info.marshal(marshaller, context);
    }
    */
}
