package com.example.al_mirath.controller;

import com.example.al_mirath.Main;
import com.example.al_mirath.model.Achievement;
import com.example.al_mirath.service.AchievementLibrary;
import com.example.al_mirath.service.ProgressService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

/**
 * Hall of Legacy: lifetime statistics and the achievement catalogue.
 *
 * <p>Locked entries stay visible so the player can see what is left to chase.
 * Achievements flagged hidden keep their hint masked until earned, so the
 * spoiler ones do not give away endings.
 */
public class AchievementsController {

    @FXML private Label progressLabel;
    @FXML private Label percentLabel;
    @FXML private ProgressBar completionBar;
    @FXML private FlowPane statisticsPane;
    @FXML private FlowPane achievementPane;
    @FXML private ScrollPane achievementScroll;

    private Main mainApp;

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void initialize() {
        buildSummary();
        buildStatistics();
        buildAchievementCards();
    }

    private void buildSummary() {
        int unlocked = ProgressService.unlockedCount();
        int total = AchievementLibrary.total();

        progressLabel.setText(unlocked + " of " + total + " unlocked");

        double ratio = total == 0 ? 0 : (double) unlocked / total;

        completionBar.setProgress(ratio);
        percentLabel.setText(Math.round(ratio * 100) + "%");
    }

    private void buildStatistics() {
        statisticsPane.getChildren().clear();

        addStatistic("Lives completed", ProgressService.statistic(ProgressService.STAT_LIVES_COMPLETED));
        addStatistic("Choices made", ProgressService.statistic(ProgressService.STAT_CHOICES_MADE));
        addStatistic("Titles earned", ProgressService.statistic(ProgressService.STAT_TITLES_EARNED));
        addStatistic("Threads won", ProgressService.statistic(ProgressService.STAT_REWINDS_WON));
        addStatistic("Threads lost", ProgressService.statistic(ProgressService.STAT_REWINDS_LOST));
        addStatistic("Oldest age reached", ProgressService.statistic(ProgressService.STAT_OLDEST_AGE));
        addStatistic("Best score", ProgressService.statistic(ProgressService.STAT_HIGHEST_SCORE));
        addStatistic("Eras completed", ProgressService.erasCompleted() + " / 4");
    }

    private void addStatistic(String name, Object value) {
        Label label = new Label(name + ": " + value);
        label.getStyleClass().add("statistic-chip");

        statisticsPane.getChildren().add(label);
    }

    private void buildAchievementCards() {
        achievementPane.getChildren().clear();

        for (Achievement achievement : AchievementLibrary.all()) {
            achievementPane.getChildren().add(buildCard(achievement));
        }
    }

    private VBox buildCard(Achievement achievement) {
        boolean unlocked = ProgressService.isUnlocked(achievement.id());

        // A hidden achievement reveals neither its name nor its hint until earned.
        boolean concealed = achievement.hidden() && !unlocked;

        Label name = new Label(concealed ? "???" : achievement.name());
        name.getStyleClass().add("achievement-name");
        name.setWrapText(true);

        Label hint = new Label(concealed
                ? "A secret still waiting to be uncovered."
                : achievement.hint());
        hint.getStyleClass().add("achievement-hint");
        hint.setWrapText(true);

        Label state = new Label(unlocked ? "UNLOCKED" : "LOCKED");
        state.getStyleClass().add(unlocked ? "achievement-state-unlocked" : "achievement-state-locked");

        VBox card = new VBox(6, state, name, hint);
        card.getStyleClass().addAll("achievement-card",
                unlocked ? "achievement-card-unlocked" : "achievement-card-locked");
        card.setPrefWidth(360);
        card.setMinHeight(126);

        return card;
    }

    @FXML
    private void returnToMainMenu() {
        if (mainApp != null) {
            mainApp.showWelcomeScreen();
        }
    }
}
