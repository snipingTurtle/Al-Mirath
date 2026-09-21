package com.example.al_mirath.minigame;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * The Poet's Meter — finish the line in front of people who will notice.
 *
 * <p>A couplet is read out with its last word missing, and four words are
 * offered. Only one of them both rhymes and scans; the rest are what a person
 * who has read poetry but never written any would choose. Recitation is a
 * public skill in this world, and so is failing at it.
 */
public class ProsodyMiniGame extends AbstractMiniGame {

    /**
     * A couplet with its closing word lifted out.
     *
     * @param opening     the first line, complete
     * @param closing     the second line, with {@code ___} where the word goes
     * @param answer      the word that rhymes and scans
     * @param distractors words that do one or neither
     */
    private record Verse(String opening, String closing, String answer, List<String> distractors) {
    }

    private static final List<Verse> VERSES = List.of(
            new Verse(
                    "The caravan leaves when the morning is cold,",
                    "and returns with a story worth more than the ___.",
                    "gold",
                    List.of("silver", "treasure", "cargo")),

            new Verse(
                    "He counted his enemies, one to a page,",
                    "and burned the whole ledger when he came of ___.",
                    "age",
                    List.of("years", "sorrow", "silence")),

            new Verse(
                    "The judge heard them argue from noon until night,",
                    "then ruled for the woman who had the least ___.",
                    "might",
                    List.of("power", "money", "standing")),

            new Verse(
                    "A house is not built by the men who complain,",
                    "nor a garden by those who sit waiting on ___.",
                    "rain",
                    List.of("weather", "the seasons", "clouds")),

            new Verse(
                    "He learned the whole Diwan by the end of the spring,",
                    "and still could not say a true word to the ___.",
                    "king",
                    List.of("sultan", "caliph", "governor")),

            new Verse(
                    "A promise is cheap in the cool of the shade,",
                    "and dear in the sun, where the reckoning is ___.",
                    "made",
                    List.of("settled", "counted", "spoken")),

            new Verse(
                    "He studied the stars for a decade or more,",
                    "then married the girl who lived next to his ___.",
                    "door",
                    List.of("house", "garden", "workshop")),

            new Verse(
                    "They gave him a sword and a horse and a name,",
                    "and expected the three of them all to mean the ___.",
                    "same",
                    List.of("identical", "alike", "one thing")),

            new Verse(
                    "The water ran out on the seventeenth day,",
                    "and the guide, who had warned us, had nothing to ___.",
                    "say",
                    List.of("add", "offer", "mention")),

            new Verse(
                    "A ruler is praised for the peace that he keeps,",
                    "and never for what it has cost while he ___.",
                    "sleeps",
                    List.of("rests", "governs", "slumbers")),

            new Verse(
                    "The copyist's hand is worth more than his eye,",
                    "for an eye can be fooled but a hand cannot ___.",
                    "lie",
                    List.of("deceive", "pretend", "err")),

            new Verse(
                    "He wanted the office, the seal, and the chair,",
                    "and got all the three, and no one to ___.",
                    "share",
                    List.of("tell", "confide in", "trust")),

            new Verse(
                    "A debt is a rope you have tied to your own",
                    "and handed the other end to someone ___.",
                    "unknown",
                    List.of("unfamiliar", "strange", "else")),

            new Verse(
                    "The years take the hair and they take the desire,",
                    "and leave you alone with the books and the ___.",
                    "fire",
                    List.of("lamp", "embers", "candle"))
    );

    private final Random random = new Random();

    private final int rounds;
    private final double secondsPerRound;

    private final List<Verse> deck = new ArrayList<>(VERSES);

    private int round = 0;
    private int score = 0;
    private boolean acceptingInput = false;

    private Label statusLabel;
    private Label openingLabel;
    private Label closingLabel;
    private VBox optionsBox;
    private ProgressBar clock;
    private Timeline countdown;

    public ProsodyMiniGame(int difficulty) {
        super(difficulty);

        this.rounds = 2 + (this.difficulty + 1) / 2;
        this.secondsPerRound = Math.max(7, 20 - this.difficulty * 2.5);

        Collections.shuffle(deck, random);
    }

    @Override
    public String getTitle() {
        return "The Poet's Meter";
    }

    @Override
    public String getFlavourText() {
        return "The circle has gone round twice and stopped at you. Somebody has left out the last "
                + "word on purpose, and everyone in the room already knows which one it is.";
    }

    @Override
    public String getInstructions() {
        return "Choose the word that closes the couplet — it must rhyme, and it must scan.\n"
                + "Three of the four are the choice of a man who reads poetry and does not write it.\n"
                + "Get all " + rounds + " right. A single wrong word ends the recitation.";
    }

    @Override
    public Node buildView() {
        statusLabel = new Label("Couplet 1 of " + rounds);
        statusLabel.getStyleClass().add("minigame-status");

        openingLabel = new Label();
        openingLabel.getStyleClass().add("verse-line");
        openingLabel.setWrapText(true);
        openingLabel.setMaxWidth(600);
        openingLabel.setAlignment(Pos.CENTER);

        closingLabel = new Label();
        closingLabel.getStyleClass().add("verse-line");
        closingLabel.setWrapText(true);
        closingLabel.setMaxWidth(600);
        closingLabel.setAlignment(Pos.CENTER);

        clock = new ProgressBar(1.0);
        clock.setPrefWidth(340);
        clock.getStyleClass().add("minigame-timer");

        optionsBox = new VBox(9);
        optionsBox.setAlignment(Pos.CENTER);

        VBox root = new VBox(16, statusLabel, openingLabel, closingLabel, clock, optionsBox);
        root.setAlignment(Pos.CENTER);

        return root;
    }

    @Override
    public void start() {
        nextVerse();
    }

    @Override
    public void stop() {
        acceptingInput = false;

        if (countdown != null) {
            countdown.stop();
        }
    }

    private void nextVerse() {
        round++;

        if (round > rounds) {
            finish(MiniGameResult.won(
                    score,
                    "You closed every line without a pause, and the circle did the thing it does "
                            + "when it approves: it moved on immediately and said nothing at all."
            ));
            return;
        }

        Verse verse = deck.get((round - 1) % deck.size());

        statusLabel.setText("Couplet " + round + " of " + rounds);
        openingLabel.setText(verse.opening());
        closingLabel.setText(verse.closing());

        List<String> options = new ArrayList<>(verse.distractors());
        options.add(verse.answer());
        Collections.shuffle(options, random);

        optionsBox.getChildren().clear();

        for (String option : options) {
            Button choice = new Button(option);
            choice.getStyleClass().add("scroll-popup-button");
            choice.setPrefWidth(280);
            choice.setMinWidth(Region.USE_PREF_SIZE);
            choice.setFocusTraversable(false);
            choice.setOnAction(event -> answer(option.equals(verse.answer()), verse));

            optionsBox.getChildren().add(choice);
        }

        startCountdown();
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
                answer(false, null);
            }
        }));

        countdown.setCycleCount(Timeline.INDEFINITE);
        countdown.play();
    }

    private void answer(boolean correct, Verse verse) {
        if (!acceptingInput || isFinished()) {
            return;
        }

        acceptingInput = false;

        if (countdown != null) {
            countdown.stop();
        }

        if (!correct) {
            String shouldHaveBeen = verse == null
                    ? "You let the silence go on too long."
                    : "The word was \"" + verse.answer() + "\".";

            finish(MiniGameResult.lost(
                    score,
                    shouldHaveBeen + " Somebody further round the circle supplied it, kindly, "
                            + "and the evening went on without you."
            ));
            return;
        }

        score += 70 + round * 25;
        statusLabel.setText("It scans.");

        optionsBox.setDisable(true);

        Timeline pause = new Timeline(new KeyFrame(Duration.millis(520), event -> {
            optionsBox.setDisable(false);
            nextVerse();
        }));

        pause.play();
    }
}
