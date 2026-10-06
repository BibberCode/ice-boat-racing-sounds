package de.codingbibber.iceboatracesounds.client;

import de.codingbibber.iceboatracesounds.ModSounds;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.AbstractBoatEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.ChestBoatEntity;
import net.minecraft.entity.vehicle.ChestRaftEntity;
import net.minecraft.entity.vehicle.RaftEntity;

/**
 * Fremde Boote in der Nähe bekommen leise Motorstimmen (max. 3):
 * Entfernung steuert Lautstärke, dazu dezenter Doppler und subtile
 * Bootstyp-Profile. Erzeugt nur Sounds – ändert keine anderen Boote.
 */
public class NearbyBoatAudio {

    private static final int MAX_VOICES = 3;
    private static final float RANGE = 48.0f;

    private final MinecraftClient client;
    private final Slot[] slots = new Slot[MAX_VOICES];

    private static final class Slot {
        Entity boat;
        DynamicLoopSound voice;
    }

    public NearbyBoatAudio(MinecraftClient client) {
        this.client = client;
        for (int i = 0; i < MAX_VOICES; i++) {
            slots[i] = new Slot();
        }
    }

    /** Subtiles Klangprofil je Bootstyp (Klasse, keine Gameplay-Werte). */
    public static float profilePitch(Entity boat) {
        if (boat instanceof ChestBoatEntity) {
            return 0.94f;
        }
        if (boat instanceof ChestRaftEntity) {
            return 0.97f;
        }
        if (boat instanceof RaftEntity) {
            return 1.04f;
        }
        if (boat instanceof BoatEntity) {
            return 1.0f;
        }
        return 1.0f;
    }

    public void tick(SimState s, ModConfig cfg) {
        if (!cfg.nearbyBoatsEnabled) {
            releaseAll();
            return;
        }
        if (s.ticks % 10 == 0) {
            rescan(s);
        }
        updateVoices(s, cfg);
    }

    public void stop() {
        releaseAll();
    }

    private void rescan(SimState s) {
        if (client.player == null || client.world == null) {
            return;
        }
        Entity own = client.player.getVehicle();
        // Nächste Boote (ohne eigenes) einsammeln – ohne Allokationen.
        Entity best1 = null;
        Entity best2 = null;
        Entity best3 = null;
        double d1 = RANGE * RANGE;
        double d2 = RANGE * RANGE;
        double d3 = RANGE * RANGE;
        double px = client.player.getX();
        double py = client.player.getY();
        double pz = client.player.getZ();
        for (Object o : client.world.getEntities()) {
            if (!(o instanceof AbstractBoatEntity boat) || boat == own) {
                continue;
            }
            double dx = boat.getX() - px;
            double dy = boat.getY() - py;
            double dz = boat.getZ() - pz;
            double d = dx * dx + dy * dy + dz * dz;
            if (d < d1) {
                d3 = d2;
                best3 = best2;
                d2 = d1;
                best2 = best1;
                d1 = d;
                best1 = boat;
            } else if (d < d2) {
                d3 = d2;
                best3 = best2;
                d2 = d;
                best2 = boat;
            } else if (d < d3) {
                d3 = d;
                best3 = boat;
            }
        }
        assign(0, best1);
        assign(1, best2);
        assign(2, best3);
    }

    private void assign(int i, Entity boat) {
        Slot slot = slots[i];
        if (slot.boat == boat) {
            return;
        }
        if (slot.voice != null) {
            client.getSoundManager().stop(slot.voice);
            slot.voice = null;
        }
        slot.boat = boat;
        if (boat != null) {
            DynamicLoopSound voice = new DynamicLoopSound(client, ModSounds.ENGINE_LOOP_LOW);
            voice.setFollow(boat);
            client.getSoundManager().play(voice);
            slot.voice = voice;
        }
    }

    private void updateVoices(SimState s, ModConfig cfg) {
        if (client.player == null) {
            return;
        }
        double px = client.player.getX();
        double py = client.player.getY();
        double pz = client.player.getZ();
        var myV = client.player.getVelocity();
        for (Slot slot : slots) {
            if (slot.boat == null || slot.voice == null) {
                continue;
            }
            if (slot.boat.isRemoved()) {
                release(slot);
                continue;
            }
            double dx = slot.boat.getX() - px;
            double dy = slot.boat.getY() - py;
            double dz = slot.boat.getZ() - pz;
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (dist > RANGE) {
                release(slot);
                continue;
            }
            var theirV = slot.boat.getVelocity();
            double theirSpeed = theirV.length() * 20.0;
            double speedN = clamp(theirSpeed / 40.0, 0.0, 1.0);

            // Doppler: Annäherung leicht höher, Entfernung leicht tiefer.
            double doppler = 1.0;
            if (cfg.dopplerEnabled && dist > 0.001) {
                double nx = dx / dist;
                double ny = dy / dist;
                double nz = dz / dist;
                double closing = (myV.x - theirV.x) * nx
                        + (myV.y - theirV.y) * ny
                        + (myV.z - theirV.z) * nz;
                doppler = 1.0 + clamp(closing / 343.0, -0.04, 0.04);
            }

            float profile = profilePitch(slot.boat);
            slot.voice.setPitch((float) ((0.85 + speedN * 0.5) * profile * doppler));
            float closeness = (float) (1.0 - dist / RANGE);
            slot.voice.setVolume(cfg.nearbyVolume * closeness * closeness * 0.7f * (float) (0.3 + 0.7 * speedN));
        }
    }

    private void release(Slot slot) {
        if (slot.voice != null) {
            client.getSoundManager().stop(slot.voice);
            slot.voice = null;
        }
        slot.boat = null;
    }

    private void releaseAll() {
        for (Slot slot : slots) {
            release(slot);
        }
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(value, max));
    }
}
