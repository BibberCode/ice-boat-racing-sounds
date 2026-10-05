package de.codingbibber.iceboatracesounds.client;

import de.codingbibber.iceboatracesounds.ModSounds;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.AbstractBoatEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;

/**
 * Endlos-Loop für das Motorgeräusch. Pitch folgt der Bootsgeschwindigkeit
 * (Blöcke/Tick), Volume folgt Gaspedal + Untergrund (Eis = volle Lautstärke).
 */
public class EngineLoopSound extends MovingSoundInstance {

    private static final float BASE_PITCH = 0.75f;
    private static final float SPEED_FACTOR = 1.1f;
    private static final float GAS_BOOST = 0.25f;
    private static final float MIN_PITCH = 0.6f;
    private static final float MAX_PITCH = 2.0f;
    private static final float RPM_SMOOTHING = 0.08f;

    private static final float IDLE_VOLUME = 0.55f;
    private static final float GAS_VOLUME = 0.9f;
    private static final float OFF_ICE_FACTOR = 0.7f;
    private static final float MIN_VOLUME = 0.35f;
    private static final float MAX_VOLUME = 1.0f;
    private static final float VOLUME_SMOOTHING = 0.08f;

    private final MinecraftClient client;
    private float rpm = BASE_PITCH;

    public EngineLoopSound(MinecraftClient client) {
        super(ModSounds.ENGINE_STAND, SoundCategory.PLAYERS,
                net.minecraft.util.math.random.Random.create());
        this.client = client;
        this.repeat = true;
        this.repeatDelay = 0;
        this.volume = IDLE_VOLUME;
        this.pitch = BASE_PITCH;
    }

    @Override
    public boolean canPlay() {
        return client.player != null
                && client.player.getVehicle() instanceof AbstractBoatEntity;
    }

    @Override
    public void tick() {
        if (!canPlay()) {
            // Sorgt dafür, dass der SoundManager den Loop auch ohne
            // explizites stop() aufräumt (z.B. Tod, Dimensionwechsel).
            setDone();
            return;
        }

        double speed = client.player.getVelocity().length(); // Blöcke pro Tick
        boolean gasPressed = client.options.forwardKey.isPressed();

        float targetRpm = (float) (BASE_PITCH + speed * SPEED_FACTOR);
        if (gasPressed) {
            targetRpm += GAS_BOOST;
        }
        targetRpm = clamp(targetRpm, MIN_PITCH, MAX_PITCH);

        // Motor-Trägheit: RPM folgt dem Ziel verzögert
        rpm += (targetRpm - rpm) * RPM_SMOOTHING;
        this.pitch = rpm;

        float targetVolume = (gasPressed ? GAS_VOLUME : IDLE_VOLUME)
                * (isBoatOnIce() ? 1.0f : OFF_ICE_FACTOR);
        this.volume += (targetVolume - this.volume) * VOLUME_SMOOTHING;
        this.volume = clamp(this.volume, MIN_VOLUME, MAX_VOLUME);

        this.x = client.player.getX();
        this.y = client.player.getY();
        this.z = client.player.getZ();
    }

    private boolean isBoatOnIce() {
        if (client.world == null || client.player == null) {
            return false;
        }
        Entity vehicle = client.player.getVehicle();
        if (!(vehicle instanceof AbstractBoatEntity boat)) {
            return false;
        }
        BlockPos below = boat.getBlockPos().down();
        var state = client.world.getBlockState(below);
        return state.isOf(Blocks.ICE)
                || state.isOf(Blocks.PACKED_ICE)
                || state.isOf(Blocks.BLUE_ICE)
                || state.isOf(Blocks.FROSTED_ICE);
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(value, max));
    }
}
