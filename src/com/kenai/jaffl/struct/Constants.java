package com.kenai.jaffl.struct;

import com.kenai.jaffl.Type;

/**
 * Various platform-dependent constants needed for Struct construction
 */
final class Constants {
    static final int LONG_SIZE = Type.SLONG.size() * 8;
    static final int ADDRESS_SIZE = Type.ADDRESS.size() * 8;
    static final long LONG_MASK = LONG_SIZE == 32 ? 2147483647L : 9223372036854775807L;
    static final int LONG_ALIGN = Type.SLONG.alignment() * 8;
    static final int INT64_ALIGN = Type.SLONGLONG.alignment() * 8;
    static final int DOUBLE_ALIGN = Type.DOUBLE.alignment() * 8;
    static final int FLOAT_ALIGN = Type.FLOAT.alignment() * 8;
    
}
