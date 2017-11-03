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
/*
 * Some of the design and code of this class is from the javolution project.
 *
 * Copyright (C) 2006 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */

package jnr.ffi;

import jnr.ffi.provider.ParameterFlags;
import jnr.ffi.provider.jffi.ArrayMemoryIO;
import jnr.ffi.util.EnumMapper;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.nio.charset.Charset;

/**
 * Representation of C structures in java.
 *
 * <b>Note:</b> This class is not threadsafe.
 */
public abstract class Struct {
    static final Charset ASCII = Charset.forName("ASCII");
    static final Charset UTF8 = Charset.forName("UTF-8");

    static final class Info {
        private final Runtime runtime;
        private jnr.ffi.Pointer memory = null;
        Struct enclosing = null;
        int offset = 0; // offset within enclosing Struct

        int size = 0;
        int minAlign = 1;
        boolean isUnion = false;
        boolean resetIndex = false;

        Alignment alignment = new Alignment(0);

        public Info(Runtime runtime) {
            this.runtime = runtime;
        }

        public final int getOffset() {
            return enclosing == null ? 0 : offset + enclosing.__info.getOffset();
        }

        public final jnr.ffi.Pointer getMemory(int flags) {
            return enclosing != null ? enclosing.__info.getMemory(flags) : memory != null ? memory : (memory = allocateMemory(flags));
        }

        public final jnr.ffi.Pointer getMemory() {
            return getMemory(ParameterFlags.TRANSIENT);
        }

        final boolean isDirect() {
            return (enclosing != null && enclosing.__info.isDirect()) || (memory != null && memory.isDirect());
        }

        final int size() {
            return this.alignment.intValue() > 0 ? size + ((-this.size) & (this.minAlign - 1)) : size;
        }

        final int getMinimumAlignment() {
            return minAlign;
        }

        private jnr.ffi.Pointer allocateMemory(int flags) {
            if (ParameterFlags.isDirect(flags)) {
                return runtime.getMemoryManager().allocateDirect(size(), true);
            } else {
                return runtime.getMemoryManager().allocate(size());
            }
        }

        public final void useMemory(jnr.ffi.Pointer io) {
            this.memory = io;
        }

        protected final int addField(int sizeBits, int alignBits, Offset offset) {
            this.size = Math.max(this.size, offset.intValue() + (sizeBits >> 3));
            this.minAlign = Math.max(this.minAlign, alignBits >> 3);
            return offset.intValue();
        }

        protected final int addField(int sizeBits, int alignBits) {
            final int alignment = this.alignment.intValue() > 0 ? Math.min(this.alignment.intValue(), (alignBits >> 3)) : (alignBits >> 3);
            final int offset = resetIndex ? 0 : align(this.size, alignment);
            this.size = Math.max(this.size, offset + (sizeBits >> 3));
            this.minAlign = Math.max(this.minAlign, alignment);
            return offset;
        }
    }
    final Info __info;

    /**
     * Creates a new {@code Struct}.
     *
     * @param runtime The current runtime.
     */
    protected Struct(Runtime runtime) {
        this.__info = new Info(runtime);
    }

    protected Struct(Runtime runtime, Alignment alignment) {
        this(runtime);
        __info.alignment = alignment;
    }

    protected Struct(Runtime runtime, Struct enclosing) {
        this(runtime);
        __info.alignment = enclosing.__info.alignment;
    }

    /**
     * Creates a new <tt>Struct</tt>.
     *
     * @param isUnion if this Struct is a Union
     */
    protected Struct(Runtime runtime, final boolean isUnion) {
        this(runtime);
        __info.resetIndex = isUnion;
        __info.isUnion = isUnion;
    }

    public final Runtime getRuntime() {
        return __info.runtime;
    }

    /**
     * Uses the specified memory address as the backing store for this structure.
     *
     * @param address the native memory area.
     */
    public final void useMemory(jnr.ffi.Pointer address) {
        __info.useMemory(address);
    }

    public static jnr.ffi.Pointer getMemory(Struct struct) {
        return struct.__info.getMemory(0);
    }

    public static jnr.ffi.Pointer getMemory(Struct struct, int flags) {
        return struct.__info.getMemory(flags);
    }

    public static int size(Struct struct) {
        return struct.__info.size();
    }

    public static int alignment(Struct struct) {
        return struct.__info.getMinimumAlignment();
    }

    public static boolean isDirect(Struct struct) {
        return struct.__info.isDirect();
    }

    private static int align(int offset, int align) {
        return (offset + align - 1) & ~(align - 1);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Struct> T[] arrayOf(Runtime runtime, Class<T> type, int length) {
        try {
            T[] array = (T[]) Array.newInstance(type, length);
            Constructor<T> c = type.getConstructor(Runtime.class);
            for (int i = 0; i < length; ++i) {
                array[i] = c.newInstance(runtime);
            }

            if (array.length > 0) {
                final int structSize = align(Struct.size(array[0]), Struct.alignment(array[0]));

                jnr.ffi.Pointer memory = runtime.getMemoryManager().allocateDirect(structSize * length);
                for (int i = 0; i < array.length; ++i) {
                    array[i].useMemory(memory.slice(structSize * i, structSize));
                }
            }

            return array;
        } catch (RuntimeException ex) {
            throw ex;

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }


    /**
     * Returns a human readable {@link java.lang.String} representation of the structure.
     *
     * @return a {@code String} representation of this structure.
     */
    @Override
    public java.lang.String toString() {
        StringBuilder sb = new StringBuilder();
        java.lang.reflect.Field[] fields = getClass().getDeclaredFields();
        sb.append(getClass().getSimpleName()).append(" { \n");
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

    public static final class Alignment extends Number {
        private final int alignment;

        public Alignment(int alignment) {
            this.alignment = alignment;
        }

        @Override
        public int intValue() {
            return alignment;
        }

        @Override
        public long longValue() {
            return alignment;
        }

        @Override
        public float floatValue() {
            return alignment;
        }

        @Override
        public double doubleValue() {
            return alignment;
        }
    }

    /**
     * Interface all Struct members must implement.
     */
    protected abstract class Member {
        /**
         * Gets the {@code Struct} this {@code Member} is a member of.
         *
         * @return a {@code Struct}.
         */
        abstract Struct struct();

        /**
         * Gets the memory object used to store this {@code Member}
         *
         * @return a {@code Pointer}
         */
        abstract jnr.ffi.Pointer getMemory();

        /**
         * Gets the offset within the structure for this field.
         *
         * @return the offset within the structure for this field.
         */
        abstract long offset();
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
     * Creates an array of <tt>Enum8</tt> instances.
     *
     * @param array     the array to store the instances in
     * @param enumClass class of <tt>java.lang.Enum</tt>, these <tt>Enum8</tt> instances will represent
     * @param <T>       The type of the <tt>java.lang.Enum</tt>
     * @return the array that was passed in
     */
    protected <T extends java.lang.Enum<T>> Enum8<T>[] array(Enum8<T>[] array, Class<T> enumClass) {
        arrayBegin();
        for (int i = 0; i < array.length; i++) {
            array[i] = new Enum8<T>(enumClass);
        }
        arrayEnd();
        return array;
    }

    /**
     * Creates an array of <tt>Enum16</tt> instances.
     *
     * @param array     the array to store the instances in
     * @param enumClass class of <tt>java.lang.Enum</tt>, these <tt>Enum16</tt> instances will represent
     * @param <T>       The type of the <tt>java.lang.Enum</tt>
     * @return the array that was passed in
     */
    protected <T extends java.lang.Enum<T>> Enum16<T>[] array(Enum16<T>[] array, Class<T> enumClass) {
        arrayBegin();
        for (int i = 0; i < array.length; i++) {
            array[i] = new Enum16<T>(enumClass);
        }
        arrayEnd();
        return array;
    }

    /**
     * Creates an array of <tt>Enum32</tt> instances.
     *
     * @param array     the array to store the instances in
     * @param enumClass class of <tt>java.lang.Enum</tt>, these <tt>Enum32</tt> instances will represent
     * @param <T>       The type of the <tt>java.lang.Enum</tt>
     * @return the array that was passed in
     */
    protected <T extends java.lang.Enum<T>> Enum32<T>[] array(Enum32<T>[] array, Class<T> enumClass) {
        arrayBegin();
        for (int i = 0; i < array.length; i++) {
            array[i] = new Enum32<T>(enumClass);
        }
        arrayEnd();
        return array;
    }

    /**
     * Creates an array of <tt>Enum64</tt> instances.
     *
     * @param array     the array to store the instances in
     * @param enumClass class of <tt>java.lang.Enum</tt>, these <tt>Enum64</tt> instances will represent
     * @param <T>       The type of the <tt>java.lang.Enum</tt>
     * @return the array that was passed in
     */
    protected <T extends java.lang.Enum<T>> Enum64<T>[] array(Enum64<T>[] array, Class<T> enumClass) {
        arrayBegin();
        for (int i = 0; i < array.length; i++) {
            array[i] = new Enum64<T>(enumClass);
        }
        arrayEnd();
        return array;
    }

    /**
     * Creates an array of <tt>Enum</tt> instances.
     *
     * @param array     the array to store the instances in
     * @param enumClass class of <tt>java.lang.Enum</tt>, these <tt>Enum</tt> instances will represent
     * @param <T>       The type of the <tt>java.lang.Enum</tt>
     * @return the array that was passed in
     */
    protected <T extends java.lang.Enum<T>> Enum<T>[] array(Enum<T>[] array, Class<T> enumClass) {
        arrayBegin();
        for (int i = 0; i < array.length; i++) {
            array[i] = new Enum<T>(enumClass);
        }
        arrayEnd();
        return array;
    }

    /**
     * Creates an array of <tt>Struct</tt> instances.
     *
     * @param array the array to store the instances in
     * @param <T> the type of Struct
     * @return the array that was passed in
     */
    protected <T extends Struct> T[] array(T[] array) {
        arrayBegin();
        try {
            Class<?> type = array.getClass().getComponentType();
            Constructor c = type.getConstructor(Runtime.class);

            for (int i = 0; i < array.length; i++) {
                array[i] = inner((T) c.newInstance(getRuntime()));
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
     * Creates an array of <tt>UTF8String</tt> instances.
     *
     * @param array        the array to store the instances in
     * @param stringLength length of each string in array
     * @return the array that was passed in
     */
    protected UTF8String[] array(UTF8String[] array, int stringLength) {
        arrayBegin();
        for (int i = 0; i < array.length; i++) {
            array[i] = new UTF8String(stringLength);
        }
        arrayEnd();
        return array;
    }

    protected final <T extends Struct> T inner(T struct) {
        int alignment = __info.alignment.intValue() > 0 ? Math.min(__info.alignment.intValue(), struct.__info.getMinimumAlignment()) : struct.__info.getMinimumAlignment();
        int offset = __info.resetIndex ? 0 : align(__info.size, alignment);
        struct.__info.enclosing = this;
        struct.__info.offset = offset;
        __info.size = Math.max(__info.size, offset + struct.__info.size);
        return struct;
    }

    /**
     * Base implementation of Member
     */
    protected abstract class AbstractMember extends Member {
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

        protected AbstractMember(NativeType type) {
            final Type t = getRuntime().findType(type);
            this.offset = __info.addField(t.size() * 8, t.alignment() * 8);
        }

        protected AbstractMember(NativeType type, Offset offset) {
            final Type t = getRuntime().findType(type);
            this.offset = __info.addField(t.size() * 8, t.alignment() * 8, offset);
        }

        public final jnr.ffi.Pointer getMemory() {
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
            return offset + __info.getOffset();
        }
    }

    /**
     * Base class for Boolean fields
     */
    protected abstract class AbstractBoolean extends AbstractMember {
        protected AbstractBoolean(NativeType type) {
            super(type);
        }

        protected AbstractBoolean(NativeType type, Offset offset) {
            super(type, offset);
        }

        /**
         * Gets the value for this field.
         *
         * @return a boolean.
         */
        public abstract boolean get();

        /**
         * Sets the field to a new value.
         *
         * @param value The new value.
         */
        public abstract void set(boolean value);

        /**
         * Returns a string representation of this <code>Address</code>.
         *
         * @return a string representation of this <code>Address</code>.
         */
        @Override
        public java.lang.String toString() {
            return java.lang.Boolean.toString(get());
        }
    }

    /**
     * A normal C boolean - 1 byte in size
     */
    public final class Boolean extends AbstractBoolean {
        public Boolean() {
            super(NativeType.SCHAR);
        }

        public final boolean get() {
            return getMemory().getByte(offset()) != 0;
        }

        public final void set(boolean value) {
            getMemory().putByte(offset(), (byte) (value ? 1 : 0));
        }
    }

    /**
     * A Windows BOOL - 4 bytes
     */
    public final class WBOOL extends AbstractBoolean {
        public WBOOL() {
            super(NativeType.SINT);
        }

        public final boolean get() {
            return getMemory().getInt(offset()) != 0;
        }

        public final void set(boolean value) {
            getMemory().putInt(offset(), value ? 1 : 0);
        }
    }

  public final class BOOL16 extends AbstractBoolean {
    public BOOL16() {
      super(NativeType.SSHORT);
    }

    public final boolean get() {
      return getMemory().getShort(offset()) != 0;
    }

    public final void set(boolean value) {
      getMemory().putShort(offset(), (short) (value ? 1 : 0));
    }
  }

  public final class BYTE extends Unsigned8 {
    public BYTE() {
    }

    public BYTE(Offset offset) {
      super(offset);
    }
  }

  public final class WORD extends Unsigned16 {
    public WORD() {
    }

    public WORD(Offset offset) {
      super(offset);
    }
  }

  public final class DWORD extends Unsigned32 {
    public DWORD() {
    }

    public DWORD(Offset offset) {
      super(offset);
    }
  }

  public final class LONG extends Signed32 {
    public LONG() {
    }

    public LONG(Offset offset) {
      super(offset);
    }
  }

    /**
     * Base class for all Number structure fields.
     */
    public abstract class NumberField extends Member {
        /**
         * Offset from the start of the <tt>Struct</tt> memory this field is located at.
         */
        private final int offset;
        protected final Type type;

        protected NumberField(NativeType type) {
            Type t = this.type = getRuntime().findType(type);
            this.offset = __info.addField(t.size() * 8, t.alignment() * 8);
        }

        protected NumberField(NativeType type, Offset offset) {
            Type t = this.type = getRuntime().findType(type);
            this.offset = __info.addField(t.size() * 8, t.alignment() * 8, offset);
        }

        protected NumberField(TypeAlias type) {
            Type t = this.type = getRuntime().findType(type);
            this.offset = __info.addField(t.size() * 8, t.alignment() * 8);
        }

        protected NumberField(TypeAlias type, Offset offset) {
            Type t = this.type = getRuntime().findType(type);
            this.offset = __info.addField(t.size() * 8, t.alignment() * 8, offset);
        }


        public final jnr.ffi.Pointer getMemory() {
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
            return offset + __info.getOffset();
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
         * Returns a string representation of this <code>Number</code>.
         *
         * @return a string representation of this <code>Number</code>.
         */
        @Override
        public java.lang.String toString() {
            return java.lang.Integer.toString(intValue(), 10);
        }
    }

    public abstract class IntegerAlias extends NumberField {
        IntegerAlias(TypeAlias type) {
            super(type);
        }

        IntegerAlias(TypeAlias type, Offset offset) {
            super(type, offset);
        }

        @Override
        public void set(Number value) {
            getMemory().putInt(type, offset(), value.longValue());
        }

        public void set(long value) {
            getMemory().putInt(type, offset(), value);
        }

        /**
         * Gets the value for this field.
         *
         * @return a long.
         */
        public final long get() {
            return getMemory().getInt(type, offset());
        }


        @Override
        public int intValue() {
            return (int) get();
        }

        @Override
        public long longValue() {
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
            super(NativeType.UINT, offset);
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
            return getMemory().getLongLong(offset());
        }

        /**
         * Sets the value for this field.
         *
         * @param value the 64 bit value to set.
         */
        public final void set(long value) {
            getMemory().putLongLong(offset(), value);
        }

        public void set(java.lang.Number value) {
            getMemory().putLongLong(offset(), value.longValue());
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
            return getMemory().getLongLong(offset());
        }

        /**
         * Sets the value for this field.
         *
         * @param value the 64 bit value to set.
         */
        public final void set(long value) {
            getMemory().putLongLong(offset(), value);
        }

        public void set(java.lang.Number value) {
            getMemory().putLongLong(offset(), value.longValue());
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
            final long mask = getRuntime().findType(NativeType.SLONG).size() == 4 ? 0xffffffffL : 0xffffffffffffffffL;
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
         * Reads an {@code Address} value from the struct.
         *
         * @return a {@link jnr.ffi.Address}.
         */
        public final jnr.ffi.Address get() {
            return jnr.ffi.Address.valueOf(getMemory().getAddress(offset()));
        }

        /**
         * Puts a {@link jnr.ffi.Address} value into the native memory.
         *
         * @param value the value to write.
         */
        public final void set(jnr.ffi.Address value) {
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


    public abstract class PointerField extends NumberField {
        /**
         * Creates a new <tt>Address</tt> field.
         */
        public PointerField() {
            super(NativeType.ADDRESS);
        }

        public PointerField(Offset offset) {
            super(NativeType.ADDRESS, offset);
        }

        /**
         * Gets the {@link jnr.ffi.Pointer} value from the native memory.
         *
         * @return a {@link jnr.ffi.Pointer}.
         */
        protected final jnr.ffi.Pointer getPointer() {
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
         * Puts a {@link jnr.ffi.Address} value into the native memory.
         *
         * @param value the value to write.
         */
        public final void set(jnr.ffi.Pointer value) {
            jnr.ffi.Pointer finalPointer = value;
            if (value instanceof ArrayMemoryIO) {
                ArrayMemoryIO arrayMemory = (ArrayMemoryIO) value;
                byte[] valueArray = arrayMemory.array();
                finalPointer = Memory.allocateDirect(getRuntime(), valueArray.length);
                finalPointer.put(0, valueArray, 0, valueArray.length);
            }
            getMemory().putPointer(offset(), finalPointer);
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
        public int intValue() {
            return (int) getMemory().getAddress(offset());
        }

        /**
         * Returns an {@code long} representation of this <code>Pointer</code>.
         *
         * @return an {@code long} value for this <code>Pointer</code>.
         */
        @Override
        public long longValue() {
            return getMemory().getAddress(offset());
        }

        /**
         * Returns a string representation of this <code>Pointer</code>.
         *
         * @return a string representation of this <code>Pointer</code>.
         */
        @Override
        public java.lang.String toString() {
            return getPointer().toString();
        }
    }

    /**
     * Represents a native memory address.
     */
    public class Pointer extends PointerField {
        /**
         * Creates a new <tt>Address</tt> field.
         */
        public Pointer() {
        }

        public Pointer(Offset offset) {
            super(offset);
        }

        /**
         * Gets the {@link jnr.ffi.Pointer} value from the native memory.
         *
         * @return a {@link jnr.ffi.Pointer}.
         */
        public final jnr.ffi.Pointer get() {
            return getPointer();
        }

        /**
         * Returns an integer representation of this <code>Pointer</code>.
         *
         * @return an integer value for this <code>Pointer</code>.
         */
        @Override
        public final int intValue() {
            return super.intValue();
        }

        /**
         * Returns an {@code long} representation of this <code>Pointer</code>.
         *
         * @return an {@code long} value for this <code>Pointer</code>.
         */
        @Override
        public final long longValue() {
            return super.longValue();
        }

        /**
         * Returns a string representation of this <code>Pointer</code>.
         *
         * @return a string representation of this <code>Pointer</code>.
         */
        @Override
        public final java.lang.String toString() {
            return super.toString();
        }
    }

    /**
     * Represents a reference to a Struct or and array of Structs
     * @param <T> - Struct type
     */
    public class StructRef<T extends Struct> extends PointerField {
        private final Constructor<T> structConstructor;
        private final Class<T> structType;
        private final int size;

        public StructRef(Class<T> structType) {
            this.structType = structType;
            try {
                structConstructor = structType.getDeclaredConstructor(Runtime.class);
                size = Struct.size(structConstructor.newInstance(getRuntime()));
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        /**
         * @param structType         Struct type
         * @param initialStructCount the number of struct instances. Use this to allocate memory without setting value.
         */
        public StructRef(Class<T> structType, int initialStructCount) {
            this(structType);
            set(Memory.allocateDirect(getRuntime(), size * initialStructCount));
        }

        public StructRef(Offset offset, Class<T> structType) {
            super(offset);
            this.structType = structType;
            try {
                structConstructor = structType.getDeclaredConstructor(Runtime.class);
                size = Struct.size(structConstructor.newInstance(getRuntime()));
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        /**
         * @param offset             offset of the field
         * @param structType         Struct type
         * @param initialStructCount the number of struct instances. Use this to allocate memory without setting value.
         */
        public StructRef(Offset offset, Class<T> structType, int initialStructCount) {
            this(offset, structType);
            set(Memory.allocateDirect(getRuntime(), size * initialStructCount));
        }

        public final void set(T struct) {
            jnr.ffi.Pointer structMemory = Struct.getMemory(struct);
            set(structMemory);
        }

        public final void set(T[] structs) {
            if (structs.length == 0) {
                set(Memory.allocateDirect(getRuntime(), 0));
                return;
            }
            jnr.ffi.Pointer value = Memory.allocateDirect(getRuntime(), size * structs.length);
            byte[] data = new byte[size];
            for (int i = 0; i < structs.length; i++) {
                Struct.getMemory(structs[i]).get(0L, data, 0, size);
                value.put(size * i, data, 0, size);
            }
            set(value);
        }

        /**
         * @return struct from memory
         */
        public final T get() {
            T struct;
            try {
                struct = structConstructor.newInstance(getRuntime());
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            struct.useMemory(getPointer());
            return struct;
        }

        /**
         * @return struct from memory
         */
        public final T[] get(int length) {
            try {
                T[] array = (T[]) Array.newInstance(structType, length);
                for (int i = 0; i < length; ++i) {
                    array[i] = structConstructor.newInstance(getRuntime());
                    array[i].useMemory(getPointer().slice(Struct.size(array[i]) * i));
                }
                return array;
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        @Override
        public java.lang.String toString() {
            return "struct @ " + super.toString()
                    + '\n' + get();
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
            return enumClass.cast(EnumMapper.getInstance(enumClass).valueOf(intValue()));
        }

        /**
         * Sets the native integer value using a java Enum value.
         *
         * @param value the java <tt>Enum</tt> value.
         */
        public final void set(E value) {
            getMemory().putByte(offset(), (byte) EnumMapper.getInstance(enumClass).intValue(value));
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
            return enumClass.cast(EnumMapper.getInstance(enumClass).valueOf(intValue()));
        }
        public final void set(E value) {
            getMemory().putShort(offset(), (short) EnumMapper.getInstance(enumClass).intValue(value));
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
            return enumClass.cast(EnumMapper.getInstance(enumClass).valueOf(intValue()));
        }
        public final void set(E value) {
            getMemory().putInt(offset(), EnumMapper.getInstance(enumClass).intValue(value));
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
            return enumClass.cast(EnumMapper.getInstance(enumClass).valueOf(intValue()));
        }
        public final void set(E value) {
            getMemory().putLongLong(offset(), EnumMapper.getInstance(enumClass).intValue(value));
        }
        public void set(java.lang.Number value) {
            getMemory().putLongLong(offset(), value.longValue());
        }
        @Override
        public final int intValue() {
            return (int) longValue();
        }
        @Override
        public final long longValue() {
            return getMemory().getLongLong(offset());
        }
    }

    public class EnumLong<E extends java.lang.Enum<E>> extends EnumField<E> {
        public EnumLong(Class<E> enumClass) {
            super(NativeType.SLONG, enumClass);
        }

        public final E get() {
            return enumClass.cast(EnumMapper.getInstance(enumClass).valueOf(intValue()));
        }
        public final void set(E value) {
            getMemory().putNativeLong(offset(), EnumMapper.getInstance(enumClass).intValue(value));
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

        protected abstract jnr.ffi.Pointer getStringMemory();
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
        protected jnr.ffi.Pointer getStringMemory() {
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
            super(size, UTF8);
        }
    }

    public class AsciiString extends UTFString {
        public AsciiString(int size) {
            super(size, ASCII);
        }
    }

    public class UTFStringRef extends String {
        private jnr.ffi.Pointer valueHolder;

        public UTFStringRef(int length, Charset cs) {
            super(getRuntime().findType(NativeType.ADDRESS).size() * 8, getRuntime().findType(NativeType.ADDRESS).alignment() * 8,
                    length, cs);
        }

        public UTFStringRef(Charset cs) {
            this(Integer.MAX_VALUE, cs);
        }

        protected jnr.ffi.Pointer getStringMemory() {
            return getMemory().getPointer(offset(), length());
        }

        public final java.lang.String get() {
            jnr.ffi.Pointer ptr = getStringMemory();
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
            super(size, UTF8);
        }
        public UTF8StringRef() {
            super(Integer.MAX_VALUE, UTF8);
        }
    }

    public class AsciiStringRef extends UTFStringRef {
        public AsciiStringRef(int size) {
            super(size, ASCII);
        }
        public AsciiStringRef() {
            super(Integer.MAX_VALUE, ASCII);
        }
    }

    /**
     * Specialized padding fields for structs.  Use this instead of arrays of other
     * members for more efficient struct construction.
     */
    protected final class Padding extends AbstractMember {
        public Padding(Type type, int length) {
            super(type.size() * 8 * length, type.alignment() * 8);
        }

        public Padding(NativeType type, int length) {
            super(getRuntime().findType(type).size() * 8 * length, getRuntime().findType(type).alignment() * 8);
        }
    }

    public final class Function<T> extends AbstractMember {
        private final Class<? extends T> closureClass;
        private T instance;

        public Function(Class<? extends T> closureClass) {
            super(NativeType.ADDRESS);
            this.closureClass = closureClass;
        }

        public final void set(T value) {
            getMemory().putPointer(offset(), getRuntime().getClosureManager().getClosurePointer(closureClass, instance = value));
        }
    }

    protected final <T> Function<T> function(Class<T> closureClass) {
        return new Function<T>(closureClass);
    }

    public final class int8_t extends IntegerAlias {
        public int8_t() { super(TypeAlias.int8_t); }
        public int8_t(Offset offset) { super(TypeAlias.int8_t, offset); }
    }

    public final class u_int8_t extends IntegerAlias {
        public u_int8_t() { super(TypeAlias.u_int8_t); }
        public u_int8_t(Offset offset) { super(TypeAlias.u_int8_t, offset); }
    }

    public final class int16_t extends IntegerAlias {
        public int16_t() { super(TypeAlias.int16_t); }
        public int16_t(Offset offset) { super(TypeAlias.int16_t, offset); }
    }

    public final class u_int16_t extends IntegerAlias {
        public u_int16_t() { super(TypeAlias.u_int16_t); }
        public u_int16_t(Offset offset) { super(TypeAlias.u_int16_t, offset); }
    }

    public final class int32_t extends IntegerAlias {
        public int32_t() { super(TypeAlias.int32_t); }
        public int32_t(Offset offset) { super(TypeAlias.int32_t, offset); }
    }

    public final class u_int32_t extends IntegerAlias {
        public u_int32_t() { super(TypeAlias.u_int32_t); }
        public u_int32_t(Offset offset) { super(TypeAlias.u_int32_t, offset); }
    }

    public final class int64_t extends IntegerAlias {
        public int64_t() { super(TypeAlias.int64_t); }
        public int64_t(Offset offset) { super(TypeAlias.int64_t, offset); }
    }

    public final class u_int64_t extends IntegerAlias {
        public u_int64_t() { super(TypeAlias.u_int64_t); }
        public u_int64_t(Offset offset) { super(TypeAlias.u_int64_t, offset); }
    }

    public final class intptr_t extends IntegerAlias {
        public intptr_t() { super(TypeAlias.intptr_t); }
        public intptr_t(Offset offset) { super(TypeAlias.intptr_t, offset); }
    }

    public final class uintptr_t extends IntegerAlias {
        public uintptr_t() { super(TypeAlias.uintptr_t); }
        public uintptr_t(Offset offset) { super(TypeAlias.uintptr_t, offset); }
    }

    public final class caddr_t extends IntegerAlias {
        public caddr_t() { super(TypeAlias.caddr_t); }
        public caddr_t(Offset offset) { super(TypeAlias.caddr_t, offset); }
    }

    public final class dev_t extends IntegerAlias {
        public dev_t() { super(TypeAlias.dev_t); }
        public dev_t(Offset offset) { super(TypeAlias.dev_t, offset); }
    }

    public final class blkcnt_t extends IntegerAlias {
        public blkcnt_t() { super(TypeAlias.blkcnt_t); }
        public blkcnt_t(Offset offset) { super(TypeAlias.blkcnt_t, offset); }
    }

    public final class blksize_t extends IntegerAlias {
        public blksize_t() { super(TypeAlias.blksize_t); }
        public blksize_t(Offset offset) { super(TypeAlias.blksize_t, offset); }
    }

    public final class gid_t extends IntegerAlias {
        public gid_t() { super(TypeAlias.gid_t); }
        public gid_t(Offset offset) { super(TypeAlias.gid_t, offset); }
    }

    public final class in_addr_t extends IntegerAlias {
        public in_addr_t() { super(TypeAlias.in_addr_t); }
        public in_addr_t(Offset offset) { super(TypeAlias.in_addr_t, offset); }
    }

    public final class in_port_t extends IntegerAlias {
        public in_port_t() { super(TypeAlias.in_port_t); }
        public in_port_t(Offset offset) { super(TypeAlias.in_port_t, offset); }
    }

    public final class ino_t extends IntegerAlias {
        public ino_t() { super(TypeAlias.ino_t); }
        public ino_t(Offset offset) { super(TypeAlias.ino_t, offset); }
    }

    public final class ino64_t extends IntegerAlias {
        public ino64_t() { super(TypeAlias.ino64_t); }
        public ino64_t(Offset offset) { super(TypeAlias.ino64_t, offset); }
    }

    public final class key_t extends IntegerAlias {
        public key_t() { super(TypeAlias.key_t); }
        public key_t(Offset offset) { super(TypeAlias.key_t, offset); }
    }

    public final class mode_t extends IntegerAlias {
        public mode_t() { super(TypeAlias.mode_t); }
        public mode_t(Offset offset) { super(TypeAlias.mode_t, offset); }
    }

    public final class nlink_t extends IntegerAlias {
        public nlink_t() { super(TypeAlias.nlink_t); }
        public nlink_t(Offset offset) { super(TypeAlias.nlink_t, offset); }
    }

    public final class id_t extends IntegerAlias {
        public id_t() { super(TypeAlias.id_t); }
        public id_t(Offset offset) { super(TypeAlias.id_t, offset); }
    }

    public final class pid_t extends IntegerAlias {
        public pid_t() { super(TypeAlias.pid_t); }
        public pid_t(Offset offset) { super(TypeAlias.pid_t, offset); }
    }

    public final class off_t extends IntegerAlias {
        public off_t() { super(TypeAlias.off_t); }
        public off_t(Offset offset) { super(TypeAlias.off_t, offset); }
    }

    public final class swblk_t extends IntegerAlias {
        public swblk_t() { super(TypeAlias.swblk_t); }
        public swblk_t(Offset offset) { super(TypeAlias.swblk_t, offset); }
    }

    public final class uid_t extends IntegerAlias {
        public uid_t() { super(TypeAlias.uid_t); }
        public uid_t(Offset offset) { super(TypeAlias.uid_t, offset); }
    }

    public final class clock_t extends IntegerAlias {
        public clock_t() { super(TypeAlias.clock_t); }
        public clock_t(Offset offset) { super(TypeAlias.clock_t, offset); }
    }

    public final class size_t extends IntegerAlias {
        public size_t() { super(TypeAlias.size_t); }
        public size_t(Offset offset) { super(TypeAlias.size_t, offset); }
    }

    public final class ssize_t extends IntegerAlias {
        public ssize_t() { super(TypeAlias.ssize_t); }
        public ssize_t(Offset offset) { super(TypeAlias.ssize_t, offset); }
    }

    public final class time_t extends IntegerAlias {
        public time_t() { super(TypeAlias.time_t); }
        public time_t(Offset offset) { super(TypeAlias.time_t, offset); }
    }

    public final class fsblkcnt_t extends IntegerAlias {
        public fsblkcnt_t() { super(TypeAlias.fsblkcnt_t); }
        public fsblkcnt_t(Offset offset) { super(TypeAlias.fsblkcnt_t, offset); }
    }

    public final class fsfilcnt_t extends IntegerAlias {
        public fsfilcnt_t() { super(TypeAlias.fsfilcnt_t); }
        public fsfilcnt_t(Offset offset) { super(TypeAlias.fsfilcnt_t, offset); }
    }

    public final class sa_family_t extends IntegerAlias {
        public sa_family_t() { super(TypeAlias.sa_family_t); }
        public sa_family_t(Offset offset) { super(TypeAlias.sa_family_t, offset); }
    }

    public final class socklen_t extends IntegerAlias {
        public socklen_t() { super(TypeAlias.socklen_t); }
        public socklen_t(Offset offset) { super(TypeAlias.socklen_t, offset); }
    }

    public final class rlim_t extends IntegerAlias {
        public rlim_t() { super(TypeAlias.rlim_t); }
        public rlim_t(Offset offset) { super(TypeAlias.rlim_t, offset); }
    }
}
