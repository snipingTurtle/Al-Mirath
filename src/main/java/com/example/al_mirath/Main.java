package com.example.al_mirath;

import com.example.al_mirath.controller.AchievementsController;
import com.example.al_mirath.controller.GameController;
import com.example.al_mirath.controller.LegacyRecordsController;
import com.example.al_mirath.controller.SettingsController;
import com.example.al_mirath.controller.WelcomeController;
import com.example.al_mirath.service.GameEngine;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private Stage primaryStage;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        this.primaryStage.setTitle("Al-Mirath: The Legacy");
        showWelcomeScreen();
    }

    /**
     * Installs a freshly loaded screen root onto the primary stage.
     *
     * <p>Every screen swap used to build a brand-new {@code Scene} and hand it
     * to the stage directly, which silently dropped a maximized or full-screen
     * window back down to the base 1280x720 size — JavaFX resets those flags
     * when the scene changes unless they're explicitly reapplied afterward.
     * Centralizing the swap here means every screen gets that fix for free.
     */
    private void applyScene(Parent root) {
        boolean wasMaximized = primaryStage.isMaximized();
        boolean wasFullScreen = primaryStage.isFullScreen();

        Scene scene = new Scene(root, 1280, 720);
        primaryStage.setScene(scene);
        primaryStage.show();

        if (wasFullScreen) {
            primaryStage.setFullScreen(true);
        } else if (wasMaximized) {
            // setMaximized(true) immediately after setScene doesn't always
            // stick until the new scene has completed its first layout pass.
            Platform.runLater(() -> primaryStage.setMaximized(true));
        }
    }

    public void showWelcomeScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/al_mirath/fxml/welcome-screen.fxml")
            );

            Parent root = loader.load();

            WelcomeController controller = loader.getController();
            controller.setMainApp(this);

            applyScene(root);

            System.out.println("Welcome screen loaded.");

        } catch (Exception e) {
            System.out.println("Failed to load welcome screen.");
            e.printStackTrace();
        }
    }

    public void showGameScreen() {
        showGameScreen(null);
    }

    public void showGameScreen(GameEngine restoredEngine) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/al_mirath/fxml/game-screen.fxml")
            );

            GameController controller = new GameController();
            controller.setRestoredEngine(restoredEngine);
            loader.setController(controller);

            Parent root = loader.load();

            controller.setMainApp(this);

            applyScene(root);

            System.out.println("Game screen loaded.");

        } catch (Exception e) {
            System.out.println("Failed to load game screen.");
            e.printStackTrace();
        }
    }

    public void showAchievementsScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/al_mirath/fxml/achievements-screen.fxml")
            );

            Parent root = loader.load();

            AchievementsController controller = loader.getController();
            controller.setMainApp(this);

            applyScene(root);

            System.out.println("Achievements screen loaded.");

        } catch (Exception e) {
            System.out.println("Failed to load achievements screen.");
            e.printStackTrace();
        }
    }

    public void showSettingsScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/al_mirath/fxml/settings-screen.fxml")
            );

            Parent root = loader.load();

            SettingsController controller = loader.getController();
            controller.setMainApp(this);

            applyScene(root);

            System.out.println("Settings screen loaded.");

        } catch (Exception e) {
            System.out.println("Failed to load settings screen.");
            e.printStackTrace();
        }
    }

    public void showLegacyRecordsScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/al_mirath/fxml/legacy-records-screen.fxml")
            );

            Parent root = loader.load();

            LegacyRecordsController controller = loader.getController();
            controller.setMainApp(this);

            applyScene(root);

            System.out.println("Legacy records screen loaded.");

        } catch (Exception e) {
            System.out.println("Failed to load legacy records screen.");
            e.printStackTrace();
        }
    }

    public void exitGame() {
        primaryStage.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
