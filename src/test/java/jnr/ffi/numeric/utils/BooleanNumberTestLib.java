package jnr.ffi.numeric.utils;

import jnr.ffi.annotations.LongLong;
import jnr.ffi.types.u_int64_t;

/**
 * Same as {@link NumberTestLib} but returns booleans to test boolean mapping.
 * Since return type doesn't change signature in Java, we need these this mapping separate
 * from {@link NumberTestLib}
 */
public interface BooleanNumberTestLib {

    public boolean ret_int8_t(byte i);
    public Boolean ret_int8_t(Byte i);

    public boolean ret_int16_t(short i);
    public Boolean ret_int16_t(Short i);

    public boolean ret_int32_t(int i);
    public Boolean ret_int32_t(Integer i);

    public boolean ret_long(long i);
    public Boolean ret_long(Long i);

    public boolean ret_int64_t(@LongLong long i);
    public Boolean ret_int64_t(@LongLong Long i);

    public boolean ret_uint8_t(byte i);
    public Boolean ret_uint8_t(Byte i);

    public boolean ret_uint16_t(short i);
    public Boolean ret_uint16_t(Short i);

    public boolean ret_uint32_t(int i);
    public Boolean ret_uint32_t(Integer i);

    public boolean ret_ulong(long i);
    public Boolean ret_ulong(Long i);

    public boolean ret_uint64_t(@u_int64_t long i);
    public Boolean ret_uint64_t(@LongLong Long i);

    public boolean ret_float(float f);
    public Boolean ret_float(Float f);

    public boolean ret_double(double f);
    public Boolean ret_double(Double f);

    public boolean ret_bool(boolean b);
    public Boolean ret_bool(Boolean b);

    public byte ret_bool(byte b);
    public Byte ret_bool(Byte b);

    public short ret_bool(short b);
    public Short ret_bool(Short b);

    public int ret_bool(int b);
    public Integer ret_bool(Integer b);

    public long ret_bool(long b);
    public Long ret_bool(Long b);

}