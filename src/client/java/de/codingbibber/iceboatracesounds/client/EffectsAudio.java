package de.codingbibber.iceboatracesounds.client;

import de.codingbibber.iceboatracesounds.ModSounds;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;

import java.util.Random;

/**
 * Kurze Effekt-Sounds mit Cooldowns (kein Spam): Backfire mit Intensitäten,
 * Blow-off bei hartem Gaswegnehmen, leichte/schwere Kollisionen und Landungen.
 * Reagiert nur auf beobachtete Vanilla-Bewegung – greift nie in Physik ein.
 */
public class EffectsAudio {

    private final Random random = new Random();

    private float lastLoad;
    private int popsLeft;
    private int popTimer;
    private int lastBackfireTick = -100;
    private int lastBlowoffTick = -100;
    private int lastImpactTick = -100;
    private int lastLandingTick = -100;
    private boolean prevGrounded = true;

    public void tick(MinecraftClient client, SimState s, ModConfig cfg) {
        updateBackfire(client, s, cfg);
        updateBlowoff(client, s, cfg);
        updateImpact(client, s, cfg);
        updateLanding(client, s, cfg);
        lastLoad = s.load01;
    }

    private void updateBackfire(MinecraftClient client, SimState s, ModConfig cfg) {
        if (popsLeft <= 0) {
            // Nur nach hoher Last + Gaswegnehmen, mit Cooldown und Intensität.
            boolean lifted = s.wasGas && !s.gas;
            if (lifted && s.rpm01 > 0.4f && lastLoad > 0.55f
                    && s.ticks - lastBackfireTick > 15) {
                lastBackfireTick = s.ticks;
                boolean big = lastLoad > 0.8f;
                popsLeft = big ? 2 + random.nextInt(2) : 1 + random.nextInt(2);
                popTimer = 2;
            }
        }
        if (popsLeft > 0) {
            popTimer--;
            if (popTimer <= 0) {
                float pitch = 0.8f + random.nextFloat() * 0.4f;
                OneShot.play(client, ModSounds.ENGINE_BACKFIRE, cfg.backfireVolume, pitch);
                popsLeft--;
                popTimer = 2 + random.nextInt(4);
            }
        }
    }

    private void updateBlowoff(MinecraftClient client, SimState s, ModConfig cfg) {
        // Hartes Gaswegnehmen bei hoher Drehzahl und Last.
        if (s.wasGas && !s.gas && s.rpm01 > 0.7f && lastLoad > 0.7f
                && s.ticks - lastBlowoffTick > 40) {
            lastBlowoffTick = s.ticks;
            float pitch = 0.95f + random.nextFloat() * 0.15f;
            OneShot.play(client, ModSounds.BLOWOFF, cfg.effectsVolume, pitch);
        }
    }

    private void updateImpact(MinecraftClient client, SimState s, ModConfig cfg) {
        // Zwei unabhängige Signale (ODER): Das Client-Boot wird vom Server
        // bewegt, daher ist horizontalCollision dort nicht verlässlich –
        // ein harter Tempoverlust im selben Tick verrät die Wand ebenfalls.
        // Intensität nach Tempo/Verlust, Cooldown gegen Dauergeräusche.
        if (client.player == null || client.player.getVehicle() == null) {
            return;
        }
        Entity boat = client.player.getVehicle();
        boolean collided = boat.horizontalCollision;
        boolean slammed = s.speedDrop > 4.0f && s.speedDrop < 30.0f;
        if (!collided && !slammed) {
            return;
        }
        if (s.ticks - lastImpactTick <= 20) {
            return;
        }
        lastImpactTick = s.ticks;
        s.duck = 0.35f;
        float pitch = 0.9f + random.nextFloat() * 0.2f;
        if (s.speedMs > 12.0f || s.speedDrop > 9.0f) {
            OneShot.play(client, ModSounds.IMPACT_HEAVY, cfg.effectsVolume, pitch);
        } else if (s.speedMs > 3.0f) {
            OneShot.play(client, ModSounds.IMPACT_LIGHT, cfg.effectsVolume, pitch);
        }
    }

    private void updateLanding(MinecraftClient client, SimState s, ModConfig cfg) {
        boolean grounded = isGrounded(client);
        // Bodenkontakt nach Flugphase mit Falltempo (kein Wasser: dort platscht Vanilla).
        if (!prevGrounded && grounded && s.vyMs < -2.5f
                && s.ticks - lastLandingTick > 30 && !isOverWater(client)) {
            lastLandingTick = s.ticks;
            s.duck = 0.5f;
            float pitch = 0.9f + random.nextFloat() * 0.2f;
            float vol = cfg.effectsVolume * clamp(-s.vyMs / 10.0f, 0.4f, 1.0f);
            OneShot.play(client, ModSounds.LANDING, vol, pitch);
        }
        prevGrounded = grounded;
    }

    private boolean isGrounded(MinecraftClient client) {
        if (client.world == null || client.player == null
                || client.player.getVehicle() == null) {
            return true;
        }
        var below = client.world.getBlockState(
                client.player.getVehicle().getBlockPos().down());
        return !below.isAir();
    }

    private boolean isOverWater(MinecraftClient client) {
        if (client.world == null || client.player == null
                || client.player.getVehicle() == null) {
            return false;
        }
        var below = client.world.getBlockState(
                client.player.getVehicle().getBlockPos().down());
        return below.isOf(Blocks.WATER);
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(value, max));
    }
}
