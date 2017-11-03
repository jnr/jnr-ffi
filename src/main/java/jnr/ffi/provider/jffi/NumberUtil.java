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

package jnr.ffi.provider.jffi;

import jnr.ffi.NativeType;
import jnr.ffi.provider.SigType;
import org.objectweb.asm.Label;

public final class NumberUtil {
    private NumberUtil() {}
    
    static Class getBoxedClass(Class c) {
        if (!c.isPrimitive()) {
            return c;
        }

        if (void.class == c) {
            return Void.class;

        } else if (byte.class == c) {
            return Byte.class;
        
        } else if (char.class == c) {
            return Character.class;

        } else if (short.class == c) {
            return Short.class;

        } else if (int.class == c) {
            return Integer.class;

        } else if (long.class == c) {
            return Long.class;

        } else if (float.class == c) {
            return Float.class;

        } else if (double.class == c) {
            return Double.class;

        } else if (boolean.class == c) {
            return Boolean.class;

        } else {
            throw new IllegalArgumentException("unknown primitive class");
        }
    }

    static Class getPrimitiveClass(Class c) {
        if (Void.class == c) {
            return void.class;

        } else if (Boolean.class == c) {
            return boolean.class;

        } else if (Byte.class == c) {
            return byte.class;

        } else if (Character.class == c) {
            return char.class;

        } else if (Short.class == c) {
            return short.class;

        } else if (Integer.class == c) {
            return int.class;

        } else if (Long.class == c) {
            return long.class;

        } else if (Float.class == c) {
            return float.class;

        } else if (Double.class == c) {
            return double.class;
        
        } else if (c.isPrimitive()) {
            return c;
        } else {
            throw new IllegalArgumentException("unsupported number class");
        }
    }

    public static boolean isPrimitiveInt(Class c) {
        return byte.class == c || char.class == c || short.class == c || int.class == c || boolean.class == c;
    }


    public static void widen(SkinnyMethodAdapter mv, Class from, Class to) {
        if (long.class == to && long.class != from && isPrimitiveInt(from)) {
            mv.i2l();

        } else if (boolean.class == to && boolean.class != from && isPrimitiveInt(from)) {
            // Ensure only 0x0 and 0x1 values are used for boolean
            Label zero = new Label();
            Label ret = new Label();
            mv.ifeq(zero);
            mv.iconst_1();
            mv.go_to(ret);
            mv.label(zero);
            mv.iconst_0();
            mv.label(ret);
        }
    }

    @SuppressWarnings("unused")
    public static void widen(SkinnyMethodAdapter mv, Class from, Class to, NativeType nativeType) {
        if (isPrimitiveInt(from)) {
            if (nativeType == NativeType.UCHAR) {
                mv.pushInt(0xff);
                mv.iand();

            } else if (nativeType == NativeType.USHORT) {
                mv.pushInt(0xffff);
                mv.iand();
            }

            if (long.class == to) {
                mv.i2l();
                switch (nativeType) {
                    case UINT:
                    case ULONG:
                    case ADDRESS:
                        if (sizeof(nativeType) < 8) {
                            // strip off bits 32:63
                            mv.ldc(0xffffffffL);
                            mv.land();
                        }
                        break;
                }
            }
        }
    }


    public static void narrow(SkinnyMethodAdapter mv, Class from, Class to) {
        if (!from.equals(to)) {
            if (byte.class == to || short.class == to || char.class == to || int.class == to || boolean.class == to) {
                if (boolean.class == to) {
                    if (long.class == from) {
                        mv.lconst_0();
                        mv.lcmp();
                    }
                    /* Equivalent to
                       return result == 0 ? true : false;
                    */
                    Label zero = new Label();
                    Label ret = new Label();
                    mv.ifeq(zero);
                    mv.iconst_1();
                    mv.go_to(ret);
                    mv.label(zero);
                    mv.iconst_0();
                    mv.label(ret);
                } else {
                    if (long.class == from) {
                        mv.l2i();
                    }

                    if (byte.class == to) {
                        mv.i2b();

                    } else if (short.class == to) {
                        mv.i2s();

                    } else if (char.class == to) {
                        mv.i2c();
                    }
                }
            }
        }
    }


    public static void convertPrimitive(SkinnyMethodAdapter mv, final Class from, final Class to) {
        narrow(mv, from, to);
        widen(mv, from, to);
    }


    public static void convertPrimitive(SkinnyMethodAdapter mv, final Class from, final Class to, final NativeType nativeType) {
        if (boolean.class == to) {
            switch (nativeType) {
                case SCHAR:
                case UCHAR:
                case SSHORT:
                case USHORT:
                case SINT:
                case UINT:
                case SLONG:
                case ULONG:
                case ADDRESS:
                    if (sizeof(nativeType) <= 4) {
                        narrow(mv, from, int.class);
                        switch (nativeType) {
                            // some compiler may not clean higher bits
                            // https://en.wikipedia.org/wiki/X86_calling_conventions#Microsoft_x64_calling_convention
                            // Parameters less than 64 bits long are not zero extended; the high bits are not zeroed.
                            // such as we can still get 0x80000000 from `u_int32_t2u_int8_t(0x80000000)`
                            case SCHAR:
                            case UCHAR:
                                narrow(mv, int.class, byte.class);
                                break;
                            case USHORT:
                            case SSHORT:
                                narrow(mv, int.class, short.class);
                                break;
                        }
                        narrow(mv, int.class, to);
                    } else {
                        narrow(mv, from, to);
                    }
                    break;
                case FLOAT:
                case DOUBLE:
                    // TODO
                    break;
                default:
                    narrow(mv, from, to);
                    break;
            }
            return;
        }

        switch (nativeType) {
            case SCHAR:
                narrow(mv, from, byte.class);
                // maybe to is char.class
                narrow(mv, byte.class, to);
                widen(mv, byte.class, to);
                break;

            case SSHORT:
                narrow(mv, from, short.class);
                // `to` may be byte.class
                narrow(mv, short.class, to);
                widen(mv, short.class, to);
                break;

            case SINT:
                narrow(mv, from, int.class);
                // `to` may be byte.class
                narrow(mv, int.class, to);
                widen(mv, int.class, to);
                break;

            case UCHAR:
                narrow(mv, from, int.class);
                mv.pushInt(0xff);
                mv.iand();
                // `to` may be byte.class
                narrow(mv, int.class, to);
                widen(mv, int.class, to);
                break;

            case USHORT:
                narrow(mv, from, int.class);
                mv.pushInt(0xffff);
                mv.iand();
                // `to` may be byte.class
                narrow(mv, int.class, to);
                widen(mv, int.class, to);
                break;

            case UINT:
            case ULONG:
            case ADDRESS:
                if (sizeof(nativeType) <= 4) {
                    narrow(mv, from, int.class);
                    if (long.class == to) {
                        mv.i2l();
                        // strip off bits 32:63
                        mv.ldc(0xffffffffL);
                        mv.land();
                    } else {
                        // `to` may be byte.class
                        narrow(mv, int.class, to);
                    }
                } else {
                    // `to` may be byte.class
                    narrow(mv, from, to);
                    widen(mv, from, to);
                }
                break;


            case FLOAT:
            case DOUBLE:
                break;

            default:
                narrow(mv, from, to);
                widen(mv, from, to);
                break;
        }
    }

    static int sizeof(SigType type) {
        return sizeof(type.getNativeType());
    }

    static int sizeof(NativeType nativeType) {
        switch (nativeType) {
            case SCHAR:
                return com.kenai.jffi.Type.SCHAR.size();

            case UCHAR:
                return com.kenai.jffi.Type.UCHAR.size();

            case SSHORT:
                return com.kenai.jffi.Type.SSHORT.size();

            case USHORT:
                return com.kenai.jffi.Type.USHORT.size();

            case SINT:
                return com.kenai.jffi.Type.SINT.size();

            case UINT:
                return com.kenai.jffi.Type.UINT.size();

            case SLONG:
                return com.kenai.jffi.Type.SLONG.size();

            case ULONG:
                return com.kenai.jffi.Type.ULONG.size();

            case SLONGLONG:
                return com.kenai.jffi.Type.SLONG_LONG.size();

            case ULONGLONG:
                return com.kenai.jffi.Type.ULONG_LONG.size();

            case FLOAT:
                return com.kenai.jffi.Type.FLOAT.size();

            case DOUBLE:
                return com.kenai.jffi.Type.DOUBLE.size();

            case ADDRESS:
                return com.kenai.jffi.Type.POINTER.size();

            case VOID:
                return 0;

            default:
                throw new UnsupportedOperationException("cannot determine size of " + nativeType);
        }
    }

}
