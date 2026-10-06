package de.codingbibber.iceboatracesounds.client.mixin;

import de.codingbibber.iceboatracesounds.client.IceCheck;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.AbstractBoatEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Schaltet die Vanilla-Paddelgeräusche stumm, solange das Boot des Spielers
 * auf Eis fährt – dort ersetzt der Mod-Motor sie. Auf Wasser paddelt man
 * normal weiter.
 */
@Mixin(SoundManager.class)
public class BoatSoundMuteMixin {

    private static final Identifier PADDLE_WATER_ID = Identifier.of("minecraft", "entity.boat.paddle_water");
    private static final Identifier PADDLE_LAND_ID = Identifier.of("minecraft", "entity.boat.paddle_land");
    private static final double MUTE_RADIUS = 24.0;

    @Inject(method = "play(Lnet/minecraft/client/sound/SoundInstance;)V", at = @At("HEAD"), cancellable = true)
    private void ibrs$mutePaddlesOnIce(SoundInstance sound, CallbackInfo ci) {
        if (shouldMutePaddle(sound)) {
            ci.cancel();
        }
    }

    @Inject(method = "play(Lnet/minecraft/client/sound/SoundInstance;I)V", at = @At("HEAD"), cancellable = true)
    private void ibrs$mutePaddlesOnIceDelayed(SoundInstance sound, int delay, CallbackInfo ci) {
        if (shouldMutePaddle(sound)) {
            ci.cancel();
        }
    }

    private static boolean shouldMutePaddle(SoundInstance sound) {
        Identifier id = sound.getId();
        if (!PADDLE_WATER_ID.equals(id) && !PADDLE_LAND_ID.equals(id)) {
            return false;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) {
            return false;
        }
        Entity vehicle = client.player.getVehicle();
        if (!(vehicle instanceof AbstractBoatEntity)) {
            return false;
        }
        if (!IceCheck.isBoatOnIce(client.world, vehicle)) {
            return false;
        }
        // Nur Geräusche nahe beim Spieler betreffen die eigene Bootsfahrt.
        double dx = sound.getX() - client.player.getX();
        double dy = sound.getY() - client.player.getY();
        double dz = sound.getZ() - client.player.getZ();
        return dx * dx + dy * dy + dz * dz < MUTE_RADIUS * MUTE_RADIUS;
    }
}
