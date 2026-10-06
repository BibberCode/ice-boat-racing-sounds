package de.codingbibber.iceboatracesounds.client;

import de.codingbibber.iceboatracesounds.ModSounds;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;

/**
 * Racing-Dynamik für Bootsmotoren: simuliert Gänge (hörbare Schaltvorgänge
 * mit Schalt-Sound, auch beim Runterschalten), Drehzahl mit Motor-Trägheit,
 * Drehzahl-Atmung, Begrenzer-Stottern und Schubknallen bei Gaswegnahme,
 * dazu zwei Motor-Layer im Crossfade, böiger Fahrtwind und Eis-Slide.
 *
 * Eine Instanz lebt genau so lange, wie der Spieler im Boot sitzt.
 */
public class BoatEngineAudio {

    private static final int GEARS = 5;
    /** Volle Drehzahl ist auf Eis schnell erreicht – kurze Gänge für viele Schaltvorgänge. */
    private static final float TOP_SPEED_MS = 40.0f;
    /** Wind ist schon bei mittlerem Tempo voll da (eigene Referenz). */
    private static final float WIND_TOP_MS = 35.0f;

    private static final float RPM_SMOOTHING = 0.10f;
    private static final float GAS_BONUS = 0.22f;
    private static final float COAST_MALUS = 0.12f;
    private static final float SHIFT_DIP = 0.35f;
    private static final float SHIFT_DIP_DECAY = 0.12f;

    private static final float OFF_ICE_FACTOR = 0.7f;
    private static final float IDLE_THROTTLE_VOL = 0.5f;

    private final MinecraftClient client;
    private final DynamicLoopSound lowLoop;
    private final DynamicLoopSound highLoop;
    private final DynamicLoopSound windLoop;
    private final DynamicLoopSound skidLoop;

    private int gear = 1;
    private float rpm01;
    private float engineGain;
    private float shiftDip;
    private int ticks;
    private boolean wasGas;
    private boolean limiterActive;
    private int popsLeft;
    private int popTimer;
    private float gust = 1.0f;
    private float gustTarget = 1.0f;

    public BoatEngineAudio(MinecraftClient client) {
        this.client = client;
        this.lowLoop = new DynamicLoopSound(client, ModSounds.ENGINE_LOOP_LOW);
        this.highLoop = new DynamicLoopSound(client, ModSounds.ENGINE_LOOP_HIGH);
        this.windLoop = new DynamicLoopSound(client, ModSounds.WIND_LOOP);
        this.skidLoop = new DynamicLoopSound(client, ModSounds.SKID_LOOP);
        client.getSoundManager().play(lowLoop);
        client.getSoundManager().play(highLoop);
        client.getSoundManager().play(windLoop);
        client.getSoundManager().play(skidLoop);
    }

    /** Wird jeden Client-Tick aufgerufen, solange der Spieler im Boot sitzt. */
    public void tick() {
        if (client.player == null || client.world == null) {
            return;
        }
        ticks++;

        double speedBlocksPerTick = client.player.getVelocity().length();
        float speedMs = (float) (speedBlocksPerTick * 20.0);
        boolean gas = client.options.forwardKey.isPressed();

        updateGear(speedMs, gas);
        updateRpm(speedMs, gas);
        updateEngineLayers(gas);
        updateWind(speedMs);
        updateSkid(speedMs);
        updateOverrunPops(gas);
        wasGas = gas;
    }

    public void stop() {
        client.getSoundManager().stop(lowLoop);
        client.getSoundManager().stop(highLoop);
        client.getSoundManager().stop(windLoop);
        client.getSoundManager().stop(skidLoop);
    }

    private void updateGear(float speedMs, boolean gas) {
        float span = TOP_SPEED_MS / GEARS;
        int newGear = clampInt((int) (speedMs / span) + 1, 1, GEARS);
        if (newGear != gear) {
            if (engineGain > 0.5f) {
                if (newGear > gear) {
                    // Hochschalten knallt (mit leicht zufälliger Tonhöhe).
                    float pitch = 0.9f + (float) Math.random() * 0.2f;
                    client.getSoundManager().play(PositionedSoundInstance.master(ModSounds.ENGINE_SHIFT, pitch));
                } else {
                    // Runterschalten gibt einen dumpferen Zwischengas-Blip.
                    float pitch = 0.7f + (float) Math.random() * 0.15f;
                    client.getSoundManager().play(PositionedSoundInstance.master(ModSounds.ENGINE_SHIFT, pitch));
                }
            }
            gear = newGear;
            shiftDip = 1.0f;
        }
        shiftDip = Math.max(0.0f, shiftDip - SHIFT_DIP_DECAY);
    }

    private void updateRpm(float speedMs, boolean gas) {
        float span = TOP_SPEED_MS / GEARS;
        float inGear = clamp((speedMs - (gear - 1) * span) / span, 0.0f, 1.0f);

        float target = inGear + (gas ? GAS_BONUS : -COAST_MALUS);
        target -= shiftDip * SHIFT_DIP;

        // Lebendigkeit: Die Drehzahl atmet deutlich (Lastwechsel, Wind).
        target += (float) Math.sin(ticks * 0.11f) * 0.030f
                + (float) Math.sin(ticks * 0.031f + 1.7f) * 0.035f
                + (float) (Math.random() - 0.5) * 0.015f;

        // Begrenzer: Im letzten Gang bei hohem Tempo stottert die Zündung.
        limiterActive = gear == GEARS && inGear > 0.85f && gas;
        if (limiterActive) {
            target += (float) Math.sin(ticks * 1.4f) * 0.06f;
        }

        rpm01 += (clamp(target, 0.0f, 1.0f) - rpm01) * RPM_SMOOTHING;
        // Loop-Blend nach dem Start-Sound (ca. 1,2 s).
        engineGain = Math.min(1.0f, engineGain + 0.04f);
    }

    private void updateEngineLayers(boolean gas) {
        float iceVol = isBoatOnIce() ? 1.0f : OFF_ICE_FACTOR;
        float throttleVol = gas ? 1.0f : IDLE_THROTTLE_VOL;
        // Auspuff-Pulsieren: Der Motor wirkt weniger statisch.
        float pulse = 1.0f + (float) Math.sin(ticks * 0.4f) * 0.05f;
        // Begrenzer-Stottern: Zündung setzt hörbar aus (braaap-braaap).
        float cut = 1.0f;
        if (limiterActive) {
            cut = (float) Math.sin(ticks * 2.2f) > 0.2f ? 1.0f : 0.45f;
        }

        lowLoop.setPitch(0.65f + rpm01 * 0.75f);
        lowLoop.setVolume(engineGain * (1.0f - rpm01 * 0.75f) * throttleVol * iceVol * 0.95f * pulse * cut);

        highLoop.setPitch(0.65f + rpm01 * 1.10f + (float) Math.sin(ticks * 0.23f) * 0.02f);
        highLoop.setVolume(engineGain * (0.05f + rpm01 * 0.95f) * throttleVol * iceVol * pulse * cut);
    }

    private void updateWind(float speedMs) {
        float speed01 = clamp(speedMs / WIND_TOP_MS, 0.0f, 1.0f);
        // Böen: neues Zufallsziel, dem die Stärke träge folgt.
        if (ticks % 25 == 0) {
            gustTarget = 0.6f + (float) Math.random() * 0.8f;
        }
        gust += (gustTarget - gust) * 0.08f;
        windLoop.setPitch(0.7f + speed01 * 1.0f);
        windLoop.setVolume((float) Math.pow(speed01, 1.5) * 0.9f * gust);
    }

    private void updateSkid(float speedMs) {
        float speed01 = clamp(speedMs / WIND_TOP_MS, 0.0f, 1.0f);
        double lateralMs = lateralSpeedMs();
        float slide01 = clamp((((float) lateralMs) - 1.2f) / 6.0f, 0.0f, 1.0f);
        skidLoop.setPitch(0.9f + slide01 * 0.3f);
        skidLoop.setVolume(slide01 * 0.8f * clamp(speed01 * 3.0f, 0.0f, 1.0f));
    }

    /** Schubknallen: Wer bei Drehzahl vom Gas geht, bekommt 1–3 Pops. */
    private void updateOverrunPops(boolean gas) {
        if (wasGas && !gas && rpm01 > 0.35f && engineGain > 0.5f) {
            popsLeft = 1 + (int) (Math.random() * 3.0);
            popTimer = 2;
        }
        if (popsLeft > 0) {
            popTimer--;
            if (popTimer <= 0) {
                float pitch = 0.8f + (float) Math.random() * 0.4f;
                client.getSoundManager().play(PositionedSoundInstance.master(ModSounds.ENGINE_BACKFIRE, pitch));
                popsLeft--;
                popTimer = 2 + (int) (Math.random() * 4.0);
            }
        }
    }

    /** Seitliche Geschwindigkeit (Drift) in m/s aus Blickrichtung vs. Bewegung. */
    private double lateralSpeedMs() {
        if (client.player == null) {
            return 0.0;
        }
        var v = client.player.getVelocity();
        double yawRad = Math.toRadians(client.player.getYaw());
        double fx = -Math.sin(yawRad);
        double fz = Math.cos(yawRad);
        double forward = v.x * fx + v.z * fz;
        double latX = v.x - forward * fx;
        double latZ = v.z - forward * fz;
        return Math.sqrt(latX * latX + latZ * latZ) * 20.0;
    }

    private boolean isBoatOnIce() {
        if (client.world == null || client.player == null) {
            return false;
        }
        return IceCheck.isBoatOnIce(client.world, client.player.getVehicle());
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(value, max));
    }

    private static int clampInt(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }
}
