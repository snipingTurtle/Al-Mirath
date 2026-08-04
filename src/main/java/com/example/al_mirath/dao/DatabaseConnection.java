package com.example.al_mirath.dao;

import com.example.al_mirath.core.AppPaths;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Owns the game's embedded SQLite database and its schema.
 *
 * <p>Al-Mirath previously connected to a MySQL server on localhost using
 * hardcoded root credentials, which meant the game only ran on a machine where
 * someone had installed MySQL and created the schema by hand. SQLite keeps the
 * same JDBC/SQL model but stores everything in a single file under the player's
 * application-data directory, so a downloaded build works on first launch with
 * nothing to configure.
 */
public final class DatabaseConnection {

    private static final Object SCHEMA_LOCK = new Object();

    private static boolean schemaReady = false;
    private static boolean databaseAvailable = true;

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        ensureSchema();

        return openConnection();
    }

    /**
     * Reports whether the database could be opened. The UI uses this to warn
     * the player that progress will not persist rather than silently losing
     * their run.
     */
    public static boolean isAvailable() {
        try {
            ensureSchema();
        } catch (SQLException e) {
            return false;
        }

        return databaseAvailable;
    }

    private static Connection openConnection() throws SQLException {
        String url = "jdbc:sqlite:" + AppPaths.databaseFile().toAbsolutePath();

        Connection connection = DriverManager.getConnection(url);

        try (Statement statement = connection.createStatement()) {
            // Durability and concurrency defaults that suit a desktop game.
            statement.execute("PRAGMA journal_mode = WAL");
            statement.execute("PRAGMA synchronous = NORMAL");
            statement.execute("PRAGMA foreign_keys = ON");
        }

        return connection;
    }

    /**
     * Creates the schema on first use. Every statement is idempotent so the
     * method is safe to run on each launch.
     */
    private static void ensureSchema() throws SQLException {
        synchronized (SCHEMA_LOCK) {
            if (schemaReady) {
                return;
            }

            try (
                    Connection connection = openConnection();
                    Statement statement = connection.createStatement()
            ) {
                statement.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS game_save (
                            slot        INTEGER PRIMARY KEY,
                            save_data   TEXT    NOT NULL,
                            label       TEXT    NOT NULL DEFAULT '',
                            updated_at  TEXT    NOT NULL DEFAULT (datetime('now'))
                        )
                        """);

                statement.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS legacy_records (
                            id                INTEGER PRIMARY KEY AUTOINCREMENT,
                            character_name    TEXT NOT NULL,
                            era               TEXT NOT NULL,
                            origin            TEXT NOT NULL,
                            family_condition  TEXT NOT NULL,
                            ending_title      TEXT NOT NULL,
                            legacy_titles     TEXT NOT NULL,
                            age_at_end        INTEGER NOT NULL DEFAULT 0,
                            final_status      TEXT NOT NULL DEFAULT '',
                            score             INTEGER NOT NULL DEFAULT 0,
                            created_at        TEXT NOT NULL DEFAULT (datetime('now'))
                        )
                        """);

                statement.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS achievements (
                            id           TEXT PRIMARY KEY,
                            unlocked_at  TEXT NOT NULL DEFAULT (datetime('now'))
                        )
                        """);

                statement.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS statistics (
                            key    TEXT PRIMARY KEY,
                            value  INTEGER NOT NULL DEFAULT 0
                        )
                        """);

                statement.executeUpdate(
                        "CREATE INDEX IF NOT EXISTS idx_legacy_created ON legacy_records (created_at DESC)"
                );

                schemaReady = true;
                databaseAvailable = true;

            } catch (SQLException e) {
                databaseAvailable = false;
                System.out.println("Database unavailable: " + e.getMessage());
                throw e;
            }
        }
    }
}
