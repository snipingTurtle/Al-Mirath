package com.example.al_mirath.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Reads and writes the single continue-slot save.
 *
 * <p>SQLite spells the upsert as ON CONFLICT rather than MySQL's
 * ON DUPLICATE KEY UPDATE, so the statement differs from the original even
 * though the behaviour is the same.
 */
public class GameSaveDAO {

    private static final int DEFAULT_SLOT = 1;

    public void saveGame(String saveJson) {
        String sql = """
                INSERT INTO game_save (slot, save_data, label, updated_at)
                VALUES (?, ?, ?, datetime('now'))
                ON CONFLICT(slot) DO UPDATE SET
                    save_data  = excluded.save_data,
                    label      = excluded.label,
                    updated_at = excluded.updated_at
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setInt(1, DEFAULT_SLOT);
            statement.setString(2, saveJson);
            statement.setString(3, "");
            statement.executeUpdate();

        } catch (Exception e) {
            System.out.println("Failed to save game: " + e.getMessage());
        }
    }

    public String loadGame() {
        String sql = "SELECT save_data FROM game_save WHERE slot = ?";

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setInt(1, DEFAULT_SLOT);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("save_data");
                }
            }

        } catch (Exception e) {
            System.out.println("Failed to load game: " + e.getMessage());
        }

        return null;
    }

    public boolean hasSave() {
        return loadGame() != null;
    }

    public void deleteSave() {
        String sql = "DELETE FROM game_save WHERE slot = ?";

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setInt(1, DEFAULT_SLOT);
            statement.executeUpdate();

        } catch (Exception e) {
            System.out.println("Failed to delete save: " + e.getMessage());
        }
    }
}
