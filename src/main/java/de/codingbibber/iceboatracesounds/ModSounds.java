package de.codingbibber.iceboatracesounds;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public final class ModSounds {

    public static final String MOD_ID = "ice_boat_racing_sounds";

    public static final Identifier ENGINE_START_ID = Identifier.of(MOD_ID, "engine_start");
    public static final Identifier ENGINE_STAND_ID = Identifier.of(MOD_ID, "engine_stand");
    public static final Identifier ENGINE_TURNOFF_ID = Identifier.of(MOD_ID, "engine_turnoff");

    public static final SoundEvent ENGINE_START = SoundEvent.of(ENGINE_START_ID);
    public static final SoundEvent ENGINE_STAND = SoundEvent.of(ENGINE_STAND_ID);
    public static final SoundEvent ENGINE_TURNOFF = SoundEvent.of(ENGINE_TURNOFF_ID);

    private ModSounds() {
    }

    public static void register() {
        Registry.register(Registries.SOUND_EVENT, ENGINE_START_ID, ENGINE_START);
        Registry.register(Registries.SOUND_EVENT, ENGINE_STAND_ID, ENGINE_STAND);
        Registry.register(Registries.SOUND_EVENT, ENGINE_TURNOFF_ID, ENGINE_TURNOFF);
    }
}
