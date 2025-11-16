package co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController;

import javafx.animation.TranslateTransition;
import javafx.scene.Cursor;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * Custom theme toggle switch with sun/moon animation.
 * Professional sliding toggle similar to iOS switches.
 */
public class ThemeToggleSwitch extends Pane {

    private static final double WIDTH = 60;
    private static final double HEIGHT = 28;
    private static final double CIRCLE_RADIUS = 12;
    private static final double PADDING = 2;

    private final Rectangle background;
    private final Circle thumb;
    private final Text sunIcon;
    private final Text moonIcon;

    private boolean isDarkMode = false;
    private Runnable onToggle;

    public ThemeToggleSwitch() {
        // Background track
        background = new Rectangle(WIDTH, HEIGHT);
        background.setArcWidth(HEIGHT);
        background.setArcHeight(HEIGHT);
        background.setFill(Color.web("#e0e0e0")); // Light gray for light mode
        background.setStroke(Color.web("#bdbdbd"));
        background.setStrokeWidth(1);

        // Sun icon (☀)
        sunIcon = new Text("☀");
        sunIcon.setFont(Font.font(14));
        sunIcon.setFill(Color.web("#FDB813")); // Golden yellow
        sunIcon.setLayoutX(8);
        sunIcon.setLayoutY(19);

        // Moon icon (🌙)
        moonIcon = new Text("🌙");
        moonIcon.setFont(Font.font(14));
        moonIcon.setLayoutX(WIDTH - 22);
        moonIcon.setLayoutY(19);
        moonIcon.setOpacity(0.3); // Dimmed when not active

        // Thumb (sliding circle)
        thumb = new Circle(CIRCLE_RADIUS);
        thumb.setFill(Color.WHITE);
        thumb.setStroke(Color.web("#bdbdbd"));
        thumb.setStrokeWidth(1);
        thumb.setCenterX(PADDING + CIRCLE_RADIUS);
        thumb.setCenterY(HEIGHT / 2);

        // Add drop shadow to thumb
        thumb.setEffect(new javafx.scene.effect.DropShadow(
            3, 0, 1, Color.rgb(0, 0, 0, 0.3)
        ));

        // Add all elements
        getChildren().addAll(background, sunIcon, moonIcon, thumb);

        // Set preferred size
        setPrefSize(WIDTH, HEIGHT);
        setMaxSize(WIDTH, HEIGHT);
        setMinSize(WIDTH, HEIGHT);

        // Add hover cursor
        setCursor(Cursor.HAND);

        // Handle click
        setOnMouseClicked(event -> toggle());
    }

    /**
     * Toggles the switch state with animation.
     */
    public void toggle() {
        isDarkMode = !isDarkMode;
        animateToggle();

        if (onToggle != null) {
            onToggle.run();
        }
    }

    /**
     * Sets the toggle state without animation (for initialization).
     */
    public void setState(boolean darkMode) {
        this.isDarkMode = darkMode;
        updateStateInstant();
    }

    /**
     * Sets the callback to run when toggled.
     */
    public void setOnToggle(Runnable onToggle) {
        this.onToggle = onToggle;
    }

    /**
     * Animates the toggle switch.
     */
    private void animateToggle() {
        // Calculate target position
        double targetX = isDarkMode
            ? WIDTH - PADDING - CIRCLE_RADIUS
            : PADDING + CIRCLE_RADIUS;

        // Animate thumb position
        TranslateTransition thumbTransition = new TranslateTransition(
            Duration.millis(200), thumb
        );
        thumbTransition.setToX(targetX - thumb.getCenterX());
        thumbTransition.setInterpolator(javafx.animation.Interpolator.EASE_BOTH);

        // Animate background color
        javafx.animation.Timeline colorTransition = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(
                Duration.millis(200),
                new javafx.animation.KeyValue(
                    background.fillProperty(),
                    isDarkMode ? Color.web("#424242") : Color.web("#e0e0e0")
                )
            )
        );

        // Animate icon opacity
        javafx.animation.Timeline iconTransition = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(
                Duration.millis(200),
                new javafx.animation.KeyValue(sunIcon.opacityProperty(), isDarkMode ? 0.3 : 1.0),
                new javafx.animation.KeyValue(moonIcon.opacityProperty(), isDarkMode ? 1.0 : 0.3)
            )
        );

        // Play all animations
        thumbTransition.play();
        colorTransition.play();
        iconTransition.play();
    }

    /**
     * Updates state instantly without animation (for initialization).
     */
    private void updateStateInstant() {
        double targetX = isDarkMode
            ? WIDTH - PADDING - CIRCLE_RADIUS
            : PADDING + CIRCLE_RADIUS;

        thumb.setTranslateX(targetX - thumb.getCenterX());
        background.setFill(isDarkMode ? Color.web("#424242") : Color.web("#e0e0e0"));
        sunIcon.setOpacity(isDarkMode ? 0.3 : 1.0);
        moonIcon.setOpacity(isDarkMode ? 1.0 : 0.3);
    }

    /**
     * Returns the current state.
     */
    public boolean isDarkMode() {
        return isDarkMode;
    }
}
