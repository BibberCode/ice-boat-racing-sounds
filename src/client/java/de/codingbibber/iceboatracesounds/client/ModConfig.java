package de.codingbibber.iceboatracesounds.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Audio-Konfiguration unter {@code config/ice-boat-racing-sounds.json}.
 * Alle Werte wirken ausschließlich auf Lautstärken und optionale
 * Audio-Features – niemals auf Gameplay.
 */
public final class ModConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger("ice_boat_racing_sounds");
    private static final String FILE_NAME = "ice-boat-racing-sounds.json";

    public float engineVolume = 1.0f;
    public float windVolume = 1.0f;
    public float skidVolume = 1.0f;
    public float turboVolume = 1.0f;
    public float backfireVolume = 1.0f;
    public float shiftVolume = 1.0f;
    public float effectsVolume = 1.0f;
    public float nearbyVolume = 1.0f;

    public boolean dopplerEnabled = true;
    public boolean surfaceAudioEnabled = true;
    public boolean nearbyBoatsEnabled = true;
    public boolean debugAudio = false;

    public static ModConfig load() {
        ModConfig config = new ModConfig();
        Path path = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
        if (Files.notExists(path)) {
            config.save(path);
            return config;
        }
        try (Reader reader = Files.newBufferedReader(path)) {
            ModConfig loaded = new Gson().fromJson(reader, ModConfig.class);
            if (loaded != null) {
                return loaded;
            }
        } catch (Exception e) {
            LOGGER.warn("[ice-boat-racing-sounds] Konnte {} nicht lesen, nutze Standardwerte.", FILE_NAME, e);
        }
        return config;
    }

    private void save(Path path) {
        try {
            Files.createDirectories(path.getParent());
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            try (Writer writer = Files.newBufferedWriter(path)) {
                gson.toJson(this, writer);
            }
        } catch (IOException e) {
            LOGGER.warn("[ice-boat-racing-sounds] Konnte {} nicht schreiben.", FILE_NAME, e);
        }
    }
}
