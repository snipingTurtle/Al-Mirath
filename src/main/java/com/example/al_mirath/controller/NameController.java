package com.example.al_mirath.controller;

import com.example.al_mirath.Main;
import com.example.al_mirath.service.BackgroundLibrary;
import com.example.al_mirath.service.CharacterGenerator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

/**
 * The one thing about a life the player decides before living it.
 *
 * <p>Everything else — the era, the household, the temperament — is still
 * rolled, because a run is supposed to hand you a person rather than let you
 * build one. The name is the exception: it goes on the buttons, into the
 * chronicle and onto the house that outlives you, and there is no reason it
 * should be somebody else's choice.
 */
public class NameController implements ScreenLifecycle {

    private Main mainApp;

    /** Only here to roll names for the button that asks for one. */
    private final CharacterGenerator generator = new CharacterGenerator();

    @FXML private StackPane nameRoot;
    @FXML private ImageView nameBackground;
    @FXML private Rectangle nameOverlay;
    @FXML private Label nameNoteLabel;
    @FXML private TextField nameField;
    @FXML private Button beginButton;
    @FXML private Button rollNameButton;

    @FXML
    public void initialize() {
        bindBackground();
        loadBackground();

        // Opening on a rolled name means a player who just wants to play can
        // press Begin without inventing anything, and a player who does want
        // to name themselves has an example of the shape names take here.
        if (nameField != null) {
            nameField.setText(generator.randomName());

            Platform.runLater(() -> {
                nameField.requestFocus();
                nameField.selectAll();
            });
        }

        System.out.println("NameController initialized.");
    }

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    private void bindBackground() {
        if (nameRoot != null && nameBackground != null) {
            nameBackground.fitWidthProperty().bind(nameRoot.widthProperty());
            nameBackground.fitHeightProperty().bind(nameRoot.heightProperty());
            nameBackground.setPreserveRatio(false);
            nameBackground.setSmooth(true);
        }

        if (nameRoot != null && nameOverlay != null) {
            nameOverlay.widthProperty().bind(nameRoot.widthProperty());
            nameOverlay.heightProperty().bind(nameRoot.heightProperty());
        }
    }

    private void loadBackground() {
        if (nameBackground == null) {
            return;
        }

        try {
            String path = BackgroundLibrary.getMenuBackground();

            if (path == null || path.isBlank()) {
                return;
            }

            nameBackground.setImage(
                    new Image(getClass().getResource(path).toExternalForm(), true)
            );

        } catch (Exception e) {
            System.out.println("Failed to load naming background.");
        }
    }

    /** What the player typed, or a rolled name if they typed nothing usable. */
    public String chosenName() {
        return generator.nameOrRandom(nameField == null ? null : nameField.getText());
    }

    @FXML
    private void rollAName() {
        if (nameField != null) {
            nameField.setText(generator.randomName());
            nameField.selectAll();
        }
    }

    @FXML
    private void beginLife() {
        if (mainApp == null) {
            System.out.println("ERROR: mainApp is null in NameController.");
            return;
        }

        mainApp.showNewLife(chosenName());
    }

    @FXML
    private void backToMenu() {
        if (mainApp != null) {
            mainApp.showWelcomeScreen();
        }
    }

    @Override
    public void dispose() {
        // No timelines here; the screen is still a ScreenLifecycle so Main can
        // treat every screen the same way.
    }
}
