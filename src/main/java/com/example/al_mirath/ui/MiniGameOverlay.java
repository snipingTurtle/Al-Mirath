package com.example.al_mirath.ui;

import com.example.al_mirath.minigame.MiniGame;
import com.example.al_mirath.minigame.MiniGameResult;
import javafx.animation.FadeTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.function.Consumer;

/**
 * Full-screen host for a Threads of Fate challenge.
 *
 * <p>Runs the three-beat flow every attempt shares — briefing, play, verdict —
 * so the individual mini-games only implement their own rules. The overlay is
 * added on top of the game root and removes itself once the player dismisses
 * the verdict.
 */
public class MiniGameOverlay extends StackPane {

    private final MiniGame miniGame;
    private final Consumer<MiniGameResult> onComplete;

    private final VBox contentBox = new VBox(20);

    private MiniGameResult result;

    public MiniGameOverlay(MiniGame miniGame, Consumer<MiniGameResult> onComplete) {
        this.miniGame = miniGame;
        this.onComplete = onComplete;

        getStyleClass().add("minigame-overlay");
        setAlignment(Pos.CENTER);

        contentBox.setAlignment(Pos.CENTER);
        contentBox.setMaxWidth(760);
        contentBox.getStyleClass().add("minigame-panel");

        getChildren().add(contentBox);

        showBriefing();
    }

    /** Beat one: what this challenge is and what it costs. */
    private void showBriefing() {
        Label title = new Label(miniGame.getTitle());
        title.getStyleClass().add("minigame-title");

        Label flavour = new Label(miniGame.getFlavourText());
        flavour.getStyleClass().add("minigame-flavour");
        flavour.setWrapText(true);
        flavour.setMaxWidth(620);

        Label instructions = new Label(miniGame.getInstructions());
        instructions.getStyleClass().add("minigame-instructions");
        instructions.setWrapText(true);
        instructions.setMaxWidth(620);

        Button begin = new Button("Begin the Trial");
        begin.getStyleClass().add("scroll-popup-button");
        begin.setOnAction(event -> showGame());

        Button withdraw = new Button("Withdraw");
        withdraw.getStyleClass().add("scroll-popup-button");
        withdraw.setOnAction(event -> {
            // Backing out costs nothing: the token is only spent on a real attempt.
            result = null;
            dismiss();
        });

        HBox buttons = new HBox(16, begin, withdraw);
        buttons.setAlignment(Pos.CENTER);

        replaceContent(title, flavour, instructions, buttons);
    }

    /** Beat two: hand the screen over to the mini-game itself. */
    private void showGame() {
        Label title = new Label(miniGame.getTitle());
        title.getStyleClass().add("minigame-title");

        var view = miniGame.buildView();

        miniGame.setOnFinished(gameResult -> {
            this.result = gameResult;
            showResult(gameResult);
        });

        replaceContent(title, view);

        // The view must be in the scene before timers start, and focused so
        // key-driven games (the courier's Space bar) receive input immediately.
        view.requestFocus();
        miniGame.start();
    }

    /** Beat three: the verdict, and what it means for the run. */
    private void showResult(MiniGameResult gameResult) {
        Label title = new Label(gameResult.success() ? "The Thread Holds" : "The Thread Snaps");
        title.getStyleClass().addAll("minigame-title", gameResult.success() ? "result-win" : "result-loss");

        Label summary = new Label(gameResult.summary());
        summary.getStyleClass().add("minigame-flavour");
        summary.setWrapText(true);
        summary.setMaxWidth(620);

        Label score = new Label("Score: " + gameResult.score());
        score.getStyleClass().add("minigame-status");

        Label consequence = new Label(gameResult.success()
                ? "Your decision is undone. Choose again."
                : "The decision stands. The thread is spent.");
        consequence.getStyleClass().add("minigame-instructions");
        consequence.setWrapText(true);
        consequence.setMaxWidth(620);

        Button close = new Button(gameResult.success() ? "Return and Choose" : "Accept It");
        close.getStyleClass().add("scroll-popup-button");
        close.setOnAction(event -> dismiss());

        replaceContent(title, summary, score, consequence, close);
    }

    private void replaceContent(javafx.scene.Node... nodes) {
        contentBox.getChildren().setAll(nodes);

        FadeTransition fade = new FadeTransition(Duration.millis(200), contentBox);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    private void dismiss() {
        miniGame.stop();

        FadeTransition fade = new FadeTransition(Duration.millis(180), this);
        fade.setFromValue(getOpacity());
        fade.setToValue(0);

        fade.setOnFinished(event -> {
            if (getParent() instanceof javafx.scene.layout.Pane parent) {
                parent.getChildren().remove(this);
            }

            if (onComplete != null) {
                onComplete.accept(result);
            }
        });

        fade.play();
    }
}
