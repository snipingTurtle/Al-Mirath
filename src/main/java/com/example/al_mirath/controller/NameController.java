package com.example.al_mirath.controller;

import com.example.al_mirath.Main;
import com.example.al_mirath.model.CityProfile;
import com.example.al_mirath.model.LifeStart;
import com.example.al_mirath.service.BackgroundLibrary;
import com.example.al_mirath.service.CharacterGenerator;
import com.example.al_mirath.service.Eras;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

/**
 * What the player decides about a life before living it.
 *
 * <p>The household, the station and the temperament stay rolled, because a
 * run is supposed to hand you a person rather than let you build one. Three
 * things are the player's: the name, which goes into the chronicle and onto
 * the house that outlives them, and — now that a life is pinned to a real
 * year — when and where it begins.
 *
 * <p>All three default to leaving it to the roll, so a player who just wants
 * to play presses Begin and gets exactly what they always got. A player who
 * wants to be in Baghdad for 1258, or Cairo for the plague, can say so.
 */
public class NameController implements ScreenLifecycle {

    private Main mainApp;

    /** Only here to roll names for the button that asks for one. */
    private final CharacterGenerator generator = new CharacterGenerator();

    @FXML private StackPane nameRoot;
    @FXML private ImageView nameBackground;
    @FXML private Rectangle nameOverlay;
    @FXML private Label nameNoteLabel;
    @FXML private TextField nameField;
    @FXML private Button beginButton;
    @FXML private Button rollNameButton;

    @FXML private ComboBox<String> eraChoice;
    @FXML private Spinner<Integer> yearChoice;
    @FXML private Label yearLabel;
    @FXML private ComboBox<String> cityChoice;

    /** What the era and city boxes say when the player has chosen nothing. */
    private static final String LET_FATE_DECIDE = "Let fate decide";

    @FXML
    public void initialize() {
        bindBackground();
        loadBackground();
        buildWhenAndWhere();

        // Opening on a rolled name means a player who just wants to play can
        // press Begin without inventing anything, and a player who does want
        // to name themselves has an example of the shape names take here.
        if (nameField != null) {
            nameField.setText(generator.randomName());

            Platform.runLater(() -> {
                nameField.requestFocus();
                nameField.selectAll();
            });
        }

        System.out.println("NameController initialized.");
    }

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    /**
     * Fills the three boxes and keeps them honest with each other.
     *
     * <p>They are dependent in one direction: the era decides which years are
     * possible, and the year decides which cities existed to be born in. Pick
     * the Abbasids and the year box offers 762 to 1233; move it back before
     * 1453 and Istanbul stops being on the list, because it was not there.
     */
    private void buildWhenAndWhere() {
        if (eraChoice == null || yearChoice == null || cityChoice == null) {
            return;
        }

        eraChoice.setItems(FXCollections.observableArrayList(LET_FATE_DECIDE));
        eraChoice.getItems().addAll(Eras.all());
        eraChoice.getSelectionModel().select(LET_FATE_DECIDE);

        yearChoice.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 0, 0));

        eraChoice.valueProperty().addListener(
                (observable, wasChosen, isChosen) -> eraChosen(isChosen));

        yearChoice.valueProperty().addListener(
                (observable, wasYear, isYear) -> fillCities());

        eraChosen(LET_FATE_DECIDE);
    }

    /** Re-ranges the year box for an era, and re-fills the cities under it. */
    private void eraChosen(String era) {
        boolean rolled = era == null || era.equals(LET_FATE_DECIDE);

        yearChoice.setDisable(rolled);
        cityChoice.setDisable(rolled);

        if (rolled) {
            yearChoice.setValueFactory(
                    new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 0, 0));

            yearLabel.setText("Year");

            cityChoice.setItems(FXCollections.observableArrayList(LET_FATE_DECIDE));
            cityChoice.getSelectionModel().select(LET_FATE_DECIDE);

            return;
        }

        int first = Eras.startOf(era);

        // The whole era, not the range the roll uses. Rolling stops short of
        // the end so a rolled life has years in front of it; a player who
        // deliberately asks to be born in 1250 wants the eight years to 1258,
        // and it is not the game's business to talk them out of it.
        int last = Eras.endOf(era);

        yearLabel.setText("Year (" + first + "-" + last + ")");

        yearChoice.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(first, last, first));

        fillCities();
    }

    /** The cities of the chosen era that were there in the chosen year. */
    private void fillCities() {
        if (cityChoice == null || eraChoice == null) {
            return;
        }

        String era = eraChoice.getValue();

        if (era == null || era.equals(LET_FATE_DECIDE)) {
            return;
        }

        String wasChosen = cityChoice.getValue();
        int year = yearChoice.getValue() == null ? Eras.startOf(era) : yearChoice.getValue();

        cityChoice.setItems(FXCollections.observableArrayList(LET_FATE_DECIDE));

        for (CityProfile profile : Eras.citiesIn(era, year)) {
            cityChoice.getItems().add(profile.name());
        }

        // Keep the player's city if that year still has one, rather than
        // silently resetting every time they nudge the year.
        if (wasChosen != null && cityChoice.getItems().contains(wasChosen)) {
            cityChoice.getSelectionModel().select(wasChosen);
        } else {
            cityChoice.getSelectionModel().select(LET_FATE_DECIDE);
        }
    }

    /** When and where the player asked to be born, if they asked at all. */
    public LifeStart chosenStart() {
        if (eraChoice == null || eraChoice.getValue() == null
                || eraChoice.getValue().equals(LET_FATE_DECIDE)) {

            return LifeStart.rolled();
        }

        String era = eraChoice.getValue();

        int year = yearChoice == null || yearChoice.getValue() == null
                ? 0
                : yearChoice.getValue();

        String city = cityChoice == null
                || cityChoice.getValue() == null
                || cityChoice.getValue().equals(LET_FATE_DECIDE)
                ? null
                : cityChoice.getValue();

        return new LifeStart(era, year, city);
    }

    private void bindBackground() {
        if (nameRoot != null && nameBackground != null) {
            nameBackground.fitWidthProperty().bind(nameRoot.widthProperty());
            nameBackground.fitHeightProperty().bind(nameRoot.heightProperty());
            nameBackground.setPreserveRatio(false);
            nameBackground.setSmooth(true);
        }

        if (nameRoot != null && nameOverlay != null) {
            nameOverlay.widthProperty().bind(nameRoot.widthProperty());
            nameOverlay.heightProperty().bind(nameRoot.heightProperty());
        }
    }

    private void loadBackground() {
        if (nameBackground == null) {
            return;
        }

        try {
            String path = BackgroundLibrary.getMenuBackground();

            if (path == null || path.isBlank()) {
                return;
            }

            nameBackground.setImage(
                    new Image(getClass().getResource(path).toExternalForm(), true)
            );

        } catch (Exception e) {
            System.out.println("Failed to load naming background.");
        }
    }

    /** What the player typed, or a rolled name if they typed nothing usable. */
    public String chosenName() {
        return generator.nameOrRandom(nameField == null ? null : nameField.getText());
    }

    @FXML
    private void rollAName() {
        if (nameField != null) {
            nameField.setText(generator.randomName());
            nameField.selectAll();
        }
    }

    @FXML
    private void beginLife() {
        if (mainApp == null) {
            System.out.println("ERROR: mainApp is null in NameController.");
            return;
        }

        mainApp.showNewLife(chosenName(), chosenStart());
    }

    @FXML
    private void backToMenu() {
        if (mainApp != null) {
            mainApp.showWelcomeScreen();
        }
    }

    @Override
    public void dispose() {
        // No timelines here; the screen is still a ScreenLifecycle so Main can
        // treat every screen the same way.
    }
}
