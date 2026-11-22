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

    public static SessionAuthMode fromStoredValue(String value) {
        if (value == null || value.trim().isEmpty()) return MICROSOFT;
        try {
            return valueOf(value.trim().toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return MICROSOFT;
        }
    }
}
