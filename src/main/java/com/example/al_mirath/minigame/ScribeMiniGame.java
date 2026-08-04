package com.example.al_mirath.minigame;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The Scribe's Hand — a copying test framed as a scholar's memory drill.
 *
 * <p>A sequence of glyphs lights up in order and the player repeats it from
 * memory. Each round adds one glyph; clearing every round wins the rewind.
 * A single mistake ends the attempt, so the tension rises as the sequence
 * grows and the player has more to lose.
 */
public class ScribeMiniGame extends AbstractMiniGame {

    private static final String[] GLYPHS = {"ا", "ب", "ج", "د", "ه", "و", "ز", "ح", "ط"};

    private final Random random = new Random();
    private final List<Integer> sequence = new ArrayList<>();
    private final List<Button> glyphButtons = new ArrayList<>();

    private final int rounds;
    private final int gridSize;

    private int currentRound = 0;
    private int playerIndex = 0;
    private int score = 0;

    private boolean acceptingInput = false;

    private Label statusLabel;
    private Timeline playbackTimeline;

    public ScribeMiniGame(int difficulty) {
        super(difficulty);

        // Harder runs mean both a bigger board and more rounds to survive.
        this.gridSize = this.difficulty <= 2 ? 4 : (this.difficulty == 3 ? 6 : 9);
        this.rounds = 2 + this.difficulty;
    }

    @Override
    public String getTitle() {
        return "The Scribe's Hand";
    }

    @Override
    public String getFlavourText() {
        return "An old scribe sets a reed pen before you. \"A record survives only if the hand "
                + "remembers what the eye saw. Copy what I write, and the page may yet be rewritten.\"";
    }

    @Override
    public String getInstructions() {
        return "Watch the glyphs light up in order, then repeat the sequence by clicking them.\n"
                + "Each round adds one glyph. Survive all " + rounds + " rounds to win.\n"
                + "One wrong glyph ends the attempt.";
    }

    @Override
    public Node buildView() {
        statusLabel = new Label("Watch carefully...");
        statusLabel.getStyleClass().add("minigame-status");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(14);
        grid.setVgap(14);
        grid.getStyleClass().add("minigame-grid");

        int columns = gridSize <= 4 ? 2 : 3;

        for (int i = 0; i < gridSize; i++) {
            Button glyph = new Button(GLYPHS[i]);
            glyph.getStyleClass().add("glyph-button");
            glyph.setPrefSize(90, 90);
            glyph.setFocusTraversable(false);

            final int index = i;
            glyph.setOnAction(event -> handleGlyphPress(index));

            glyphButtons.add(glyph);
            grid.add(glyph, i % columns, i / columns);
        }

        VBox root = new VBox(20, statusLabel, grid);
        root.setAlignment(Pos.CENTER);

        return root;
    }

    @Override
    public void start() {
        nextRound();
    }

    @Override
    public void stop() {
        acceptingInput = false;

        if (playbackTimeline != null) {
            playbackTimeline.stop();
        }
    }

    private void nextRound() {
        currentRound++;

        if (currentRound > rounds) {
            finish(MiniGameResult.won(
                    score,
                    "The scribe nods. Your hand held every stroke he set before you."
            ));
            return;
        }

        sequence.add(random.nextInt(gridSize));
        playerIndex = 0;

        statusLabel.setText("Round " + currentRound + " of " + rounds + " — watch the sequence");

        playSequence();
    }

    /**
     * Flashes the sequence, then hands control back to the player.
     * Playback speed increases with difficulty to keep later rounds sharp.
     */
    private void playSequence() {
        acceptingInput = false;
        setBoardEnabled(false);

        double stepMillis = Math.max(380, 700 - (difficulty * 60));

        playbackTimeline = new Timeline();

        for (int i = 0; i < sequence.size(); i++) {
            int glyphIndex = sequence.get(i);
            double offset = i * stepMillis;

            playbackTimeline.getKeyFrames().add(new KeyFrame(
                    Duration.millis(offset),
                    event -> highlight(glyphIndex, true)
            ));

            playbackTimeline.getKeyFrames().add(new KeyFrame(
                    Duration.millis(offset + stepMillis * 0.6),
                    event -> highlight(glyphIndex, false)
            ));
        }

        playbackTimeline.getKeyFrames().add(new KeyFrame(
                Duration.millis(sequence.size() * stepMillis + 200),
                event -> {
                    statusLabel.setText("Now repeat it — " + sequence.size() + " glyphs");
                    acceptingInput = true;
                    setBoardEnabled(true);
                }
        ));

        playbackTimeline.play();
    }

    private void handleGlyphPress(int index) {
        if (!acceptingInput || isFinished()) {
            return;
        }

        flash(index);

        if (sequence.get(playerIndex) != index) {
            finish(MiniGameResult.lost(
                    score,
                    "Your hand faltered at stroke " + (playerIndex + 1)
                            + ". The scribe closes the book without a word."
            ));
            return;
        }

        playerIndex++;
        score += 10 * currentRound;

        if (playerIndex >= sequence.size()) {
            acceptingInput = false;
            setBoardEnabled(false);
            statusLabel.setText("Correct.");

            Timeline pause = new Timeline(new KeyFrame(
                    Duration.millis(600),
                    event -> nextRound()
            ));

            pause.play();
        }
    }

    private void highlight(int index, boolean active) {
        Button glyph = glyphButtons.get(index);

        if (active) {
            if (!glyph.getStyleClass().contains("glyph-active")) {
                glyph.getStyleClass().add("glyph-active");
            }
        } else {
            glyph.getStyleClass().remove("glyph-active");
        }
    }

    /** Brief acknowledgement flash so a click always feels registered. */
    private void flash(int index) {
        highlight(index, true);

        Timeline off = new Timeline(new KeyFrame(
                Duration.millis(180),
                event -> highlight(index, false)
        ));

        off.play();
    }

    private void setBoardEnabled(boolean enabled) {
        for (Button glyph : glyphButtons) {
            glyph.setDisable(!enabled);
        }
    }
}
