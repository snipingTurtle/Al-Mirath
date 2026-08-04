package com.example.al_mirath.minigame;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * The Merchant's Scales — settle a debt to the exact dirham before time runs out.
 *
 * <p>The player is given a target and a purse of coins, and must select a
 * subset that sums to it exactly. Every puzzle is generated from a known-good
 * solution first, then padded with distractor coins, so a solvable answer is
 * always present. Clearing a target refills the clock a little, which rewards
 * quick arithmetic without making a slow start unrecoverable.
 */
public class MerchantMiniGame extends AbstractMiniGame {

    private final Random random = new Random();
    private final List<Integer> coins = new ArrayList<>();
    private final List<Button> coinButtons = new ArrayList<>();
    private final List<Integer> selected = new ArrayList<>();

    private final int targetsToClear;
    private final double totalSeconds;

    private int target;
    private int cleared = 0;
    private int score = 0;
    private double secondsRemaining;

    private Label targetLabel;
    private Label currentSumLabel;
    private Label progressLabel;
    private ProgressBar timerBar;
    private FlowPane coinPane;
    private Timeline clock;

    public MerchantMiniGame(int difficulty) {
        super(difficulty);

        this.targetsToClear = 2 + (this.difficulty / 2);
        this.totalSeconds = Math.max(18, 40 - (this.difficulty * 4));
        this.secondsRemaining = this.totalSeconds;
    }

    @Override
    public String getTitle() {
        return "The Merchant's Scales";
    }

    @Override
    public String getFlavourText() {
        return "A creditor spreads your coins across a brass scale. \"Pay the sum exactly — "
                + "not one dirham over, not one under. Do that, and I will forget what was written.\"";
    }

    @Override
    public String getInstructions() {
        return "Click coins so their total matches the demanded sum exactly.\n"
                + "Click a selected coin again to put it back.\n"
                + "Settle " + targetsToClear + " debts before the sand runs out.";
    }

    @Override
    public Node buildView() {
        targetLabel = new Label();
        targetLabel.getStyleClass().add("minigame-target");

        currentSumLabel = new Label("Selected: 0");
        currentSumLabel.getStyleClass().add("minigame-status");

        progressLabel = new Label();
        progressLabel.getStyleClass().add("minigame-status");

        timerBar = new ProgressBar(1.0);
        timerBar.setPrefWidth(420);
        timerBar.getStyleClass().add("minigame-timer");

        coinPane = new FlowPane();
        coinPane.setHgap(12);
        coinPane.setVgap(12);
        coinPane.setAlignment(Pos.CENTER);
        coinPane.setPrefWrapLength(460);

        HBox header = new HBox(24, targetLabel, progressLabel);
        header.setAlignment(Pos.CENTER);

        VBox root = new VBox(18, header, coinPane, currentSumLabel, timerBar);
        root.setAlignment(Pos.CENTER);

        return root;
    }

    @Override
    public void start() {
        newTarget();
        startClock();
    }

    @Override
    public void stop() {
        if (clock != null) {
            clock.stop();
        }
    }

    private void startClock() {
        clock = new Timeline(new KeyFrame(Duration.millis(100), event -> {
            secondsRemaining -= 0.1;

            timerBar.setProgress(Math.max(0, secondsRemaining / totalSeconds));

            if (secondsRemaining <= 0) {
                finish(MiniGameResult.lost(
                        score,
                        "The sand runs out. The creditor sweeps the coins away, unpaid."
                ));
            }
        }));

        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();
    }

    /**
     * Builds a fresh purse. A random subset is chosen as the intended answer
     * and its sum becomes the target, which guarantees solvability; the rest of
     * the coins are noise.
     */
    private void newTarget() {
        coins.clear();
        selected.clear();

        int coinCount = 5 + difficulty;
        int solutionSize = 2 + random.nextInt(2);

        List<Integer> solution = new ArrayList<>();

        for (int i = 0; i < solutionSize; i++) {
            solution.add(3 + random.nextInt(18 + difficulty * 4));
        }

        target = solution.stream().mapToInt(Integer::intValue).sum();
        coins.addAll(solution);

        while (coins.size() < coinCount) {
            coins.add(3 + random.nextInt(18 + difficulty * 4));
        }

        Collections.shuffle(coins, random);

        renderCoins();
        updateLabels();
    }

    private void renderCoins() {
        coinPane.getChildren().clear();
        coinButtons.clear();

        for (int i = 0; i < coins.size(); i++) {
            Button coin = new Button(String.valueOf(coins.get(i)));
            coin.getStyleClass().add("coin-button");
            coin.setPrefSize(72, 72);
            coin.setFocusTraversable(false);

            final int index = i;
            coin.setOnAction(event -> toggleCoin(index));

            coinButtons.add(coin);
            coinPane.getChildren().add(coin);
        }
    }

    private void toggleCoin(int index) {
        if (isFinished()) {
            return;
        }

        Button coin = coinButtons.get(index);

        if (selected.contains(index)) {
            selected.remove(Integer.valueOf(index));
            coin.getStyleClass().remove("coin-selected");
        } else {
            selected.add(index);

            if (!coin.getStyleClass().contains("coin-selected")) {
                coin.getStyleClass().add("coin-selected");
            }
        }

        updateLabels();
        checkSum();
    }

    private void checkSum() {
        int sum = currentSum();

        if (sum == target) {
            cleared++;
            score += 50 + (int) (secondsRemaining * 2);

            if (cleared >= targetsToClear) {
                finish(MiniGameResult.won(
                        score,
                        "Every debt settled to the dirham. The creditor tears up the page."
                ));
                return;
            }

            // A small time refund keeps a strong run rolling.
            secondsRemaining = Math.min(totalSeconds, secondsRemaining + 6);

            flashSuccess();
            newTarget();

        } else if (sum > target) {
            // Overpaying is a dead end: force the player to rebuild the pile.
            clearSelection();
        }
    }

    private void clearSelection() {
        for (int index : selected) {
            coinButtons.get(index).getStyleClass().remove("coin-selected");
        }

        selected.clear();
        updateLabels();
    }

    private void flashSuccess() {
        Timeline flash = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(coinPane.opacityProperty(), 1.0)),
                new KeyFrame(Duration.millis(120), new KeyValue(coinPane.opacityProperty(), 0.35)),
                new KeyFrame(Duration.millis(280), new KeyValue(coinPane.opacityProperty(), 1.0))
        );

        flash.play();
    }

    private int currentSum() {
        int sum = 0;

        for (int index : selected) {
            sum += coins.get(index);
        }

        return sum;
    }

    private void updateLabels() {
        targetLabel.setText("Owed: " + target);
        currentSumLabel.setText("Selected: " + currentSum());
        progressLabel.setText("Debts settled: " + cleared + " / " + targetsToClear);
    }
}
