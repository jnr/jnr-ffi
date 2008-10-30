
package com.kenai.jaffl;

/**
 * Enables getting/setting of the unix errno value
 */
public final class LastError {
    private LastError() {}
    public static final int getLastError() {
        return FFIProvider.getProvider().getLastError();
    }
    public static final void setLastError(int error) {
        FFIProvider.getProvider().setLastError(error);
    }
}
