package com.example.al_mirath.core;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Player preferences, stored as a properties file beside the save database.
 *
 * <p>Loaded once on first access and written back whenever a value changes, so
 * settings survive a crash without needing an explicit save step.
 */
public final class GameSettings {

    private static final String KEY_MUSIC_VOLUME = "audio.music";
    private static final String KEY_EFFECTS_VOLUME = "audio.effects";
    private static final String KEY_TYPEWRITER = "ui.typewriter";
    private static final String KEY_TRIALS = "gameplay.trials";

    private static Properties properties;

    private GameSettings() {
    }

    private static synchronized Properties properties() {
        if (properties != null) {
            return properties;
        }

        properties = new Properties();

        Path file = AppPaths.settingsFile();

        if (Files.exists(file)) {
            try (InputStream in = Files.newInputStream(file)) {
                properties.load(in);
            } catch (Exception e) {
                System.out.println("Could not read settings, using defaults: " + e.getMessage());
            }
        }

        return properties;
    }

    private static synchronized void persist() {
        try (OutputStream out = Files.newOutputStream(AppPaths.settingsFile())) {
            properties().store(out, "Al-Mirath settings");
        } catch (Exception e) {
            System.out.println("Could not write settings: " + e.getMessage());
        }
    }

    private static double getDouble(String key, double fallback) {
        try {
            String raw = properties().getProperty(key);
            return raw == null ? fallback : Double.parseDouble(raw);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static boolean getBoolean(String key, boolean fallback) {
        String raw = properties().getProperty(key);
        return raw == null ? fallback : Boolean.parseBoolean(raw);
    }

    public static double getMusicVolume() {
        return clampVolume(getDouble(KEY_MUSIC_VOLUME, 0.45));
    }

    public static void setMusicVolume(double volume) {
        properties().setProperty(KEY_MUSIC_VOLUME, String.valueOf(clampVolume(volume)));
        persist();
    }

    public static double getEffectsVolume() {
        return clampVolume(getDouble(KEY_EFFECTS_VOLUME, 0.7));
    }

    public static void setEffectsVolume(double volume) {
        properties().setProperty(KEY_EFFECTS_VOLUME, String.valueOf(clampVolume(volume)));
        persist();
    }

    public static boolean isTypewriterEnabled() {
        return getBoolean(KEY_TYPEWRITER, true);
    }

    public static void setTypewriterEnabled(boolean enabled) {
        properties().setProperty(KEY_TYPEWRITER, String.valueOf(enabled));
        persist();
    }

    /**
     * Whether a stat check offers a playable Trial of Skill. Off means every
     * check is resolved by the engine's roll, as it was before trials existed.
     */
    public static boolean areTrialsEnabled() {
        return getBoolean(KEY_TRIALS, true);
    }

    public static void setTrialsEnabled(boolean enabled) {
        properties().setProperty(KEY_TRIALS, String.valueOf(enabled));
        persist();
    }

    private static double clampVolume(double volume) {
        return Math.max(0.0, Math.min(1.0, volume));
    }
}
