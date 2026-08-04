package com.example.al_mirath.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class GameSaveDAO {

    public void saveGame(String saveJson) {
        String sql = """
                INSERT INTO game_save (id, save_data)
                VALUES (1, ?)
                ON DUPLICATE KEY UPDATE save_data = VALUES(save_data)
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, saveJson);
            statement.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String loadGame() {
        String sql = "SELECT save_data FROM game_save WHERE id = 1";

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {
            if (resultSet.next()) {
                return resultSet.getString("save_data");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean hasSave() {
        return loadGame() != null;
    }

    public void deleteSave() {
        String sql = "DELETE FROM game_save WHERE id = 1";

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}