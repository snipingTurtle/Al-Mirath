package com.example.al_mirath.core;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Resolves the per-user directory where Al-Mirath stores its save database,
 * settings, and logs.
 *
 * <p>A distributed build cannot write into its own installation folder: on
 * Windows that lives under Program Files, and Steam replaces the whole
 * directory on every update. Everything the player accumulates therefore lives
 * in the platform's standard application-data location instead.
 */
public final class AppPaths {

    private static final String APP_FOLDER = "AlMirath";

    private AppPaths() {
    }

    /**
     * Returns the writable data directory, creating it if necessary.
     * Falls back to a folder beside the working directory if the platform
     * location cannot be created.
     */
    public static Path dataDirectory() {
        Path directory = resolvePlatformDirectory();

        try {
            Files.createDirectories(directory);
            return directory;
        } catch (Exception e) {
            System.out.println("Could not create data directory at " + directory + ": " + e.getMessage());

            Path fallback = Paths.get(System.getProperty("user.dir"), ".almirath");

            try {
                Files.createDirectories(fallback);
            } catch (Exception ignored) {
                // If even this fails the caller will surface the IO error.
            }

            return fallback;
        }
    }

    public static Path databaseFile() {
        return dataDirectory().resolve("almirath.db");
    }

    public static Path settingsFile() {
        return dataDirectory().resolve("settings.properties");
    }

    private static Path resolvePlatformDirectory() {
        String os = System.getProperty("os.name", "").toLowerCase();
        String home = System.getProperty("user.home", ".");

        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");

            if (appData != null && !appData.isBlank()) {
                return Paths.get(appData, APP_FOLDER);
            }

            return Paths.get(home, "AppData", "Roaming", APP_FOLDER);
        }

        if (os.contains("mac") || os.contains("darwin")) {
            return Paths.get(home, "Library", "Application Support", APP_FOLDER);
        }

        String xdgDataHome = System.getenv("XDG_DATA_HOME");

        if (xdgDataHome != null && !xdgDataHome.isBlank()) {
            return Paths.get(xdgDataHome, APP_FOLDER);
        }

        return Paths.get(home, ".local", "share", APP_FOLDER);
    }
}
