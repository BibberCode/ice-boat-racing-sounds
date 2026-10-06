package de.codingbibber.iceboatracesounds.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

/**
 * Spielt kurze One-Shot-Effekte positionsbezogen am Spieler mit einstellbarer
 * Lautstärke. Nur Audio – keine Gameplay-Wirkung.
 */
public final class OneShot {

    private OneShot() {
    }

    public static void play(MinecraftClient client, SoundEvent event, float volume, float pitch) {
        if (client.player == null || volume <= 0.001f) {
            return;
        }
        client.getSoundManager().play(new PositionedSoundInstance(
                event,
                SoundCategory.PLAYERS,
                volume,
                pitch,
                SoundInstance.createRandom(),
                client.player.getX(),
                client.player.getY(),
                client.player.getZ()));
    }
}
