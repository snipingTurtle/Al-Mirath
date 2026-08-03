package com.example.al_mirath.controller;

import com.example.al_mirath.Main;
import com.example.al_mirath.model.Choice;
import com.example.al_mirath.model.EndingResult;
import com.example.al_mirath.model.FactionRelations;
import com.example.al_mirath.model.GameEvent;
import com.example.al_mirath.model.LegacyRecord;
import com.example.al_mirath.model.PlayerCharacter;
import com.example.al_mirath.service.BackgroundLibrary;
import com.example.al_mirath.service.GameEngine;
import com.example.al_mirath.service.LegacyArchive;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import javafx.application.Platform;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

public class GameController {

    private GameEngine engine;
    private Main mainApp;

    private String pendingLegacyTitleMessage = "";
    private String pendingStatusChangeMessage = "";

    private boolean legacyRecorded = false;
    private boolean birthIntroShown = false;
    private boolean backgroundMotionStarted = false;
    private boolean finalChronicleShown = false;

    private boolean characterDrawerOpen = false;
    private boolean factionDrawerOpen = false;

    private static final double CHARACTER_DRAWER_CLOSED_X = -305;
    private static final double FACTION_DRAWER_CLOSED_X = 305;

    private static final double CHARACTER_HANDLE_CLOSED_X = -34;
    private static final double CHARACTER_HANDLE_OPEN_X = 241;

    private static final double FACTION_HANDLE_CLOSED_X = 25;
    private static final double FACTION_HANDLE_OPEN_X = -250;

    private String activePopupTitle = "";

    private Image popupScrollImage;

    @FXML private StackPane gameRoot;
    @FXML private BorderPane hudLayer;

    @FXML private ImageView backgroundImage;
    @FXML private Rectangle darkOverlay;

    @FXML private VBox statsPanel;
    @FXML private VBox characterPanel;
    @FXML private VBox eventPanel;
    @FXML private VBox factionPanel;

    @FXML private Button characterDrawerButton;
    @FXML private Button factionDrawerButton;

    @FXML private Label nameLabel;
    @FXML private Label eraLabel;
    @FXML private Label originLabel;
    @FXML private Label traitLabel;

    @FXML private Label eventTitleLabel;
    @FXML private Label eventDescriptionLabel;

    @FXML private Button choiceButton1;
    @FXML private Button choiceButton2;
    @FXML private Button choiceButton3;

    @FXML private Label positiveCueLabel;
    @FXML private Label negativeCueLabel;

    @FXML private Label healthLabel;
    @FXML private Label wealthLabel;
    @FXML private Label educationLabel;
    @FXML private Label reputationLabel;
    @FXML private Label powerLabel;
    @FXML private Label moralityLabel;
    @FXML private Label familyLabel;
    @FXML private Label stressLabel;

    @FXML private ProgressBar healthBar;
    @FXML private ProgressBar wealthBar;
    @FXML private ProgressBar educationBar;
    @FXML private ProgressBar reputationBar;
    @FXML private ProgressBar powerBar;
    @FXML private ProgressBar moralityBar;
    @FXML private ProgressBar familyBar;
    @FXML private ProgressBar stressBar;

    @FXML private Label courtLabel;
    @FXML private Label noblesLabel;
    @FXML private Label militaryLabel;
    @FXML private Label scholarsLabel;
    @FXML private Label merchantsLabel;
    @FXML private Label commonPeopleLabel;
    @FXML private Label familyCouncilLabel;
    @FXML private Label shadowNetworkLabel;

    @FXML private ProgressBar courtBar;
    @FXML private ProgressBar noblesBar;
    @FXML private ProgressBar militaryBar;
    @FXML private ProgressBar scholarsBar;
    @FXML private ProgressBar merchantsBar;
    @FXML private ProgressBar commonPeopleBar;
    @FXML private ProgressBar familyCouncilBar;
    @FXML private ProgressBar shadowNetworkBar;

    @FXML private StackPane resultPopup;
    @FXML private StackPane popupShell;
    @FXML private ImageView popupScrollBackground;
    @FXML private VBox popupContentBox;
    @FXML private Label popupTitleLabel;
    @FXML private Label resultTextLabel;
    @FXML private ScrollPane popupMessageScroll;
    @FXML private Button popupContinueButton;

    @FXML
    public void initialize() {
        engine = new GameEngine();

        pendingLegacyTitleMessage = "";
        pendingStatusChangeMessage = "";

        activePopupTitle = "";
        legacyRecorded = false;
        birthIntroShown = false;
        finalChronicleShown = false;

        bindBackgroundToWindow();
        initializePopupAssets();
        closeDrawersInstantly();
        hideChangeCue();

        updateCharacterInfo();
        updateStats();
        updateFactions();

        eventTitleLabel.setText("Birth of a Life");
        eventDescriptionLabel.setText(
                "Your life is about to begin..."
        );

        setGameplayPanelsVisible(false);

        Platform.runLater(() -> {
            String birthBackground =
                    BackgroundLibrary.getBirthBackground(
                            engine.getPlayer()
                    );

            loadBackgroundInstantly(birthBackground);
            applyScenarioTheme(birthBackground);
            animateBackgroundMotion();

            birthIntroShown = true;

            showPopup(
                    "Birth of a Life",
                    engine.getBirthIntroMessage()
            );
        });
    }

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    private void initializePopupAssets() {
        popupScrollImage = loadFirstAvailablePopupImage(
                "/com/example/al_mirath/images_jpg/scroll_01.png",
                "/com/example/al_mirath/images_jpg/scroll_01.jpg",
                "/com/example/al_mirath/images_jpg/scroll_01.jpeg"
        );

        if (popupScrollBackground != null && popupScrollImage != null) {
            popupScrollBackground.setImage(popupScrollImage);
            popupScrollBackground.setVisible(true);
        }
    }

    private Image loadFirstAvailablePopupImage(String... paths) {
        for (String path : paths) {
            Image image = loadPopupImage(path);
            if (image != null) {
                System.out.println("Popup scroll loaded: " + path);
                return image;
            }
        }

        System.out.println("No popup scroll image found. Expected scroll_01.png/jpg/jpeg in images_jpg.");
        return null;
    }

    private Image loadPopupImage(String path) {
        try (InputStream stream = getClass().getResourceAsStream(path)) {
            if (stream == null) {
                return null;
            }

            return new Image(stream);
        } catch (Exception e) {
            System.out.println("Failed to load popup scroll image: " + path);
            e.printStackTrace();
            return null;
        }
    }

    private void bindBackgroundToWindow() {
        if (backgroundImage != null && gameRoot != null) {
            backgroundImage.fitWidthProperty().bind(gameRoot.widthProperty());
            backgroundImage.fitHeightProperty().bind(gameRoot.heightProperty());
            backgroundImage.setPreserveRatio(false);
            backgroundImage.setSmooth(true);
        }

        if (darkOverlay != null && gameRoot != null) {
            darkOverlay.widthProperty().bind(gameRoot.widthProperty());
            darkOverlay.heightProperty().bind(gameRoot.heightProperty());
        }
    }

    private void loadBackgroundInstantly(String imagePath) {
        Image image = createOptimizedBackgroundImage(imagePath);

        if (image == null) {
            return;
        }

        backgroundImage.setImage(image);
        backgroundImage.setOpacity(1.0);
        backgroundImage.setScaleX(1.04);
        backgroundImage.setScaleY(1.04);
    }

    private void changeBackground(String imagePath) {
        Image nextImage = createOptimizedBackgroundImage(imagePath);

        if (nextImage == null) {
            return;
        }

        applyScenarioTheme(imagePath);

        FadeTransition fadeOut =
                new FadeTransition(Duration.millis(160), backgroundImage);

        fadeOut.setToValue(0.15);

        fadeOut.setOnFinished(event -> {
            backgroundImage.setImage(nextImage);
            backgroundImage.setScaleX(1.04);
            backgroundImage.setScaleY(1.04);

            FadeTransition fadeIn =
                    new FadeTransition(Duration.millis(360), backgroundImage);

            fadeIn.setFromValue(0.15);
            fadeIn.setToValue(1);
            fadeIn.play();
        });

        fadeOut.play();
    }

    private void applyScenarioTheme(String imagePath) {
        if (gameRoot == null) {
            return;
        }

        boolean darkScenario = isDarkScenario(imagePath);

        gameRoot.getStyleClass().removeAll("light-scenario", "dark-scenario");

        if (darkScenario) {
            gameRoot.getStyleClass().add("dark-scenario");
            if (darkOverlay != null) darkOverlay.setOpacity(0.15);
        } else {
            gameRoot.getStyleClass().add("light-scenario");
            if (darkOverlay != null) darkOverlay.setOpacity(0.06);
        }
    }

    private boolean isDarkScenario(String imagePath) {
        if (imagePath == null) return false;

        String path = imagePath.toLowerCase();

        return path.contains("08_shadow")
                || path.contains("10_crisis")
                || path.contains("13_prison")
                || path.contains("dark")
                || path.contains("night")
                || path.contains("prison")
                || path.contains("shadow")
                || path.contains("spy")
                || path.contains("secret")
                || path.contains("unrest")
                || path.contains("famine")
                || path.contains("ruined");
    }

    private void animateBackgroundMotion() {
        if (backgroundMotionStarted || backgroundImage == null) {
            return;
        }

        backgroundMotionStarted = true;

        backgroundImage.setScaleX(1.06);
        backgroundImage.setScaleY(1.06);

        Timeline motion = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(backgroundImage.translateXProperty(), -14)),
                new KeyFrame(Duration.seconds(15), new KeyValue(backgroundImage.translateXProperty(), 14))
        );

        motion.setAutoReverse(true);
        motion.setCycleCount(Timeline.INDEFINITE);
        motion.play();
    }

    private void setGameplayPanelsVisible(boolean visible) {
        closeDrawersInstantly();

        double targetOpacity = visible ? 1.0 : 0.0;

        if (hudLayer != null) {
            hudLayer.setMouseTransparent(!visible);

            FadeTransition hudFade =
                    new FadeTransition(Duration.millis(visible ? 260 : 150), hudLayer);

            hudFade.setToValue(targetOpacity);
            hudFade.play();
        }

        if (characterDrawerButton != null) {
            characterDrawerButton.setVisible(visible);
            characterDrawerButton.setManaged(visible);
            characterDrawerButton.setMouseTransparent(!visible);
        }

        if (factionDrawerButton != null) {
            factionDrawerButton.setVisible(visible);
            factionDrawerButton.setManaged(visible);
            factionDrawerButton.setMouseTransparent(!visible);
        }
    }

    private void fadeNode(Node node, double targetOpacity) {
        if (node == null) return;

        node.setMouseTransparent(targetOpacity == 0.0);

        FadeTransition fade = new FadeTransition(Duration.millis(260), node);
        fade.setToValue(targetOpacity);
        fade.play();
    }

    private void updateCharacterInfo() {
        PlayerCharacter player = engine.getPlayer();

        if (nameLabel != null) nameLabel.setText("Name: " + player.getName());
        if (eraLabel != null) eraLabel.setText("Era: " + player.getEra());
        if (originLabel != null) originLabel.setText("Origin: " + player.getOrigin());

        if (traitLabel != null) {
            traitLabel.setText(
                    "Current Status: " + player.getCurrentStatus()
                            + "\nAge: " + player.getAge()
                            + "\nFamily: " + player.getFamilyCondition()
                            + "\nTrait: " + player.getTrait()
                            + "\nTitles: " + player.getLegacyTitlesText()
            );
        }
    }

    private void updateStats() {
        updateStatsWithDeltas(null);
    }

    private void updateStatsWithDeltas(Map<String, Integer> beforeStats) {
        PlayerCharacter p = engine.getPlayer();

        updateStatLabel(healthLabel, healthBar, "Health", p.getHealth(), getDelta(beforeStats, "health", p.getHealth()));
        updateStatLabel(wealthLabel, wealthBar, "Wealth", p.getWealth(), getDelta(beforeStats, "wealth", p.getWealth()));
        updateStatLabel(educationLabel, educationBar, "Education", p.getEducation(), getDelta(beforeStats, "education", p.getEducation()));
        updateStatLabel(reputationLabel, reputationBar, "Reputation", p.getReputation(), getDelta(beforeStats, "reputation", p.getReputation()));
        updateStatLabel(powerLabel, powerBar, "Political Power", p.getPoliticalPower(), getDelta(beforeStats, "politicalPower", p.getPoliticalPower()));
        updateStatLabel(moralityLabel, moralityBar, "Morality", p.getMorality(), getDelta(beforeStats, "morality", p.getMorality()));
        updateStatLabel(familyLabel, familyBar, "Family Loyalty", p.getFamilyLoyalty(), getDelta(beforeStats, "familyLoyalty", p.getFamilyLoyalty()));
        updateStatLabel(stressLabel, stressBar, "Stress", p.getStress(), getDelta(beforeStats, "stress", p.getStress()));
    }

    private void updateFactions() {
        updateFactionsWithDeltas(null);
    }

    private void updateFactionsWithDeltas(Map<String, Integer> beforeFactions) {
        FactionRelations f = engine.getFactions();

        updateStatLabel(courtLabel, courtBar, "Court", f.getCourt(), getDelta(beforeFactions, "court", f.getCourt()));
        updateStatLabel(noblesLabel, noblesBar, "Nobles", f.getNobles(), getDelta(beforeFactions, "nobles", f.getNobles()));
        updateStatLabel(militaryLabel, militaryBar, "Military", f.getMilitary(), getDelta(beforeFactions, "military", f.getMilitary()));
        updateStatLabel(scholarsLabel, scholarsBar, "Scholars", f.getScholars(), getDelta(beforeFactions, "scholars", f.getScholars()));
        updateStatLabel(merchantsLabel, merchantsBar, "Merchants", f.getMerchants(), getDelta(beforeFactions, "merchants", f.getMerchants()));
        updateStatLabel(commonPeopleLabel, commonPeopleBar, "Common People", f.getCommonPeople(), getDelta(beforeFactions, "commonPeople", f.getCommonPeople()));
        updateStatLabel(familyCouncilLabel, familyCouncilBar, "Family Council", f.getFamilyCouncil(), getDelta(beforeFactions, "familyCouncil", f.getFamilyCouncil()));
        updateStatLabel(shadowNetworkLabel, shadowNetworkBar, "Shadow Network", f.getShadowNetwork(), getDelta(beforeFactions, "shadowNetwork", f.getShadowNetwork()));
    }

    private void updateStatLabel(Label label, ProgressBar bar, String name, int value, int delta) {
        if (label == null) return;

        label.setText(formatValueWithDelta(name, value, delta));
        applyDeltaStyle(label, delta);

        if (bar != null) {
            animateProgressBar(bar, value);
        }
    }

    private void animateProgressBar(ProgressBar bar, int value) {
        if (bar == null) return;

        double newProgress = Math.max(0, Math.min(100, value)) / 100.0;

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(450), new KeyValue(bar.progressProperty(), newProgress))
        );

        timeline.play();
    }

    private Image createOptimizedBackgroundImage(String imagePath) {
        if (imagePath == null || imagePath.isBlank()) {
            return null;
        }

        try {
            var resource = getClass().getResource(imagePath);

            if (resource == null) {
                System.out.println("Background resource not found: " + imagePath);
                return null;
            }

            return new Image(
                    resource.toExternalForm(),
                    1920,
                    1080,
                    false,
                    true,
                    true
            );

        } catch (Exception e) {
            System.out.println("Could not load background: " + imagePath);
            e.printStackTrace();
            return null;
        }
    }

    private void loadCurrentEvent() {
        hideChangeCue();

        updateCharacterInfo();
        updateStats();
        updateFactions();

        GameEvent event = engine.getCurrentEvent();

        if (event == null) {
            EndingResult ending = engine.getEndingResult();

            if (!legacyRecorded) {
                PlayerCharacter player = engine.getPlayer();

                LegacyArchive.addRecord(new LegacyRecord(
                        player.getName(),
                        player.getEra(),
                        player.getOrigin(),
                        player.getFamilyCondition(),
                        ending.getTitle(),
                        player.getLegacyTitlesText()
                ));

                legacyRecorded = true;
            }

            choiceButton1.setVisible(false);
            choiceButton1.setManaged(false);

            choiceButton2.setVisible(false);
            choiceButton2.setManaged(false);

            choiceButton3.setVisible(false);
            choiceButton3.setManaged(false);

            setGameplayPanelsVisible(false);

            if (!finalChronicleShown) {
                finalChronicleShown = true;

                String endingMessage =
                        ending.getDescription()
                                + "\n\n"
                                + engine.getLifeSummary();

                showPopup("Final Chronicle", endingMessage);
            }

            return;
        }

        System.out.println(
                "Loading event: "
                        + event.getLifeStage()
                        + " — "
                        + event.getTitle()
        );

        String backgroundPath =
                BackgroundLibrary.getBackgroundForEvent(
                        event,
                        engine.getPlayer()
                );

        changeBackground(backgroundPath);

        eventTitleLabel.setText(
                event.getLifeStage()
                        + " — "
                        + event.getTitle()
        );

        eventDescriptionLabel.setText(event.getDescription());

        configureChoiceIfAvailable(choiceButton1, event, 0);
        configureChoiceIfAvailable(choiceButton2, event, 1);
        configureChoiceIfAvailable(choiceButton3, event, 2);

        fadeEventText();
    }

    private void configureChoiceIfAvailable(Button button, GameEvent event, int index) {
        if (button == null || event == null) return;

        if (index >= event.getChoices().size()) {
            button.setVisible(false);
            button.setManaged(false);
            return;
        }

        configureChoiceButton(button, event.getChoices().get(index));
        button.setVisible(true);
        button.setManaged(true);
    }

    private void fadeEventText() {
        eventTitleLabel.setOpacity(0);
        eventDescriptionLabel.setOpacity(0);

        FadeTransition titleFade = new FadeTransition(Duration.millis(250), eventTitleLabel);
        titleFade.setFromValue(0);
        titleFade.setToValue(1);

        FadeTransition descriptionFade = new FadeTransition(Duration.millis(350), eventDescriptionLabel);
        descriptionFade.setFromValue(0);
        descriptionFade.setToValue(1);

        titleFade.play();
        descriptionFade.play();
    }

    private void configureChoiceButton(Button button, Choice choice) {
        String lockedReason = engine.getLockedReason(choice);

        button.setWrapText(true);

        Tooltip tooltip = new Tooltip(buildChoicePreview(choice, lockedReason));
        tooltip.setWrapText(true);
        tooltip.setMaxWidth(380);
        button.setTooltip(tooltip);

        if (lockedReason.isEmpty()) {
            button.setText(choice.getText());
            button.setDisable(false);
        } else {
            button.setText(choice.getText() + "\n" + lockedReason);
            button.setDisable(true);
        }
    }

    private String buildChoicePreview(Choice choice, String lockedReason) {
        StringBuilder preview = new StringBuilder();

        preview.append("Choice Preview\n\n");

        if (lockedReason.isEmpty()) {
            preview.append("Status: Available\n");
        } else {
            preview.append("Status: Locked\n");
            preview.append(lockedReason).append("\n");
        }

        if (choice.requiresStatCheck()) {
            preview.append("\nAlgorithm Check:\n");
            preview.append("Checks: ").append(choice.getCheckStat()).append("\n");
            preview.append("Difficulty: ").append(choice.getDifficulty()).append("\n");
            preview.append("Result: Success or failure depends on stat value.\n");
        } else {
            preview.append("\nAlgorithm Check:\n");
            preview.append("Guaranteed outcome if selected.\n");
        }

        preview.append("\nThis choice may change stats, factions, memory flags, status, and future events.");

        return preview.toString();
    }

    @FXML
    private void handleChoice1() {
        choose(0);
    }

    @FXML
    private void handleChoice2() {
        choose(1);
    }

    @FXML
    private void handleChoice3() {
        choose(2);
    }

    @FXML
    private void choose(int index) {
        GameEvent event = engine.getCurrentEvent();

        if (event == null || index >= event.getChoices().size()) {
            return;
        }

        Choice choice = event.getChoices().get(index);

        if (!engine.canChoose(choice)) {
            showPopup("Choice Locked", engine.getLockedReason(choice));
            return;
        }

        Map<String, Integer> beforeStats = snapshotStats();
        Map<String, Integer> beforeFactions = snapshotFactions();

        choiceButton1.setDisable(true);
        choiceButton2.setDisable(true);
        choiceButton3.setDisable(true);

        setGameplayPanelsVisible(false);

        String consequenceBackground = BackgroundLibrary.getConsequenceBackground(event, choice, engine.getPlayer());
        changeBackground(consequenceBackground);

        String result = engine.applyChoice(choice);

        pendingLegacyTitleMessage = engine.consumeLatestLegacyTitleMessage();
        pendingStatusChangeMessage = engine.consumeLatestStatusChangeMessage();

        updateCharacterInfo();

        /*
         * Important:
         * Do NOT update stats with deltas here.
         * Otherwise +5 / -5 remains on the stats panel after the popup closes.
         */
        updateStats();
        updateFactions();
        hideChangeCue();

        String effectReport = buildEffectReport(beforeStats, beforeFactions);
        String popupMessage = result;

        if (!effectReport.isBlank()) {
            popupMessage += "\n\nEffects of this choice:\n" + effectReport;
        }

        String finalPopupMessage = popupMessage;

        Timeline popupDelay = new Timeline(
                new KeyFrame(Duration.millis(520), e -> showPopup("Consequence", finalPopupMessage))
        );

        popupDelay.play();
    }

    private void showPopup(String title, String message) {
        String safeTitle = title == null ? "" : title;
        String safeMessage = message == null ? "" : message;

        activePopupTitle = safeTitle;

        popupTitleLabel.setText(safeTitle);
        resultTextLabel.setText(safeMessage);

        configurePopupAppearance(safeTitle);

        if (popupMessageScroll != null) {
            popupMessageScroll.setVvalue(0);
        }

        applyPopupVisualEffect(safeTitle);
        setGameplayPanelsVisible(false);

        resultPopup.setManaged(true);
        resultPopup.setVisible(true);
        resultPopup.setOpacity(0);
        resultPopup.setMouseTransparent(false);
        resultPopup.toFront();

        FadeTransition fadeIn =
                new FadeTransition(Duration.millis(220), resultPopup);

        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    private void configurePopupAppearance(String title) {
        String lower = title == null ? "" : title.toLowerCase();

        boolean verticalScrollPopup =
                lower.contains("legacy title")
                        || lower.contains("status changed");

        boolean birthPopup =
                lower.contains("birth");

        boolean endingPopup =
                lower.contains("ending")
                        || lower.contains("death")
                        || lower.contains("final")
                        || lower.contains("chronicle");

        resultPopup.getStyleClass().removeAll(
                "popup-birth",
                "popup-consequence",
                "popup-status",
                "popup-legacy",
                "popup-ending",
                "popup-info",
                "popup-horizontal-mode",
                "popup-vertical-mode",
                "popup-birth-mode"
        );

        popupShell.getStyleClass().removeAll(
                "scroll-popup-horizontal",
                "scroll-popup-vertical",
                "scroll-popup-birth"
        );

        popupContentBox.getStyleClass().removeAll(
                "scroll-content-horizontal",
                "scroll-content-vertical",
                "scroll-content-birth"
        );

        if (popupScrollBackground != null && popupScrollImage != null) {
            popupScrollBackground.setImage(popupScrollImage);
            popupScrollBackground.setVisible(true);
            popupScrollBackground.setPreserveRatio(false);
            popupScrollBackground.setSmooth(true);
            popupScrollBackground.setCache(true);
        }

        if (verticalScrollPopup) {
            resultPopup.getStyleClass().add("popup-vertical-mode");
            popupShell.getStyleClass().add("scroll-popup-vertical");
            popupContentBox.getStyleClass().add("scroll-content-vertical");

            if (lower.contains("legacy")) {
                resultPopup.getStyleClass().add("popup-legacy");
                popupContinueButton.setText("Seal Decree");
            } else {
                resultPopup.getStyleClass().add("popup-status");
                popupContinueButton.setText("Accept Decree");
            }

            popupShell.setPrefWidth(560);
            popupShell.setPrefHeight(780);
            popupShell.setMaxWidth(560);
            popupShell.setMaxHeight(780);

            popupScrollBackground.setRotate(90);
            popupScrollBackground.setFitWidth(780);
            popupScrollBackground.setFitHeight(560);

            popupContentBox.setMaxWidth(330);
            popupContentBox.setPrefWidth(330);
            popupContentBox.setStyle("-fx-padding: 122 62 116 62;");

            popupTitleLabel.setMaxWidth(320);
            resultTextLabel.setMaxWidth(300);

            popupMessageScroll.setPrefViewportHeight(235);
            popupMessageScroll.setMaxWidth(320);

        } else {
            resultPopup.getStyleClass().add("popup-horizontal-mode");

            if (birthPopup) {
                resultPopup.getStyleClass().add("popup-birth");
                popupShell.getStyleClass().add("scroll-popup-birth");
                popupContentBox.getStyleClass().add("scroll-content-birth");
                popupContinueButton.setText("Begin Life");

                popupShell.setPrefWidth(1000);
                popupShell.setPrefHeight(560);
                popupShell.setMaxWidth(1000);
                popupShell.setMaxHeight(560);

                popupScrollBackground.setRotate(0);
                popupScrollBackground.setFitWidth(1000);
                popupScrollBackground.setFitHeight(560);

                popupContentBox.setMaxWidth(620);
                popupContentBox.setPrefWidth(620);
                popupContentBox.setStyle("-fx-padding: 72 155 78 155;");

                popupTitleLabel.setMaxWidth(610);
                resultTextLabel.setMaxWidth(570);

                popupMessageScroll.setPrefViewportHeight(180);
                popupMessageScroll.setMaxWidth(600);

            } else {
                popupShell.getStyleClass().add("scroll-popup-horizontal");
                popupContentBox.getStyleClass().add("scroll-content-horizontal");

                if (endingPopup) {
                    resultPopup.getStyleClass().add("popup-ending");
                    popupContinueButton.setText("Close Chronicle");
                } else {
                    resultPopup.getStyleClass().add("popup-consequence");
                    popupContinueButton.setText("Continue");
                }

                popupShell.setPrefWidth(980);
                popupShell.setPrefHeight(560);
                popupShell.setMaxWidth(980);
                popupShell.setMaxHeight(560);

                popupScrollBackground.setRotate(0);
                popupScrollBackground.setFitWidth(980);
                popupScrollBackground.setFitHeight(560);

                popupContentBox.setMaxWidth(620);
                popupContentBox.setPrefWidth(620);
                popupContentBox.setStyle("-fx-padding: 78 145 82 145;");

                popupTitleLabel.setMaxWidth(610);
                resultTextLabel.setMaxWidth(575);

                popupMessageScroll.setPrefViewportHeight(200);
                popupMessageScroll.setMaxWidth(600);
            }
        }
    }

    private void applyPopupVisualEffect(String title) {
        String lowerTitle = title == null ? "" : title.toLowerCase();

        Color glowColor;
        double glowLevel;

        if (lowerTitle.contains("legacy")) {
            glowColor = Color.rgb(255, 209, 94);
            glowLevel = 0.28;
        } else if (lowerTitle.contains("birth")) {
            glowColor = Color.rgb(255, 229, 150);
            glowLevel = 0.26;
        } else if (lowerTitle.contains("ending")
                || lowerTitle.contains("death")
                || lowerTitle.contains("final")
                || lowerTitle.contains("chronicle")) {
            glowColor = Color.rgb(190, 71, 43);
            glowLevel = 0.22;
        } else {
            glowColor = Color.rgb(218, 155, 58);
            glowLevel = 0.20;
        }

        DropShadow titleShadow = new DropShadow();
        titleShadow.setColor(glowColor);
        titleShadow.setRadius(10);
        titleShadow.setSpread(0.18);

        Glow titleGlow = new Glow(glowLevel);
        titleGlow.setInput(titleShadow);

        popupTitleLabel.setEffect(titleGlow);

        DropShadow popupShadow = new DropShadow();
        popupShadow.setColor(Color.rgb(0, 0, 0, 0.62));
        popupShadow.setRadius(24);
        popupShadow.setSpread(0.12);

        resultPopup.setEffect(popupShadow);
    }

    @FXML
    private void closePopup() {
        if (resultPopup == null || !resultPopup.isVisible()) {
            return;
        }

        popupContinueButton.setDisable(true);

        String closingTitle = activePopupTitle;

        FadeTransition fade =
                new FadeTransition(Duration.millis(180), resultPopup);

        fade.setFromValue(resultPopup.getOpacity());
        fade.setToValue(0);

        fade.setOnFinished(event -> {
            resultPopup.setVisible(false);
            resultPopup.setManaged(false);
            popupContinueButton.setDisable(false);

            /*
             * Show earned legacy title after the consequence popup.
             */
            if (!pendingLegacyTitleMessage.isBlank()) {
                String message = pendingLegacyTitleMessage;
                pendingLegacyTitleMessage = "";

                showPopup("Legacy Title Gained", message);
                return;
            }

            /*
             * Show status change after legacy title.
             */
            if (!pendingStatusChangeMessage.isBlank()) {
                String message = pendingStatusChangeMessage;
                pendingStatusChangeMessage = "";

                showPopup("Status Changed", message);
                return;
            }

            String lowerTitle =
                    closingTitle == null
                            ? ""
                            : closingTitle.toLowerCase();

            /*
             * Closing the final chronicle returns to the menu.
             */
            if (lowerTitle.contains("final")
                    || lowerTitle.contains("chronicle")
                    || lowerTitle.contains("death")
                    || lowerTitle.contains("ending")) {

                activePopupTitle = "";

                if (mainApp != null) {
                    mainApp.showWelcomeScreen();
                }

                return;
            }

            activePopupTitle = "";

            /*
             * This is the important line:
             * it retrieves and displays the next event.
             */
            loadCurrentEvent();

            /*
             * Do not reveal the HUD if loadCurrentEvent opened the ending popup.
             */
            if (!resultPopup.isVisible()) {
                setGameplayPanelsVisible(true);
            }
        });

        fade.play();
    }

    @FXML
    private void restartGame() {
        engine = new GameEngine();

        pendingLegacyTitleMessage = "";
        pendingStatusChangeMessage = "";

        activePopupTitle = "";
        legacyRecorded = false;
        birthIntroShown = true;
        finalChronicleShown = false;

        if (resultPopup != null) {
            resultPopup.setVisible(false);
            resultPopup.setManaged(false);
        }

        closeDrawersInstantly();
        hideChangeCue();
        setGameplayPanelsVisible(false);

        updateCharacterInfo();
        updateStats();
        updateFactions();

        eventTitleLabel.setText("Birth of a Life");
        eventDescriptionLabel.setText(
                "Your life is about to begin..."
        );

        String birthBackground =
                BackgroundLibrary.getBirthBackground(
                        engine.getPlayer()
                );

        changeBackground(birthBackground);

        showPopup(
                "Birth of a Life",
                engine.getBirthIntroMessage()
        );
    }

    @FXML
    private void returnToMainMenu() {
        if (mainApp != null) {
            mainApp.showWelcomeScreen();
        }
    }

    @FXML
    private void exitGame() {
        if (mainApp != null) {
            mainApp.exitGame();
        }
    }

    @FXML
    private void toggleCharacterDrawer() {
        if (characterDrawerOpen) {
            closeCharacterDrawer();
        } else {
            openCharacterDrawer();
        }
    }

    @FXML
    private void toggleFactionDrawer() {
        if (factionDrawerOpen) {
            closeFactionDrawer();
        } else {
            openFactionDrawer();
        }
    }

    private void openCharacterDrawer() {
        characterDrawerOpen = true;

        slideNode(characterPanel, 0);
        slideNode(characterDrawerButton, CHARACTER_HANDLE_OPEN_X);
    }

    private void closeCharacterDrawer() {
        characterDrawerOpen = false;

        slideNode(characterPanel, CHARACTER_DRAWER_CLOSED_X);
        slideNode(characterDrawerButton, CHARACTER_HANDLE_CLOSED_X);
    }

    private void openFactionDrawer() {
        factionDrawerOpen = true;

        slideNode(factionPanel, 0);
        slideNode(factionDrawerButton, FACTION_HANDLE_OPEN_X);
    }

    private void closeFactionDrawer() {
        factionDrawerOpen = false;

        slideNode(factionPanel, FACTION_DRAWER_CLOSED_X);
        slideNode(factionDrawerButton, FACTION_HANDLE_CLOSED_X);
    }

    private void closeDrawersInstantly() {
        characterDrawerOpen = false;
        factionDrawerOpen = false;

        if (characterPanel != null) {
            characterPanel.setTranslateX(CHARACTER_DRAWER_CLOSED_X);
        }

        if (factionPanel != null) {
            factionPanel.setTranslateX(FACTION_DRAWER_CLOSED_X);
        }

        if (characterDrawerButton != null) {
            characterDrawerButton.setTranslateX(CHARACTER_HANDLE_CLOSED_X);
        }

        if (factionDrawerButton != null) {
            factionDrawerButton.setTranslateX(FACTION_HANDLE_CLOSED_X);
        }
    }

    private void slideNode(Node node, double targetX) {
        if (node == null) {
            return;
        }

        TranslateTransition transition = new TranslateTransition(Duration.millis(280), node);
        transition.setToX(targetX);
        transition.play();
    }

    private Map<String, Integer> snapshotStats() {
        PlayerCharacter p = engine.getPlayer();

        Map<String, Integer> stats = new LinkedHashMap<>();
        stats.put("health", p.getHealth());
        stats.put("wealth", p.getWealth());
        stats.put("education", p.getEducation());
        stats.put("reputation", p.getReputation());
        stats.put("politicalPower", p.getPoliticalPower());
        stats.put("morality", p.getMorality());
        stats.put("familyLoyalty", p.getFamilyLoyalty());
        stats.put("stress", p.getStress());

        return stats;
    }

    private Map<String, Integer> snapshotFactions() {
        FactionRelations f = engine.getFactions();

        Map<String, Integer> factions = new LinkedHashMap<>();
        factions.put("court", f.getCourt());
        factions.put("nobles", f.getNobles());
        factions.put("military", f.getMilitary());
        factions.put("scholars", f.getScholars());
        factions.put("merchants", f.getMerchants());
        factions.put("commonPeople", f.getCommonPeople());
        factions.put("familyCouncil", f.getFamilyCouncil());
        factions.put("shadowNetwork", f.getShadowNetwork());

        return factions;
    }

    private int getDelta(Map<String, Integer> before, String key, int currentValue) {
        if (before == null || !before.containsKey(key)) {
            return 0;
        }

        return currentValue - before.get(key);
    }

    private String formatValueWithDelta(String name, int value, int delta) {
        if (delta > 0) return name + ": " + value + "   +" + delta;
        if (delta < 0) return name + ": " + value + "   " + delta;
        return name + ": " + value;
    }

    private void applyDeltaStyle(Label label, int delta) {
        if (label == null) return;

        label.getStyleClass().removeAll("delta-positive", "delta-negative", "delta-neutral");

        if (delta > 0) {
            label.getStyleClass().add("delta-positive");
        } else if (delta < 0) {
            label.getStyleClass().add("delta-negative");
        } else {
            label.getStyleClass().add("delta-neutral");
        }
    }

    private void showChangeCue(Map<String, Integer> beforeStats, Map<String, Integer> beforeFactions) {
        StringBuilder positive = new StringBuilder();
        StringBuilder negative = new StringBuilder();

        PlayerCharacter p = engine.getPlayer();

        addDeltaCue(positive, negative, beforeStats, "health", "Health", p.getHealth());
        addDeltaCue(positive, negative, beforeStats, "wealth", "Wealth", p.getWealth());
        addDeltaCue(positive, negative, beforeStats, "education", "Education", p.getEducation());
        addDeltaCue(positive, negative, beforeStats, "reputation", "Reputation", p.getReputation());
        addDeltaCue(positive, negative, beforeStats, "politicalPower", "Power", p.getPoliticalPower());
        addDeltaCue(positive, negative, beforeStats, "morality", "Morality", p.getMorality());
        addDeltaCue(positive, negative, beforeStats, "familyLoyalty", "Family", p.getFamilyLoyalty());
        addDeltaCue(positive, negative, beforeStats, "stress", "Stress", p.getStress());

        FactionRelations f = engine.getFactions();

        addDeltaCue(positive, negative, beforeFactions, "court", "Court", f.getCourt());
        addDeltaCue(positive, negative, beforeFactions, "nobles", "Nobles", f.getNobles());
        addDeltaCue(positive, negative, beforeFactions, "military", "Military", f.getMilitary());
        addDeltaCue(positive, negative, beforeFactions, "scholars", "Scholars", f.getScholars());
        addDeltaCue(positive, negative, beforeFactions, "merchants", "Merchants", f.getMerchants());
        addDeltaCue(positive, negative, beforeFactions, "commonPeople", "People", f.getCommonPeople());
        addDeltaCue(positive, negative, beforeFactions, "familyCouncil", "Family Council", f.getFamilyCouncil());
        addDeltaCue(positive, negative, beforeFactions, "shadowNetwork", "Shadow", f.getShadowNetwork());

        setCueLabel(positiveCueLabel, positive.toString());
        setCueLabel(negativeCueLabel, negative.toString());
    }

    private void addDeltaCue(StringBuilder positive,
                             StringBuilder negative,
                             Map<String, Integer> before,
                             String key,
                             String displayName,
                             int currentValue) {
        int delta = getDelta(before, key, currentValue);

        if (delta > 0) {
            if (!positive.isEmpty()) positive.append("   ");
            positive.append(displayName).append(" +").append(delta);
        } else if (delta < 0) {
            if (!negative.isEmpty()) negative.append("   ");
            negative.append(displayName).append(" ").append(delta);
        }
    }

    private void setCueLabel(Label label, String text) {
        if (label == null) return;

        boolean hasText = text != null && !text.isBlank();

        label.setText(text);
        label.setVisible(hasText);
        label.setManaged(hasText);
    }

    private void hideChangeCue() {
        setCueLabel(positiveCueLabel, "");
        setCueLabel(negativeCueLabel, "");
    }

    private String buildEffectReport(Map<String, Integer> beforeStats,
                                     Map<String, Integer> beforeFactions) {
        StringBuilder report = new StringBuilder();

        PlayerCharacter p = engine.getPlayer();

        addEffectLine(report, beforeStats, "health", "Health", p.getHealth());
        addEffectLine(report, beforeStats, "wealth", "Wealth", p.getWealth());
        addEffectLine(report, beforeStats, "education", "Education", p.getEducation());
        addEffectLine(report, beforeStats, "reputation", "Reputation", p.getReputation());
        addEffectLine(report, beforeStats, "politicalPower", "Political Power", p.getPoliticalPower());
        addEffectLine(report, beforeStats, "morality", "Morality", p.getMorality());
        addEffectLine(report, beforeStats, "familyLoyalty", "Family Loyalty", p.getFamilyLoyalty());
        addEffectLine(report, beforeStats, "stress", "Stress", p.getStress());

        FactionRelations f = engine.getFactions();

        addEffectLine(report, beforeFactions, "court", "Court", f.getCourt());
        addEffectLine(report, beforeFactions, "nobles", "Nobles", f.getNobles());
        addEffectLine(report, beforeFactions, "military", "Military", f.getMilitary());
        addEffectLine(report, beforeFactions, "scholars", "Scholars", f.getScholars());
        addEffectLine(report, beforeFactions, "merchants", "Merchants", f.getMerchants());
        addEffectLine(report, beforeFactions, "commonPeople", "Common People", f.getCommonPeople());
        addEffectLine(report, beforeFactions, "familyCouncil", "Family Council", f.getFamilyCouncil());
        addEffectLine(report, beforeFactions, "shadowNetwork", "Shadow Network", f.getShadowNetwork());

        return report.toString().trim();
    }

    private void addEffectLine(StringBuilder report,
                               Map<String, Integer> before,
                               String key,
                               String displayName,
                               int currentValue) {
        int delta = getDelta(before, key, currentValue);

        if (delta == 0) {
            return;
        }

        if (!report.isEmpty()) {
            report.append("\n");
        }

        if (delta > 0) {
            report.append("+").append(delta).append(" ").append(displayName);
        } else {
            report.append(delta).append(" ").append(displayName);
        }
    }
}