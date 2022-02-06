package jnr.ffi.numeric.utils;

import jnr.ffi.NativeLong;

@SuppressWarnings("UnnecessaryBoxing")
public class NumberUtils {
    public static Byte box(byte b) {return Byte.valueOf(b);}

    public static Short box(short s) {return Short.valueOf(s);}

    public static Integer box(int i) {return Integer.valueOf(i);}

    public static Long box(long l) {return Long.valueOf(l);}

    public static Float box(float f) {return Float.valueOf(f);}

    public static Double box(double f) {return Double.valueOf(f);}

    public static Boolean box(boolean b) {return Boolean.valueOf(b);}

    public static NativeLong nativeLong(long l) {return NativeLong.valueOf(l);}
}
