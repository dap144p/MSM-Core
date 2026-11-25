package dev.cylox.msm.profile;

/**
 * Selects which Minecraft identity model a session profile expects.
 *
 * OFFLINE_LOCAL is intended only for offline-mode servers operated or
 * explicitly configured by the player. It does not provide Microsoft auth.
 */
public enum SessionAuthMode {
    MICROSOFT,
    OFFLINE_LOCAL;

    /**
     * Missing values are from legacy profiles and default to Microsoft. An
     * explicit but unrecognized value is rejected rather than silently lost.
     */
    public static SessionAuthMode fromStoredValue(String value) {
        if (value == null) return MICROSOFT;
        return valueOf(value.trim().toUpperCase(java.util.Locale.ROOT));
    }
}
