package com.example.al_mirath.minigame;

import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.util.Duration;

import java.util.Random;

/**
 * The Butts — loose an arrow while the aim will not hold still.
 *
 * <p>The reticle drifts on two crossed rhythms, so it never repeats the same
 * path and cannot be learned as a loop. The player looses when it crosses the
 * gold. Every arrow after the first drifts wider: a steady hand is easiest on
 * the first shot and hardest on the last, which is also true of archery.
 */
public class ArcheryMiniGame extends AbstractMiniGame {

    private static final double FACE_RADIUS = 150;
    private static final double RETICLE_RADIUS = 7;

    private final Random random = new Random();

    private final int arrows;
    private final int targetScore;

    private double driftX;
    private double driftY;
    private double speedX;
    private double speedY;
    private double phaseX;
    private double phaseY;

    private int arrowsLoosed = 0;
    private int score = 0;

    private Circle reticle;

    /** The crosshair group, moved as one so the lines stay on the reticle. */
    private Pane sight;

    private Label statusLabel;
    private Label tallyLabel;
    private Button looseButton;
    private Pane face;
    private AnimationTimer aim;

    public ArcheryMiniGame(int difficulty) {
        super(difficulty);

        this.arrows = 3 + (this.difficulty / 3);

        // Scored out of ten a ring, so the bar is "most arrows near the gold"
        // rather than "every arrow perfect" — one bad shot should not end it.
        this.targetScore = arrows * (4 + this.difficulty);

        this.driftX = 40 + this.difficulty * 16;
        this.driftY = 30 + this.difficulty * 14;
        this.speedX = 1.5 + this.difficulty * 0.45;
        this.speedY = 1.1 + this.difficulty * 0.38;
        this.phaseX = random.nextDouble() * Math.PI * 2;
        this.phaseY = random.nextDouble() * Math.PI * 2;
    }

    @Override
    public String getTitle() {
        return "The Butts";
    }

    @Override
    public String getFlavourText() {
        return "Straw bosses at eighty paces, and a captain of archers watching without appearing to. "
                + "\"The bow does not care whose son you are,\" he says. \"Draw.\"";
    }

    @Override
    public String getInstructions() {
        return "Your aim drifts. Loose when the point crosses the gold.\n"
                + "You have " + arrows + " arrows. The gold is worth ten, the outermost ring one.\n"
                + "Score " + targetScore + " or better to be counted a marksman.\n"
                + "Press Loose, or tap Space.";
    }

    @Override
    public Node buildView() {
        statusLabel = new Label("Arrow 1 of " + arrows);
        statusLabel.getStyleClass().add("minigame-status");

        tallyLabel = new Label("Score 0 — you need " + targetScore);
        tallyLabel.getStyleClass().add("minigame-instructions");

        face = new Pane();
        face.setPrefSize(FACE_RADIUS * 2, FACE_RADIUS * 2);
        face.setMaxSize(FACE_RADIUS * 2, FACE_RADIUS * 2);
        face.setMinSize(FACE_RADIUS * 2, FACE_RADIUS * 2);

        // Five rings, gold in the middle, drawn from the outside in.
        Color[] ringColours = {
                Color.web("#2f2415"),
                Color.web("#4a3a20"),
                Color.web("#6d5527"),
                Color.web("#b38a2e"),
                Color.web("#f2c94c")
        };

        for (int ring = 0; ring < ringColours.length; ring++) {
            double radius = FACE_RADIUS * (1.0 - ring * 0.19);

            Circle circle = new Circle(FACE_RADIUS, FACE_RADIUS, radius);
            circle.setFill(ringColours[ring]);
            circle.setStroke(Color.web("#1a1208"));
            circle.setStrokeWidth(1.4);
            circle.setMouseTransparent(true);

            face.getChildren().add(circle);
        }

        reticle = new Circle(RETICLE_RADIUS);
        reticle.setFill(Color.TRANSPARENT);
        reticle.setStroke(Color.web("#ffe9a8"));
        reticle.setStrokeWidth(2.4);
        reticle.setMouseTransparent(true);

        Line crossHorizontal = new Line(-14, 0, 14, 0);
        Line crossVertical = new Line(0, -14, 0, 14);

        for (Line line : new Line[]{crossHorizontal, crossVertical}) {
            line.setStroke(Color.web("#ffe9a8"));
            line.setStrokeWidth(1.4);
            line.setMouseTransparent(true);
        }

        sight = new Pane(reticle, crossHorizontal, crossVertical);
        sight.setMouseTransparent(true);
        sight.setManaged(false);

        face.getChildren().add(sight);

        StackPane faceHolder = new StackPane(face);
        faceHolder.setAlignment(Pos.CENTER);

        looseButton = new Button("Loose");
        looseButton.getStyleClass().add("scroll-popup-button");
        looseButton.setPrefWidth(180);
        looseButton.setMinWidth(Region.USE_PREF_SIZE);
        looseButton.setOnAction(event -> loose());

        VBox root = new VBox(18, statusLabel, faceHolder, tallyLabel, looseButton);
        root.setAlignment(Pos.CENTER);
        root.setFocusTraversable(true);

        root.setOnKeyPressed(event -> {
            if (event.getCode().getName().equalsIgnoreCase("Space")) {
                loose();
                event.consume();
            }
        });

        return root;
    }

    @Override
    public void start() {
        final long[] startNanos = {0};

        aim = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (startNanos[0] == 0) {
                    startNanos[0] = now;
                }

                double seconds = (now - startNanos[0]) / 1_000_000_000.0;

                double x = FACE_RADIUS + Math.sin(seconds * speedX + phaseX) * driftX;
                double y = FACE_RADIUS + Math.sin(seconds * speedY + phaseY) * driftY;

                sight.setLayoutX(x);
                sight.setLayoutY(y);
            }
        };

        aim.start();
    }

    @Override
    public void stop() {
        if (aim != null) {
            aim.stop();
        }
    }

    private void loose() {
        if (isFinished() || aim == null || arrowsLoosed >= arrows) {
            return;
        }

        double dx = sight.getLayoutX() - FACE_RADIUS;
        double dy = sight.getLayoutY() - FACE_RADIUS;
        double distance = Math.hypot(dx, dy);

        int ringScore = scoreFor(distance);
        score += ringScore;
        arrowsLoosed++;

        markArrow(sight.getLayoutX(), sight.getLayoutY(), ringScore);

        statusLabel.setText(describe(ringScore));
        tallyLabel.setText("Score " + score + " — you need " + targetScore);

        if (arrowsLoosed >= arrows) {
            looseButton.setDisable(true);

            Timeline settle = new Timeline(new KeyFrame(
                    Duration.millis(700),
                    event -> finishRound()
            ));

            settle.play();
            return;
        }

        // Each arrow is harder than the last: the arm tires.
        driftX *= 1.14;
        driftY *= 1.14;
        speedX *= 1.06;
        speedY *= 1.06;

        Timeline nextArrow = new Timeline(new KeyFrame(
                Duration.millis(550),
                event -> statusLabel.setText("Arrow " + (arrowsLoosed + 1) + " of " + arrows)
        ));

        nextArrow.play();
    }

    private void finishRound() {
        if (score >= targetScore) {
            finish(MiniGameResult.won(
                    score * 6,
                    "The captain walks to the boss, counts your arrows, and walks back without saying anything. "
                            + "That is what it looks like when he is impressed."
            ));
            return;
        }

        finish(MiniGameResult.lost(
                score * 6,
                "You scored " + score + " where " + targetScore + " was wanted. "
                        + "The captain has already turned to the next man on the line."
        ));
    }

    /** Ten in the gold down to one at the edge, and nothing off the face. */
    private int scoreFor(double distance) {
        if (distance > FACE_RADIUS) {
            return 0;
        }

        double fraction = distance / FACE_RADIUS;
        return Math.max(1, (int) Math.ceil((1.0 - fraction) * 10));
    }

    private String describe(int ringScore) {
        if (ringScore >= 9) {
            return "In the gold.";
        }

        if (ringScore >= 6) {
            return "Close in — " + ringScore + ".";
        }

        if (ringScore >= 3) {
            return "Wide — " + ringScore + ".";
        }

        if (ringScore >= 1) {
            return "Barely on the straw — " + ringScore + ".";
        }

        return "Off the boss entirely.";
    }

    /** Leaves the arrow where it landed, so the grouping is visible. */
    private void markArrow(double x, double y, int ringScore) {
        Circle shaft = new Circle(x, y, 4);
        shaft.setFill(ringScore >= 9 ? Color.web("#ffffff") : Color.web("#e8ddc4"));
        shaft.setStroke(Color.web("#2a1d0c"));
        shaft.setStrokeWidth(1.2);
        shaft.setMouseTransparent(true);

        face.getChildren().add(face.getChildren().size() - 1, shaft);
    }
}
