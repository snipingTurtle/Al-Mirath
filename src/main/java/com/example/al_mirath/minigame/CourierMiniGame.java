package com.example.al_mirath.minigame;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.Random;

/**
 * The Night Courier — slip a sealed letter past the watch.
 *
 * <p>A marker sweeps back and forth across a bar and the player stops it inside
 * a narrow safe window. Each success narrows the window and speeds the sweep,
 * so the difficulty curves upward within a single attempt rather than being
 * flat. Missing once ends the run, which makes every press deliberate.
 */
public class CourierMiniGame extends AbstractMiniGame {

    private static final double TRACK_WIDTH = 520;
    private static final double TRACK_HEIGHT = 64;
    private static final double MARKER_WIDTH = 8;

    private final Random random = new Random();
    private final int requiredPasses;

    private int passes = 0;
    private int score = 0;

    private double zoneWidth;
    private double sweepMillis;

    private Rectangle safeZone;
    private Rectangle marker;
    private Label statusLabel;
    private Button stopButton;
    private Timeline sweep;

    public CourierMiniGame(int difficulty) {
        super(difficulty);

        this.requiredPasses = 2 + this.difficulty;
        this.zoneWidth = Math.max(58, 150 - (this.difficulty * 16));
        this.sweepMillis = Math.max(620, 1500 - (this.difficulty * 160));
    }

    @Override
    public String getTitle() {
        return "The Night Courier";
    }

    @Override
    public String getFlavourText() {
        return "A sealed letter, a dark street, and a patrol that turns at the corner. "
                + "\"Move when the lane is clear,\" the courier whispers. \"Not a breath sooner.\"";
    }

    @Override
    public String getInstructions() {
        return "The marker sweeps across the lane. Stop it inside the lit gap.\n"
                + "Press Stop, or tap Space, at the right moment.\n"
                + "Slip past " + requiredPasses + " patrols. Each pass narrows the gap. One miss and you are caught.";
    }

    @Override
    public Node buildView() {
        statusLabel = new Label("Patrol 1 of " + requiredPasses);
        statusLabel.getStyleClass().add("minigame-status");

        Rectangle track = new Rectangle(TRACK_WIDTH, TRACK_HEIGHT);
        track.getStyleClass().add("courier-track");
        track.setArcWidth(14);
        track.setArcHeight(14);

        safeZone = new Rectangle(zoneWidth, TRACK_HEIGHT);
        safeZone.getStyleClass().add("courier-zone");
        safeZone.setArcWidth(10);
        safeZone.setArcHeight(10);

        marker = new Rectangle(MARKER_WIDTH, TRACK_HEIGHT + 14);
        marker.getStyleClass().add("courier-marker");
        marker.setArcWidth(4);
        marker.setArcHeight(4);

        Pane lane = new Pane(track, safeZone, marker);
        lane.setPrefSize(TRACK_WIDTH, TRACK_HEIGHT + 14);
        lane.setMaxSize(TRACK_WIDTH, TRACK_HEIGHT + 14);

        track.setLayoutY(7);
        safeZone.setLayoutY(7);
        marker.setLayoutY(0);

        StackPane laneHolder = new StackPane(lane);
        laneHolder.setAlignment(Pos.CENTER);

        stopButton = new Button("Stop");
        stopButton.getStyleClass().add("scroll-popup-button");
        stopButton.setPrefWidth(180);
        stopButton.setOnAction(event -> attemptStop());

        VBox root = new VBox(22, statusLabel, laneHolder, stopButton);
        root.setAlignment(Pos.CENTER);

        // Space is the natural key for a timing game; the button stays for mouse users.
        root.setFocusTraversable(true);
        root.setOnKeyPressed(event -> {
            if (event.getCode().getName().equalsIgnoreCase("Space")) {
                attemptStop();
                event.consume();
            }
        });

        return root;
    }

    @Override
    public void start() {
        beginPass();
    }

    @Override
    public void stop() {
        if (sweep != null) {
            sweep.stop();
        }
    }

    private void beginPass() {
        if (passes >= requiredPasses) {
            finish(MiniGameResult.won(
                    score,
                    "The last patrol turns the corner. The letter reaches the door unopened."
            ));
            return;
        }

        statusLabel.setText("Patrol " + (passes + 1) + " of " + requiredPasses);

        // Keep the gap away from the extreme edges so no pass is unwinnable.
        double maxStart = TRACK_WIDTH - zoneWidth - 20;
        double zoneStart = 20 + random.nextDouble() * Math.max(1, maxStart - 20);

        safeZone.setWidth(zoneWidth);
        safeZone.setLayoutX(zoneStart);

        if (sweep != null) {
            sweep.stop();
        }

        marker.setLayoutX(0);

        sweep = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(marker.layoutXProperty(), 0, Interpolator.LINEAR)),
                new KeyFrame(Duration.millis(sweepMillis),
                        new KeyValue(marker.layoutXProperty(), TRACK_WIDTH - MARKER_WIDTH, Interpolator.LINEAR))
        );

        sweep.setAutoReverse(true);
        sweep.setCycleCount(Timeline.INDEFINITE);
        sweep.play();
    }

    private void attemptStop() {
        if (isFinished() || sweep == null) {
            return;
        }

        sweep.pause();

        double markerCentre = marker.getLayoutX() + (MARKER_WIDTH / 2);
        double zoneStart = safeZone.getLayoutX();
        double zoneEnd = zoneStart + safeZone.getWidth();

        if (markerCentre < zoneStart || markerCentre > zoneEnd) {
            finish(MiniGameResult.lost(
                    score,
                    "A lantern swings your way at the wrong moment. The letter never arrives."
            ));
            return;
        }

        // Reward precision: the closer to the centre, the higher the score.
        double zoneCentre = zoneStart + (safeZone.getWidth() / 2);
        double offset = Math.abs(markerCentre - zoneCentre);
        double accuracy = 1.0 - (offset / (safeZone.getWidth() / 2));

        passes++;
        score += 40 + (int) (accuracy * 60);

        // Tighten the screws for the next patrol.
        zoneWidth = Math.max(34, zoneWidth * 0.84);
        sweepMillis = Math.max(420, sweepMillis * 0.9);

        statusLabel.setText(accuracy > 0.75 ? "Clean pass." : "Through — barely.");

        Timeline pause = new Timeline(new KeyFrame(
                Duration.millis(550),
                event -> beginPass()
        ));

        pause.play();
    }
}
