package de.codingbibber.iceboatracesounds.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Optionaler Audio-Debug-Modus (per Config aktivierbar). Gibt ausschließlich
 * Audio-Telemetrie ins Log aus – kein HUD, keine Gameplay-Funktion.
 */
public final class AudioDebug {

    private static final Logger LOGGER = LoggerFactory.getLogger("ice_boat_racing_sounds");

    private AudioDebug() {
    }

    public static void logTick(SimState s, float engineVol, float windVol, float skidVol) {
        LOGGER.info("[ice-boat-racing-sounds] audio rpm={} gear={}/5 load={} gas={} speed={}m/s "
                        + "slide={} surface={} open={} engVol={} windVol={} skidVol={} limiter={}",
                fmt(s.rpm01), s.gear, fmt(s.load01), s.gas, fmt(s.speedMs),
                fmt(s.slide01), s.surface, fmt(s.openness),
                fmt(engineVol), fmt(windVol), fmt(skidVol), s.limiterActive);
    }

    private static String fmt(float value) {
        return String.format(java.util.Locale.ROOT, "%.2f", value);
    }
}
