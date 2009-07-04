
package com.kenai.jaffl;

/**
 * Enables getting/setting of the unix errno and windows GetLastError() value
 */
public final class LastError {
    private LastError() {}

    /**
     * Gets the value of errno from the last native call.
     *
     * @return An integer containing the errno value.
     */
    public static final int getLastError() {
        return FFIProvider.getProvider().getLastError();
    }

    /**
     * Sets the native errno value.
     *
     * @param error The value to set errno to.
     */
    public static final void setLastError(int error) {
        FFIProvider.getProvider().setLastError(error);
    }
}
