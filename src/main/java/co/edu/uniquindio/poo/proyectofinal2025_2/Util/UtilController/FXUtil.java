package co.edu.uniquindio.poo.proyectofinal2025_2.Util.UtilController;

import co.edu.uniquindio.poo.proyectofinal2025_2.Util.UtilModel.Logger;
import javafx.beans.property.BooleanProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.IOException;

/**
 * Utility class for JavaFX helpers: FXML loading and UI helpers like password toggles.
 */
public class FXUtil {

    // ========================================
    // Password Toggle Methods
    // ========================================

    /**
     * Sets up a toggle between a PasswordField and a visible TextField.
     *
     * @param passwordField The hidden password field.
     * @param visibleField  The visible text field.
     * @param toggleIcon    The ImageView used as toggle button.
     * @param visibilityFlag A BooleanProperty to track visibility state.
     * @param resourceClass Class used to load icon resources.
     */
    public static void setupPasswordToggle(PasswordField passwordField, TextField visibleField, ImageView toggleIcon,
                                           BooleanProperty visibilityFlag, Class<?> resourceClass) {
        passwordField.textProperty().bindBidirectional(visibleField.textProperty());
        updateToggleIcon(toggleIcon, visibilityFlag.get(), resourceClass);

        toggleIcon.setOnMouseClicked(event -> {
            visibilityFlag.set(!visibilityFlag.get());
            visibleField.setVisible(visibilityFlag.get());
            visibleField.setManaged(visibilityFlag.get());
            passwordField.setVisible(!visibilityFlag.get());
            passwordField.setManaged(!visibilityFlag.get());
            updateToggleIcon(toggleIcon, visibilityFlag.get(), resourceClass);
        });
    }

    /**
     * Updates the toggle icon based on visibility state.
     */
    private static void updateToggleIcon(ImageView iconView, boolean isVisible, Class<?> resourceClass) {
        String iconPath = isVisible
                ? "/co/edu/uniquindio/poo/proyectofinal2025_2/Images/eye-open.png"
                : "/co/edu/uniquindio/poo/proyectofinal2025_2/Images/eye-closed.png";
        try {
            iconView.setImage(new Image(resourceClass.getResourceAsStream(iconPath)));
        } catch (Exception e) {
            Logger.error("Error loading toggle icon: " + e.getMessage(), e);
        }
    }

    // ========================================
    // FXML Loading Methods
    // ========================================

    /**
     * Loads an FXML safely and returns its root node.
     *
     * @param fxmlPath      The path to the FXML file (starting with '/').
     * @param resourceClass The class used as resource reference.
     * @return Parent node if successful, null otherwise.
     */
    public static Parent loadViewSafe(String fxmlPath, Class<?> resourceClass) {
        try {
            FXMLLoader loader = new FXMLLoader(resourceClass.getResource(fxmlPath));
            return loader.load();
        } catch (IOException e) {
            Logger.error("Error loading FXML '" + fxmlPath + "': " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Loads an FXML safely and injects an external controller.
     *
     * @param fxmlPath      The path to the FXML file (starting with '/').
     * @param resourceClass The class used as resource reference.
     * @param controller    External controller to inject.
     * @return Parent node if successful, null otherwise.
     */
    public static Parent loadViewSafe(String fxmlPath, Class<?> resourceClass, Object controller) {
        try {
            FXMLLoader loader = new FXMLLoader(resourceClass.getResource(fxmlPath));
            loader.setController(controller);
            return loader.load();
        } catch (IOException e) {
            Logger.error("Error loading FXML '" + fxmlPath + "': " + e.getMessage(), e);
            return null;
        }
    }
}
