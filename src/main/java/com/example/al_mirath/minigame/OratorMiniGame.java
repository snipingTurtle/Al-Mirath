package com.example.al_mirath.minigame;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * The Orator's Floor — hold a hostile room by choosing the right register.
 *
 * <p>The room's mood swings between hostile, sceptical, and receptive, and the
 * three responses (Concede, Reason, Assert) each work in exactly one of those
 * moods and backfire in another. The mood is shown, so this is a reading test
 * rather than a guessing game: the pressure comes from a shrinking clock and
 * from needing a run of correct reads rather than a single lucky one.
 */
public class OratorMiniGame extends AbstractMiniGame {

    private enum Mood {
        HOSTILE("The room is hostile — shouted over, arms folded", "Concede"),
        SCEPTICAL("The room is sceptical — listening, unconvinced", "Reason"),
        RECEPTIVE("The room is with you — leaning in, waiting", "Assert");

        final String description;
        final String correctResponse;

        Mood(String description, String correctResponse) {
            this.description = description;
            this.correctResponse = correctResponse;
        }
    }

    private final Random random = new Random();
    private final List<Mood> sequence = new ArrayList<>();
    private final int roundsToWin;

    private int round = 0;
    private int score = 0;
    private double secondsLeft;
    private final double secondsPerRound;

    private Label moodLabel;
    private Label progressLabel;
    private ProgressBar clockBar;
    private final List<Button> responseButtons = new ArrayList<>();
    private Timeline clock;

    public OratorMiniGame(int difficulty) {
        super(difficulty);

        this.roundsToWin = 3 + this.difficulty;
        this.secondsPerRound = Math.max(2.0, 5.0 - (this.difficulty * 0.5));
        this.secondsLeft = this.secondsPerRound;
    }

    @Override
    public String getTitle() {
        return "The Orator's Floor";
    }

    @Override
    public String getFlavourText() {
        return "The room is full and it has already decided it does not like you. "
                + "A speaker who cannot feel which way a room is leaning will lose it "
                + "in three sentences.";
    }

    @Override
    public String getInstructions() {
        return "Read the room, then answer in the register it will accept.\n"
                + "Hostile rooms want you to concede. Sceptical rooms want reasoning.\n"
                + "A room already with you wants you to assert.\n"
                + "Hold the floor for " + roundsToWin + " exchanges. One wrong register loses it.";
    }

    @Override
    public Node buildView() {
        moodLabel = new Label();
        moodLabel.getStyleClass().add("minigame-target");
        moodLabel.setWrapText(true);
        moodLabel.setMaxWidth(560);

        progressLabel = new Label();
        progressLabel.getStyleClass().add("minigame-status");

        clockBar = new ProgressBar(1.0);
        clockBar.setPrefWidth(420);
        clockBar.getStyleClass().add("minigame-timer");

        HBox buttons = new HBox(14);
        buttons.setAlignment(Pos.CENTER);

        for (String response : List.of("Concede", "Reason", "Assert")) {
            Button button = new Button(response);
            button.getStyleClass().add("scroll-popup-button");
            button.setPrefWidth(170);
            button.setFocusTraversable(false);
            button.setOnAction(event -> answer(response));

            responseButtons.add(button);
            buttons.getChildren().add(button);
        }

        VBox root = new VBox(20, progressLabel, moodLabel, buttons, clockBar);
        root.setAlignment(Pos.CENTER);

        return root;
    }

    @Override
    public void start() {
        buildSequence();
        nextRound();
        startClock();
    }

    @Override
    public void stop() {
        if (clock != null) {
            clock.stop();
        }
    }

    /** Pre-rolls the moods so no round can repeat the same one three times over. */
    private void buildSequence() {
        sequence.clear();

        for (int i = 0; i < roundsToWin; i++) {
            sequence.add(Mood.values()[random.nextInt(Mood.values().length)]);
        }

        Collections.shuffle(sequence, random);
    }

    private void startClock() {
        clock = new Timeline(new KeyFrame(Duration.millis(100), event -> {
            secondsLeft -= 0.1;
            clockBar.setProgress(Math.max(0, secondsLeft / secondsPerRound));

            if (secondsLeft <= 0) {
                finish(MiniGameResult.lost(
                        score,
                        "You hesitated a beat too long. The room turned to the next speaker."
                ));
            }
        }));

        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();
    }

    private void nextRound() {
        if (round >= sequence.size()) {
            finish(MiniGameResult.won(
                    score,
                    "You held the floor to the end, and the room forgot it had come in hostile."
            ));
            return;
        }

        moodLabel.setText(sequence.get(round).description);
        progressLabel.setText("Exchange " + (round + 1) + " of " + roundsToWin);

        secondsLeft = secondsPerRound;
    }

    private void answer(String response) {
        if (isFinished() || round >= sequence.size()) {
            return;
        }

        Mood mood = sequence.get(round);

        if (!mood.correctResponse.equals(response)) {
            finish(MiniGameResult.lost(
                    score,
                    "Wrong register. The room hardens against you and does not soften again."
            ));
            return;
        }

        score += 30 + (int) (secondsLeft * 8);
        round++;

        nextRound();
    }
}
