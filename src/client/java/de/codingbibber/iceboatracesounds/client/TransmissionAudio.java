package de.codingbibber.iceboatracesounds.client;

import de.codingbibber.iceboatracesounds.ModSounds;
import net.minecraft.client.MinecraftClient;

import java.util.Random;

/**
 * Getriebe-Simulation (reine Audio-Simulation): Gänge aus Tempo, hörbare
 * Schaltvorgänge mit Cooldown, Rev-Matching-Blip beim Runterschalten,
 * Drehzahleinbruch beim Hochschalten und simulierter Rev-Limiter.
 * Beeinflusst niemals Tempo oder Physik.
 */
public class TransmissionAudio {

    private static final int GEARS = 5;
    private static final float TOP_SPEED_MS = 40.0f;
    private static final float SHIFT_DIP = 0.35f;
    private static final float SHIFT_DIP_DECAY = 0.12f;
    private static final float REV_BLIP_DECAY = 0.15f;
    private static final int SHIFT_COOLDOWN_TICKS = 12;

    private final Random random = new Random();
    private int lastShiftTick = -100;

    public void tick(MinecraftClient client, SimState s, ModConfig cfg) {
        float span = TOP_SPEED_MS / GEARS;
        int newGear = clampInt((int) (s.speedMs / span) + 1, 1, GEARS);
        if (newGear != s.gear && s.ticks - lastShiftTick > SHIFT_COOLDOWN_TICKS) {
            lastShiftTick = s.ticks;
            if (newGear > s.gear) {
                // Hochschalten: kurzer RPM-Drop + Schaltknall.
                s.shiftDip = 1.0f;
                float pitch = 0.9f + random.nextFloat() * 0.2f;
                OneShot.play(client, ModSounds.ENGINE_SHIFT, cfg.shiftVolume, pitch);
            } else {
                // Runterschalten: Rev-Matching-Blip + dumpferer Schaltlaut.
                s.revBlip = 1.0f;
                float pitch = 1.05f + random.nextFloat() * 0.15f;
                OneShot.play(client, ModSounds.ENGINE_SHIFT, cfg.shiftVolume * 0.8f, pitch);
            }
            s.gear = newGear;
        }
        s.shiftDip = Math.max(0.0f, s.shiftDip - SHIFT_DIP_DECAY);
        s.revBlip = Math.max(0.0f, s.revBlip - REV_BLIP_DECAY);
        s.rpmAdd = s.revBlip * 0.30f - s.shiftDip * SHIFT_DIP;

        float inGear = clamp((s.speedMs - (s.gear - 1) * span) / span, 0.0f, 1.0f);
        s.inGear = inGear;

        // Simulierter Rev-Limiter (stottert in EngineAudio via limiterCut).
        s.limiterActive = s.gear == GEARS && inGear > 0.85f && s.gas;
        s.limiterCut = 1.0f;
        if (s.limiterActive) {
            s.limiterCut = (float) Math.sin(s.ticks * 2.2f) > 0.2f ? 1.0f : 0.45f;
        }
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(value, max));
    }

    private static int clampInt(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }
}
