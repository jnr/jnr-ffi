package jnr.ffi;

/**
 * A wrapper on {@link jnr.ffi.Pointer}
 * <p>
 * Extend to use as typesafe pointer. Example:
 * <pre>
 * {@code
 * public interface TestLib {
 *      CustomPointer ptr_malloc(@size_t int size);
 *      void ptr_free(CustomPointer ptr);
 *
 *      class CustomPointer extends PointerWrapper {
 *          private CustomPointer(Pointer pointer) {
 *              super(pointer);
 *          }
 *      }
 *  }
 * }
 * </pre>
 */
public abstract class PointerWrapper {
    private final Pointer pointer;

    protected PointerWrapper(Pointer pointer) {
        this.pointer = pointer;
    }

    public Pointer pointer() {
        return pointer;
    }
}

