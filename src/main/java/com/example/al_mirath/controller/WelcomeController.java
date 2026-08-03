package com.example.al_mirath.controller;

import com.example.al_mirath.Main;
import com.example.al_mirath.service.BackgroundLibrary;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class WelcomeController {

    private Main mainApp;

    @FXML private StackPane welcomeRoot;
    @FXML private ImageView menuBackground;
    @FXML private Rectangle menuOverlay;

    private boolean motionStarted = false;

    @FXML
    public void initialize() {
        bindBackground();
        loadMenuBackground();
        animateBackground();

        System.out.println("WelcomeController initialized.");
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

    private void animateBackground() {
        if (motionStarted || menuBackground == null) {
            return;
        }

        motionStarted = true;

        Timeline motion = new Timeline(
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

    @FXML
    private void startNewGame() {
        System.out.println("New Game button clicked.");

        if (mainApp == null) {
            System.out.println("ERROR: mainApp is null in WelcomeController.");
            return;
        }

        mainApp.showGameScreen();
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
    private void exitGame() {
        System.out.println("Exit button clicked.");

        if (mainApp != null) {
            mainApp.exitGame();
        }
    }
}