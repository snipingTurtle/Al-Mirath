package com.example.al_mirath.controller;

import com.example.al_mirath.Main;
import com.example.al_mirath.model.LegacyRecord;
import com.example.al_mirath.service.LegacyArchive;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class LegacyRecordsController {

    @FXML
    private VBox recordsBox;

    @FXML
    private Label emptyLabel;

    private Main mainApp;

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void initialize() {
        loadRecords();
    }

    private void loadRecords() {
        recordsBox.getChildren().clear();

        if (LegacyArchive.isEmpty()) {
            emptyLabel.setVisible(true);
            emptyLabel.setManaged(true);
            return;
        }

        emptyLabel.setVisible(false);
        emptyLabel.setManaged(false);

        for (LegacyRecord record : LegacyArchive.getRecords()) {
            Label recordLabel = new Label(record.getDisplayText());
            recordLabel.setWrapText(true);
            recordLabel.getStyleClass().add("legacy-record-card");

            recordsBox.getChildren().add(recordLabel);
        }
    }

    @FXML
    private void returnToMainMenu() {
        if (mainApp != null) {
            mainApp.showWelcomeScreen();
        }
    }

    @FXML
    private void clearRecords() {
        LegacyArchive.clearRecords();
        loadRecords();
    }
}
