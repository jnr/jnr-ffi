package jnr.ffi.mapper;

/**
 *
 */
public final class FromNativeTypes {

    public static FromNativeType create(FromNativeConverter converter) {
        if (converter == null) {
            return null;
        }
        return converter.getClass().isAnnotationPresent(FromNativeConverter.Cacheable.class)
                ? new Cacheable(converter) : new UnCacheable(converter);
    }

    @FromNativeType.Cacheable
    static class Cacheable extends AbstractFromNativeType {
        public Cacheable(FromNativeConverter converter) {
            super(converter);
        }
    }

    static class UnCacheable extends AbstractFromNativeType {
        public UnCacheable(FromNativeConverter converter) {
            super(converter);
        }
    }
}
