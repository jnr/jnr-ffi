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

import jnr.ffi.annotations.Encoding;
import jnr.ffi.mapper.MethodParameterContext;
import jnr.ffi.mapper.ToNativeContext;

import java.lang.annotation.Annotation;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;
import java.nio.charset.*;
import java.util.Arrays;
import java.util.Collection;

final class StringUtil {
    private StringUtil() {}

    static CharsetEncoder getEncoder(Charset charset, ThreadLocal<Reference<CharsetEncoder>> localEncoder) {
        Reference<CharsetEncoder> ref = localEncoder.get();
        CharsetEncoder encoder;
        return ref != null && (encoder = ref.get()) != null && encoder.charset() == charset
                ? encoder : initEncoder(charset, localEncoder);
    }

    static CharsetDecoder getDecoder(Charset charset, ThreadLocal<Reference<CharsetDecoder>> localDecoder) {
        Reference<CharsetDecoder> ref = localDecoder.get();
        CharsetDecoder decoder;
        return ref != null && (decoder = ref.get()) != null && decoder.charset() == charset
                ? decoder : initDecoder(charset, localDecoder);
    }

    private static CharsetEncoder initEncoder(Charset charset, ThreadLocal<Reference<CharsetEncoder>> localEncoder) {
        CharsetEncoder encoder = charset.newEncoder();
        encoder.onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE);
        localEncoder.set(new SoftReference<CharsetEncoder>(encoder));

        return encoder;
    }

    private static CharsetDecoder initDecoder(Charset charset, ThreadLocal<Reference<CharsetDecoder>> localDecoder) {
        CharsetDecoder decoder = charset.newDecoder();
        decoder.onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE);
        localDecoder.set(new SoftReference<CharsetDecoder>(decoder));

        return decoder;
    }

    static Charset getCharset(ToNativeContext toNativeContext) {
        Charset charset = Charset.defaultCharset();

        if (toNativeContext instanceof MethodParameterContext) {
            // See if the interface class has a global @Encoding declaration
            Charset cs = getEncodingCharset(Arrays.asList(((MethodParameterContext) toNativeContext).getMethod().getDeclaringClass().getAnnotations()));
            if (cs != null) {
                charset = cs;
            }

            // Allow each method to override the default
            cs = getEncodingCharset(Arrays.asList(((MethodParameterContext) toNativeContext).getMethod().getAnnotations()));
            if (cs != null) {
                charset = cs;
            }
        }

        // Override on a per-parameter basis
        Charset cs = getEncodingCharset(toNativeContext.getAnnotations());
        if (cs != null) {
            charset = cs;
        }

        return charset;
    }

    private static Charset getEncodingCharset(Collection<Annotation> annotations) {
        for (Annotation a : annotations) {
            if (a instanceof Encoding) {
                return Charset.forName(((Encoding) a).value());
            }
        }

        return null;
    }

    static void throwException(CoderResult result) {
        try {
            result.throwException();
        } catch (RuntimeException re) {
            throw re;
        } catch (CharacterCodingException cce) {
            throw new RuntimeException(cce);
        }
    }

    private static final Charset UTF8 = Charset.forName("UTF-8");
    private static final Charset USASCII = Charset.forName("US-ASCII");
    private static final Charset ISO8859_1 = Charset.forName("ISO-8859-1");
    private static final Charset UTF16 = Charset.forName("UTF-16");
    private static final Charset UTF16LE = Charset.forName("UTF-16LE");
    private static final Charset UTF16BE = Charset.forName("UTF-16BE");

    static int terminatorWidth(Charset charset) {
        if (charset.equals(UTF8) || charset.equals(USASCII) || charset.equals(ISO8859_1)) {
            return 1;

        } else if (charset.equals(UTF16) || charset.equals(UTF16LE) || charset.equals(UTF16BE)) {
            return 2;

        } else {
            return 4;
        }
    }

    static int stringLength(ByteBuffer in, int terminatorWidth) {
        if (in.hasArray()) {
            byte[] array = in.array();
            int end = in.arrayOffset() + in.limit();
            int tcount = 0;
            for (int idx = in.arrayOffset() + in.position(); idx < end; ) {
                if (array[idx++] == 0) {
                    tcount++;
                } else {
                    tcount = 0;
                }
                if (tcount == terminatorWidth) {
                    return idx - terminatorWidth;
                }
            }
        } else {
            int begin = in.position();
            int end = in.limit();
            int tcount = 0;
            for (int idx = begin; idx < end; ) {
                if (in.get(idx++) == 0) {
                    tcount++;
                } else {
                    tcount = 0;
                }
                if (tcount == terminatorWidth) {
                    return idx - terminatorWidth;
                }
            }
        }

        return -1;
    }
}
