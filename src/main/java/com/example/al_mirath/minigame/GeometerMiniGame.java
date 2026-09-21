package com.example.al_mirath.minigame;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * The Geometer's Tile — finish a pattern that was never allowed to be arbitrary.
 *
 * <p>A tiling is built four-fold symmetric, the way the craft actually builds
 * them, and then one quarter of it is lifted out. Exactly one of the offered
 * tiles restores the symmetry; the others are near misses, and get nearer as
 * the work gets harder.
 *
 * <p>The rule is never stated, because a geometer is not told it either. It is
 * visible in the three quarters still on the wall.
 */
public class GeometerMiniGame extends AbstractMiniGame {

    private static final double MAIN_CELL = 26;
    private static final double OPTION_CELL = 17;

    private final Random random = new Random();

    private final int size;
    private final int half;
    private final int rounds;
    private final int mistakesAllowed;
    private final double secondsPerRound;

    private int round = 0;
    private int mistakes = 0;
    private int score = 0;

    private boolean[][] pattern;
    private boolean[][] answer;

    private Label statusLabel;
    private GridPane mainGrid;
    private HBox optionsRow;
    private ProgressBar clock;
    private Timeline countdown;
    private boolean acceptingInput = false;

    public GeometerMiniGame(int difficulty) {
        super(difficulty);

        this.size = this.difficulty >= 4 ? 8 : 6;
        this.half = size / 2;
        this.rounds = 2 + (this.difficulty + 1) / 2;
        this.mistakesAllowed = this.difficulty <= 2 ? 1 : 0;
        this.secondsPerRound = Math.max(6, 16 - this.difficulty * 2);
    }

    @Override
    public String getTitle() {
        return "The Geometer's Tile";
    }

    @Override
    public String getFlavourText() {
        return "A panel has come off the wall of the madrasa and been broken. The master mason "
                + "lays four replacements on the bench. \"Only one of these was ever on that wall,\" "
                + "he says. \"Look at the rest of it and tell me which.\"";
    }

    @Override
    public String getInstructions() {
        return "One quarter of the panel is missing. Choose the tile that belongs there.\n"
                + "The pattern obeys a rule. It is not written down; it is on the wall.\n"
                + "Survive " + rounds + " panels"
                + (mistakesAllowed > 0 ? ", with one mistake forgiven." : ". A single wrong tile ends it.")
                + "\nEach panel is timed.";
    }

    @Override
    public Node buildView() {
        statusLabel = new Label("Panel 1 of " + rounds);
        statusLabel.getStyleClass().add("minigame-status");

        mainGrid = new GridPane();
        mainGrid.setAlignment(Pos.CENTER);
        mainGrid.setHgap(2);
        mainGrid.setVgap(2);

        optionsRow = new HBox(20);
        optionsRow.setAlignment(Pos.CENTER);

        clock = new ProgressBar(1.0);
        clock.setPrefWidth(340);
        clock.getStyleClass().add("minigame-timer");

        VBox root = new VBox(18, statusLabel, mainGrid, clock, optionsRow);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(4));

        return root;
    }

    @Override
    public void start() {
        nextPanel();
    }

    @Override
    public void stop() {
        acceptingInput = false;

        if (countdown != null) {
            countdown.stop();
        }
    }

    private void nextPanel() {
        round++;

        if (round > rounds) {
            finish(MiniGameResult.won(
                    score,
                    "The mason fits the last tile himself, to be sure, and it goes in without a gap. "
                            + "\"You have the eye,\" he says, which from him is a contract."
            ));
            return;
        }

        statusLabel.setText("Panel " + round + " of " + rounds);

        buildPattern();
        renderMainGrid();
        renderOptions();
        startCountdown();
    }

    /**
     * Builds a four-fold symmetric panel, then lifts out the lower-right
     * quarter. Folding one random quarter onto the other three is what makes
     * the missing piece uniquely recoverable.
     */
    private void buildPattern() {
        boolean[][] seed = new boolean[half][half];

        for (int row = 0; row < half; row++) {
            for (int column = 0; column < half; column++) {
                seed[row][column] = random.nextInt(100) < 45;
            }
        }

        pattern = new boolean[size][size];

        for (int row = 0; row < size; row++) {
            for (int column = 0; column < size; column++) {
                int foldedRow = Math.min(row, size - 1 - row);
                int foldedColumn = Math.min(column, size - 1 - column);

                pattern[row][column] = seed[foldedRow][foldedColumn];
            }
        }

        answer = new boolean[half][half];

        for (int row = 0; row < half; row++) {
            for (int column = 0; column < half; column++) {
                answer[row][column] = pattern[half + row][half + column];
            }
        }
    }

    private void renderMainGrid() {
        mainGrid.getChildren().clear();

        for (int row = 0; row < size; row++) {
            for (int column = 0; column < size; column++) {
                boolean missing = row >= half && column >= half;

                Rectangle cell = new Rectangle(MAIN_CELL, MAIN_CELL);
                cell.setArcWidth(4);
                cell.setArcHeight(4);

                if (missing) {
                    cell.setFill(Color.web("#12100c"));
                    cell.setStroke(Color.web("#5a4a22"));
                    cell.getStrokeDashArray().addAll(3.0, 3.0);
                } else {
                    cell.setFill(pattern[row][column]
                            ? Color.web("#e5b84f")
                            : Color.web("#2c2317"));
                    cell.setStroke(Color.web("#1a1409"));
                }

                cell.setStrokeWidth(1);

                mainGrid.add(cell, column, row);
            }
        }
    }

    private void renderOptions() {
        optionsRow.getChildren().clear();

        List<boolean[][]> tiles = new ArrayList<>();
        tiles.add(answer);

        int mutations = Math.max(1, 4 - difficulty);

        while (tiles.size() < 4) {
            boolean[][] candidate = mutate(answer, mutations);

            if (!containsTile(tiles, candidate)) {
                tiles.add(candidate);
            }
        }

        Collections.shuffle(tiles, random);

        for (boolean[][] tile : tiles) {
            boolean correct = sameTile(tile, answer);

            GridPane view = new GridPane();
            view.setHgap(2);
            view.setVgap(2);
            view.getStyleClass().add("tile-option");
            view.setPadding(new Insets(7));

            for (int row = 0; row < half; row++) {
                for (int column = 0; column < half; column++) {
                    Rectangle cell = new Rectangle(OPTION_CELL, OPTION_CELL);
                    cell.setArcWidth(3);
                    cell.setArcHeight(3);
                    cell.setFill(tile[row][column]
                            ? Color.web("#e5b84f")
                            : Color.web("#2c2317"));
                    cell.setStroke(Color.web("#1a1409"));
                    cell.setStrokeWidth(1);
                    cell.setMouseTransparent(true);

                    view.add(cell, column, row);
                }
            }

            view.setOnMouseClicked(event -> choose(correct));

            optionsRow.getChildren().add(view);
        }
    }

    /** A near miss: the same tile with a handful of cells turned over. */
    private boolean[][] mutate(boolean[][] source, int cellsToFlip) {
        boolean[][] copy = new boolean[half][half];

        for (int row = 0; row < half; row++) {
            System.arraycopy(source[row], 0, copy[row], 0, half);
        }

        for (int flip = 0; flip < cellsToFlip; flip++) {
            int row = random.nextInt(half);
            int column = random.nextInt(half);

            copy[row][column] = !copy[row][column];
        }

        return copy;
    }

    private boolean containsTile(List<boolean[][]> tiles, boolean[][] candidate) {
        for (boolean[][] tile : tiles) {
            if (sameTile(tile, candidate)) {
                return true;
            }
        }

        return false;
    }

    private boolean sameTile(boolean[][] left, boolean[][] right) {
        for (int row = 0; row < half; row++) {
            for (int column = 0; column < half; column++) {
                if (left[row][column] != right[row][column]) {
                    return false;
                }
            }
        }

        return true;
    }

    private void startCountdown() {
        acceptingInput = true;

        if (countdown != null) {
            countdown.stop();
        }

        clock.setProgress(1.0);

        final double tick = 0.05;
        final double[] remaining = {secondsPerRound};

        countdown = new Timeline(new KeyFrame(Duration.seconds(tick), event -> {
            remaining[0] -= tick;
            clock.setProgress(Math.max(0, remaining[0] / secondsPerRound));

            if (remaining[0] <= 0) {
                countdown.stop();
                choose(false);
            }
        }));

        countdown.setCycleCount(Timeline.INDEFINITE);
        countdown.play();
    }

    private void choose(boolean correct) {
        if (!acceptingInput || isFinished()) {
            return;
        }

        acceptingInput = false;

        if (countdown != null) {
            countdown.stop();
        }

        if (correct) {
            score += 60 + round * 20;
            statusLabel.setText("It fits.");

            Timeline pause = new Timeline(new KeyFrame(
                    Duration.millis(520),
                    event -> nextPanel()
            ));

            pause.play();
            return;
        }

        mistakes++;

        if (mistakes > mistakesAllowed) {
            finish(MiniGameResult.lost(
                    score,
                    "The mason holds your tile against the gap without a word, and the pattern "
                            + "breaks across the join. He puts it back on the bench."
            ));
            return;
        }

        statusLabel.setText("Not that one. You have no more allowances.");

        Timeline pause = new Timeline(new KeyFrame(
                Duration.millis(900),
                event -> nextPanel()
        ));

        pause.play();
    }
}
