package com.example.al_mirath.ui;

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * The menu every long list in the game is shown through: activities, posts,
 * property, and the people in a life.
 *
 * <p>A scrolling list of rows with a name, a line of explanation, a price on
 * the right, and a reason printed on anything that cannot be taken. That last
 * part is the whole design: a greyed row reading "Not until you are 18" is a
 * thing to aim at, while a row that is simply absent teaches nothing and
 * leaves the player believing the game is smaller than it is.
 *
 * <p>Built in code rather than FXML because the contents are entirely
 * data-driven and change every year.
 */
public class ListMenuOverlay extends StackPane {

    /** The measure the rows were written for. */
    private static final double PANEL_WIDTH = 720;
    private static final double PANEL_MARGIN = 56;

    private final VBox listBox = new VBox(8);
    private final VBox panel = new VBox(14);
    private final Label subtitleLabel = new Label();

    private Runnable onClose;

    /**
     * One line of the menu.
     *
     * @param name     what it is called
     * @param detail   one line of what it is, or an empty string
     * @param trailing what sits on the right — a price, a wage, a standing
     * @param locked   why it cannot be taken, or an empty string when it can
     * @param action   run when the row is chosen; ignored while locked
     */
    public record Row(String name, String detail, String trailing,
                      String locked, Runnable action) {

        public static Row open(String name, String detail, String trailing, Runnable action) {
            return new Row(name, detail, trailing, "", action);
        }

        public static Row shut(String name, String detail, String trailing, String locked) {
            return new Row(name, detail, trailing, locked, null);
        }

        public boolean isOpen() {
            return locked == null || locked.isEmpty();
        }
    }

    public ListMenuOverlay(String title, String subtitle) {
        getStyleClass().add("menu-overlay");
        setAlignment(Pos.CENTER);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("menu-title");

        subtitleLabel.setText(subtitle == null ? "" : subtitle);
        subtitleLabel.getStyleClass().add("menu-subtitle");
        subtitleLabel.setWrapText(true);
        subtitleLabel.setVisible(!subtitleLabel.getText().isBlank());
        subtitleLabel.setManaged(subtitleLabel.isVisible());

        listBox.setFillWidth(true);

        ScrollPane scroll = new ScrollPane(listBox);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setPannable(true);
        scroll.getStyleClass().addAll("menu-scroll", "popup-scroll");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        Button close = new Button("Close");
        close.getStyleClass().addAll("scroll-popup-button", "continue-button");
        close.setMinWidth(Region.USE_PREF_SIZE);
        close.setPrefWidth(180);
        close.setOnAction(event -> dismiss());

        HBox footer = new HBox(close);
        footer.setAlignment(Pos.CENTER);

        panel.getStyleClass().addAll("menu-panel", "parchment-panel");
        panel.setAlignment(Pos.TOP_CENTER);
        panel.setPadding(new Insets(22, 26, 20, 26));
        panel.getChildren().addAll(titleLabel, subtitleLabel, scroll, footer);

        fitToWindow();

        getChildren().add(panel);

        // Clicking the dimmed area outside the panel closes it, which is what
        // everybody tries first.
        setOnMouseClicked(event -> {
            if (event.getTarget() == this) {
                dismiss();
            }
        });

        setOnKeyPressed(event -> {
            if (event.getCode().getName().equalsIgnoreCase("Esc")
                    || event.getCode().getName().equalsIgnoreCase("Escape")) {

                dismiss();
                event.consume();
            }
        });

        setFocusTraversable(true);

        setOpacity(0);

        FadeTransition fade = new FadeTransition(Duration.millis(170), this);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    /**
     * Keeps the panel inside the window on a small screen.
     *
     * <p>A fixed height would put the Close button off the bottom edge of a
     * 720-high window, which is the one control the menu cannot afford to
     * lose.
     */
    private void fitToWindow() {
        panel.maxWidthProperty().bind(
                javafx.beans.binding.Bindings.createDoubleBinding(
                        () -> {
                            double available = getWidth() - PANEL_MARGIN;
                            return available <= 0 ? PANEL_WIDTH : Math.min(PANEL_WIDTH, available);
                        },
                        widthProperty()
                )
        );

        panel.maxHeightProperty().bind(
                javafx.beans.binding.Bindings.createDoubleBinding(
                        () -> Math.max(220, getHeight() - PANEL_MARGIN),
                        heightProperty()
                )
        );
    }

    /** Called when the menu closes, however it was closed. */
    public ListMenuOverlay onClose(Runnable onClose) {
        this.onClose = onClose;
        return this;
    }

    /** A heading above the rows that follow it. */
    public ListMenuOverlay section(String heading) {
        Label label = new Label(heading);
        label.getStyleClass().add("menu-section");

        VBox.setMargin(label, new Insets(listBox.getChildren().isEmpty() ? 2 : 14, 0, 2, 0));
        listBox.getChildren().add(label);

        return this;
    }

    /** A line of plain text between rows, for when there is nothing to list. */
    public ListMenuOverlay note(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("menu-note");
        label.setWrapText(true);

        listBox.getChildren().add(label);

        return this;
    }

    public ListMenuOverlay add(Row row) {
        listBox.getChildren().add(buildRow(row));
        return this;
    }

    /**
     * One row.
     *
     * <p>A box rather than a Button on purpose. A Button lays its graphic out
     * at the graphic's own preferred size and never stretches it, so a row
     * built that way has to be told its width by a binding — and a wrapped
     * label asked for its height before that binding has fired computes the
     * height of a column one character wide. Every row came out six hundred
     * pixels tall. A box is sized by its parent, which is what a row wants.
     */
    private Node buildRow(Row row) {
        Label name = new Label(row.name());
        name.getStyleClass().add("menu-row-name");
        name.setWrapText(true);

        VBox text = new VBox(2, name);
        text.setFillWidth(true);

        if (row.detail() != null && !row.detail().isBlank()) {
            Label detail = new Label(row.detail());
            detail.getStyleClass().add("menu-row-detail");
            detail.setWrapText(true);
            text.getChildren().add(detail);
        }

        if (!row.isOpen()) {
            Label locked = new Label(row.locked());
            locked.getStyleClass().add("menu-row-locked");
            locked.setWrapText(true);
            text.getChildren().add(locked);
        }

        HBox.setHgrow(text, Priority.ALWAYS);

        HBox rowBox = new HBox(14, text);
        rowBox.setAlignment(Pos.CENTER_LEFT);
        rowBox.getStyleClass().add("menu-row");
        rowBox.setMaxWidth(Double.MAX_VALUE);
        rowBox.setMinHeight(Region.USE_PREF_SIZE);

        if (row.trailing() != null && !row.trailing().isBlank()) {
            Label trailing = new Label(row.trailing());
            trailing.getStyleClass().add("menu-row-trailing");
            trailing.setMinWidth(Region.USE_PREF_SIZE);
            trailing.setTextAlignment(javafx.scene.text.TextAlignment.RIGHT);

            rowBox.getChildren().add(trailing);
        }

        if (row.isOpen()) {
            rowBox.setOnMouseClicked(event -> {
                event.consume();

                if (row.action() != null) {
                    row.action().run();
                }
            });
        } else {
            rowBox.getStyleClass().add("menu-row-shut");
        }

        return rowBox;
    }

    /** Takes the menu off the screen and tells the caller. */
    public void dismiss() {
        FadeTransition fade = new FadeTransition(Duration.millis(150), this);
        fade.setFromValue(getOpacity());
        fade.setToValue(0);

        fade.setOnFinished(event -> {
            if (getParent() instanceof javafx.scene.layout.Pane parent) {
                parent.getChildren().remove(this);
            }

            if (onClose != null) {
                onClose.run();
            }
        });

        fade.play();
    }
}
