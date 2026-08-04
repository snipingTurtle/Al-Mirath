package com.example.al_mirath.service;

import com.example.al_mirath.dao.GameSaveDAO;

public class SaveManager {

    private static final GameSaveDAO gameSaveDAO = new GameSaveDAO();

    private SaveManager() {
    }

    public static void saveGame(GameEngine engine) {
        gameSaveDAO.saveGame(engine.toSaveJson());
    }

    public static GameEngine loadGame() {
        String saveJson = gameSaveDAO.loadGame();

        if (saveJson == null) {
            return null;
        }

        return GameEngine.fromSaveJson(saveJson);
    }

    public static boolean hasSave() {
        return gameSaveDAO.hasSave();
    }

    public static void clearSave() {
        gameSaveDAO.deleteSave();
    }
}