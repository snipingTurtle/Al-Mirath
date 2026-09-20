package com.example.al_mirath.minigame;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;

import java.util.Random;

/**
 * The Bargain — find a seller's floor without insulting him off it.
 *
 * <p>He has a price he will not go under and will not say what it is. Every
 * refusal tells you something and costs you some of his patience, and an
 * offer far below his floor costs a great deal of it. Win by closing inside
 * your own budget before either his patience or your offers run out.
 *
 * <p>It is a search problem wearing a robe: the honest way to play is to
 * halve the range each time, which is also how haggling actually works.
 */
public class HagglingMiniGame extends AbstractMiniGame {

    private static final int ASKING_PRICE = 400;

    private final Random random = new Random();

    /** The lowest he will take. Never shown. */
    private final int floorPrice;

    /** The most the player may pay and still be said to have won. */
    private final int budget;

    private final int maxOffers;

    private int offersLeft;
    private int patience = 100;
    private int offer;

    private Label statusLabel;
    private Label offerLabel;
    private Label rangeLabel;
    private ProgressBar patienceBar;
    private Button offerButton;
    private HBox adjustRow;

    public HagglingMiniGame(int difficulty) {
        super(difficulty);

        // A tighter budget and fewer offers as the stakes rise. The floor is
        // always somewhere in the middle third, so the opening guess is never
        // free information.
        this.floorPrice = 120 + random.nextInt(160);
        this.budget = floorPrice + Math.max(20, 100 - this.difficulty * 14);
        this.maxOffers = Math.max(5, 10 - this.difficulty);
        this.offersLeft = maxOffers;

        // Opens low and climbs, the way a bargain actually runs. Opening at
        // half the asking price would let an unlucky first guess close the
        // deal above budget before the player had learned anything.
        this.offer = 60;
    }

    @Override
    public String getTitle() {
        return "The Bargain";
    }

    @Override
    public String getFlavourText() {
        return "He names four hundred without looking up from the cloth. Everyone in the row "
                + "knows it is not four hundred. Finding out what it is, without making an enemy, "
                + "is the whole of the trade.";
    }

    @Override
    public String getInstructions() {
        return "He is asking " + ASKING_PRICE + " dirhams. You may pay at most " + budget + ".\n"
                + "Raise or lower your offer and put it to him. He will refuse, and how he refuses "
                + "tells you how close you are.\n"
                + "You have " + maxOffers + " offers. Insult him badly enough and he walks away.";
    }

    @Override
    public Node buildView() {
        statusLabel = new Label("He waits.");
        statusLabel.getStyleClass().add("minigame-status");

        offerLabel = new Label(offer + " dirhams");
        offerLabel.getStyleClass().add("minigame-target");

        rangeLabel = new Label(summaryLine());
        rangeLabel.getStyleClass().add("minigame-instructions");

        patienceBar = new ProgressBar(1.0);
        patienceBar.setPrefWidth(320);
        patienceBar.getStyleClass().add("minigame-timer");

        Label patienceCaption = new Label("His patience");
        patienceCaption.getStyleClass().add("minigame-instructions");

        adjustRow = new HBox(10,
                adjustButton("−50", -50),
                adjustButton("−10", -10),
                adjustButton("−1", -1),
                adjustButton("+1", 1),
                adjustButton("+10", 10),
                adjustButton("+50", 50)
        );
        adjustRow.setAlignment(Pos.CENTER);

        offerButton = new Button("Put it to him");
        offerButton.getStyleClass().add("scroll-popup-button");
        offerButton.setPrefWidth(220);
        offerButton.setMinWidth(Region.USE_PREF_SIZE);
        offerButton.setOnAction(event -> makeOffer());

        VBox root = new VBox(16,
                statusLabel,
                offerLabel,
                adjustRow,
                offerButton,
                patienceCaption,
                patienceBar,
                rangeLabel
        );

        root.setAlignment(Pos.CENTER);

        return root;
    }

    private Button adjustButton(String text, int delta) {
        Button button = new Button(text);
        button.getStyleClass().add("coin-button");
        button.setMinWidth(Region.USE_PREF_SIZE);
        button.setPrefWidth(66);
        button.setFocusTraversable(false);
        button.setOnAction(event -> adjust(delta));
        return button;
    }

    @Override
    public void start() {
        // Nothing runs on a clock here; the pressure is his patience, not time.
    }

    @Override
    public void stop() {
        // Nothing to unwind.
    }

    private void adjust(int delta) {
        if (isFinished()) {
            return;
        }

        offer = Math.max(1, Math.min(ASKING_PRICE, offer + delta));
        offerLabel.setText(offer + " dirhams");
    }

    private void makeOffer() {
        if (isFinished()) {
            return;
        }

        offersLeft--;

        if (offer >= floorPrice) {
            closeTheDeal();
            return;
        }

        // He refuses. How far under he was decides how much it cost you.
        int shortfall = floorPrice - offer;
        double proportion = (double) shortfall / floorPrice;

        int patienceCost = (int) Math.round(8 + proportion * 70);
        patience = Math.max(0, patience - patienceCost);

        patienceBar.setProgress(patience / 100.0);
        statusLabel.setText(refusal(proportion));
        rangeLabel.setText(summaryLine());

        if (patience <= 0) {
            walksAway();
            return;
        }

        if (offersLeft <= 0) {
            outOfOffers();
        }
    }

    private void closeTheDeal() {
        setControlsEnabled(false);

        int overpaid = offer - floorPrice;

        if (offer > budget) {
            statusLabel.setText("Taken — and taken quickly.");

            Timeline settle = new Timeline(new KeyFrame(Duration.millis(700), event ->
                    finish(MiniGameResult.lost(
                            Math.max(0, 200 - overpaid),
                            "He agreed before you had finished speaking, which tells you what the "
                                    + "price was really worth. You paid " + offer + " where "
                                    + budget + " was the most the deal could bear."
                    ))
            ));

            settle.play();
            return;
        }

        statusLabel.setText("Agreed at " + offer + ".");

        int score = Math.max(20, 400 - overpaid * 3 + offersLeft * 25);

        Timeline settle = new Timeline(new KeyFrame(Duration.millis(700), event ->
                finish(MiniGameResult.won(
                        score,
                        "He spits on his palm and takes yours. You paid " + offer
                                + " for something he would not have let go under " + floorPrice
                                + ", and he still thinks he did well out of you."
                ))
        ));

        settle.play();
    }

    private void walksAway() {
        setControlsEnabled(false);
        statusLabel.setText("He folds the cloth away.");

        Timeline settle = new Timeline(new KeyFrame(Duration.millis(700), event ->
                finish(MiniGameResult.lost(
                        offersLeft * 10,
                        "\"Go and insult someone else's morning.\" He turns to the next customer "
                                + "and does not look at you again."
                ))
        ));

        settle.play();
    }

    private void outOfOffers() {
        setControlsEnabled(false);
        statusLabel.setText("The light has gone.");

        Timeline settle = new Timeline(new KeyFrame(Duration.millis(700), event ->
                finish(MiniGameResult.lost(
                        Math.max(0, patience),
                        "The row is shutting up around you and you are still talking. "
                                + "He was never going under " + floorPrice + ", and you never found it."
                ))
        ));

        settle.play();
    }

    /**
     * How he refuses, which is the only information the player gets.
     * Deliberately coarse: precise feedback would turn this into arithmetic.
     */
    private String refusal(double proportion) {
        if (proportion > 0.45) {
            return "\"Are we talking about the same cloth?\" He is genuinely offended.";
        }

        if (proportion > 0.25) {
            return "\"No.\" He does not elaborate. You are a long way off.";
        }

        if (proportion > 0.12) {
            return "\"That is not serious.\" But he has not put it away.";
        }

        if (proportion > 0.05) {
            return "He makes a face, and lets the silence go on. You are close.";
        }

        return "He almost says yes. You are a hair under it.";
    }

    private String summaryLine() {
        return "Offers left: " + offersLeft + "   ·   Your ceiling: " + budget + " dirhams";
    }

    private void setControlsEnabled(boolean enabled) {
        offerButton.setDisable(!enabled);
        adjustRow.setDisable(!enabled);
    }
}
