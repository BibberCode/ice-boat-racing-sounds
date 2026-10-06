package de.codingbibber.iceboatracesounds;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public final class ModSounds {

    public static final String MOD_ID = "ice_boat_racing_sounds";

    public static final Identifier ENGINE_START_ID = Identifier.of(MOD_ID, "engine_start");
    public static final Identifier ENGINE_LOOP_LOW_ID = Identifier.of(MOD_ID, "engine_loop_low");
    public static final Identifier ENGINE_LOOP_MID_ID = Identifier.of(MOD_ID, "engine_loop_mid");
    public static final Identifier ENGINE_LOOP_HIGH_ID = Identifier.of(MOD_ID, "engine_loop_high");
    public static final Identifier ENGINE_LOOP_REDLINE_ID = Identifier.of(MOD_ID, "engine_loop_redline");
    public static final Identifier TURBO_LOOP_ID = Identifier.of(MOD_ID, "turbo_loop");
    public static final Identifier ENGINE_SHIFT_ID = Identifier.of(MOD_ID, "engine_shift");
    public static final Identifier ENGINE_BACKFIRE_ID = Identifier.of(MOD_ID, "engine_backfire");
    public static final Identifier BLOWOFF_ID = Identifier.of(MOD_ID, "blowoff");
    public static final Identifier IMPACT_LIGHT_ID = Identifier.of(MOD_ID, "impact_light");
    public static final Identifier IMPACT_HEAVY_ID = Identifier.of(MOD_ID, "impact_heavy");
    public static final Identifier LANDING_ID = Identifier.of(MOD_ID, "landing");
    public static final Identifier ENGINE_TURNOFF_ID = Identifier.of(MOD_ID, "engine_turnoff");
    public static final Identifier WIND_LOOP_ID = Identifier.of(MOD_ID, "wind_loop");
    public static final Identifier SKID_LOOP_ID = Identifier.of(MOD_ID, "skid_loop");

    public static final SoundEvent ENGINE_START = SoundEvent.of(ENGINE_START_ID);
    public static final SoundEvent ENGINE_LOOP_LOW = SoundEvent.of(ENGINE_LOOP_LOW_ID);
    public static final SoundEvent ENGINE_LOOP_MID = SoundEvent.of(ENGINE_LOOP_MID_ID);
    public static final SoundEvent ENGINE_LOOP_HIGH = SoundEvent.of(ENGINE_LOOP_HIGH_ID);
    public static final SoundEvent ENGINE_LOOP_REDLINE = SoundEvent.of(ENGINE_LOOP_REDLINE_ID);
    public static final SoundEvent TURBO_LOOP = SoundEvent.of(TURBO_LOOP_ID);
    public static final SoundEvent ENGINE_SHIFT = SoundEvent.of(ENGINE_SHIFT_ID);
    public static final SoundEvent ENGINE_BACKFIRE = SoundEvent.of(ENGINE_BACKFIRE_ID);
    public static final SoundEvent BLOWOFF = SoundEvent.of(BLOWOFF_ID);
    public static final SoundEvent IMPACT_LIGHT = SoundEvent.of(IMPACT_LIGHT_ID);
    public static final SoundEvent IMPACT_HEAVY = SoundEvent.of(IMPACT_HEAVY_ID);
    public static final SoundEvent LANDING = SoundEvent.of(LANDING_ID);
    public static final SoundEvent ENGINE_TURNOFF = SoundEvent.of(ENGINE_TURNOFF_ID);
    public static final SoundEvent WIND_LOOP = SoundEvent.of(WIND_LOOP_ID);
    public static final SoundEvent SKID_LOOP = SoundEvent.of(SKID_LOOP_ID);

    private ModSounds() {
    }

    public static void register() {
        Registry.register(Registries.SOUND_EVENT, ENGINE_START_ID, ENGINE_START);
        Registry.register(Registries.SOUND_EVENT, ENGINE_LOOP_LOW_ID, ENGINE_LOOP_LOW);
        Registry.register(Registries.SOUND_EVENT, ENGINE_LOOP_MID_ID, ENGINE_LOOP_MID);
        Registry.register(Registries.SOUND_EVENT, ENGINE_LOOP_HIGH_ID, ENGINE_LOOP_HIGH);
        Registry.register(Registries.SOUND_EVENT, ENGINE_LOOP_REDLINE_ID, ENGINE_LOOP_REDLINE);
        Registry.register(Registries.SOUND_EVENT, TURBO_LOOP_ID, TURBO_LOOP);
        Registry.register(Registries.SOUND_EVENT, ENGINE_SHIFT_ID, ENGINE_SHIFT);
        Registry.register(Registries.SOUND_EVENT, ENGINE_BACKFIRE_ID, ENGINE_BACKFIRE);
        Registry.register(Registries.SOUND_EVENT, BLOWOFF_ID, BLOWOFF);
        Registry.register(Registries.SOUND_EVENT, IMPACT_LIGHT_ID, IMPACT_LIGHT);
        Registry.register(Registries.SOUND_EVENT, IMPACT_HEAVY_ID, IMPACT_HEAVY);
        Registry.register(Registries.SOUND_EVENT, LANDING_ID, LANDING);
        Registry.register(Registries.SOUND_EVENT, ENGINE_TURNOFF_ID, ENGINE_TURNOFF);
        Registry.register(Registries.SOUND_EVENT, WIND_LOOP_ID, WIND_LOOP);
        Registry.register(Registries.SOUND_EVENT, SKID_LOOP_ID, SKID_LOOP);
    }
}
