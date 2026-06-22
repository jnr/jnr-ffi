/*
 * Copyright (C) 2012 Wayne Meissner
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

package jnr.ffi.provider.converters;

import jnr.ffi.Pointer;
import jnr.ffi.annotations.Encoding;
import jnr.ffi.mapper.FromNativeContext;
import jnr.ffi.mapper.FromNativeConverter;
import jnr.ffi.mapper.MethodResultContext;

import java.lang.annotation.Annotation;
import java.lang.ref.Reference;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Arrays;
import java.util.Collection;

import static jnr.ffi.provider.converters.StringUtil.getDecoder;

/**
 * Converts a native pointer result into a java String
 */
@FromNativeConverter.NoContext
@FromNativeConverter.Cacheable
public class StringResultConverter implements FromNativeConverter<String, Pointer> {
    private static final FromNativeConverter<String, Pointer> DEFAULT = new StringResultConverter(Charset.defaultCharset());
    private final ThreadLocal<Reference<CharsetDecoder>> localDecoder = new ThreadLocal<Reference<CharsetDecoder>>();
    private final Charset charset;
    private final int terminatorWidth;

    private StringResultConverter(Charset charset) {
        this.charset = charset;
        this.terminatorWidth = StringUtil.terminatorWidth(charset);
    }

    public static FromNativeConverter<String, Pointer> getInstance(Charset cs) {
        return Charset.defaultCharset().equals(cs) ? DEFAULT : new StringResultConverter(cs);
    }

    public static FromNativeConverter<String, Pointer> getInstance(FromNativeContext fromNativeContext) {
        Charset charset = Charset.defaultCharset();

        if (fromNativeContext instanceof MethodResultContext) {
            // See if the interface class has a global @Encoding declaration
            Encoding e = getEncoding(Arrays.asList(((MethodResultContext) fromNativeContext).getMethod().getDeclaringClass().getAnnotations()));
            if (e != null) {
                charset = Charset.forName(e.value());
            }
        }

        // Allow each method to override the default
        Encoding e = getEncoding(fromNativeContext.getAnnotations());
        if (e != null) {
            charset = Charset.forName(e.value());
        }

        return getInstance(charset);
    }

    @Override
    public String fromNative(Pointer pointer, FromNativeContext context) {
        if (pointer == null) {
            return null;
        }

        Search: for (int idx = 0; ; ) {
            idx += pointer.indexOf(idx, (byte) 0);
            
            // Align idx up to the next char boundary. If indexOf landed on an
            // unaligned zero (e.g. the 0x00 in 0x00 0x03 of U+0300 in UTF-16LE),
            // skip past it and resume searching from the aligned position.
            boolean wasAligned = true;
            while (idx % terminatorWidth != 0) {
                idx++;
                wasAligned = false;
            }
            if (!wasAligned) {
                continue Search;
            }

            // idx is aligned and points at a zero byte. Check the rest of the char.
            for (int tcount = 1; tcount < terminatorWidth; tcount++) {
                if (pointer.getByte(idx + tcount) != 0) {
                    idx += terminatorWidth;
                    continue Search;
                }
            }

            byte[] bytes = new byte[idx];
            pointer.get(0, bytes, 0, bytes.length);
            try {
                return getDecoder(charset, localDecoder).reset().decode(ByteBuffer.wrap(bytes)).toString();
            } catch (CharacterCodingException cce) {
                throw new RuntimeException(cce);
            }
        }
    }

    @Override
    public Class<Pointer> nativeType() {
        return Pointer.class;
    }

    private static Encoding getEncoding(Collection<Annotation> annotations) {
        for (Annotation a : annotations) {
            if (a instanceof Encoding) {
                return (Encoding) a;
            }
        }

        return null;
    }
}
