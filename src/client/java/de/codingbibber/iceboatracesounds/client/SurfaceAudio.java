package de.codingbibber.iceboatracesounds.client;

/**
 * Klangfärbung je nach Eis-Sorte. Reine Multiplikatoren/Additionen auf Volume
 * und Pitch – Blöcke und Physik bleiben unverändert.
 */
public final class SurfaceAudio {

    private SurfaceAudio() {
    }

    private static boolean enabled(ModConfig cfg) {
        return cfg.surfaceAudioEnabled;
    }

    public static float enginePitch(SimState s, ModConfig cfg) {
        if (!enabled(cfg)) {
            return 1.0f;
        }
        switch (s.surface) {
            case BLUE:
                return 1.03f;
            case PACKED:
                return 1.01f;
            case FROSTED:
                return 0.98f;
            default:
                return 1.0f;
        }
    }

    public static float engineVolume(SimState s, ModConfig cfg) {
        if (!enabled(cfg)) {
            return 1.0f;
        }
        switch (s.surface) {
            case BLUE:
                return 1.05f;
            case FROSTED:
                return 0.95f;
            default:
                return 1.0f;
        }
    }

    public static float skidPitchAdd(SimState s, ModConfig cfg) {
        if (!enabled(cfg)) {
            return 0.0f;
        }
        switch (s.surface) {
            case BLUE:
                return 0.15f;
            case PACKED:
                return 0.05f;
            case FROSTED:
                return -0.05f;
            default:
                return 0.0f;
        }
    }

    public static float skidVolume(SimState s, ModConfig cfg) {
        if (!enabled(cfg)) {
            return 1.0f;
        }
        switch (s.surface) {
            case BLUE:
                return 1.1f;
            case FROSTED:
                return 0.8f;
            default:
                return 1.0f;
        }
    }

    public static float windVolume(SimState s, ModConfig cfg) {
        if (!enabled(cfg)) {
            return 1.0f;
        }
        switch (s.surface) {
            case BLUE:
                return 1.15f;
            case PACKED:
                return 1.05f;
            case FROSTED:
                return 0.95f;
            default:
                return 1.0f;
        }
    }
}
