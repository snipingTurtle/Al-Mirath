package com.example.al_mirath.controller;

import com.example.al_mirath.Main;
import com.example.al_mirath.model.Choice;
import com.example.al_mirath.model.EndingResult;
import com.example.al_mirath.model.FactionRelations;
import com.example.al_mirath.model.GameEvent;
import com.example.al_mirath.model.LegacyRecord;
import com.example.al_mirath.model.PlayerCharacter;
import com.example.al_mirath.core.GameSettings;
import com.example.al_mirath.minigame.MiniGame;
import com.example.al_mirath.minigame.MiniGameFactory;
import com.example.al_mirath.service.AchievementEvaluator;
import com.example.al_mirath.service.BackgroundLibrary;
import com.example.al_mirath.service.GameEngine;
import com.example.al_mirath.service.LegacyArchive;
import com.example.al_mirath.service.ProgressService;
import com.example.al_mirath.service.SaveManager;
import com.example.al_mirath.ui.MiniGameOverlay;
import com.example.al_mirath.ui.TrialPromptOverlay;
import com.example.al_mirath.ui.ToastLayer;
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
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.input.KeyEvent;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import javafx.application.Platform;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

public class GameController {

    /**
     * Which kind of popup is on screen. Driving layout and color from this
     * instead of sniffing keywords out of the title text keeps things correct
     * now that world events carry arbitrary, unpredictable titles.
     */
    private enum PopupCategory {
        BIRTH, CONSEQUENCE, LOCKED_INFO, LEGACY_TITLE, STATUS_CHANGE, FATE_TOKEN, WORLD_EVENT, ECHO, ENDING
    }

    private GameEngine engine;
    private Main mainApp;
    private GameEngine restoredEngine;

    private String pendingLegacyTitleMessage = "";
    private String pendingStatusChangeMessage = "";
    private String pendingWorldEventTitle = "";
    private String pendingWorldEventMessage = "";
    private String pendingEchoTitle = "";
    private String pendingEchoMessage = "";

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
    private PopupCategory activePopupCategory;
    private String pendingFateTokenMessage = "";

    private Image popupScrollImage;

    private ToastLayer toastLayer;

    /** Set while a rewind is available so the popup can offer the challenge. */
    private boolean rewindOfferAvailable = false;

    private Timeline typewriterTimeline;
    private boolean typewriterEnabled = GameSettings.isTypewriterEnabled();

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

    @FXML private Label fateTokenLabel;
    @FXML private javafx.scene.layout.HBox popupButtonRow;
    @FXML private Button popupRewindButton;

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
        boolean continuingGame = restoredEngine != null;

        engine = continuingGame ? restoredEngine : new GameEngine();

        pendingLegacyTitleMessage = "";
        pendingStatusChangeMessage = "";

        activePopupTitle = "";
        legacyRecorded = false;
        birthIntroShown = continuingGame;
        finalChronicleShown = false;

        pendingFateTokenMessage = "";
        pendingWorldEventTitle = "";
        pendingWorldEventMessage = "";
        pendingEchoTitle = "";
        pendingEchoMessage = "";
        rewindOfferAvailable = false;

        bindBackgroundToWindow();
        initializePopupAssets();
        initializeToastLayer();
        installKeyboardShortcuts();
        closeDrawersInstantly();
        hideChangeCue();

        updateCharacterInfo();
        updateStats();
        updateFactions();
        updateFateTokenLabel();

        if (continuingGame) {
            setGameplayPanelsVisible(true);

            Platform.runLater(() -> {
                animateBackgroundMotion();
                loadCurrentEvent();
                setGameplayPanelsVisible(true);
            });

            return;
        }

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
                    engine.getBirthIntroMessage(),
                    PopupCategory.BIRTH
            );
        });
    }

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    public void setRestoredEngine(GameEngine restoredEngine) {
        this.restoredEngine = restoredEngine;
    }

    /**
     * Binds the keyboard once the scene exists.
     *
     * <p>The handler is attached to the scene rather than a control so it works
     * no matter what currently holds focus. Keys are ignored while a trial
     * overlay is up, since the mini-games handle their own input.
     */
    private void installKeyboardShortcuts() {
        if (gameRoot == null) {
            return;
        }

        gameRoot.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.setOnKeyPressed(this::handleKeyPress);
            }
        });
    }

    private void handleKeyPress(KeyEvent event) {
        if (isMiniGameActive()) {
            return;
        }

        boolean popupOpen = resultPopup != null && resultPopup.isVisible();

        switch (event.getCode()) {
            case DIGIT1, NUMPAD1 -> {
                if (!popupOpen) {
                    triggerChoiceButton(choiceButton1, 0);
                }
            }
            case DIGIT2, NUMPAD2 -> {
                if (!popupOpen) {
                    triggerChoiceButton(choiceButton2, 1);
                }
            }
            case DIGIT3, NUMPAD3 -> {
                if (!popupOpen) {
                    triggerChoiceButton(choiceButton3, 2);
                }
            }
            case SPACE, ENTER -> {
                if (popupOpen) {
                    closePopup();
                } else if (typewriterTimeline != null) {
                    // Impatient readers can skip the reveal.
                    completeTypewriter();
                    revealFullDescription();
                }
            }
            case R -> {
                if (popupOpen && popupRewindButton != null && popupRewindButton.isVisible()) {
                    openRewindChallenge();
                }
            }
            case ESCAPE -> returnToMainMenu();
            default -> {
                // Every other key is left to the focused control.
            }
        }

        event.consume();
    }

    private void triggerChoiceButton(Button button, int index) {
        if (button == null || !button.isVisible() || button.isDisabled()) {
            return;
        }

        choose(index);
    }

    /** True while a Threads of Fate overlay is on screen. */
    private boolean isMiniGameActive() {
        if (gameRoot == null) {
            return false;
        }

        for (Node node : gameRoot.getChildren()) {
            if (node instanceof MiniGameOverlay) {
                return true;
            }
        }

        return false;
    }

    /**
     * Reveals the event description a character at a time.
     *
     * <p>Skipped when the reveal would be slower than reading it, and any
     * in-flight reveal is cancelled before the next one starts so two events
     * can never type over each other.
     */
    private void typewriteDescription(String text) {
        if (eventDescriptionLabel == null) {
            return;
        }

        if (typewriterTimeline != null) {
            typewriterTimeline.stop();
        }

        String safeText = text == null ? "" : text;

        if (!typewriterEnabled || safeText.length() > 600) {
            eventDescriptionLabel.setText(safeText);
            return;
        }

        eventDescriptionLabel.setText("");

        typewriterTimeline = new Timeline();

        // One keyframe per character, then a final frame guaranteeing the full text.
        double perCharacter = 12;

        for (int i = 1; i <= safeText.length(); i++) {
            final int end = i;

            typewriterTimeline.getKeyFrames().add(new KeyFrame(
                    Duration.millis(i * perCharacter),
                    event -> eventDescriptionLabel.setText(safeText.substring(0, end))
            ));
        }

        typewriterTimeline.setOnFinished(event -> eventDescriptionLabel.setText(safeText));
        typewriterTimeline.play();
    }

    /** Lets a click or key finish the reveal instantly. */
    private void completeTypewriter() {
        if (typewriterTimeline != null) {
            typewriterTimeline.stop();
            typewriterTimeline = null;
        }
    }

    /** Prints the current event's description in full, skipping the reveal. */
    private void revealFullDescription() {
        GameEvent event = engine == null ? null : engine.getCurrentEvent();

        if (event != null && eventDescriptionLabel != null) {
            eventDescriptionLabel.setText(event.getDescription());
        }
    }

    /**
     * Adds the notification layer above the HUD and routes achievement unlocks
     * into it for the lifetime of this screen.
     */
    private void initializeToastLayer() {
        if (gameRoot == null) {
            return;
        }

        toastLayer = new ToastLayer();
        gameRoot.getChildren().add(toastLayer);

        ProgressService.setUnlockListener(achievement -> {
            if (toastLayer != null) {
                toastLayer.showAchievement(achievement);
            }
        });
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
                    "Life Stage: " + engine.getCurrentLifeStage()
                            + "\nCurrent Status: " + player.getCurrentStatus()
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
                int score = engine.calculateScore();

                LegacyArchive.addRecord(new LegacyRecord(
                        player.getName(),
                        player.getEra(),
                        player.getOrigin(),
                        player.getFamilyCondition(),
                        ending.getTitle(),
                        player.getLegacyTitlesText(),
                        player.getAge(),
                        player.getCurrentStatus(),
                        score
                ));

                AchievementEvaluator.afterLifeComplete(engine, ending, score);

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

                showPopup("Final Chronicle", endingMessage, PopupCategory.ENDING);
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

        eventTitleLabel.setText(event.getTitle());

        typewriteDescription(event.getDescription());

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
            showPopup("Choice Locked", engine.getLockedReason(choice), PopupCategory.LOCKED_INFO);
            return;
        }

        // A choice that tests a stat can be earned by hand instead of rolled.
        // The player is asked once, here, rather than being given a fourth
        // button that would crowd the event panel.
        if (choice.requiresStatCheck() && GameSettings.areTrialsEnabled()) {
            offerTrialOfSkill(choice);
            return;
        }

        commitChoice(choice, null);
    }

    /**
     * Presents the fork between letting the engine roll the stat check and
     * playing for the outcome directly.
     */
    private void offerTrialOfSkill(Choice choice) {
        setGameplayPanelsVisible(false);

        TrialPromptOverlay prompt = new TrialPromptOverlay(
                choice,
                engine.getPlayer().getStatValue(choice.getCheckStat()),
                decision -> {
                    switch (decision) {
                        case PLAY -> startTrialOfSkill(choice);
                        case ROLL -> commitChoice(choice, null);
                        case CANCEL -> {
                            // Back out entirely; the event stays on screen.
                            setGameplayPanelsVisible(true);
                            loadCurrentEvent();
                        }
                    }
                }
        );

        gameRoot.getChildren().add(prompt);
        prompt.requestFocus();
    }

    /** Runs the challenge, then applies the choice with the earned outcome. */
    private void startTrialOfSkill(Choice choice) {
        MiniGame miniGame = MiniGameFactory.createForStatCheck(
                choice.getCheckStat(),
                choice.getDifficulty()
        );

        MiniGameOverlay overlay = new MiniGameOverlay(miniGame, result -> {
            if (result == null) {
                // Withdrew from the briefing: fall back to the ordinary roll.
                commitChoice(choice, null);
                return;
            }

            engine.recordMinigameOutcome(result.success());
            AchievementEvaluator.afterMiniGame(engine, miniGame.getTitle(), result.success());

            if (toastLayer != null) {
                toastLayer.show(
                        result.success() ? "Trial Won" : "Trial Lost",
                        result.success()
                                ? "You earned the outcome yourself."
                                : "The attempt failed on its own terms.",
                        result.success() ? "toast-achievement" : "toast-rewind"
                );
            }

            commitChoice(choice, result.success());
        });

        gameRoot.getChildren().add(overlay);
        overlay.requestFocus();
    }

    /**
     * Applies a choice and runs the whole post-choice presentation.
     *
     * @param forcedOutcome non-null when a Trial of Skill decided the result
     */
    private void commitChoice(Choice choice, Boolean forcedOutcome) {
        GameEvent event = engine.getCurrentEvent();

        if (event == null) {
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

        String result = engine.applyChoice(choice, forcedOutcome);

        pendingLegacyTitleMessage = engine.consumeLatestLegacyTitleMessage();
        pendingStatusChangeMessage = engine.consumeLatestStatusChangeMessage();
        pendingFateTokenMessage = engine.consumeLatestFateTokenMessage();

        String worldEventTitle = engine.getLatestWorldEventTitle();
        if (!worldEventTitle.isBlank()) {
            pendingWorldEventTitle = worldEventTitle;
            pendingWorldEventMessage = engine.consumeLatestWorldEventMessage();
        }

        String echoTitle = engine.getLatestConsequenceEchoTitle();
        if (!echoTitle.isBlank()) {
            pendingEchoTitle = echoTitle;
            pendingEchoMessage = engine.consumeLatestConsequenceEchoMessage();
        }

        AchievementEvaluator.afterChoice(engine);

        // The consequence popup that follows is the one place a rewind is offered.
        rewindOfferAvailable = true;
        updateFateTokenLabel();

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
                new KeyFrame(Duration.millis(520), e -> showPopup("", finalPopupMessage, PopupCategory.CONSEQUENCE))
        );

        popupDelay.play();
    }

    /**
     * Opens a Threads of Fate challenge for the decision just made.
     *
     * <p>The thread is spent the moment the trial begins, so losing still costs
     * the player something. Withdrawing from the briefing screen refunds it,
     * because nothing was actually attempted.
     */
    @FXML
    private void openRewindChallenge() {
        if (engine == null || !engine.canRewind()) {
            return;
        }

        if (!engine.spendFateToken()) {
            return;
        }

        updateFateTokenLabel();

        // Hide the consequence popup while the trial plays out.
        resultPopup.setVisible(false);
        resultPopup.setManaged(false);

        MiniGame miniGame = MiniGameFactory.createFor(
                engine.getPlayer(),
                engine.getCurrentStageIndex()
        );

        MiniGameOverlay overlay = new MiniGameOverlay(miniGame, result -> {
            if (result == null) {
                // Withdrew before playing: give the thread back and restore the popup.
                engine.refundFateToken();
                updateFateTokenLabel();
                reopenConsequencePopup();
                return;
            }

            engine.recordMinigameOutcome(result.success());
            AchievementEvaluator.afterMiniGame(engine, miniGame.getTitle(), result.success());

            if (result.success()) {
                applySuccessfulRewind();
            } else {
                reopenConsequencePopup();
            }
        });

        gameRoot.getChildren().add(overlay);
        overlay.requestFocus();
    }

    /**
     * Rolls the run back to just before the last choice and puts the player in
     * front of the same event again.
     */
    private void applySuccessfulRewind() {
        boolean rewound = engine.rewindToLastSnapshot();

        if (!rewound) {
            reopenConsequencePopup();
            return;
        }

        // The undone choice never happened, so its follow-up messages are void.
        pendingLegacyTitleMessage = "";
        pendingStatusChangeMessage = "";
        pendingFateTokenMessage = "";
        pendingWorldEventTitle = "";
        pendingWorldEventMessage = "";
        pendingEchoTitle = "";
        pendingEchoMessage = "";
        activePopupTitle = "";
        rewindOfferAvailable = false;

        resultPopup.setVisible(false);
        resultPopup.setManaged(false);

        updateCharacterInfo();
        updateStats();
        updateFactions();
        updateFateTokenLabel();
        hideChangeCue();

        loadCurrentEvent();
        setGameplayPanelsVisible(true);

        if (toastLayer != null) {
            toastLayer.show(
                    "Destiny Rewritten",
                    "The moment returns. Choose again.",
                    "toast-rewind"
            );
        }
    }

    /** Brings back the consequence popup after a trial ends without a rewind. */
    private void reopenConsequencePopup() {
        rewindOfferAvailable = false;

        resultPopup.setManaged(true);
        resultPopup.setVisible(true);
        resultPopup.setOpacity(1);
        resultPopup.setMouseTransparent(false);
        resultPopup.toFront();

        updateRewindButtonVisibility();
    }

    private void updateFateTokenLabel() {
        if (fateTokenLabel == null || engine == null) {
            return;
        }

        int tokens = engine.getFateTokens();

        fateTokenLabel.setText("Threads of Fate: " + tokens);
        fateTokenLabel.setOpacity(tokens > 0 ? 1.0 : 0.45);
    }

    /**
     * The challenge is only offered on a consequence popup, while a thread is
     * held and an undo point exists.
     */
    private void updateRewindButtonVisibility() {
        if (popupRewindButton == null) {
            return;
        }

        boolean offer = rewindOfferAvailable
                && engine != null
                && engine.canRewind();

        popupRewindButton.setVisible(offer);
        popupRewindButton.setManaged(offer);

        if (offer) {
            // The Threads of Fate counter is already visible in the top bar;
            // repeating the count here just crowds out the label.
            popupRewindButton.setText("Challenge Fate");
        }
    }

    private void showPopup(String title, String message, PopupCategory category) {
        String safeTitle = title == null ? "" : title;
        String safeMessage = message == null ? "" : message;

        activePopupTitle = safeTitle;
        activePopupCategory = category;

        // The plain outcome of a choice needs no header of its own — the
        // scroll text already says what happened.
        boolean showTitle = category != PopupCategory.CONSEQUENCE && !safeTitle.isBlank();

        popupTitleLabel.setText(showTitle ? safeTitle : "");
        popupTitleLabel.setVisible(showTitle);
        popupTitleLabel.setManaged(showTitle);

        resultTextLabel.setText(safeMessage);

        configurePopupAppearance(category);

        if (popupMessageScroll != null) {
            popupMessageScroll.setVvalue(0);
        }

        applyPopupVisualEffect(category);
        setGameplayPanelsVisible(false);
        updateRewindButtonVisibility();

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

    private void configurePopupAppearance(PopupCategory category) {
        boolean verticalScrollPopup =
                category == PopupCategory.LEGACY_TITLE || category == PopupCategory.STATUS_CHANGE;

        boolean birthPopup = category == PopupCategory.BIRTH;
        boolean endingPopup = category == PopupCategory.ENDING;

        resultPopup.getStyleClass().removeAll(
                "popup-birth",
                "popup-consequence",
                "popup-status",
                "popup-legacy",
                "popup-ending",
                "popup-info",
                "popup-worldevent",
                "popup-echo",
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

            if (category == PopupCategory.LEGACY_TITLE) {
                resultPopup.getStyleClass().add("popup-legacy");
                popupContinueButton.setText("Seal Decree");
            } else {
                resultPopup.getStyleClass().add("popup-status");
                popupContinueButton.setText("Accept Decree");
            }

            popupShell.setPrefWidth(680);
            popupShell.setPrefHeight(860);
            popupShell.setMaxWidth(680);
            popupShell.setMaxHeight(860);

            popupScrollBackground.setRotate(90);
            popupScrollBackground.setFitWidth(860);
            popupScrollBackground.setFitHeight(680);

            popupContentBox.setMaxWidth(440);
            popupContentBox.setPrefWidth(440);
            popupContentBox.setStyle("-fx-padding: 150 90 140 90;");

            popupTitleLabel.setMaxWidth(420);
            resultTextLabel.setMaxWidth(400);

            popupMessageScroll.setPrefViewportHeight(300);
            popupMessageScroll.setMaxWidth(420);

        } else {
            resultPopup.getStyleClass().add("popup-horizontal-mode");

            if (birthPopup) {
                resultPopup.getStyleClass().add("popup-birth");
                popupShell.getStyleClass().add("scroll-popup-birth");
                popupContentBox.getStyleClass().add("scroll-content-birth");
                popupContinueButton.setText("Begin Life");

                popupShell.setPrefWidth(1180);
                popupShell.setPrefHeight(640);
                popupShell.setMaxWidth(1180);
                popupShell.setMaxHeight(640);

                popupScrollBackground.setRotate(0);
                popupScrollBackground.setFitWidth(1180);
                popupScrollBackground.setFitHeight(640);

                popupContentBox.setMaxWidth(780);
                popupContentBox.setPrefWidth(780);
                popupContentBox.setStyle("-fx-padding: 76 200 80 200;");

                popupTitleLabel.setMaxWidth(760);
                resultTextLabel.setMaxWidth(720);

                popupMessageScroll.setPrefViewportHeight(190);
                popupMessageScroll.setMaxWidth(760);

            } else {
                popupShell.getStyleClass().add("scroll-popup-horizontal");
                popupContentBox.getStyleClass().add("scroll-content-horizontal");

                if (endingPopup) {
                    resultPopup.getStyleClass().add("popup-ending");
                    popupContinueButton.setText("Close Chronicle");
                } else if (category == PopupCategory.WORLD_EVENT) {
                    resultPopup.getStyleClass().add("popup-worldevent");
                    popupContinueButton.setText("Continue");
                } else if (category == PopupCategory.ECHO) {
                    resultPopup.getStyleClass().add("popup-echo");
                    popupContinueButton.setText("So It Returns");
                } else if (category == PopupCategory.LOCKED_INFO) {
                    resultPopup.getStyleClass().add("popup-info");
                    popupContinueButton.setText("Understood");
                } else {
                    resultPopup.getStyleClass().add("popup-consequence");
                    popupContinueButton.setText("Continue");
                }

                popupShell.setPrefWidth(1180);
                popupShell.setPrefHeight(640);
                popupShell.setMaxWidth(1180);
                popupShell.setMaxHeight(640);

                popupScrollBackground.setRotate(0);
                popupScrollBackground.setFitWidth(1180);
                popupScrollBackground.setFitHeight(640);

                popupContentBox.setMaxWidth(780);
                popupContentBox.setPrefWidth(780);
                popupContentBox.setStyle("-fx-padding: 82 190 86 190;");

                popupTitleLabel.setMaxWidth(760);
                resultTextLabel.setMaxWidth(730);

                popupMessageScroll.setPrefViewportHeight(210);
                popupMessageScroll.setMaxWidth(760);
            }
        }

        applyScrollTint(category);
    }

    /**
     * Tints the single scroll art asset per popup type using a ColorAdjust
     * effect, so a birth reads warm, an ending reads dark and somber, and so
     * on, without needing a separate image for every category.
     */
    private void applyScrollTint(PopupCategory category) {
        if (popupScrollBackground == null) {
            return;
        }

        ColorAdjust tint = new ColorAdjust();

        // The parchment must stay light in every mood. Body text on the scroll
        // is dark brown, so darkening the art was making Status and Final
        // Chronicle text nearly unreadable. Categories are distinguished by
        // hue and by title/border colour instead of by brightness.
        switch (category) {
            case BIRTH -> {
                tint.setHue(0.04);
                tint.setSaturation(0.14);
                tint.setBrightness(0.06);
            }
            case LEGACY_TITLE -> {
                tint.setHue(-0.02);
                tint.setSaturation(0.16);
                tint.setBrightness(0.03);
            }
            case STATUS_CHANGE -> {
                tint.setHue(0.03);
                tint.setSaturation(0.10);
                tint.setBrightness(0.04);
            }
            case FATE_TOKEN -> {
                tint.setHue(-0.01);
                tint.setSaturation(0.10);
                tint.setBrightness(0.04);
            }
            case WORLD_EVENT -> {
                tint.setHue(0.11);
                tint.setSaturation(-0.04);
                tint.setBrightness(0.02);
            }
            case ECHO -> {
                tint.setHue(0.02);
                tint.setSaturation(-0.10);
                tint.setBrightness(-0.04);
            }
            case LOCKED_INFO -> {
                tint.setHue(0.0);
                tint.setSaturation(-0.16);
                tint.setBrightness(0.0);
            }
            case ENDING -> {
                // Somber, but never dark enough to swallow the writing.
                tint.setHue(-0.05);
                tint.setSaturation(-0.12);
                tint.setBrightness(-0.05);
            }
            default -> {
                // CONSEQUENCE stays the plain parchment tone.
            }
        }

        popupScrollBackground.setEffect(tint);
    }

    private void applyPopupVisualEffect(PopupCategory category) {
        Color glowColor;
        double glowLevel;

        switch (category) {
            case LEGACY_TITLE -> {
                glowColor = Color.rgb(255, 209, 94);
                glowLevel = 0.28;
            }
            case BIRTH -> {
                glowColor = Color.rgb(255, 229, 150);
                glowLevel = 0.26;
            }
            case ENDING -> {
                glowColor = Color.rgb(190, 71, 43);
                glowLevel = 0.22;
            }
            case WORLD_EVENT -> {
                glowColor = Color.rgb(158, 196, 130);
                glowLevel = 0.22;
            }
            case ECHO -> {
                glowColor = Color.rgb(206, 122, 74);
                glowLevel = 0.26;
            }
            default -> {
                glowColor = Color.rgb(218, 155, 58);
                glowLevel = 0.20;
            }
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

        PopupCategory closingCategory = activePopupCategory;

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

                showPopup("Legacy Title Gained", message, PopupCategory.LEGACY_TITLE);
                return;
            }

            /*
             * Show status change after legacy title.
             */
            if (!pendingStatusChangeMessage.isBlank()) {
                String message = pendingStatusChangeMessage;
                pendingStatusChangeMessage = "";

                showPopup("Status Changed", message, PopupCategory.STATUS_CHANGE);
                return;
            }

            /*
             * Announce a Thread of Fate earned by surviving a life stage.
             */
            if (!pendingFateTokenMessage.isBlank()) {
                String message = pendingFateTokenMessage;
                pendingFateTokenMessage = "";

                updateFateTokenLabel();
                showPopup("A Thread is Spun", message, PopupCategory.FATE_TOKEN);
                return;
            }

            /*
             * An echo of a choice made decades earlier finally coming due.
             * Shown ahead of world news, since it is personal.
             */
            if (!pendingEchoMessage.isBlank()) {
                String echoTitle = pendingEchoTitle;
                String message = pendingEchoMessage;
                pendingEchoTitle = "";
                pendingEchoMessage = "";

                showPopup(echoTitle, message, PopupCategory.ECHO);
                return;
            }

            /*
             * Ancient decrees about the wider world, unrelated to this choice.
             */
            if (!pendingWorldEventMessage.isBlank()) {
                String eventTitle = pendingWorldEventTitle;
                String message = pendingWorldEventMessage;
                pendingWorldEventTitle = "";
                pendingWorldEventMessage = "";

                showPopup(eventTitle, message, PopupCategory.WORLD_EVENT);
                return;
            }

            /*
             * Closing the final chronicle returns to the menu.
             */
            if (closingCategory == PopupCategory.ENDING) {
                activePopupTitle = "";

                SaveManager.clearSave();

                if (mainApp != null) {
                    mainApp.showWelcomeScreen();
                }

                return;
            }

            activePopupTitle = "";

            // Moving on accepts the consequence: the undo point is gone.
            rewindOfferAvailable = false;
            engine.clearSnapshot();

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
        SaveManager.clearSave();

        engine = new GameEngine();

        pendingLegacyTitleMessage = "";
        pendingStatusChangeMessage = "";
        pendingFateTokenMessage = "";
        pendingWorldEventTitle = "";
        pendingWorldEventMessage = "";
        pendingEchoTitle = "";
        pendingEchoMessage = "";

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
                engine.getBirthIntroMessage(),
                PopupCategory.BIRTH
        );
    }

    @FXML
    private void returnToMainMenu() {
        persistGameIfInProgress();

        if (mainApp != null) {
            mainApp.showWelcomeScreen();
        }
    }

    @FXML
    private void exitGame() {
        persistGameIfInProgress();

        if (mainApp != null) {
            mainApp.exitGame();
        }
    }

    /**
     * Saves the current run to the database so it can be resumed later,
     * as long as the game hasn't already reached its ending.
     */
    private void persistGameIfInProgress() {
        if (engine != null && !legacyRecorded) {
            SaveManager.saveGame(engine);
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