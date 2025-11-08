package co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.AuthenticablePerson;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Person;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.geometry.Pos;
import javafx.scene.image.ImageView;

import java.io.InputStream;
import java.util.Optional;

/**
 * Utility class that centralizes dialog display logic (alerts, confirmations)
 * and reusable UI helpers such as image loading and status badges.
 * <p>
 * Prevents repetitive alert and image-handling code across controllers.
 * </p>
 */
public final class DialogUtil {

    // Prevent instantiation
    private DialogUtil() {}

    // =================================================================================================================
    // Dialog Methods
    // =================================================================================================================

    public static void showSuccess(String message) {
        showAlert(Alert.AlertType.INFORMATION, "Success", null, message);
    }

    public static void showError(String message) {
        showAlert(Alert.AlertType.ERROR, "Error", null, message);
    }

    public static void showInfo(String title, String message) {
        showAlert(Alert.AlertType.INFORMATION, title, null, message);
    }

    public static void showWarning(String title, String message) {
        showAlert(Alert.AlertType.WARNING, title, null, message);
    }

    public static void showError(String title, String message) {
        showAlert(Alert.AlertType.ERROR, title, null, message);
    }

    public static void showSuccess(String title, String message) {
        showAlert(Alert.AlertType.INFORMATION, title, null, message);
    }

    public static boolean showConfirmation(String title, String message) {
        return showConfirmation(title, null, message);
    }

    public static boolean showConfirmation(String title, String header, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    public static boolean showWarningConfirmation(String title, String header, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);

        ButtonType confirm = new ButtonType("Confirm", ButtonType.OK.getButtonData());
        ButtonType cancel = new ButtonType("Cancel", ButtonType.CANCEL.getButtonData());
        alert.getButtonTypes().setAll(confirm, cancel);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == confirm;
    }

    private static void showAlert(Alert.AlertType type, String title, String header, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Muestra un diálogo de perfil para un usuario dado, usando una imagen por defecto si es necesario.
     * <p>
     * La imagen del perfil se ajusta a los parámetros de ancho y alto proporcionados.
     *
     * @param person El usuario cuyo perfil se mostrará.
     * @param defaultImage Imagen por defecto si el usuario no tiene foto.
     * @param width Ancho de la imagen en el diálogo.
     * @param height Alto de la imagen en el diálogo.
     */
    public static void showProfileDialog(Person person, Image defaultImage, double width, double height) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información de Perfil");
        alert.setHeaderText(null);

        // Load profile image - use loadFromFile for file system paths
        Image profileImage = defaultImage;
        if (person instanceof AuthenticablePerson authPerson && authPerson.getProfileImagePath() != null) {
            Image loadedImage = ImageUtil.loadFromFile(authPerson.getProfileImagePath());
            if (loadedImage != null) {
                profileImage = loadedImage;
            }
        }

        ImageView imgView = new ImageView(profileImage);
        imgView.setFitWidth(width);
        imgView.setFitHeight(height);
        imgView.setPreserveRatio(true);

        // Apply circular clip to profile image
        ImageUtil.applyCircularClip(imgView, width / 2);

        alert.setGraphic(imgView);

        if (person == null) {
            alert.setContentText("No hay usuario autenticado.");
        } else {
            alert.setContentText("Nombre: " + person.getName() + " " + person.getLastName() +
                               "\nCorreo: " + person.getEmail());
        }

        alert.showAndWait();
    }


    // =================================================================================================================
    // UI Helpers
    // =================================================================================================================

    /**
     * Loads a user image from the given path, falling back to a default image if necessary.
     *
     * @param imagePath The file path or resource path to the image.
     * @param width     Desired width.
     * @param height    Desired height.
     * @return The loaded {@link Image}, or {@code null} if loading failed.
     */
    public static Image loadUserImage(String imagePath, double width, double height) {
        try {
            if (imagePath != null && !imagePath.isEmpty()) {
                Image image = new Image("file:" + imagePath, width, height, true, true);
                if (!image.isError()) return image;
            }
            InputStream defaultStream = DialogUtil.class.getResourceAsStream(
                    "/co/edu/uniquindio/poo/ProyectoFinal2025_2/Images/default-userImage.png"
            );
            return (defaultStream != null) ? new Image(defaultStream, width, height, true, true) : null;
        } catch (Exception e) {
            Logger.error("[DialogUtil] Failed to load user image: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Creates a styled badge label representing a user's active/inactive status.
     *
     * @param active Whether the user is active.
     * @return A {@link Label} styled as a colored badge.
     */
    public static Label createStatusBadge(boolean active) {
        Label badge = new Label(active ? "Activo" : "Inactivo");

        // Color de fondo y texto según el estado
        String style = active
                ? "-fx-background-color: #d4edda; -fx-text-fill: #155724;"
                : "-fx-background-color: #f8d7da; -fx-text-fill: #721c24;";

        // Estilo general para que quede como badge
        style += " -fx-padding: 4 10; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold;";
        badge.setStyle(style);

        // Importante: quitamos el clip, el badge será sólido y el texto centrado
        badge.setMinWidth(Label.USE_PREF_SIZE);
        badge.setPrefHeight(Label.USE_COMPUTED_SIZE);
        badge.setAlignment(Pos.CENTER);

        return badge;
    }

}
