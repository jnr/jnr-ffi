package jnr.ffi;

/**
 * Access library global variables.
 *
 * <p>
 *     To access global variables, declare a method with a parameterized return type of this class.
 * <p>
 * <b>Example</b>
 * <pre>
 *     {@code
 *
 *     public interface MyLib {
 *         public Variable<Long> my_int_var();
 *     }
 *
 *     MyLib lib = LibraryLoader.create(MyLib.class).load("mylib"):
 *     System.out.println("native value=" + lib.my_int_var().get())
 *
 *     lib.my_int_var().set(0xdeadbeef);
 *     }
 * </pre>
 */
public interface Variable<T> {
    /**
     * Gets the current value of the global variable
     *
     * @return The value of the variable
     */
    public T get();

    /**
     * Sets the global variable to a value
     *
     * @param value The value to set the global variable to.
     */
    public void set(T value);
}
