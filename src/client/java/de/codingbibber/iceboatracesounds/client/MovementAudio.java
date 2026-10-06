package de.codingbibber.iceboatracesounds.client;

import de.codingbibber.iceboatracesounds.ModSounds;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

import java.util.Random;

/**
 * Fahrtwind (mit Böen), Eis-Slide/Drift in drei Stufen und Umgebungs-Offenheit
 * (offene Fläche vs. enge Bereiche). Alles nur Audio-Modulation.
 */
public class MovementAudio {

    private static final float WIND_TOP_MS = 35.0f;

    private final MinecraftClient client;
    private final DynamicLoopSound windLoop;
    private final DynamicLoopSound skidLoop;
    private final Random random = new Random();

    private float gust = 1.0f;
    private float gustTarget = 1.0f;
    private float lastWindVol;
    private float lastSkidVol;

    public MovementAudio(MinecraftClient client) {
        this.client = client;
        this.windLoop = new DynamicLoopSound(client, ModSounds.WIND_LOOP);
        this.skidLoop = new DynamicLoopSound(client, ModSounds.SKID_LOOP);
        client.getSoundManager().play(windLoop);
        client.getSoundManager().play(skidLoop);
    }

    public void tick(SimState s, ModConfig cfg, float fade) {
        updateSlide(s);
        updateWind(s, cfg, fade);
        updateSkid(s, cfg, fade);
        if (s.ticks % 10 == 0) {
            updateOpenness(s);
        }
    }

    public void stop() {
        client.getSoundManager().stop(windLoop);
        client.getSoundManager().stop(skidLoop);
    }

    public float getLastWindVol() {
        return lastWindVol;
    }

    public float getLastSkidVol() {
        return lastSkidVol;
    }

    private void updateSlide(SimState s) {
        if (client.player == null) {
            s.slide01 = 0.0f;
            s.lateralMs = 0.0f;
            return;
        }
        var v = client.player.getVelocity();
        double yawRad = Math.toRadians(client.player.getYaw());
        double fx = -Math.sin(yawRad);
        double fz = Math.cos(yawRad);
        double forward = v.x * fx + v.z * fz;
        double latX = v.x - forward * fx;
        double latZ = v.z - forward * fz;
        s.lateralMs = (float) (Math.sqrt(latX * latX + latZ * latZ) * 20.0);
        s.slide01 = clamp((s.lateralMs - 1.2f) / 6.0f, 0.0f, 1.0f);
    }

    private void updateWind(SimState s, ModConfig cfg, float fade) {
        float speed01 = clamp(s.speedMs / WIND_TOP_MS, 0.0f, 1.0f);
        if (s.ticks % 25 == 0) {
            gustTarget = 0.6f + random.nextFloat() * 0.8f;
        }
        gust += (gustTarget - gust) * 0.08f;
        windLoop.setPitch(0.7f + speed01 * 1.0f);
        float vol = (float) Math.pow(speed01, 1.5) * 0.9f * gust
                * SurfaceAudio.windVolume(s, cfg) * cfg.windVolume * fade;
        windLoop.setVolume(vol);
        lastWindVol = vol;
    }

    private void updateSkid(SimState s, ModConfig cfg, float fade) {
        float speed01 = clamp(s.speedMs / WIND_TOP_MS, 0.0f, 1.0f);
        // Drei Stufen: leichtes Kratzen, Rutschen, voller Drift (lauter + höher).
        float warble = 1.0f + (float) Math.sin(s.ticks * 3.1f) * 0.05f * s.slide01;
        skidLoop.setPitch((0.9f + s.slide01 * 0.3f + SurfaceAudio.skidPitchAdd(s, cfg)) * warble);
        float vol = s.slide01 * 0.8f * clamp(speed01 * 3.0f, 0.0f, 1.0f)
                * SurfaceAudio.skidVolume(s, cfg) * cfg.skidVolume * fade;
        skidLoop.setVolume(vol);
        lastSkidVol = vol;
    }

    /** Offenheit: 1.0 = freie Fläche, 0.0 = eng umbaut (z. B. Tunnel). */
    private void updateOpenness(SimState s) {
        if (client.world == null || client.player == null) {
            s.openness = 1.0f;
            return;
        }
        BlockPos center = client.player.getBlockPos();
        int air = 0;
        int total = 0;
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = 0; dy <= 2; dy++) {
                for (int dz = -2; dz <= 2; dz++) {
                    total++;
                    if (client.world.getBlockState(center.add(dx, dy, dz)).isAir()) {
                        air++;
                    }
                }
            }
        }
        s.openness = (float) air / (float) total;
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(value, max));
    }
}
