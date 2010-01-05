/*
 * Copyright (C) 2009 Wayne Meissner
 *
 * This file is part of jffi.
 *
 * This code is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License version 3 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * version 3 for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * version 3 along with this work.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.kenai.jaffl.provider.jffi;

import com.kenai.jaffl.NativeLong;

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
        return byte.class == c || short.class == c || int.class == c || boolean.class == c;
    }


    public static final void widen(SkinnyMethodAdapter mv, Class from, Class to) {
        if (long.class == to && long.class != from && isPrimitiveInt(from)) {
            mv.i2l();
        }
    }

    public static final void narrow(SkinnyMethodAdapter mv, Class from, Class to) {
        if (!from.equals(to)) {
            if (byte.class == to || short.class == to || char.class == to || int.class == to) {
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
