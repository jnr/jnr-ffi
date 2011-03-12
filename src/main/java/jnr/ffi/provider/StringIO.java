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

package jnr.ffi.provider;

import jnr.ffi.util.BufferUtil;
import java.lang.ref.SoftReference;
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
public final class StringIO {

    private static final class StaticDataHolder {
        static final StringIO INSTANCE = new StringIO(Charset.defaultCharset());
        static final ThreadLocal<SoftReference<CharsetEncoder>> ENCODER = new ThreadLocal<SoftReference<CharsetEncoder>>();
        static final ThreadLocal<SoftReference<CharsetDecoder>> DECODER = new ThreadLocal<SoftReference<CharsetDecoder>>();
    }

    private final Charset charset;
    
    public static StringIO getStringIO() {
        return StaticDataHolder.INSTANCE;
    }

    private StringIO(Charset charset) {
        this.charset = charset;
    }

    private static CharsetEncoder getEncoder(Charset charset) {
        SoftReference<CharsetEncoder> ref = StaticDataHolder.ENCODER.get();
        CharsetEncoder encoder;
        if (ref != null && (encoder = ref.get()) != null && encoder.charset() == charset) {
            return encoder;
        }

        return initEncoder(charset);
    }

    private static CharsetDecoder getDecoder(Charset charset) {
        SoftReference<CharsetDecoder> ref = StaticDataHolder.DECODER.get();
        CharsetDecoder decoder;
        if (ref != null && (decoder = ref.get()) != null && decoder.charset() == charset) {
            return decoder;
        }

        return initDecoder(charset);
    }

    private static CharsetEncoder initEncoder(Charset charset) {
        CharsetEncoder encoder = charset.newEncoder();
        encoder.onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE);
        StaticDataHolder.ENCODER.set(new SoftReference<CharsetEncoder>(encoder));

        return encoder;
    }
    
    private static CharsetDecoder initDecoder(Charset charset) {
        CharsetDecoder decoder = charset.newDecoder();
        decoder.onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE);
        StaticDataHolder.DECODER.set(new SoftReference<CharsetDecoder>(decoder));

        return decoder;
    }

    public final ByteBuffer toNative(final CharSequence value, final int minSize, boolean copyIn) {
        return toNative(getEncoder(charset), value, minSize, copyIn);
    }

    public final ByteBuffer toNative(final CharSequence value, final ByteBuffer buf) {
        return toNative(getEncoder(charset), value, buf);
    }

    private static ByteBuffer toNative(CharsetEncoder encoder, final CharSequence value, final int minSize, boolean copyIn) {
        // Calculate the raw byte size required (with allowance for NUL termination)
        final int len = (int) (((float)Math.max(minSize, value.length()) + 1) * encoder.maxBytesPerChar());
        final ByteBuffer buf = ByteBuffer.allocate(len);
        if (copyIn) {
            toNative(encoder, value, buf);
        }
        return buf;
    }

    private static ByteBuffer toNative(CharsetEncoder encoder, final CharSequence value, final ByteBuffer buf) {
        //
        // Copy the string to native memory
        //
        buf.mark();
        try {
            encoder.reset();
            encoder.encode(CharBuffer.wrap(value), buf, true);
            encoder.flush(buf);
            nulTerminate(encoder, buf);
        } finally {
            buf.reset();
        }
        return buf;
    }

    
    public final CharSequence fromNative(final ByteBuffer buf, final int maxSize) {
        // Find the NUL terminator and limit to that, so the
        // StringBuffer/StringBuilder does not have superfluous NUL chars
        int end = BufferUtil.positionOf(buf, (byte) 0);
        if (end < 0 || end > maxSize) {
            end = maxSize;
        }

        final int limit = buf.limit();
        buf.limit(end);
        try {
            return getDecoder(charset).reset().decode(buf);
        } catch (CharacterCodingException ex) {
            throw new Error("Illegal character data in native string", ex);
        } finally {
            buf.limit(limit);
        }
    }


    public final CharSequence fromNative(final ByteBuffer buf) {
        try {
            return getDecoder(charset).reset().decode(buf);
        } catch (CharacterCodingException ex) {
            throw new Error("Illegal character data in native string", ex);
        }
    }

    
    public final void nulTerminate(ByteBuffer buf) {
        nulTerminate(getEncoder(charset), buf);
    }

    private static void nulTerminate(CharsetEncoder encoder, ByteBuffer buf) {
        // NUL terminate the string
        int nulSize = Math.round(encoder.maxBytesPerChar());
        while (nulSize >= 4) {
            buf.putInt(0);
            nulSize -= 4;
        }
        if (nulSize >= 2) {
            buf.putShort((short) 0);
            nulSize -= 2;
        }
        if (nulSize >= 1) {
            buf.put((byte) 0);
        }
    }
}
