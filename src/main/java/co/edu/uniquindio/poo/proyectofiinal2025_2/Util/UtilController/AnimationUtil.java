package co.edu.uniquindio.poo.proyectofiinal2025_2.Util.UtilController;

import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.util.Duration;

/**
 * Utility class for reusable animation behaviors.
 */
public class AnimationUtil {

    /**
     * Adds a hover scale animation to a Node.
     *
     * @param node target Node
     * @param scale target scale value
     * @param durationMillis animation duration
     */
    public static void addHoverScale(Node node, double scale, int durationMillis) {
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(durationMillis), node);
        scaleIn.setToX(scale);
        scaleIn.setToY(scale);

        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(durationMillis), node);
        scaleOut.setToX(1.0);
        scaleOut.setToY(1.0);

        node.setOnMouseEntered(e -> scaleIn.play());
        node.setOnMouseExited(e -> scaleOut.play());
    }

    /**
     * Plays a slide (translate) animation on a node.
     *
     * @param node target Node
     * @param toX final X position
     * @param seconds animation duration in seconds
     */
    public static void slide(Node node, double toX, double seconds) {
        if (node == null) return;
        TranslateTransition slide = new TranslateTransition(Duration.seconds(seconds), node);
        slide.setToX(toX);
        slide.play();
    }
}
