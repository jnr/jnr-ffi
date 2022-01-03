package jnr.ffi.number.utils;

import jnr.ffi.NativeLong;
import jnr.ffi.annotations.LongLong;
import jnr.ffi.types.int16_t;
import jnr.ffi.types.int32_t;
import jnr.ffi.types.int8_t;
import jnr.ffi.types.u_int16_t;
import jnr.ffi.types.u_int32_t;
import jnr.ffi.types.u_int64_t;
import jnr.ffi.types.u_int8_t;

/**
 * Mappings of the functions generated in NumberTest.c
 * For each of the possible types we test add, sub, mul, div and ret
 * There are the following types: int8, int16, int32, int64, long, float, double
 * For all types there exist an unsigned type like uint8, uint16 etc. except for float and double
 * We test using all possibilities for type mappings that is either using primitives like byte and int
 * or using Boxed types like {@link Byte} and {@link Integer}.
 * For native long (variable size) there is a special JNR type {@link NativeLong}, that is also tested.
 */
public interface NumberTestLib {
    public byte add_int8_t(byte i1, byte i2);
    public Byte add_int8_t(Byte i1, Byte i2);

    public byte sub_int8_t(byte i1, byte i2);
    public Byte sub_int8_t(Byte i1, Byte i2);

    public byte mul_int8_t(byte i1, byte i2);
    public Byte mul_int8_t(Byte i1, Byte i2);

    public byte div_int8_t(byte i1, byte i2);
    public Byte div_int8_t(Byte i1, Byte i2);

    public byte ret_int8_t(byte i);
    public Byte ret_int8_t(Byte i);

    public short add_int16_t(short i1, short i2);
    public Short add_int16_t(Short i1, Short i2);

    public short sub_int16_t(short i1, short i2);
    public Short sub_int16_t(Short i1, Short i2);

    public short mul_int16_t(short i1, short i2);
    public Short mul_int16_t(Short i1, Short i2);

    public short div_int16_t(short i1, short i2);
    public Short div_int16_t(Short i1, Short i2);

    public short ret_int16_t(short i);
    public Short ret_int16_t(Short i);

    public int add_int32_t(int i1, int i2);
    public Integer add_int32_t(Integer i1, Integer i2);

    public int sub_int32_t(int i1, int i2);
    public Integer sub_int32_t(Integer i1, Integer i2);

    public int mul_int32_t(int i1, int i2);
    public Integer mul_int32_t(Integer i1, Integer i2);

    public int div_int32_t(int i1, int i2);
    public Integer div_int32_t(Integer i1, Integer i2);

    public int ret_int32_t(int i);
    public Integer ret_int32_t(Integer i);

    public long add_long(long i1, long i2);
    public Long add_long(Long i1, Long i2);
    public NativeLong add_long(NativeLong i1, NativeLong i2);

    public long sub_long(long i1, long i2);
    public Long sub_long(Long i1, Long i2);
    public NativeLong sub_long(NativeLong i1, NativeLong i2);

    public long mul_long(long i1, long i2);
    public Long mul_long(Long i1, Long i2);
    public NativeLong mul_long(NativeLong i1, NativeLong i2);

    public long div_long(long i1, long i2);
    public Long div_long(Long i1, Long i2);
    public NativeLong div_long(NativeLong i1, NativeLong i2);

    public long ret_long(long i);
    public Long ret_long(Long i);
    public NativeLong ret_long(NativeLong i);

    public @LongLong long add_int64_t(@LongLong long i1, @LongLong long i2);
    public @LongLong Long add_int64_t(@LongLong Long i1, @LongLong Long i2);

    public @LongLong long sub_int64_t(@LongLong long i1, @LongLong long i2);
    public @LongLong Long sub_int64_t(@LongLong Long i1, @LongLong Long i2);

    public @LongLong long mul_int64_t(@LongLong long i1, @LongLong long i2);
    public @LongLong Long mul_int64_t(@LongLong Long i1, @LongLong Long i2);

    public @LongLong long div_int64_t(@LongLong long i1, @LongLong long i2);
    public @LongLong Long div_int64_t(@LongLong Long i1, @LongLong Long i2);

    public @LongLong long ret_int64_t(@LongLong long i);
    public @LongLong Long ret_int64_t(@LongLong Long i);

    public short add_uint8_t(short i1, short i2);
    public Byte add_uint8_t(Byte i1, Byte i2);

    public byte sub_uint8_t(byte i1, byte i2);
    public Byte sub_uint8_t(Byte i1, Byte i2);

    public byte mul_uint8_t(byte i1, byte i2);
    public Byte mul_uint8_t(Byte i1, Byte i2);

    public byte div_uint8_t(byte i1, byte i2);
    public Byte div_uint8_t(Byte i1, Byte i2);

    public byte ret_uint8_t(byte i);
    public Byte ret_uint8_t(Byte i);

    public short add_uint16_t(short i1, short i2);
    public Short add_uint16_t(Short i1, Short i2);

    public short sub_uint16_t(short i1, short i2);
    public Short sub_uint16_t(Short i1, Short i2);

    public short mul_uint16_t(short i1, short i2);
    public Short mul_uint16_t(Short i1, Short i2);

    public short div_uint16_t(short i1, short i2);
    public Short div_uint16_t(Short i1, Short i2);

    public short ret_uint16_t(short i);
    public Short ret_uint16_t(Short i);

    public int add_uint32_t(int i1, int i2);
    public Integer add_uint32_t(Integer i1, Integer i2);

    public int sub_uint32_t(int i1, int i2);
    public Integer sub_uint32_t(Integer i1, Integer i2);

    public int mul_uint32_t(int i1, int i2);
    public Integer mul_uint32_t(Integer i1, Integer i2);

    public int div_uint32_t(int i1, int i2);
    public Integer div_uint32_t(Integer i1, Integer i2);

    public int ret_uint32_t(int i);
    public Integer ret_uint32_t(Integer i);

    public long add_ulong(long i1, long i2);
    public Long add_ulong(Long i1, Long i2);
    public NativeLong add_ulong(NativeLong i1, NativeLong i2);

    public long sub_ulong(long i1, long i2);
    public Long sub_ulong(Long i1, Long i2);
    public NativeLong sub_ulong(NativeLong i1, NativeLong i2);

    public long mul_ulong(long i1, long i2);
    public Long mul_ulong(Long i1, Long i2);
    public NativeLong mul_ulong(NativeLong i1, NativeLong i2);

    public long div_ulong(long i1, long i2);
    public Long div_ulong(Long i1, Long i2);
    public NativeLong div_ulong(NativeLong i1, NativeLong i2);

    public long ret_ulong(long i);
    public Long ret_ulong(Long i);
    public NativeLong ret_ulong(NativeLong i);

    public @u_int64_t long add_uint64_t(@u_int64_t long i1, @u_int64_t long i2);
    public @u_int64_t Long add_uint64_t(@u_int64_t Long i1, @u_int64_t Long i2);

    public @u_int64_t long sub_uint64_t(@u_int64_t long i1, @u_int64_t long i2);
    public @u_int64_t Long sub_uint64_t(@u_int64_t Long i1, @u_int64_t Long i2);

    public @u_int64_t long mul_uint64_t(@u_int64_t long i1, @u_int64_t long i2);
    public @u_int64_t Long mul_uint64_t(@u_int64_t Long i1, @u_int64_t Long i2);

    public @u_int64_t long div_uint64_t(@u_int64_t long i1, @u_int64_t long i2);
    public @u_int64_t Long div_uint64_t(@u_int64_t Long i1, @u_int64_t Long i2);

    public @u_int64_t long ret_uint64_t(@u_int64_t long i);
    public @u_int64_t Long ret_uint64_t(@u_int64_t Long i);

    public float add_float(float f1, float f2);
    public Float add_float(Float f1, Float f2);

    public float sub_float(float f1, float f2);
    public Float sub_float(Float f1, Float f2);

    public float mul_float(float f1, float f2);
    public Float mul_float(Float f1, Float f2);

    public float div_float(float f1, float f2);
    public Float div_float(Float f1, Float f2);

    public float ret_float(float f);
    public Float ret_float(Float f);

    public double add_double(double f1, double f2);
    public Double add_double(Double f1, Double f2);

    public double sub_double(double f1, double f2);
    public Double sub_double(Double f1, Double f2);

    public double mul_double(double f1, double f2);
    public Double mul_double(Double f1, Double f2);

    public double div_double(double f1, double f2);
    public Double div_double(Double f1, Double f2);

    public double ret_double(double f);
    public Double ret_double(Double f);

    public boolean ret_bool(boolean b);
    public Boolean ret_bool(Boolean b);

    public @u_int8_t short ret_uint8_t(@u_int8_t short i);
    public @int8_t short ret_int8_t(@int8_t short i);

    public @u_int16_t int ret_uint16_t(@u_int16_t int i);
    public @int16_t int ret_int16_t(@int16_t int i);

    public @u_int32_t long ret_uint32_t(@u_int32_t long l);
    public @int32_t long ret_int32_t(@int32_t long l);
}
