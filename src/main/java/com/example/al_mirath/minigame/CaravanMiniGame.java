package com.example.al_mirath.minigame;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The Caravan Road — cross the waste on the water you can carry.
 *
 * <p>Oases are laid out in stages between the city and the destination, and
 * every leg between them costs water. The skins hold barely more than the
 * best road needs, so the obvious leg is usually the wrong one: the cheapest
 * next hop repeatedly leads into an expensive corner.
 *
 * <p>Everything is visible from the start, which is what makes it a decision
 * rather than a gamble. The player loses by being greedy one stage at a time.
 */
public class CaravanMiniGame extends AbstractMiniGame {

    private static final double NODE_RADIUS = 20;
    private static final double COLUMN_GAP = 150;
    private static final double ROW_GAP = 94;
    private static final double MARGIN = 46;

    private final Random random = new Random();

    /** Oases per intermediate stage. */
    private static final int ROWS = 3;

    /** Intermediate stages between the city and the destination. */
    private final int stages;

    /** cost[stage][fromRow][toRow], with the city as stage 0. */
    private int[][][] cost;

    private final int waterBudget;
    private int waterLeft;

    private int currentStage = 0;
    private int currentRow = 0;

    private Pane map;
    private Label statusLabel;
    private Label waterLabel;
    private final List<Circle> nodes = new ArrayList<>();
    private Circle caravan;

    public CaravanMiniGame(int difficulty) {
        super(difficulty);

        this.stages = 2 + Math.min(2, this.difficulty / 2);

        buildRoads();

        // Slack shrinks with difficulty, so a hard road demands the true best
        // route and an easy one forgives a single bad leg.
        int slack = Math.max(0, 7 - this.difficulty);
        this.waterBudget = cheapestCrossing() + slack;
        this.waterLeft = waterBudget;
    }

    /**
     * Lays out the legs. The first and last stages fan out from and into a
     * single point — the city you leave and the place you are going.
     */
    private void buildRoads() {
        cost = new int[stages + 1][][];

        for (int stage = 0; stage <= stages; stage++) {
            int fromCount = stage == 0 ? 1 : ROWS;
            int toCount = stage == stages ? 1 : ROWS;

            cost[stage] = new int[fromCount][toCount];

            for (int from = 0; from < fromCount; from++) {
                for (int to = 0; to < toCount; to++) {
                    cost[stage][from][to] = 3 + random.nextInt(9);
                }
            }
        }
    }

    /** Exact cost of the best road, by working backwards from the destination. */
    private int cheapestCrossing() {
        int[] best = {0};

        for (int stage = stages; stage >= 0; stage--) {
            int fromCount = cost[stage].length;
            int[] next = new int[fromCount];

            for (int from = 0; from < fromCount; from++) {
                int cheapest = Integer.MAX_VALUE;

                for (int to = 0; to < cost[stage][from].length; to++) {
                    cheapest = Math.min(cheapest, cost[stage][from][to] + best[to]);
                }

                next[from] = cheapest;
            }

            best = next;
        }

        return best[0];
    }

    @Override
    public String getTitle() {
        return "The Caravan Road";
    }

    @Override
    public String getFlavourText() {
        return "The skins are filled, the camels are loaded, and the road out of the gate forks "
                + "within an hour. \"Choose badly early,\" the guide says, \"and you will be "
                + "choosing between bad things for the rest of it.\"";
    }

    @Override
    public String getInstructions() {
        return "Click the next oasis to travel to it. The number on each leg is the water it costs.\n"
                + "You are carrying " + waterBudget + " skins, and the road can be crossed on that — "
                + "but only by the best route.\n"
                + "Run dry before the far side and the caravan does not arrive.";
    }

    @Override
    public Node buildView() {
        statusLabel = new Label("Out of the gate. Choose your first oasis.");
        statusLabel.getStyleClass().add("minigame-status");

        waterLabel = new Label(waterText());
        waterLabel.getStyleClass().add("minigame-target");

        map = new Pane();

        double width = MARGIN * 2 + COLUMN_GAP * (stages + 1);
        double height = MARGIN * 2 + ROW_GAP * (ROWS - 1);

        map.setPrefSize(width, height);
        map.setMinSize(width, height);
        map.setMaxSize(width, height);

        drawLegs();
        drawOases();

        caravan = new Circle(9);
        caravan.setFill(Color.web("#ffe9a8"));
        caravan.setStroke(Color.web("#3a2a10"));
        caravan.setStrokeWidth(2);
        caravan.setMouseTransparent(true);
        caravan.setCenterX(xFor(0));
        caravan.setCenterY(yFor(0, 0));

        map.getChildren().add(caravan);

        StackPane mapHolder = new StackPane(map);
        mapHolder.setAlignment(Pos.CENTER);

        VBox root = new VBox(16, statusLabel, mapHolder, waterLabel);
        root.setAlignment(Pos.CENTER);

        return root;
    }

    private void drawLegs() {
        for (int stage = 0; stage <= stages; stage++) {
            int fromCount = cost[stage].length;

            for (int from = 0; from < fromCount; from++) {
                for (int to = 0; to < cost[stage][from].length; to++) {
                    double x1 = xFor(stage);
                    double y1 = yFor(stage, from);
                    double x2 = xFor(stage + 1);
                    double y2 = yFor(stage + 1, to);

                    Line leg = new Line(x1, y1, x2, y2);
                    leg.setStroke(Color.web("#7a5f2c"));
                    leg.setStrokeWidth(1.6);
                    leg.setMouseTransparent(true);

                    map.getChildren().add(leg);

                    Label price = new Label(String.valueOf(cost[stage][from][to]));
                    price.getStyleClass().add("caravan-leg-cost");
                    price.setMouseTransparent(true);

                    // Nudged off the midpoint so overlapping legs stay readable.
                    double slide = 0.38 + (to * 0.10);
                    price.setLayoutX(x1 + (x2 - x1) * slide - 7);
                    price.setLayoutY(y1 + (y2 - y1) * slide - 10);

                    map.getChildren().add(price);
                }
            }
        }
    }

    private void drawOases() {
        for (int stage = 0; stage <= stages + 1; stage++) {
            int count = countAt(stage);

            for (int row = 0; row < count; row++) {
                Circle oasis = new Circle(xFor(stage), yFor(stage, row), NODE_RADIUS);
                oasis.getStyleClass().add("caravan-oasis");
                oasis.setFill(Color.web("#3d2f16"));
                oasis.setStroke(Color.web("#e0b755"));
                oasis.setStrokeWidth(2);

                final int targetStage = stage;
                final int targetRow = row;

                oasis.setOnMouseClicked(event -> travelTo(targetStage, targetRow));

                nodes.add(oasis);
                map.getChildren().add(oasis);

                if (stage == 0 || stage == stages + 1) {
                    Label caption = new Label(stage == 0 ? "City" : "Journey's end");
                    caption.getStyleClass().add("caravan-caption");
                    caption.setMouseTransparent(true);
                    caption.setLayoutX(xFor(stage) - 34);
                    caption.setLayoutY(yFor(stage, row) + NODE_RADIUS + 6);

                    map.getChildren().add(caption);
                }
            }
        }
    }

    private int countAt(int stage) {
        return (stage == 0 || stage == stages + 1) ? 1 : ROWS;
    }

    private double xFor(int stage) {
        return MARGIN + stage * COLUMN_GAP;
    }

    private double yFor(int stage, int row) {
        int count = countAt(stage);

        if (count == 1) {
            return MARGIN + ROW_GAP * (ROWS - 1) / 2.0;
        }

        return MARGIN + row * ROW_GAP;
    }

    @Override
    public void start() {
        // The clock is water, not time.
    }

    @Override
    public void stop() {
        // Nothing to unwind.
    }

    private void travelTo(int stage, int row) {
        if (isFinished() || stage != currentStage + 1) {
            return;
        }

        int leg = cost[currentStage][currentRow][row];

        waterLeft -= leg;
        currentStage = stage;
        currentRow = row;

        caravan.setCenterX(xFor(stage));
        caravan.setCenterY(yFor(stage, row));

        waterLabel.setText(waterText());

        if (waterLeft < 0) {
            statusLabel.setText("The skins are empty and the next well is a day away.");

            Timeline settle = new Timeline(new KeyFrame(Duration.millis(800), event ->
                    finish(MiniGameResult.lost(
                            Math.max(0, waterBudget + waterLeft) * 10,
                            "You ran dry " + (stages + 1 - stage) + " stages short. The caravan turned "
                                    + "back for the last well, and what it was carrying went with it."
                    ))
            ));

            settle.play();
            return;
        }

        if (stage == stages + 1) {
            statusLabel.setText("The walls come up out of the haze.");

            Timeline settle = new Timeline(new KeyFrame(Duration.millis(800), event ->
                    finish(MiniGameResult.won(
                            200 + waterLeft * 40,
                            "You came in with " + waterLeft + " skins still full. The guide, who has "
                                    + "crossed this forty times, asks how you chose the second leg."
                    ))
            ));

            settle.play();
            return;
        }

        statusLabel.setText("Stage " + stage + " of " + (stages + 1) + ". Choose the next oasis.");
    }

    private String waterText() {
        return "Water: " + Math.max(0, waterLeft) + " of " + waterBudget + " skins";
    }
}
