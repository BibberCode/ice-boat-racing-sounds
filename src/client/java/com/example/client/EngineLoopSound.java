package com.example.client;

import com.example.ModSounds;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.sound.SoundCategory;

public class EngineLoopSound extends MovingSoundInstance {

    private final MinecraftClient client;

    private float rpm = 0.7f;
    private double lastSpeed = 0;

    public EngineLoopSound(MinecraftClient client) {
        super(ModSounds.ENGINE_STAND, SoundCategory.PLAYERS,
                net.minecraft.util.math.random.Random.create());

        this.client = client;

        this.repeat = true;
        this.repeatDelay = 0;

        this.volume = 0.8f;
        this.pitch = 0.7f;
    }

    @Override
    public boolean canPlay() {
        return client.player != null &&
                client.player.getVehicle() instanceof BoatEntity;
    }

    @Override
    public void tick() {

        if (client.player == null) return;

        double speed = client.player.getVelocity().length();

        boolean gasPressed = client.options.forwardKey.isPressed();

        // 🔴 Ziel-RPM
        float targetRpm = (float)(0.7 + speed * 10.0);

        // 🔴 Gas boost (entscheidend für Auto-Gefühl)
        if (gasPressed) {
            targetRpm += 0.4f;
            this.volume += 0.02f;
        } else {
            targetRpm -= 0.2f;
            this.volume -= 0.03f;
        }

        // clamp RPM
        targetRpm = Math.max(0.6f, Math.min(targetRpm, 2.2f));

        // 🔴 smooth RPM (Motor-Trägheit)
        rpm += (targetRpm - rpm) * 0.10f;

        this.pitch = rpm;

        // 🔴 volume smoothing
        float targetVolume = (float)(gasPressed ? 1.0 : 0.6);
        this.volume += (targetVolume - this.volume) * 0.08f;

        this.volume = Math.max(0.5f, Math.min(volume, 1.2f));

        this.x = client.player.getX();
        this.y = client.player.getY();
        this.z = client.player.getZ();
    }
}