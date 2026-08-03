package com.example.al_mirath.service;

import com.example.al_mirath.dao.LegacyRecordDAO;
import com.example.al_mirath.model.LegacyRecord;

import java.util.List;

public class LegacyArchive {

    private static final LegacyRecordDAO legacyRecordDAO = new LegacyRecordDAO();

    private LegacyArchive() {
    }

    public static void addRecord(LegacyRecord record) {
        legacyRecordDAO.saveRecord(record);
    }

    public static List<LegacyRecord> getRecords() {
        return legacyRecordDAO.getAllRecords();
    }

    public static boolean isEmpty() {
        return getRecords().isEmpty();
    }

    public static void clearRecords() {
        legacyRecordDAO.clearRecords();
    }
}