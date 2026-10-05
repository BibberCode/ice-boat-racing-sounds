package de.codingbibber.iceboatracesounds.client;

import de.codingbibber.iceboatracesounds.ModSounds;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.vehicle.AbstractBoatEntity;

/**
 * Steuert Start-Loop-Stop der Motor-Sounds, sobald der lokale Spieler
 * in einem Boot sitzt (inkl. Chest-Booten, da alle von AbstractBoatEntity erben).
 */
public class IceBoatRacingSoundsClient implements ClientModInitializer {

    private boolean wasInBoat = false;
    private EngineLoopSound engineSound;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) {
                // z.B. Disconnect: Loop sauber beenden, damit nichts hängen bleibt
                stopEngine(client);
                wasInBoat = false;
                return;
            }

            boolean inBoat = client.player.getVehicle() instanceof AbstractBoatEntity;

            if (inBoat && !wasInBoat) {
                startEngine(client);
            } else if (!inBoat && wasInBoat) {
                stopEngine(client);
                // Abstell-Sound nur für den lokalen Spieler (clientseitig)
                client.getSoundManager().play(PositionedSoundInstance.master(ModSounds.ENGINE_TURNOFF, 1.0f));
            }

            wasInBoat = inBoat;
        });
    }

    private void startEngine(MinecraftClient client) {
        // Falls aus irgendeinem Grund noch ein alter Loop läuft: erst stoppen
        stopEngine(client);
        client.getSoundManager().play(PositionedSoundInstance.master(ModSounds.ENGINE_START, 1.0f));
        engineSound = new EngineLoopSound(client);
        client.getSoundManager().play(engineSound);
    }

    private void stopEngine(MinecraftClient client) {
        if (engineSound != null) {
            client.getSoundManager().stop(engineSound);
            engineSound = null;
        }
    }
}
