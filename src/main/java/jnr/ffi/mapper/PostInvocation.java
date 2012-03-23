package jnr.ffi.mapper;

/**
 * Used to reload a parameter converted to a native type via a custom {@link ToNativeConverter}
 */
public interface PostInvocation<J,N> {
    void postInvoke(J j, N n, ToNativeContext context);
}
