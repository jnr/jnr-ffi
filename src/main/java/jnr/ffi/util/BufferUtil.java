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

package jnr.ffi.util;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.*;

/**
 *
 */
public final class BufferUtil {
    private BufferUtil() {}
    
    public static void putString(ByteBuffer buf, Charset charset, String value) {
        putCharSequence(buf, charset, value);
    }
    public static String getString(ByteBuffer buf, Charset charset) {
        return getCharSequence(buf, charset).toString();
    }
    public static void putCharSequence(ByteBuffer buf, Charset charset, CharSequence value) {
        putCharSequence(buf, charset.newEncoder(), value);
    }
    public static void putCharSequence(ByteBuffer buf, CharsetEncoder encoder, CharSequence value) {
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
    public static CharSequence getCharSequence(ByteBuffer buf, Charset charset) {
        final ByteBuffer buffer = buf.slice();
        // Find the NUL terminator and limit to that, so the
        // StringBuffer/StringBuilder does not have superfluous NUL chars
        int end = indexOf(buffer, (byte) 0);
        if (end < 0) {
            end = buffer.limit();
        }
        buffer.position(0).limit(end);
        return charset.decode(buffer);
    }

    public static CharSequence getCharSequence(final ByteBuffer buf, final CharsetDecoder decoder) {
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

    /**
     * Finds the position of a byte relative to the start of the buffer.
     *
     * @param buf The ByteBuffer to find the value in
     * @param value The value to locate
     * @return The position within the buffer that value is found, or -1 if not
     * found.
     */
    public static int positionOf(ByteBuffer buf, byte value) {
        if (buf.hasArray()) {
            final byte[] array = buf.array();
            final int offset = buf.arrayOffset();
            final int limit = buf.limit();
            for (int pos = buf.position(); pos < limit; ++pos) {
                if (array[offset + pos] == value) {
                    return pos;
                }
            }

        } else {
            final int limit = buf.limit();
            for (int pos = buf.position(); pos < limit; ++pos) {
                if (buf.get(pos) == value) {
                    return pos;
                }
            }
        }
        
        return -1;
    }
    
    public static int indexOf(ByteBuffer buf, byte value) {
        if (buf.hasArray()) {
            byte[] array = buf.array();
            int begin = buf.arrayOffset() + buf.position();
            int end = buf.arrayOffset() + buf.limit();
            for (int offset = 0; offset < end && offset > -1; ++offset) {
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

    public static int indexOf(ByteBuffer buf, int offset, byte value) {
        if (buf.hasArray()) {
            byte[] array = buf.array();
            int begin = buf.arrayOffset() + buf.position() + offset;
            int end = buf.arrayOffset() + buf.limit();
            for (int idx = 0; idx < end && idx > -1; ++idx) {
                if (array[begin + idx] == value) {
                    return idx;
                }
            }
        } else {
            int begin = buf.position();
            for (int idx = 0; idx < buf.limit(); ++idx) {
                if (buf.get(begin + idx) == value) {
                    return idx;
                }
            }
        }
        return -1;
    }
    
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
