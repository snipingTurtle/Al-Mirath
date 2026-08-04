package com.example.al_mirath.controller;

import com.example.al_mirath.Main;
import com.example.al_mirath.core.GameSettings;
import com.example.al_mirath.service.ProgressService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;

/**
 * Preferences screen. Every control writes through to {@link GameSettings}
 * immediately, so there is no save button to forget.
 */
public class SettingsController {

    @FXML private CheckBox typewriterCheck;
    @FXML private CheckBox trialsCheck;
    @FXML private Slider musicSlider;
    @FXML private Slider effectsSlider;
    @FXML private Label musicValueLabel;
    @FXML private Label effectsValueLabel;
    @FXML private Label resetStatusLabel;
    @FXML private Button resetButton;

    private Main mainApp;

    /** Erasing progress needs a second press, so it cannot happen by accident. */
    private boolean resetArmed = false;

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void initialize() {
        typewriterCheck.setSelected(GameSettings.isTypewriterEnabled());
        trialsCheck.setSelected(GameSettings.areTrialsEnabled());

        musicSlider.setValue(GameSettings.getMusicVolume() * 100);
        effectsSlider.setValue(GameSettings.getEffectsVolume() * 100);

        updateVolumeLabels();

        typewriterCheck.selectedProperty().addListener(
                (observable, wasSelected, isSelected) -> GameSettings.setTypewriterEnabled(isSelected)
        );

        trialsCheck.selectedProperty().addListener(
                (observable, wasSelected, isSelected) -> GameSettings.setTrialsEnabled(isSelected)
        );

        musicSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            GameSettings.setMusicVolume(newValue.doubleValue() / 100.0);
            updateVolumeLabels();
        });

        effectsSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            GameSettings.setEffectsVolume(newValue.doubleValue() / 100.0);
            updateVolumeLabels();
        });
    }

    private void updateVolumeLabels() {
        musicValueLabel.setText("Music: " + Math.round(musicSlider.getValue()) + "%");
        effectsValueLabel.setText("Sound effects: " + Math.round(effectsSlider.getValue()) + "%");
    }

    /**
     * First press arms the reset and warns; second press performs it.
     */
    @FXML
    private void requestReset() {
        if (!resetArmed) {
            resetArmed = true;
            resetButton.setText("Press again to confirm");
            resetStatusLabel.setText(
                    "This will permanently erase every achievement and statistic. "
                            + "Press the button again to confirm, or leave this screen to cancel."
            );
            return;
        }

        ProgressService.resetAll();

        resetArmed = false;
        resetButton.setText("Erase All Progress");
        resetButton.setDisable(true);
        resetStatusLabel.setText("All achievements and statistics have been erased.");
    }

    @FXML
    private void returnToMainMenu() {
        if (mainApp != null) {
            mainApp.showWelcomeScreen();
        }
    }
}
