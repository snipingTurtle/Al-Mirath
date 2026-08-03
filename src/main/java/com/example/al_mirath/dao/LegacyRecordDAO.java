package com.example.al_mirath.dao;

import com.example.al_mirath.model.LegacyRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class LegacyRecordDAO {

    public void saveRecord(LegacyRecord record) {
        String sql = """
                INSERT INTO legacy_records
                (character_name, era, origin, family_condition, ending_title, legacy_titles)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, record.getCharacterName());
            statement.setString(2, record.getEra());
            statement.setString(3, record.getOrigin());
            statement.setString(4, record.getFamilyCondition());
            statement.setString(5, record.getEndingTitle());
            statement.setString(6, record.getLegacyTitles());

            statement.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<LegacyRecord> getAllRecords() {
        List<LegacyRecord> records = new ArrayList<>();

        String sql = """
                SELECT character_name, era, origin, family_condition, ending_title, legacy_titles
                FROM legacy_records
                ORDER BY created_at DESC
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {
            while (resultSet.next()) {
                LegacyRecord record = new LegacyRecord(
                        resultSet.getString("character_name"),
                        resultSet.getString("era"),
                        resultSet.getString("origin"),
                        resultSet.getString("family_condition"),
                        resultSet.getString("ending_title"),
                        resultSet.getString("legacy_titles")
                );

                records.add(record);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return records;
    }

    public void clearRecords() {
        String sql = "DELETE FROM legacy_records";

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
