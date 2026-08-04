package com.example.al_mirath.ui;

import com.example.al_mirath.model.Choice;
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
 * The fork shown when a choice tests a stat: roll for it, or play for it.
 *
 * <p>Leaving it to fate is the honest baseline — the odds shown are the real
 * ones the engine would use. Taking the trial hands the outcome to the
 * player's own performance instead, which is what stops a long run from
 * being nothing but reading and clicking.
 */
public class TrialPromptOverlay extends StackPane {

    public enum Decision {
        PLAY, ROLL, CANCEL
    }

    private final Consumer<Decision> onDecided;
    private boolean decided = false;

    public TrialPromptOverlay(Choice choice, int statValue, Consumer<Decision> onDecided) {
        this.onDecided = onDecided;

        getStyleClass().add("minigame-overlay");
        setAlignment(Pos.CENTER);

        Label heading = new Label("A Test of " + prettyStat(choice.getCheckStat()));
        heading.getStyleClass().add("minigame-title");

        Label chosen = new Label("“" + choice.getText() + "”");
        chosen.getStyleClass().add("minigame-flavour");
        chosen.setWrapText(true);
        chosen.setMaxWidth(620);

        // Mirrors GameEngine.resolveChoiceSuccess so the number shown is the
        // number actually used if the player declines the trial.
        int odds = Math.max(10, Math.min(90, 50 + (statValue - choice.getDifficulty())));

        Label explanation = new Label(
                "Your " + prettyStat(choice.getCheckStat()) + " is " + statValue
                        + ", against a difficulty of " + choice.getDifficulty() + ".\n\n"
                        + "Leave it to fate and the odds are " + odds + "%.\n"
                        + "Take the trial and the outcome is yours to earn."
        );
        explanation.getStyleClass().add("minigame-instructions");
        explanation.setWrapText(true);
        explanation.setMaxWidth(620);

        Button play = new Button("Take the Trial");
        play.getStyleClass().addAll("scroll-popup-button", "trial-button");
        play.setOnAction(event -> decide(Decision.PLAY));

        Button roll = new Button("Leave It to Fate  (" + odds + "%)");
        roll.getStyleClass().add("scroll-popup-button");
        roll.setOnAction(event -> decide(Decision.ROLL));

        Button back = new Button("Choose Differently");
        back.getStyleClass().add("scroll-popup-button");
        back.setOnAction(event -> decide(Decision.CANCEL));

        HBox buttons = new HBox(14, play, roll, back);
        buttons.setAlignment(Pos.CENTER);

        VBox content = new VBox(20, heading, chosen, explanation, buttons);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(760);
        content.getStyleClass().add("minigame-panel");

        getChildren().add(content);
    }

    private void decide(Decision decision) {
        if (decided) {
            return;
        }

        decided = true;

        FadeTransition fade = new FadeTransition(Duration.millis(160), this);
        fade.setFromValue(getOpacity());
        fade.setToValue(0);

        fade.setOnFinished(event -> {
            if (getParent() instanceof javafx.scene.layout.Pane parent) {
                parent.getChildren().remove(this);
            }

            if (onDecided != null) {
                onDecided.accept(decision);
            }
        });

        fade.play();
    }

    private String prettyStat(String stat) {
        if (stat == null) {
            return "Skill";
        }

        return switch (stat) {
            case "health" -> "Health";
            case "wealth" -> "Wealth";
            case "education" -> "Learning";
            case "reputation" -> "Standing";
            case "politicalPower" -> "Influence";
            case "morality" -> "Conscience";
            case "familyLoyalty" -> "Loyalty";
            case "stress" -> "Endurance";
            default -> "Skill";
        };
    }
}
