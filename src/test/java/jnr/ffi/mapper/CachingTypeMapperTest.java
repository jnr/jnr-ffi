package jnr.ffi.mapper;


import jnr.ffi.Pointer;
import jnr.ffi.annotations.In;
import jnr.ffi.annotations.Out;
import jnr.ffi.provider.converters.EnumSetConverter;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;


public class CachingTypeMapperTest {
    public static interface Lib {
        public static enum Enum1 {
            A, B;
        }
        public static enum Enum2 {
            A, B;
        }
        public int ret_int();
        public Set<Enum1> ret_enumset1a();
        public Set<Enum1> ret_enumset1b();
        public Set<Enum2> ret_enumset2a();
        public Set<Enum2> ret_enumset2b();
        public Set<Integer> ret_intset();
        public void enumset_param(Set<Enum1> enums);
        public void annotated_params(@In long[] in, @Out long[] out);
        public void intset_param(Set<Integer> bitfield);
    }

    private static Method getLibMethod(String name, Class... parameterTypes) {
        try {
            return Lib.class.getMethod(name, parameterTypes);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private static final class LongArrayParameterConverter implements ToNativeConverter<long[], Pointer> {
        @Override
        public Pointer toNative(long[] value, ToNativeContext context) {
            return null;
        }

        @Override
        public Class<Pointer> nativeType() {
            return Pointer.class;
        }
    }

    private static final class TestTypeMapper implements TypeMapper {
        @Override
        public FromNativeConverter getFromNativeConverter(Class type, FromNativeContext context) {
            FromNativeConverter converter;
            if (type == Set.class && (converter = EnumSetConverter.getFromNativeConverter(type, context)) != null) {
                 return converter;
            } else {
                return null;
            }
        }

        @Override
        public ToNativeConverter getToNativeConverter(Class type, ToNativeContext context) {
            ToNativeConverter converter;
            if (type == Set.class && (converter = EnumSetConverter.getToNativeConverter(type, context)) != null) {
                return converter;

            } else if (long[].class == type) {
                return new LongArrayParameterConverter();

            } else {
                return null;
            }
        }
    }

    private static final class CountingTypeMapper implements TypeMapper {
        private final Map<Class, Integer> fromConverterStats = new HashMap<Class, Integer>();
        private final Map<Class, Integer> toConverterStats = new HashMap<Class, Integer>();
        private final TypeMapper typeMapper;

        CountingTypeMapper(TypeMapper typeMapper) {
            this.typeMapper = typeMapper;
        }

        @Override
        public FromNativeConverter getFromNativeConverter(Class type, FromNativeContext context) {
            Integer count = fromConverterStats.get(type);
            fromConverterStats.put(type, count != null ? count + 1 : 1);
            return typeMapper.getFromNativeConverter(type, context);
        }

        @Override
        public ToNativeConverter getToNativeConverter(Class type, ToNativeContext context) {
            Integer count = toConverterStats.get(type);
            toConverterStats.put(type, count != null ? count + 1 : 1);
            return typeMapper.getToNativeConverter(type, context);
        }

        public int getFromNativeCount(Class type) {
            Integer count = fromConverterStats.get(type);
            return count != null ? count : 0;
        }

        public int getToNativeCount(Class type) {
            Integer count = toConverterStats.get(type);
            return count != null ? count : 0;
        }
    }

    private TypeMapper defaultTypeMapper;
    @Before
    public void setUp() {
        defaultTypeMapper = new CachingTypeMapper(new TestTypeMapper());
    }

    private ToNativeConverter getToNativeConverter(TypeMapper typeMapper, Method m, int parameterIndex) {
        return typeMapper.getToNativeConverter(m.getParameterTypes()[parameterIndex], new MethodParameterContext(m, parameterIndex));
    }

    private FromNativeConverter getFromNativeConverter(TypeMapper typeMapper, Method m) {
        return typeMapper.getFromNativeConverter(m.getReturnType(), new MethodResultContext(m));
    }


    @Test public void intReturnHasNoConverter() {
        assertNull(getFromNativeConverter(defaultTypeMapper, getLibMethod("ret_int")));
    }

    @Test public void sameResultTypeHasSameConverter() {
        FromNativeConverter converter;
        assertNotNull(converter = getFromNativeConverter(defaultTypeMapper, getLibMethod("ret_enumset1a")));
        assertSame(converter,  getFromNativeConverter(defaultTypeMapper, getLibMethod("ret_enumset1b")));
    }

    @Test public void differentEnumSet() {
        FromNativeConverter converter1;
        assertNotNull(converter1 = getFromNativeConverter(defaultTypeMapper, getLibMethod("ret_enumset1a")));
        assertSame(converter1,  getFromNativeConverter(defaultTypeMapper, getLibMethod("ret_enumset1b")));
        FromNativeConverter converter2;
        assertNotNull(converter2 = getFromNativeConverter(defaultTypeMapper, getLibMethod("ret_enumset2a")));
        assertSame(converter2,  getFromNativeConverter(defaultTypeMapper, getLibMethod("ret_enumset2b")));
        assertNotSame(converter1, converter2);
    }


    @Test public void integerSet() {
        assertNull(getFromNativeConverter(defaultTypeMapper, getLibMethod("ret_intset")));
    }


    @Test public void differentAnnotations() {
        ToNativeConverter converter1, converter2;
        Method m = getLibMethod("annotated_params", long[].class, long[].class);
        assertNotNull(converter1 = getToNativeConverter(defaultTypeMapper, m, 0));
        assertNotNull(converter2 = getToNativeConverter(defaultTypeMapper, m, 1));
        assertNotSame(converter1, converter2);
    }

    @Test public void converterIsCached() {
        CountingTypeMapper counter;
        TypeMapper typeMapper = new CachingTypeMapper(counter = new CountingTypeMapper(new TestTypeMapper()));
        FromNativeConverter converter1;
        assertNotNull(converter1 = getFromNativeConverter(typeMapper, getLibMethod("ret_enumset1a")));
        assertEquals(1, counter.getFromNativeCount(Set.class));
        assertSame(converter1,  getFromNativeConverter(typeMapper, getLibMethod("ret_enumset1b")));
        assertEquals(1, counter.getFromNativeCount(Set.class));
        FromNativeConverter converter2;
        assertNotNull(converter2 = getFromNativeConverter(typeMapper, getLibMethod("ret_enumset2a")));
        assertEquals(2, counter.getFromNativeCount(Set.class));
        assertSame(converter2,  getFromNativeConverter(typeMapper, getLibMethod("ret_enumset2b")));
        assertNotSame(converter1, converter2);
        assertEquals(2, counter.getFromNativeCount(Set.class));
    }

}
