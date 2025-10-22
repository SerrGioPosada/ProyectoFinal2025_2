package co.edu.uniquindio.poo.proyectofiinal2025_2.Util.UtilController;

import javafx.scene.control.Label;

public class MessageUtil {

    public static void showError(Label label, String message) {
        showMessage(label, message, "#ff6b6b");
    }

    public static void showSuccess(Label label, String message) {
        showMessage(label, message, "#51cf66");
    }

    private static void showMessage(Label label, String message, String color) {
        if (label == null) return;
        label.setText(message);
        label.setStyle("-fx-text-fill: " + color + ";");
        label.setVisible(true);
        label.setManaged(true);
    }

    public static void hide(Label label) {
        if (label == null) return;
        label.setVisible(false);
        label.setManaged(false);
    }
}
