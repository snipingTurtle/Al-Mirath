package com.example.al_mirath.ui;

import com.example.al_mirath.model.Achievement;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Transient corner notifications, used for achievement unlocks.
 *
 * <p>Sits above the HUD but stays mouse-transparent so a toast can never eat a
 * click meant for the game underneath. Toasts stack downward and expire on
 * their own.
 */
public class ToastLayer extends StackPane {

    private final VBox stack = new VBox(10);

    public ToastLayer() {
        setMouseTransparent(true);
        setPickOnBounds(false);
        setAlignment(Pos.TOP_RIGHT);

        stack.setAlignment(Pos.TOP_RIGHT);
        stack.setMaxWidth(380);
        stack.setPickOnBounds(false);
        stack.setMouseTransparent(true);

        StackPane.setAlignment(stack, Pos.TOP_RIGHT);
        stack.setTranslateY(84);
        stack.setTranslateX(-24);

        getChildren().add(stack);
    }

    public void showAchievement(Achievement achievement) {
        show("Achievement Unlocked", achievement.name(), "toast-achievement");
    }

    public void showFateToken(String message) {
        show("Thread of Fate", message, "toast-fate");
    }

    public void show(String heading, String body, String styleClass) {
        // Callers may fire this from an animation or DB callback; keep it on the FX thread.
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> show(heading, body, styleClass));
            return;
        }

        Label headingLabel = new Label(heading);
        headingLabel.getStyleClass().add("toast-heading");

        Label bodyLabel = new Label(body);
        bodyLabel.getStyleClass().add("toast-body");
        bodyLabel.setWrapText(true);
        bodyLabel.setMaxWidth(320);

        VBox toast = new VBox(4, headingLabel, bodyLabel);
        toast.getStyleClass().addAll("toast", styleClass);
        toast.setMaxWidth(360);
        toast.setOpacity(0);

        stack.getChildren().add(toast);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(260), toast);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        TranslateTransition slideIn = new TranslateTransition(Duration.millis(260), toast);
        slideIn.setFromX(60);
        slideIn.setToX(0);

        PauseTransition hold = new PauseTransition(Duration.seconds(3.4));

        FadeTransition fadeOut = new FadeTransition(Duration.millis(320), toast);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        SequentialTransition sequence = new SequentialTransition(fadeIn, hold, fadeOut);
        sequence.setOnFinished(event -> stack.getChildren().remove(toast));

        slideIn.play();
        sequence.play();
    }
}
