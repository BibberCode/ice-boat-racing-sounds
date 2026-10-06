package de.codingbibber.iceboatracesounds.client;

import de.codingbibber.iceboatracesounds.ModSounds;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.AbstractBoatEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dünne Einstiegslogik: Motor nur auf Eis (Start-Stopp-Automatik mit
 * Entprellung), eigentliche Klangsteuerung im {@link BoatAudioController}.
 * Auf Wasser bleibt es still – dort paddelt man vanilla. Ändert keinerlei
 * Gameplay, nur Sounds.
 */
public class IceBoatRacingSoundsClient implements ClientModInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger("ice_boat_racing_sounds");

    /** Ticks, die der Eis-Zustand stabil sein muss (Entprellung an der Eiskante). */
    private static final int ICE_STABLE_TICKS = 10;

    private static final String[] REQUIRED_FILES = {
            "sounds/engine_start.ogg",
            "sounds/engine_loop_low.ogg",
            "sounds/engine_loop_mid.ogg",
            "sounds/engine_loop_high.ogg",
            "sounds/engine_loop_redline.ogg",
            "sounds/turbo_loop.ogg",
            "sounds/engine_shift.ogg",
            "sounds/engine_backfire.ogg",
            "sounds/blowoff.ogg",
            "sounds/impact_light.ogg",
            "sounds/impact_heavy.ogg",
            "sounds/landing.ogg",
            "sounds/engine_turnoff.ogg",
            "sounds/wind_loop.ogg",
            "sounds/skid_loop.ogg"
    };

    private ModConfig config;
    private boolean wasInBoat = false;
    private boolean iceActive = false;
    private int iceStableTicks = 0;
    private BoatAudioController controller;

    @Override
    public void onInitializeClient() {
        config = ModConfig.load();
        LOGGER.info("[ice-boat-racing-sounds] Client init ok – Motor läuft nur auf Eis.");
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) {
                // Tod/Disconnect/Teleport/Weltwechsel: sofort und leise alles stoppen.
                if (controller != null) {
                    controller.stopNow();
                    controller = null;
                }
                wasInBoat = false;
                iceActive = false;
                iceStableTicks = 0;
                return;
            }

            Entity vehicle = client.player.getVehicle();
            boolean inBoat = vehicle instanceof AbstractBoatEntity;

            if (!inBoat) {
                if (controller != null) {
                    if (wasInBoat) {
                        LOGGER.info("[ice-boat-racing-sounds] Boot verlassen – Motor aus.");
                    }
                    controller.requestStop(true);
                    controller.tick();
                    if (!controller.isAlive()) {
                        controller = null;
                    }
                }
                wasInBoat = false;
                iceActive = false;
                iceStableTicks = 0;
                return;
            }

            boolean onIce = IceCheck.isBoatOnIce(client.world, vehicle);
            if (onIce != iceActive) {
                if (++iceStableTicks >= ICE_STABLE_TICKS) {
                    iceActive = onIce;
                    iceStableTicks = 0;
                    if (iceActive) {
                        checkResources(client);
                        LOGGER.info("[ice-boat-racing-sounds] Eis erkannt – starte Motor.");
                        client.getSoundManager().play(PositionedSoundInstance.master(ModSounds.ENGINE_START, 1.0f));
                        controller = new BoatAudioController(client, config);
                    } else if (controller != null) {
                        LOGGER.info("[ice-boat-racing-sounds] Eis verlassen – Motor aus.");
                        controller.requestStop(true);
                    }
                }
            } else {
                iceStableTicks = 0;
            }

            if (controller != null) {
                controller.tick();
                if (!controller.isAlive()) {
                    controller = null;
                }
            }
            wasInBoat = true;
        });
    }

    /**
     * Prüft, ob die OGG-Dateien im laufenden Spiel sichtbar sind.
     * Fehlen sie, läuft ein alter Build (ohne neue Sounds).
     */
    private static void checkResources(MinecraftClient client) {
        for (String file : REQUIRED_FILES) {
            Identifier id = Identifier.of(ModSounds.MOD_ID, file);
            if (client.getResourceManager().getResource(id).isEmpty()) {
                LOGGER.error("[ice-boat-racing-sounds] Ressource FEHLT im Spiel: {} – "
                        + "bitte ./gradlew clean build ausführen und Run-Konfiguration aktualisieren.", id);
            }
        }
    }
}
