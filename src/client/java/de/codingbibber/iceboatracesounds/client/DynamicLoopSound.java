package de.codingbibber.iceboatracesounds.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

/**
 * Ein einzelner steuerbarer Endlos-Loop. Die Audio-Module setzen pro Tick
 * Volume/Pitch; die Position folgt wahlweise dem Spieler oder per
 * {@link #setFollow(Entity)} einer anderen Entity (z. B. fremde Boote).
 */
public class DynamicLoopSound extends MovingSoundInstance {

    private final MinecraftClient client;
    private Entity follow;

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

    /** Folgt ab jetzt dieser Entity statt dem Spieler (null = Spieler). */
    public void setFollow(Entity follow) {
        this.follow = follow;
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
        Entity target = follow != null ? follow : client.player;
        if (target == null || (follow != null && follow.isRemoved())) {
            setDone();
            return;
        }
        this.x = target.getX();
        this.y = target.getY();
        this.z = target.getZ();
    }
}
