
package com.kenai.jaffl.provider.jffi;

public class SymbolNotFoundError extends java.lang.UnsatisfiedLinkError {

    public SymbolNotFoundError(String msg) {
        super(msg);
    }
}
