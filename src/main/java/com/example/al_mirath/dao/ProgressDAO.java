package com.example.al_mirath.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Cross-run progress: unlocked achievements and lifetime counters.
 *
 * <p>Unlike a save, this data survives death and deliberately is not cleared
 * when a life ends or the player restarts.
 */
public class ProgressDAO {

    // --- Achievements ---

    public Set<String> loadUnlockedAchievements() {
        Set<String> unlocked = new LinkedHashSet<>();

        String sql = "SELECT id FROM achievements";

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {
            while (resultSet.next()) {
                unlocked.add(resultSet.getString("id"));
            }

        } catch (Exception e) {
            System.out.println("Failed to load achievements: " + e.getMessage());
        }

        return unlocked;
    }

    /**
     * Records an unlock. The INSERT is ignored when the row already exists, so
     * re-unlocking is harmless and the original timestamp is preserved.
     */
    public void unlockAchievement(String id) {
        String sql = "INSERT OR IGNORE INTO achievements (id) VALUES (?)";

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, id);
            statement.executeUpdate();

        } catch (Exception e) {
            System.out.println("Failed to unlock achievement " + id + ": " + e.getMessage());
        }
    }

    // --- Statistics ---

    public Map<String, Integer> loadStatistics() {
        Map<String, Integer> stats = new LinkedHashMap<>();

        String sql = "SELECT key, value FROM statistics";

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {
            while (resultSet.next()) {
                stats.put(resultSet.getString("key"), resultSet.getInt("value"));
            }

        } catch (Exception e) {
            System.out.println("Failed to load statistics: " + e.getMessage());
        }

        return stats;
    }

    /**
     * Adds to a counter, creating it at {@code amount} when absent.
     * The arithmetic runs inside SQL so the stored value stays authoritative.
     */
    public void incrementStatistic(String key, int amount) {
        String sql = """
                INSERT INTO statistics (key, value)
                VALUES (?, ?)
                ON CONFLICT(key) DO UPDATE SET value = value + excluded.value
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, key);
            statement.setInt(2, amount);
            statement.executeUpdate();

        } catch (Exception e) {
            System.out.println("Failed to update statistic " + key + ": " + e.getMessage());
        }
    }

    /** Raises a counter only when the new value beats the stored one. */
    public void recordMaximum(String key, int value) {
        String sql = """
                INSERT INTO statistics (key, value)
                VALUES (?, ?)
                ON CONFLICT(key) DO UPDATE SET
                    value = CASE WHEN excluded.value > value THEN excluded.value ELSE value END
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, key);
            statement.setInt(2, value);
            statement.executeUpdate();

        } catch (Exception e) {
            System.out.println("Failed to record maximum " + key + ": " + e.getMessage());
        }
    }

    public void resetProgress() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("DELETE FROM achievements")) {
                statement.executeUpdate();
            }

            try (PreparedStatement statement = connection.prepareStatement("DELETE FROM statistics")) {
                statement.executeUpdate();
            }

        } catch (Exception e) {
            System.out.println("Failed to reset progress: " + e.getMessage());
        }
    }
}
