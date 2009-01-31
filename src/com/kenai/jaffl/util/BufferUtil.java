
package com.kenai.jaffl.util;

import com.kenai.jaffl.Platform;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;

/**
 *
 */
public final class BufferUtil {
    private BufferUtil() {}
    
    public final static void putString(ByteBuffer buf, Charset charset, String value) {
        putCharSequence(buf, charset, value);
    }
    public final static String getString(ByteBuffer buf, Charset charset) {
        return getCharSequence(buf, charset).toString();
    }
    public final static void putCharSequence(ByteBuffer buf, Charset charset, CharSequence value) {
        putCharSequence(buf, charset.newEncoder(), value);
    }
    public final static void putCharSequence(ByteBuffer buf, CharsetEncoder encoder, CharSequence value) {
        // 
        // Convert any CharSequence implementor (String, etc) into a native
        // C string.
        //
        encoder.reset().onMalformedInput(CodingErrorAction.REPLACE)
                .onUnmappableCharacter(CodingErrorAction.REPLACE)
                .encode(CharBuffer.wrap(value), buf, true);
        encoder.flush(buf);
        final int nulSize = Math.round(encoder.maxBytesPerChar());
        // NUL terminate the string
        if (nulSize == 4) {
            buf.putInt(0);
        } else if (nulSize == 2) {
            buf.putShort((short) 0);
        } else if (nulSize == 1) {
            buf.put((byte) 0);
        }
    }
    public final static CharSequence getCharSequence(ByteBuffer buf, Charset charset) {
        return getCharSequence(buf, charset.newDecoder());
    }
    public final static CharSequence getCharSequence(final ByteBuffer buf, final CharsetDecoder decoder) {
        final ByteBuffer buffer = buf.slice();
        // Find the NUL terminator and limit to that, so the
        // StringBuffer/StringBuilder does not have superfluous NUL chars
        int end = indexOf(buffer, (byte) 0);
        if (end < 0) {
            end = buffer.limit();
        }
        buffer.position(0).limit(end);
        try {
            return decoder.reset().onMalformedInput(CodingErrorAction.REPLACE)
                .onUnmappableCharacter(CodingErrorAction.REPLACE).decode(buffer);
        } catch (CharacterCodingException ex) {
            throw new Error("Illegal character data in native string", ex);
        }
    }
    public final static int indexOf(ByteBuffer buf, byte value) {
        if (buf.hasArray()) {
            byte[] array = buf.array();
            int begin = buf.arrayOffset() + buf.position();
            for (int offset = 0; offset > -1; ++offset) {
                if (array[begin + offset] == value) {
                    return offset;
                }
            }
        } else {
            int begin = buf.position();
            for (int offset = 0; offset < buf.limit(); ++offset) {
                if (buf.get(begin + offset) == value) {
                    return offset;
                }
            }
        }
        return -1;
    }
    private static interface AddressIO {
        public long getAddress(ByteBuffer io, int offset);
        public void putAddress(ByteBuffer io, int offset, long address);
        public static class AddressIO32 implements AddressIO {

            public static final AddressIO IMPL = new AddressIO32();

            public long getAddress(ByteBuffer io, int offset) {
                return io.getInt(offset);
            }

            public void putAddress(ByteBuffer io, int offset, long address) {
                io.putInt(offset, (int) address);
            }
        }

        public static class AddressIO64 implements AddressIO {

            public static final AddressIO IMPL = new AddressIO64();

            public long getAddress(ByteBuffer io, int offset) {
                return io.getLong(offset);
            }

            public void putAddress(ByteBuffer io, int offset, long address) {
                io.putLong(offset, address);
            }
        }
        public static final AddressIO INSTANCE = Platform.getPlatform().addressSize() == 32
                ? AddressIO32.IMPL : AddressIO64.IMPL;
    }
    
    public final static long getAddress(ByteBuffer buf, int position) {
        return AddressIO.INSTANCE.getAddress(buf, position);
    }
    public final static void putAddress(ByteBuffer buf, int position, long address) {
        AddressIO.INSTANCE.putAddress(buf, position, address);
    }
    /*
    public final static Pointer getPointer(ByteBuffer buf, int position) {
        return new Pointer(getAddress(buf, position));
    }
    public final static void putPointer(ByteBuffer buf, int position, Pointer value) {
        putAddress(buf, position, value.nativeAddress());
    }
    */
    public static ByteBuffer slice(final ByteBuffer buffer, final int position) {
        final ByteBuffer tmp = buffer.duplicate();
        tmp.position(position);
        return tmp.slice();
    }
    public static ByteBuffer slice(final ByteBuffer buffer, final int position, final int size) {
        final ByteBuffer tmp = buffer.duplicate();
        tmp.position(position).limit(position + size);
        return tmp.slice();
    }
    
}