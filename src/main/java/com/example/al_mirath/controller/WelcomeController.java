package com.example.al_mirath.controller;

import com.example.al_mirath.Main;
import com.example.al_mirath.core.GameSettings;
import com.example.al_mirath.service.BackgroundLibrary;
import com.example.al_mirath.service.GameEngine;
import com.example.al_mirath.service.SaveManager;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class WelcomeController implements ScreenLifecycle {

    private Main mainApp;

    @FXML private StackPane welcomeRoot;
    @FXML private ImageView menuBackground;
    @FXML private Rectangle menuOverlay;
    @FXML private Button continueButton;

    private boolean motionStarted = false;

    // Held so dispose() can end them; both run indefinitely otherwise.
    private Timeline backgroundRotation;
    private Timeline motion;

    @FXML
    public void initialize() {
        bindBackground();
        loadMenuBackground();

        if (GameSettings.isAmbientMotionEnabled()) {
            animateBackground();
            startBackgroundRotation();
        }

        updateContinueButtonState();

        System.out.println("WelcomeController initialized.");
    }

    private void updateContinueButtonState() {
        if (continueButton == null) {
            return;
        }

        continueButton.setDisable(!SaveManager.hasSave());
    }

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
        System.out.println("Main app injected into WelcomeController.");
    }

    private void bindBackground() {
        if (welcomeRoot != null && menuBackground != null) {
            menuBackground.fitWidthProperty().bind(welcomeRoot.widthProperty());
            menuBackground.fitHeightProperty().bind(welcomeRoot.heightProperty());
            menuBackground.setPreserveRatio(false);
            menuBackground.setSmooth(true);
        }

        if (welcomeRoot != null && menuOverlay != null) {
            menuOverlay.widthProperty().bind(welcomeRoot.widthProperty());
            menuOverlay.heightProperty().bind(welcomeRoot.heightProperty());
            menuOverlay.setOpacity(0.18);
        }
    }

    private void loadMenuBackground() {
        try {
            String path = BackgroundLibrary.getMenuBackground();

            if (path == null || path.isBlank()) {
                System.out.println("No menu background selected.");
                return;
            }

            Image image = new Image(getClass().getResource(path).toExternalForm(), true);
            menuBackground.setImage(image);
            menuBackground.setScaleX(1.06);
            menuBackground.setScaleY(1.06);

            System.out.println("Menu background loaded: " + path);

        } catch (Exception e) {
            System.out.println("Failed to load menu background.");
            e.printStackTrace();
        }
    }

    /**
     * Cycles the menu art every ten seconds with a cross-fade, so the title
     * screen keeps moving instead of sitting on one still image.
     */
    private void startBackgroundRotation() {
        if (menuBackground == null) {
            return;
        }

        backgroundRotation = new Timeline(new KeyFrame(
                Duration.seconds(10),
                event -> crossFadeToNextBackground()
        ));

        backgroundRotation.setCycleCount(Timeline.INDEFINITE);
        backgroundRotation.play();
    }

    private void crossFadeToNextBackground() {
        String path = BackgroundLibrary.getMenuBackground();

        if (path == null || path.isBlank()) {
            return;
        }

        Image next;

        try {
            // Decoded off the FX thread. Loading a 1920x1080 JPEG synchronously
            // here stalled the UI for the length of the decode, once every
            // rotation.
            next = new Image(
                    getClass().getResource(path).toExternalForm(),
                    1920,
                    1080,
                    false,
                    true,
                    true
            );
        } catch (Exception e) {
            System.out.println("Could not load next menu background: " + path);
            return;
        }

        whenReady(next, () -> startCrossFade(next));
    }

    /**
     * Runs the action once a background-loading image has finished decoding,
     * so the fade never reveals a half-drawn frame. Fires immediately for an
     * image already in memory, and drops the swap entirely if it fails.
     */
    private void whenReady(Image image, Runnable action) {
        if (image.isError()) {
            return;
        }

        if (image.getProgress() >= 1.0) {
            action.run();
            return;
        }

        image.progressProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(
                    ObservableValue<? extends Number> observable,
                    Number previous,
                    Number progress
            ) {
                if (progress.doubleValue() < 1.0) {
                    return;
                }

                image.progressProperty().removeListener(this);

                if (!image.isError()) {
                    action.run();
                }
            }
        });
    }

    private void startCrossFade(Image next) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(700), menuBackground);
        fadeOut.setFromValue(menuBackground.getOpacity());
        fadeOut.setToValue(0.0);

        fadeOut.setOnFinished(event -> {
            menuBackground.setImage(next);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(900), menuBackground);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });

        fadeOut.play();
    }

    private void animateBackground() {
        if (motionStarted || menuBackground == null) {
            return;
        }

        motionStarted = true;

        motion = new Timeline(
                new KeyFrame(
                        Duration.ZERO,
                        new KeyValue(menuBackground.translateXProperty(), -14)
                ),
                new KeyFrame(
                        Duration.seconds(18),
                        new KeyValue(menuBackground.translateXProperty(), 14)
                )
        );

        motion.setAutoReverse(true);
        motion.setCycleCount(Timeline.INDEFINITE);
        motion.play();
    }

    /**
     * Ends the menu's animations when the stage moves to another screen.
     * Both timelines are indefinite, so without this each visit to the menu
     * left another pair running against a scene that is no longer shown.
     */
    @Override
    public void dispose() {
        if (backgroundRotation != null) {
            backgroundRotation.stop();
            backgroundRotation = null;
        }

        if (motion != null) {
            motion.stop();
            motion = null;
        }

        motionStarted = false;
    }

    @FXML
    private void startNewGame() {
        System.out.println("New Game button clicked.");

        if (mainApp == null) {
            System.out.println("ERROR: mainApp is null in WelcomeController.");
            return;
        }

        // A brand-new life must start clean — Threads of Fate back at one,
        // no stage rewards or world events carried over from whatever was
        // saved before. Without this, an old in-progress save could still be
        // sitting in the database and get picked up later by "Continue".
        SaveManager.clearSave();

        mainApp.showGameScreen();
    }

    @FXML
    private void continueGame() {
        System.out.println("Continue button clicked.");

        if (mainApp == null) {
            System.out.println("ERROR: mainApp is null in WelcomeController.");
            return;
        }

        GameEngine restoredEngine = SaveManager.loadGame();

        if (restoredEngine == null) {
            System.out.println("No saved game found to continue.");
            updateContinueButtonState();
            return;
        }

        mainApp.showGameScreen(restoredEngine);
    }

    @FXML
    private void openLegacyRecords() {
        System.out.println("Legacy Records button clicked.");

        if (mainApp == null) {
            System.out.println("ERROR: mainApp is null in WelcomeController.");
            return;
        }

        mainApp.showLegacyRecordsScreen();
    }

    @FXML
    private void openAchievements() {
        if (mainApp == null) {
            System.out.println("ERROR: mainApp is null in WelcomeController.");
            return;
        }

        mainApp.showAchievementsScreen();
    }

    @FXML
    private void openSettings() {
        if (mainApp == null) {
            System.out.println("ERROR: mainApp is null in WelcomeController.");
            return;
        }

        mainApp.showSettingsScreen();
    }

    @FXML
    private void exitGame() {
        System.out.println("Exit button clicked.");

        if (mainApp != null) {
            mainApp.exitGame();
        }
    }
}