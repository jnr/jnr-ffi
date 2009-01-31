
package com.kenai.jaffl.provider;

import com.kenai.jaffl.util.BufferUtil;
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
public class StringIO {
    private static final ThreadLocal<SoftReference<StringIO>> localData = new ThreadLocal<SoftReference<StringIO>>() {

        @Override
        protected synchronized SoftReference<StringIO> initialValue() {
            return new SoftReference<StringIO>(new StringIO());
        }
    };
    private static final Charset defaultCharset = Charset.defaultCharset();

    public static final StringIO getStringIO() {
        StringIO io = localData.get().get();
        if (io == null) {
            localData.set(new SoftReference<StringIO>(io = new StringIO()));
        }
        return io;
    }
    public final CharsetEncoder encoder = defaultCharset.newEncoder();
    public final CharsetDecoder decoder = defaultCharset.newDecoder();
    public final int nulByteCount = Math.round(encoder.maxBytesPerChar());

    public StringIO() {
        encoder.onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE);
        decoder.onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE);
    }
    public final ByteBuffer toNative(final CharSequence value, final int minSize, boolean copyIn) {
        // Calculate the raw byte size required (with allowance for NUL termination)
        final int len = (int) (((float)Math.max(minSize, value.length()) + 1) * encoder.maxBytesPerChar());
        final ByteBuffer buf = ByteBuffer.allocate(len);
        if (copyIn) {
            toNative(value, buf);
        }
        return buf;
    }
    public final ByteBuffer toNative(final CharSequence value, final ByteBuffer buf) {
        //
        // Copy the string to native memory
        //
        buf.mark();
        encoder.reset();
        encoder.encode(CharBuffer.wrap(value), buf, true);
        encoder.flush(buf);
        nulTerminate(buf);
        buf.reset();
        return buf;
    }
    public final CharSequence fromNative(final ByteBuffer buf, final int maxSize) {
        // Find the NUL terminator and limit to that, so the
        // StringBuffer/StringBuilder does not have superfluous NUL chars
        int end = BufferUtil.indexOf(buf, (byte) 0);
        if (end < 0 || end > maxSize) {
            end = maxSize;
        }
        buf.rewind().limit(end);
        try {
            return decoder.reset().decode(buf);
        } catch (CharacterCodingException ex) {
            throw new Error("Illegal character data in native string", ex);
        }
    }
    public final CharSequence fromNative(final ByteBuffer buf) {
        try {
            return decoder.reset().decode(buf);
        } catch (CharacterCodingException ex) {
            throw new Error("Illegal character data in native string", ex);
        }
    }
    public final void nulTerminate(ByteBuffer buf) {
        // NUL terminate the string
        int nulSize = nulByteCount;
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
