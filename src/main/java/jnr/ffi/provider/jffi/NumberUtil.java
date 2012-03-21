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

import com.kenai.jffi.Platform;
import com.kenai.jffi.Type;
import jnr.ffi.NativeLong;
import jnr.ffi.NativeType;
import jnr.ffi.annotations.LongLong;

import java.lang.annotation.Annotation;

public final class NumberUtil {
    private NumberUtil() {}
    
    static final Class getBoxedClass(Class c) {
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

    static final Class getPrimitiveClass(Class c) {
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
        
        } else if (NativeLong.class == c) {
            return long.class;

        } else if (c.isPrimitive()) {
            return c;
        } else {
            throw new IllegalArgumentException("unsupported number class");
        }
    }

    public static boolean isPrimitiveInt(Class c) {
        return byte.class == c || char.class == c || short.class == c || int.class == c || boolean.class == c;
    }


    public static final void widen(SkinnyMethodAdapter mv, Class from, Class to) {
        if (long.class == to && long.class != from && isPrimitiveInt(from)) {
            mv.i2l();
        }
    }

    public static final void narrow(SkinnyMethodAdapter mv, Class from, Class to) {
        if (!from.equals(to)) {
            if (byte.class == to || short.class == to || char.class == to || int.class == to || boolean.class == to) {
                if (long.class == from) {
                    mv.l2i();
                }

                if (byte.class == to) {
                    mv.i2b();

                } else if (short.class == to) {
                    mv.i2s();

                } else if (char.class == to) {
                    mv.i2c();

                } else if (boolean.class == to) {
                    // Ensure only 0x0 and 0x1 values are used for boolean
                    mv.iconst_1();
                    mv.iand();
                }
            }
        }
    }

    static void constrain(SkinnyMethodAdapter mv, Class from, Class to, Class constraint) {
        narrow(mv, from, constraint);
        widen(mv, constraint, to);
        if (boolean.class == to) {
            // Ensure only 0x0 and 0x1 values are used for boolean
            mv.iconst_1();
            mv.iand();
        }
    }

    public static void convertPrimitive(SkinnyMethodAdapter mv, final Class from, final Class to, final Type jffiType) {
        if (Type.SCHAR == jffiType || Type.UCHAR == jffiType || Type.SINT8 == jffiType || Type.UINT8 == jffiType) {
            constrain(mv, from, to, byte.class);

        } else if (Type.SSHORT == jffiType || Type.USHORT == jffiType || Type.SINT16 == jffiType || Type.UINT16 == jffiType) {
            constrain(mv, from, to, short.class);

        } else if (Type.SINT == jffiType || Type.UINT == jffiType || Type.SINT32 == jffiType || Type.UINT32 == jffiType) {
            constrain(mv, from, to, int.class);

        } else if ((Type.SLONG == jffiType || Type.ULONG == jffiType) && jffiType.size() == 4) {
            constrain(mv, from, to, int.class);

        } else {
            narrow(mv, from, to);
            widen(mv, from, to);
        }
    }

    public static void convertPrimitive(SkinnyMethodAdapter mv, final Class from, final Class to, final NativeType nativeType) {
        switch (nativeType) {
            case SCHAR:
            case UCHAR:
                constrain(mv, from, to, byte.class);
                break;

            case SSHORT:
            case USHORT:
                constrain(mv, from, to, short.class);
                break;

            case SINT:
            case UINT:
                constrain(mv, from, to, int.class);
                break;

            case SLONG:
            case ULONG:
                constrain(mv, from, to, sizeof(nativeType) == 4 ? int.class : long.class);
                break;

            default:
                narrow(mv, from, to);
                widen(mv, from, to);
                break;
        }
    }

    static int sizeof(SigType type) {
        return sizeof(type.nativeType);
    }

    static int sizeof(NativeType nativeType) {
        switch (nativeType) {
            case SCHAR:
            case UCHAR:
                return 1;

            case SSHORT:
            case USHORT:
                return 2;

            case SINT:
            case UINT:
                return 4;

            case SLONG:
            case ULONG:
                return Platform.getPlatform().longSize() / 8;

            case SLONGLONG:
            case ULONGLONG:
                return 8;

            case FLOAT:
                return 4;

            case DOUBLE:
                return 8;

            case ADDRESS:
                return Platform.getPlatform().addressSize() / 8;

            default:
                throw new UnsupportedOperationException("cannot determine size of " + nativeType);
        }
    }

    static int sizeof(com.kenai.jffi.NativeType nativeType) {
        switch (nativeType) {
            case SCHAR:
            case UCHAR:
                return 1;

            case SSHORT:
            case USHORT:
                return 2;

            case SINT:
            case UINT:
                return 4;

            case SLONG:
            case ULONG:
                return Platform.getPlatform().longSize() / 8;

            case SINT64:
            case UINT64:
                return 8;

            case FLOAT:
                return 4;

            case DOUBLE:
                return 8;

            case POINTER:
                return Platform.getPlatform().addressSize() / 8;

            default:
                throw new UnsupportedOperationException("cannot determine size of " + nativeType);
        }
    }

    static boolean isLong32(Class type, Annotation[] annotations) {
        return isLong32(Platform.getPlatform(), type, annotations);
    }

    static boolean isLong32(Platform platform, Class type, Annotation[] annotations) {
        return platform.longSize() == 32
            && (((long.class == type || Long.class.isAssignableFrom(type))
                 && !InvokerUtil.hasAnnotation(annotations, LongLong.class))
               || NativeLong.class.isAssignableFrom(type));
    }

    static boolean isLong64(Class type, Annotation[] annotations) {
        final int longSize = Platform.getPlatform().longSize();
        return ((long.class == type || Long.class.isAssignableFrom(type))
                && (longSize == 64 || InvokerUtil.hasAnnotation(annotations, LongLong.class)))
            || (NativeLong.class.isAssignableFrom(type) && longSize == 64);
    }

    static boolean isInt32(Class type, Annotation[] annotations) {
        return Boolean.class.isAssignableFrom(type) || boolean.class == type
                || Byte.class.isAssignableFrom(type) || byte.class == type
                || Short.class.isAssignableFrom(type) || short.class == type
                || Integer.class.isAssignableFrom(type) || int.class == type
                || isLong32(type, annotations)
                || Enum.class.isAssignableFrom(type)
                ;
    }
}
