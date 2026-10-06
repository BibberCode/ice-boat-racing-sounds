package de.codingbibber.iceboatracesounds.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

/**
 * Ein einzelner steuerbarer Endlos-Loop. Die {@link BoatEngineAudio} setzt
 * pro Tick Volume/Pitch, die Position folgt dem Spieler von selbst.
 */
public class DynamicLoopSound extends MovingSoundInstance {

    private final MinecraftClient client;

    public DynamicLoopSound(MinecraftClient client, SoundEvent sound) {
        super(sound, SoundCategory.PLAYERS,
                net.minecraft.util.math.random.Random.create());
        this.client = client;
        this.repeat = true;
        this.repeatDelay = 0;
        this.volume = 0.0f;
        this.pitch = 1.0f;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public void finish() {
        setDone();
    }

    @Override
    public boolean canPlay() {
        return client.player != null;
    }

    /**
     * Muss {@code true} sein: Die Loops starten bewusst mit Volume 0 und
     * blenden sich ein. Ohne das würde die Sound-Engine sie beim Start als
     * „volume was zero“ still überspringen (siehe Vanilla-Minecart-Loops).
     */
    @Override
    public boolean shouldAlwaysPlay() {
        return true;
    }

    @Override
    public void tick() {
        if (client.player == null) {
            setDone();
            return;
        }
        this.x = client.player.getX();
        this.y = client.player.getY();
        this.z = client.player.getZ();
    }
}
