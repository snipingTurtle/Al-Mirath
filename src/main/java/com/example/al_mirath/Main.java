package com.example.al_mirath;

import com.example.al_mirath.controller.GameController;
import com.example.al_mirath.controller.LegacyRecordsController;
import com.example.al_mirath.controller.WelcomeController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
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

    public void showWelcomeScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/al_mirath/fxml/welcome-screen.fxml")
            );

            Scene scene = new Scene(loader.load(), 1280, 720);

            WelcomeController controller = loader.getController();
            controller.setMainApp(this);

            primaryStage.setScene(scene);
            primaryStage.show();

            System.out.println("Welcome screen loaded.");

        } catch (Exception e) {
            System.out.println("Failed to load welcome screen.");
            e.printStackTrace();
        }
    }

    public void showGameScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/al_mirath/fxml/game-screen.fxml")
            );

            Scene scene = new Scene(loader.load(), 1280, 720);

            GameController controller = loader.getController();
            controller.setMainApp(this);

            primaryStage.setScene(scene);
            primaryStage.show();

            System.out.println("Game screen loaded.");

        } catch (Exception e) {
            System.out.println("Failed to load game screen.");
            e.printStackTrace();
        }
    }

    public void showLegacyRecordsScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/al_mirath/fxml/legacy-records-screen.fxml")
            );

            Scene scene = new Scene(loader.load(), 1280, 720);

            LegacyRecordsController controller = loader.getController();
            controller.setMainApp(this);

            primaryStage.setScene(scene);
            primaryStage.show();

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