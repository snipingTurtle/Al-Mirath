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
                (character_name, era, origin, family_condition, ending_title,
                 legacy_titles, age_at_end, final_status, score)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
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
            statement.setInt(7, record.getAgeAtEnd());
            statement.setString(8, record.getFinalStatus());
            statement.setInt(9, record.getScore());

            statement.executeUpdate();

        } catch (Exception e) {
            System.out.println("Failed to save legacy record: " + e.getMessage());
        }
    }

    public List<LegacyRecord> getAllRecords() {
        return queryRecords("""
                SELECT character_name, era, origin, family_condition, ending_title,
                       legacy_titles, age_at_end, final_status, score
                FROM legacy_records
                ORDER BY created_at DESC
                """);
    }

    /** Highest-scoring lives first, for the hall-of-fame view. */
    public List<LegacyRecord> getTopRecords(int limit) {
        return queryRecords("""
                SELECT character_name, era, origin, family_condition, ending_title,
                       legacy_titles, age_at_end, final_status, score
                FROM legacy_records
                ORDER BY score DESC, created_at DESC
                LIMIT
                """ + " " + Math.max(1, limit));
    }

    private List<LegacyRecord> queryRecords(String sql) {
        List<LegacyRecord> records = new ArrayList<>();

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {
            while (resultSet.next()) {
                records.add(new LegacyRecord(
                        resultSet.getString("character_name"),
                        resultSet.getString("era"),
                        resultSet.getString("origin"),
                        resultSet.getString("family_condition"),
                        resultSet.getString("ending_title"),
                        resultSet.getString("legacy_titles"),
                        resultSet.getInt("age_at_end"),
                        resultSet.getString("final_status"),
                        resultSet.getInt("score")
                ));
            }

        } catch (Exception e) {
            System.out.println("Failed to load legacy records: " + e.getMessage());
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
