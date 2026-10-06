package de.codingbibber.iceboatracesounds.client;

import de.codingbibber.iceboatracesounds.ModSounds;
import net.minecraft.client.MinecraftClient;

/**
 * Motor-Simulation: Drehzahl mit Trägheit, simulierte Engine Load
 * (Beschleunigung/Gas/Tempo, nur beobachtet), Launch-Boost, vier
 * RPM-Layer im weichen Crossfade, Turbo-Whine und Overrun-Färbung.
 * Schreibt nur Volume/Pitch – niemals Physik.
 */
public class EngineAudio {

    private static final float GAS_BONUS = 0.10f;
    private static final float COAST_MALUS = 0.05f;
    private static final float RPM_SMOOTHING = 0.10f;
    private static final float LOAD_SMOOTHING = 0.12f;

    private final MinecraftClient client;
    private final DynamicLoopSound lowLoop;
    private final DynamicLoopSound midLoop;
    private final DynamicLoopSound highLoop;
    private final DynamicLoopSound redlineLoop;
    private final DynamicLoopSound turboLoop;

    private float engineGain;
    private float lastVolume;

    public EngineAudio(MinecraftClient client) {
        this.client = client;
        this.lowLoop = new DynamicLoopSound(client, ModSounds.ENGINE_LOOP_LOW);
        this.midLoop = new DynamicLoopSound(client, ModSounds.ENGINE_LOOP_MID);
        this.highLoop = new DynamicLoopSound(client, ModSounds.ENGINE_LOOP_HIGH);
        this.redlineLoop = new DynamicLoopSound(client, ModSounds.ENGINE_LOOP_REDLINE);
        this.turboLoop = new DynamicLoopSound(client, ModSounds.TURBO_LOOP);
        client.getSoundManager().play(lowLoop);
        client.getSoundManager().play(midLoop);
        client.getSoundManager().play(highLoop);
        client.getSoundManager().play(redlineLoop);
        client.getSoundManager().play(turboLoop);
    }

    public void tick(SimState s, ModConfig cfg, float fade) {
        // Ducking-Erholung nach Transienten (Impact/Landing).
        s.duck += (1.0f - s.duck) * 0.10f;
        updateLoad(s);
        updateRpm(s);
        updateLayers(s, cfg, fade);
        engineGain = Math.min(1.0f, engineGain + 0.04f);
    }

    public void stop() {
        client.getSoundManager().stop(lowLoop);
        client.getSoundManager().stop(midLoop);
        client.getSoundManager().stop(highLoop);
        client.getSoundManager().stop(redlineLoop);
        client.getSoundManager().stop(turboLoop);
    }

    public float getLastVolume() {
        return lastVolume;
    }

    private void updateLoad(SimState s) {
        float accelN = clamp(s.accel / 12.0f, -1.0f, 1.0f);
        float speedN = clamp(s.speedMs / 40.0f, 0.0f, 1.0f);
        float raw = 0.30f
                + (s.gas ? 0.20f : 0.0f)
                + Math.max(0.0f, accelN) * 0.45f
                + speedN * 0.20f
                - Math.max(0.0f, -accelN) * 0.25f;
        s.load01 += (clamp(raw, 0.0f, 1.0f) - s.load01) * LOAD_SMOOTHING;

        // Launch: starkes Losfahren aus langsamem Tempo.
        if (s.gas && s.speedMs < 6.0f && s.accel > 6.0f) {
            s.launchBoost = 1.0f;
        }
        s.launchBoost = Math.max(0.0f, s.launchBoost - 0.05f);
    }

    private void updateRpm(SimState s) {
        // Gas-Rev: W dreht den Motor immer hörbar hoch (auch im Stand),
        // fällt beim Loslassen schnell zurück. Tempo bleibt die Basis.
        if (s.gas) {
            s.throttleRev = Math.min(0.45f, s.throttleRev + 0.03f);
        } else {
            s.throttleRev = Math.max(0.0f, s.throttleRev - 0.05f);
        }

        float target = s.inGear + (s.gas ? GAS_BONUS : -COAST_MALUS) + s.rpmAdd + s.throttleRev;

        // Drehzahl-Atmung (Lastwechsel), damit Vollgas nicht starr klingt.
        target += (float) Math.sin(s.ticks * 0.11f) * 0.030f
                + (float) Math.sin(s.ticks * 0.031f + 1.7f) * 0.035f
                + (float) (Math.random() - 0.5) * 0.015f;

        s.rpm01 += (clamp(target, 0.0f, 1.0f) - s.rpm01) * RPM_SMOOTHING;
    }

    private void updateLayers(SimState s, ModConfig cfg, float fade) {
        float rpm = s.rpm01;
        float loadPitch = 1.0f + (s.load01 - 0.5f) * 0.12f;
        float loadVol = 0.8f + s.load01 * 0.4f;
        float throttleVol = s.gas ? 1.0f : 0.78f;
        float surfPitch = SurfaceAudio.enginePitch(s, cfg);
        float surfVol = SurfaceAudio.engineVolume(s, cfg);
        float pulse = 1.0f + (float) Math.sin(s.ticks * 0.4f) * 0.05f;
        float launchVol = 1.0f + s.launchBoost * 0.3f;
        // Enge Umgebung trägt den Motor (Tunnel-Gefühl), ganz dezent.
        float roomGain = 1.0f + (1.0f - s.openness) * 0.15f;
        float common = engineGain * fade * cfg.engineVolume * throttleVol * loadVol * surfVol * pulse * launchVol * s.limiterCut * roomGain * s.duck;

        // Weiche Layer-Gewichte (stetig, keine Klicks/Sprünge).
        float wLow = clamp(1.0f - rpm / 0.45f, 0.0f, 1.0f);
        float wMid = clamp(1.0f - Math.abs(rpm - 0.45f) / 0.35f, 0.0f, 1.0f);
        float wHigh = clamp(1.0f - Math.abs(rpm - 0.72f) / 0.28f, 0.0f, 1.0f);
        float wRed = clamp((rpm - 0.80f) / 0.20f, 0.0f, 1.0f);

        float typePitch = s.boatPitch * surfPitch * loadPitch;

        lowLoop.setPitch((0.65f + rpm * 0.50f) * typePitch);
        lowLoop.setVolume(common * wLow * 0.95f);

        midLoop.setPitch((0.70f + rpm * 0.60f) * typePitch);
        midLoop.setVolume(common * wMid * 0.90f);

        highLoop.setPitch((0.65f + rpm * 0.80f + (float) Math.sin(s.ticks * 0.23f) * 0.02f) * typePitch);
        highLoop.setVolume(common * (0.05f + wHigh * 0.95f));

        redlineLoop.setPitch((0.80f + rpm * 0.90f) * typePitch);
        redlineLoop.setVolume(common * wRed);

        // Dezentes Turbo-Jaulen unter Last und Drehzahl.
        turboLoop.setPitch((0.80f + s.load01 * rpm * 0.80f) * typePitch);
        turboLoop.setVolume(common * cfg.turboVolume * s.load01 * rpm * rpm * 0.5f);

        lastVolume = common * Math.max(Math.max(wLow, wMid), Math.max(wHigh, wRed));
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(value, max));
    }
}
