package com.example.al_mirath.minigame;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * The Physician's Draught — compound a remedy from the humoral system.
 *
 * <p>The patient presents an imbalance, and the player must select the herbs
 * whose qualities correct it: a hot-and-dry fever wants cold-and-moist
 * ingredients. Each herb carries a heat and a moisture value, and the sum of
 * the chosen herbs has to land within tolerance on both axes at once — which
 * makes it a two-dimensional balancing act rather than a single sum.
 */
public class PhysicianMiniGame extends AbstractMiniGame {

    /** An ingredient and its effect on the two humoral axes. */
    private record Herb(String name, int heat, int moisture) {
    }

    private static final List<Herb> PHARMACOPOEIA = List.of(
            new Herb("Camphor", -3, 0),
            new Herb("Rose", -2, 1),
            new Herb("Cucumber", -2, 3),
            new Herb("Lettuce", -1, 2),
            new Herb("Barley", -1, 1),
            new Herb("Violet", -2, 2),
            new Herb("Pepper", 3, -2),
            new Herb("Ginger", 3, -1),
            new Herb("Cinnamon", 2, -1),
            new Herb("Honey", 1, 1),
            new Herb("Mint", 1, -1),
            new Herb("Saffron", 2, 0)
    );

    private final Random random = new Random();
    private final List<Herb> available = new ArrayList<>();
    private final List<Integer> selected = new ArrayList<>();
    private final List<Button> herbButtons = new ArrayList<>();

    private final int patientsToTreat;
    private final int tolerance;
    private final double totalSeconds;

    private int targetHeat;
    private int targetMoisture;
    private int treated = 0;
    private int score = 0;
    private double secondsLeft;

    private Label diagnosisLabel;
    private Label mixtureLabel;
    private Label progressLabel;
    private FlowPane herbPane;
    private Timeline clock;

    public PhysicianMiniGame(int difficulty) {
        super(difficulty);

        this.patientsToTreat = 1 + (this.difficulty / 2);
        // Tighter tolerance at higher difficulty: exact compounding required.
        this.tolerance = Math.max(0, 2 - (this.difficulty / 2));
        this.totalSeconds = Math.max(25, 55 - (this.difficulty * 5));
        this.secondsLeft = this.totalSeconds;
    }

    @Override
    public String getTitle() {
        return "The Physician's Draught";
    }

    @Override
    public String getFlavourText() {
        return "The patient is burning and dry, or cold and sodden, and the remedy must "
                + "pull them back toward balance. Too little does nothing. Too much is a "
                + "different illness wearing the cure's name.";
    }

    @Override
    public String getInstructions() {
        return "Each herb shifts the patient's Heat and Moisture. Choose herbs whose "
                + "combined effect matches what the diagnosis calls for, on both axes.\n"
                + "Click a chosen herb again to return it to the shelf.\n"
                + "Treat " + patientsToTreat + " patient(s) before the water clock empties."
                + (tolerance > 0 ? "\nA margin of " + tolerance + " on each axis is acceptable."
                : "\nThe compounding must be exact.");
    }

    @Override
    public Node buildView() {
        diagnosisLabel = new Label();
        diagnosisLabel.getStyleClass().add("minigame-target");
        diagnosisLabel.setWrapText(true);
        diagnosisLabel.setMaxWidth(580);

        mixtureLabel = new Label();
        mixtureLabel.getStyleClass().add("minigame-status");

        progressLabel = new Label();
        progressLabel.getStyleClass().add("minigame-status");

        herbPane = new FlowPane();
        herbPane.setHgap(10);
        herbPane.setVgap(10);
        herbPane.setAlignment(Pos.CENTER);
        herbPane.setPrefWrapLength(600);

        VBox root = new VBox(16, progressLabel, diagnosisLabel, herbPane, mixtureLabel);
        root.setAlignment(Pos.CENTER);

        return root;
    }

    @Override
    public void start() {
        newPatient();
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
            secondsLeft -= 0.1;

            if (secondsLeft <= 0) {
                finish(MiniGameResult.lost(
                        score,
                        "The water clock empties. The draught is never finished, and the "
                                + "patient is carried out past you."
                ));
            }
        }));

        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();
    }

    /**
     * Builds a solvable case: a random subset is chosen as the intended remedy
     * and its combined effect becomes the target, so a valid answer always
     * exists among the herbs on the shelf.
     */
    private void newPatient() {
        available.clear();
        selected.clear();

        List<Herb> shelf = new ArrayList<>(PHARMACOPOEIA);
        Collections.shuffle(shelf, random);

        int remedySize = 2 + random.nextInt(2);
        List<Herb> remedy = shelf.subList(0, remedySize);

        targetHeat = remedy.stream().mapToInt(Herb::heat).sum();
        targetMoisture = remedy.stream().mapToInt(Herb::moisture).sum();

        int shelfSize = Math.min(PHARMACOPOEIA.size(), 6 + difficulty);
        available.addAll(shelf.subList(0, shelfSize));
        Collections.shuffle(available, random);

        renderHerbs();
        updateLabels();
    }

    private void renderHerbs() {
        herbPane.getChildren().clear();
        herbButtons.clear();

        for (int i = 0; i < available.size(); i++) {
            Herb herb = available.get(i);

            Button button = new Button(
                    herb.name() + "\n"
                            + formatSigned(herb.heat()) + " heat  "
                            + formatSigned(herb.moisture()) + " moist"
            );
            button.getStyleClass().add("herb-button");
            button.setPrefSize(148, 62);
            button.setFocusTraversable(false);

            final int index = i;
            button.setOnAction(event -> toggleHerb(index));

            herbButtons.add(button);
            herbPane.getChildren().add(button);
        }
    }

    private void toggleHerb(int index) {
        if (isFinished()) {
            return;
        }

        Button button = herbButtons.get(index);

        if (selected.contains(index)) {
            selected.remove(Integer.valueOf(index));
            button.getStyleClass().remove("herb-selected");
        } else {
            selected.add(index);

            if (!button.getStyleClass().contains("herb-selected")) {
                button.getStyleClass().add("herb-selected");
            }
        }

        updateLabels();
        checkMixture();
    }

    private void checkMixture() {
        if (selected.isEmpty()) {
            return;
        }

        int heat = currentHeat();
        int moisture = currentMoisture();

        if (Math.abs(heat - targetHeat) <= tolerance
                && Math.abs(moisture - targetMoisture) <= tolerance) {

            treated++;
            score += 60 + (int) (secondsLeft * 3);

            if (treated >= patientsToTreat) {
                finish(MiniGameResult.won(
                        score,
                        "The draught is right. The fever breaks before morning, and the "
                                + "household tells the story for years."
                ));
                return;
            }

            newPatient();
        }
    }

    private int currentHeat() {
        return selected.stream().mapToInt(i -> available.get(i).heat()).sum();
    }

    private int currentMoisture() {
        return selected.stream().mapToInt(i -> available.get(i).moisture()).sum();
    }

    private void updateLabels() {
        diagnosisLabel.setText(
                "The remedy must total "
                        + formatSigned(targetHeat) + " heat and "
                        + formatSigned(targetMoisture) + " moisture"
        );

        mixtureLabel.setText(
                "Your mixture: " + formatSigned(currentHeat()) + " heat, "
                        + formatSigned(currentMoisture()) + " moisture"
        );

        progressLabel.setText("Patients treated: " + treated + " / " + patientsToTreat);
    }

    private String formatSigned(int value) {
        return value > 0 ? "+" + value : String.valueOf(value);
    }
}
