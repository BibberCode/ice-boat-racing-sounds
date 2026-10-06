package de.codingbibber.iceboatracesounds.client;

import de.codingbibber.iceboatracesounds.ModSounds;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.AbstractBoatEntity;

/**
 * Zentrale Audio-Steuerung für eine Bootsfahrt auf Eis. Beobachtet
 * ausschließlich Vanilla-Bewegung (Tempo, Gas, Untergrund, Drift) und
 * verteilt sie an die Audio-Module: Getriebe, Motor, Fahrtwind/Slide,
 * Effekte und fremde Boote. Schreibt niemals Physik, Steuerung oder
 * Gameplay-Werte – nur Volume/Pitch von Sounds.
 */
public class BoatAudioController {

    private final MinecraftClient client;
    private final ModConfig config;
    private final SimState state = new SimState();

    private final TransmissionAudio transmission = new TransmissionAudio();
    private final EngineAudio engine;
    private final MovementAudio movement;
    private final EffectsAudio effects = new EffectsAudio();
    private final NearbyBoatAudio nearby;

    private float fade = 1.0f;
    private boolean fading;
    private boolean alive = true;
    private boolean hasRun;

    public BoatAudioController(MinecraftClient client, ModConfig config) {
        this.client = client;
        this.config = config;
        this.engine = new EngineAudio(client);
        this.movement = new MovementAudio(client);
        this.nearby = new NearbyBoatAudio(client);
    }

    /** Pro Client-Tick aufrufen, solange das Boot aktiv ist oder ausblendet. */
    public void tick() {
        if (client.player == null || client.world == null) {
            return;
        }
        state.ticks++;

        Entity vehicle = client.player.getVehicle();
        if (!(vehicle instanceof AbstractBoatEntity)) {
            // z.B. Aussteigen während des Ausblendens: Ohne Vehicle gibt es
            // keine Tick-Daten für ein Fade – also sofort stoppen, damit kein
            // Loop ewig weiterläuft. Der Abstell-Sound kam schon via requestStop.
            if (fading) {
                stopVoices();
                alive = false;
            }
            return;
        }

        observeBasics(vehicle);

        transmission.tick(client, state, config);
        movement.tick(state, config, fade);
        engine.tick(state, config, fade);
        effects.tick(client, state, config);
        nearby.tick(state, config);

        if (engine.getLastVolume() > 0.05f) {
            hasRun = true;
        }

        if (fading) {
            fade -= 0.12f;
            if (fade <= 0.0f) {
                stopVoices();
                alive = false;
            }
        }

        if (config.debugAudio && state.ticks % 100 == 1) {
            AudioDebug.logTick(state, engine.getLastVolume(),
                    movement.getLastWindVol(), movement.getLastSkidVol());
        }
    }

    /** Weiches Ausblenden (kein harter Cut), danach ist der Controller fertig. */
    public void requestStop(boolean playTurnoff) {
        if (!alive || fading) {
            return;
        }
        fading = true;
        if (playTurnoff && hasRun) {
            client.getSoundManager().play(PositionedSoundInstance.master(ModSounds.ENGINE_TURNOFF, 1.0f));
        }
    }

    /** Sofort-Stopp ohne Ausblenden (Tod, Disconnect, Teleport, Weltwechsel). */
    public void stopNow() {
        stopVoices();
        alive = false;
    }

    public boolean isAlive() {
        return alive;
    }

    public boolean isFading() {
        return fading && alive;
    }

    private void stopVoices() {
        engine.stop();
        movement.stop();
        nearby.stop();
    }

    /** Nur lesen: Tempo, Beschleunigung, Gas, Untergrund, Bootstyp. */
    private void observeBasics(Entity vehicle) {
        var v = client.player.getVelocity();
        float speed = (float) (v.length() * 20.0);
        if (state.ticks <= 1) {
            state.prevSpeedMs = speed;
        }
        state.speedMs = speed;
        state.accel = (speed - state.prevSpeedMs) * 20.0f;
        // Wichtig: Drop VOR dem Überschreiben sichern (EffectsAudio nutzt ihn).
        state.speedDrop = state.prevSpeedMs - speed;
        state.prevSpeedMs = speed;
        state.vyMs = (float) (v.y * 20.0);

        state.wasGas = state.gas;
        state.gas = client.options.forwardKey.isPressed();

        state.surface = IceCheck.classify(client.world, vehicle);
        state.boatPitch = NearbyBoatAudio.profilePitch(vehicle);
    }
}
