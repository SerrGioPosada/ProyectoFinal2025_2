package co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for displaying temporary messages in the UI.
 * <p>
 * Features:
 * <ul>
 *     <li>Auto-hiding messages after a configurable duration</li>
 *     <li>Smooth fade-in and fade-out animations</li>
 *     <li>Cancellable timers to prevent message overlap</li>
 *     <li>Support for error, success, and custom colored messages</li>
 * </ul>
 * </p>
 */
public class MessageUtil {

    private static final double AUTO_HIDE_DURATION_SECONDS = 3.5;
    private static final double FADE_DURATION_MILLIS = 300;

    // Track active timers for each label to cancel them if a new message is shown
    private static final Map<Label, PauseTransition> activeTimers = new HashMap<>();

    /**
     * Shows an error message with auto-hide functionality.
     * The message will automatically fade out after 3.5 seconds.
     *
     * @param label   The label to display the error message in
     * @param message The error message text
     */
    public static void showError(Label label, String message) {
        showMessage(label, message, "#ff6b6b", true);
    }

    /**
     * Shows an error message without auto-hide.
     * Useful for persistent validation errors that should remain visible.
     *
     * @param label   The label to display the error message in
     * @param message The error message text
     */
    public static void showErrorPersistent(Label label, String message) {
        showMessage(label, message, "#ff6b6b", false);
    }

    /**
     * Shows a success message with auto-hide functionality.
     * The message will automatically fade out after 3.5 seconds.
     *
     * @param label   The label to display the success message in
     * @param message The success message text
     */
    public static void showSuccess(Label label, String message) {
        showMessage(label, message, "#51cf66", true);
    }

    /**
     * Shows a message with optional auto-hide functionality.
     *
     * @param label    The label to display the message in
     * @param message  The message text
     * @param color    The text color (hex format, e.g., "#ff6b6b")
     * @param autoHide Whether the message should auto-hide after a delay
     */
    private static void showMessage(Label label, String message, String color, boolean autoHide) {
        if (label == null) return;

        // Cancel any existing timer for this label
        cancelTimer(label);

        // Set the message and style
        label.setText(message);
        label.setStyle("-fx-text-fill: " + color + ";");

        // IMPORTANT: We need BOTH visible=true AND managed=true for the label to render
        // But we set maxHeight to control the space it takes
        label.setVisible(true);
        label.setManaged(true);
        label.setMaxHeight(Double.MAX_VALUE); // Allow it to take its natural height

        // Only animate if the label was previously hidden
        if (label.getOpacity() < 0.5) {
            // Fade in animation
            FadeTransition fadeIn = new FadeTransition(Duration.millis(FADE_DURATION_MILLIS), label);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        } else {
            // Already visible, just ensure full opacity
            label.setOpacity(1.0);
        }

        // Set up auto-hide if requested
        if (autoHide) {
            PauseTransition pause = new PauseTransition(Duration.seconds(AUTO_HIDE_DURATION_SECONDS));
            pause.setOnFinished(event -> hideWithAnimation(label));
            pause.play();

            // Store the timer so we can cancel it later if needed
            activeTimers.put(label, pause);
        }
    }

    /**
     * Hides a label immediately without animation.
     *
     * @param label The label to hide
     */
    public static void hide(Label label) {
        if (label == null) return;

        // Cancel any active timer
        cancelTimer(label);

        label.setVisible(false);
        label.setManaged(false);
        label.setMaxHeight(0);
        label.setOpacity(1.0); // Reset opacity for next show
    }

    /**
     * Hides a label with a smooth fade-out animation.
     *
     * @param label The label to hide
     */
    private static void hideWithAnimation(Label label) {
        if (label == null || !label.isVisible()) return;

        FadeTransition fadeOut = new FadeTransition(Duration.millis(FADE_DURATION_MILLIS), label);
        fadeOut.setFromValue(label.getOpacity());
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(event -> {
            label.setVisible(false);
            label.setManaged(false);
            label.setMaxHeight(0);
            label.setOpacity(1.0); // Reset opacity for next show
        });
        fadeOut.play();
    }

    /**
     * Cancels any active auto-hide timer for the given label.
     *
     * @param label The label whose timer should be cancelled
     */
    private static void cancelTimer(Label label) {
        PauseTransition timer = activeTimers.remove(label);
        if (timer != null) {
            timer.stop();
        }
    }
}
