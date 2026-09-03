package com.example.al_mirath.controller;

import com.example.al_mirath.Main;
import com.example.al_mirath.model.LegacyRecord;
import com.example.al_mirath.service.BackgroundLibrary;
import com.example.al_mirath.service.LegacyArchive;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

import java.util.List;

/**
 * Legacy Records: every completed life, newest first.
 *
 * <p>Each record is built as a card rather than one block of text so the
 * name, ending and score can carry the same gold-leaf treatment the rest of
 * the game uses, with the supporting details reading as parchment beneath.
 */
public class LegacyRecordsController {

    @FXML private StackPane recordsRoot;
    @FXML private ImageView recordsBackground;
    @FXML private Rectangle recordsOverlay;

    @FXML private VBox summaryPanel;
    @FXML private Label archiveCountLabel;
    @FXML private Label bestScoreLabel;
    @FXML private FlowPane summaryPane;

    @FXML private VBox recordsBox;
    @FXML private ScrollPane recordsScroll;
    @FXML private VBox emptyPanel;

    private Main mainApp;

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void initialize() {
        bindBackground();
        loadBackground();
        loadRecords();
    }

    private void bindBackground() {
        if (recordsRoot != null && recordsBackground != null) {
            recordsBackground.fitWidthProperty().bind(recordsRoot.widthProperty());
            recordsBackground.fitHeightProperty().bind(recordsRoot.heightProperty());
        }

        if (recordsRoot != null && recordsOverlay != null) {
            recordsOverlay.widthProperty().bind(recordsRoot.widthProperty());
            recordsOverlay.heightProperty().bind(recordsRoot.heightProperty());
        }
    }

    private void loadBackground() {
        if (recordsBackground == null) {
            return;
        }

        try {
            String path = BackgroundLibrary.getMenuBackground();

            if (path == null || path.isBlank()) {
                return;
            }

            recordsBackground.setImage(
                    new Image(getClass().getResource(path).toExternalForm(), true)
            );

        } catch (Exception e) {
            System.out.println("Failed to load legacy records background.");
        }
    }

    private void loadRecords() {
        recordsBox.getChildren().clear();
        summaryPane.getChildren().clear();

        List<LegacyRecord> records = LegacyArchive.getRecords();

        boolean empty = records.isEmpty();

        show(emptyPanel, empty);
        show(summaryPanel, !empty);
        show(recordsScroll, !empty);

        if (empty) {
            return;
        }

        buildSummary(records);

        int bestScore = bestScore(records);
        boolean crownAwarded = false;

        for (LegacyRecord record : records) {
            // Only the first record holding the top score is crowned, so a
            // tie does not light up half the archive.
            boolean crowned = !crownAwarded && record.getScore() == bestScore;
            crownAwarded |= crowned;

            recordsBox.getChildren().add(buildCard(record, crowned));
        }
    }

    private void buildSummary(List<LegacyRecord> records) {
        archiveCountLabel.setText(
                records.size() + (records.size() == 1 ? " life recorded" : " lives recorded")
        );

        bestScoreLabel.setText("Best " + bestScore(records));

        addSummaryChip("Longest life", longestLife(records) + " years");
        addSummaryChip("Average score", averageScore(records));
        addSummaryChip("Most recent", records.get(0).getCharacterName());
    }

    private void addSummaryChip(String name, Object value) {
        Label chip = new Label(name + ": " + value);
        chip.getStyleClass().add("statistic-chip");

        summaryPane.getChildren().add(chip);
    }

    private VBox buildCard(LegacyRecord record, boolean crowned) {
        Label name = new Label(record.getCharacterName());
        name.getStyleClass().add("record-name");
        name.setWrapText(true);

        Label score = new Label(record.getScore() + " pts");
        score.getStyleClass().add("record-score");

        HBox heading = new HBox(14, name, spacer(), score);

        Label ending = new Label(record.getEndingTitle());
        ending.getStyleClass().add("record-ending");
        ending.setWrapText(true);

        FlowPane facts = new FlowPane(10, 8);
        facts.getStyleClass().add("record-facts");
        addFact(facts, record.getEra());
        addFact(facts, record.getOrigin());
        addFact(facts, record.getFamilyCondition());
        addFact(facts, "Died at " + record.getAgeAtEnd());
        addFact(facts, record.getFinalStatus());

        Label titles = new Label("Titles: " + titlesOf(record));
        titles.getStyleClass().add("record-titles");
        titles.setWrapText(true);

        VBox card = new VBox(8, heading, ending, facts, titles);
        card.getStyleClass().addAll(
                "legacy-record-card",
                crowned ? "legacy-record-card-crowned" : "legacy-record-card-plain"
        );

        if (crowned) {
            Label crown = new Label("FINEST LEGACY");
            crown.getStyleClass().add("record-crown");

            card.getChildren().add(0, crown);
        }

        return card;
    }

    private void addFact(FlowPane facts, String value) {
        if (value == null || value.isBlank()) {
            return;
        }

        Label fact = new Label(value);
        fact.getStyleClass().add("record-fact");

        facts.getChildren().add(fact);
    }

    private String titlesOf(LegacyRecord record) {
        String titles = record.getLegacyTitles();

        return titles == null || titles.isBlank() ? "None earned" : titles;
    }

    private Region spacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        return spacer;
    }

    private void show(javafx.scene.Node node, boolean visible) {
        if (node == null) {
            return;
        }

        node.setVisible(visible);
        node.setManaged(visible);
    }

    private int bestScore(List<LegacyRecord> records) {
        return records.stream()
                .mapToInt(LegacyRecord::getScore)
                .max()
                .orElse(0);
    }

    private int longestLife(List<LegacyRecord> records) {
        return records.stream()
                .mapToInt(LegacyRecord::getAgeAtEnd)
                .max()
                .orElse(0);
    }

    private long averageScore(List<LegacyRecord> records) {
        return Math.round(
                records.stream()
                        .mapToInt(LegacyRecord::getScore)
                        .average()
                        .orElse(0)
        );
    }

    @FXML
    private void returnToMainMenu() {
        if (mainApp != null) {
            mainApp.showWelcomeScreen();
        }
    }

    @FXML
    private void clearRecords() {
        LegacyArchive.clearRecords();
        loadRecords();
    }
}
