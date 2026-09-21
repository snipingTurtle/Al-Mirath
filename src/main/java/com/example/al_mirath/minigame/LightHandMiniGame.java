package com.example.al_mirath.minigame;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.util.Duration;

import java.util.Random;

/**
 * The Light Hand — take what you can while the watch is looking elsewhere.
 *
 * <p>The guard's attention swings on no fixed rhythm. There is a moment as he
 * turns back where the street still looks safe and is not, and the whole game
 * lives in whether the player can stop wanting one more purse during it.
 *
 * <p>Greed is the mechanic: every purse taken shortens the windows, so a run
 * that is going well is the most dangerous kind.
 */
public class LightHandMiniGame extends AbstractMiniGame {

    /** Where the guard's attention is. */
    private enum Watch {

        /** Looking away. Safe. */
        AWAY,

        /** Beginning to turn. Looks safe, is not. */
        TURNING,

        /** Looking straight at the crowd. */
        WATCHING
    }

    private final Random random = new Random();

    private final int pursesWanted;

    private double awayMillis;
    private double watchingMillis;
    private final double turningMillis;

    private Watch watch = Watch.WATCHING;
    private int purses = 0;
    private int score = 0;

    private Label statusLabel;
    private Label tallyLabel;
    private Circle lantern;
    private Ellipse eye;
    private Button takeButton;
    private Timeline cycle;

    public LightHandMiniGame(int difficulty) {
        super(difficulty);

        this.pursesWanted = 2 + this.difficulty;
        this.awayMillis = 2400 - this.difficulty * 230;
        this.watchingMillis = 1000 + this.difficulty * 130;

        // The tell. Short enough to punish a slow hand, long enough that a
        // patient player can always see it coming.
        this.turningMillis = Math.max(260, 520 - this.difficulty * 45);
    }

    @Override
    public String getTitle() {
        return "The Light Hand";
    }

    @Override
    public String getFlavourText() {
        return "Market day, and the press at the cloth stalls is shoulder to shoulder. "
                + "One guard for the whole row, and he cannot look at all of it at once.";
    }

    @Override
    public String getInstructions() {
        return "Take a purse only while the guard is looking away.\n"
                + "He gives a warning as he begins to turn back — the lantern flares. "
                + "Reaching in then is the same as reaching in while he watches.\n"
                + "Take " + pursesWanted + " purses. Every one you take makes the next window shorter.";
    }

    @Override
    public Node buildView() {
        statusLabel = new Label("He is watching the row.");
        statusLabel.getStyleClass().add("minigame-status");

        tallyLabel = new Label(tallyText());
        tallyLabel.getStyleClass().add("minigame-instructions");

        lantern = new Circle(72);
        lantern.setFill(Color.web("#3a1f14"));
        lantern.setStroke(Color.web("#7a5a2c"));
        lantern.setStrokeWidth(3);

        eye = new Ellipse(46, 26);
        eye.setFill(Color.web("#f0d79a"));
        eye.setStroke(Color.web("#241608"));
        eye.setStrokeWidth(2);

        Circle pupil = new Circle(13);
        pupil.setFill(Color.web("#1a1208"));
        pupil.setMouseTransparent(true);

        StackPane head = new StackPane(lantern, eye, pupil);
        head.setAlignment(Pos.CENTER);
        head.setMouseTransparent(true);

        takeButton = new Button("Take");
        takeButton.getStyleClass().add("scroll-popup-button");
        takeButton.setPrefWidth(200);
        takeButton.setMinWidth(Region.USE_PREF_SIZE);
        takeButton.setOnAction(event -> take());

        VBox root = new VBox(20, statusLabel, head, tallyLabel, takeButton);
        root.setAlignment(Pos.CENTER);
        root.setFocusTraversable(true);

        root.setOnKeyPressed(event -> {
            if (event.getCode().getName().equalsIgnoreCase("Space")) {
                take();
                event.consume();
            }
        });

        return root;
    }

    @Override
    public void start() {
        // Always opens on him watching, so nobody is caught before they have
        // seen what watching looks like.
        enterWatching();
    }

    @Override
    public void stop() {
        if (cycle != null) {
            cycle.stop();
        }
    }

    private void enterWatching() {
        if (isFinished()) {
            return;
        }

        watch = Watch.WATCHING;
        paint(Color.web("#c0392b"), Color.web("#f0d79a"), "He is watching the row.");

        schedule(watchingMillis * (0.7 + random.nextDouble() * 0.6), this::enterAway);
    }

    private void enterAway() {
        if (isFinished()) {
            return;
        }

        watch = Watch.AWAY;
        paint(Color.web("#27633b"), Color.web("#cfe7d0"), "He has turned to the gate. Now.");

        schedule(awayMillis * (0.6 + random.nextDouble() * 0.8), this::enterTurning);
    }

    private void enterTurning() {
        if (isFinished()) {
            return;
        }

        watch = Watch.TURNING;
        paint(Color.web("#d99b2b"), Color.web("#ffe9a8"), "The lantern swings. He is coming back.");

        schedule(turningMillis, this::enterWatching);
    }

    private void schedule(double millis, Runnable next) {
        if (cycle != null) {
            cycle.stop();
        }

        cycle = new Timeline(new KeyFrame(
                Duration.millis(Math.max(120, millis)),
                event -> next.run()
        ));

        cycle.play();
    }

    private void paint(Color ring, Color iris, String message) {
        lantern.setStroke(ring);
        eye.setFill(iris);
        statusLabel.setText(message);
    }

    private void take() {
        if (isFinished() || takeButton.isDisabled()) {
            return;
        }

        if (watch != Watch.AWAY) {
            caught();
            return;
        }

        purses++;
        score += 80 + purses * 30;
        tallyLabel.setText(tallyText());

        if (purses >= pursesWanted) {
            takeButton.setDisable(true);
            stop();

            statusLabel.setText("Enough. You are three stalls away before he turns.");

            Timeline settle = new Timeline(new KeyFrame(Duration.millis(750), event ->
                    finish(MiniGameResult.won(
                            score,
                            "Four fingers, no grip, and never the same man twice. You are out of "
                                    + "the row and into the alley before the guard has finished turning round."
                    ))
            ));

            settle.play();
            return;
        }

        // A hand that is going well has less time than one that is not.
        awayMillis = Math.max(700, awayMillis * 0.82);
        watchingMillis = Math.max(600, watchingMillis * 0.94);

        statusLabel.setText("Got it. Do not stand still.");

        // The turn comes early after a take, so a good run never settles.
        schedule(260 + random.nextInt(600), this::enterTurning);
    }

    private void caught() {
        takeButton.setDisable(true);
        stop();

        String how = watch == Watch.TURNING
                ? "You had your hand in as the lantern came round. He saw the arm before he saw the face."
                : "You reached in while he was looking straight at you, which is not stealing, it is donating.";

        statusLabel.setText("A hand closes on your wrist.");

        Timeline settle = new Timeline(new KeyFrame(Duration.millis(750), event ->
                finish(MiniGameResult.lost(
                        score,
                        how + " They take you round the back of the guardhouse, where the market cannot see."
                ))
        ));

        settle.play();
    }

    private String tallyText() {
        return "Purses: " + purses + " of " + pursesWanted;
    }
}
