/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jafl;

/**
 *
 * @author wayne
 */
public abstract class FFIProvider {
    public static final FFIProvider getInstance() {
        throw new UnsupportedOperationException("not implemented");
    }
    public abstract MemoryIO allocateMemory(int size);
    public abstract MemoryIO allocateMemoryDirect(int size);
}
