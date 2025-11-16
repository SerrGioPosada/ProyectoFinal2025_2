package co.edu.uniquindio.poo.ProyectoFinal2025_2.Controller;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Admin;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Repositories.AdminRepository;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.NotificationService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.NotificationService.NotificationType;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for the Support view.
 * Handles user support requests and sends notifications to administrators.
 */
public class SupportController implements Initializable {

    @FXML
    private TextField txtName;

    @FXML
    private TextField txtEmail;

    @FXML
    private TextField txtSubject;

    @FXML
    private ComboBox<String> cmbCategory;

    @FXML
    private TextArea txtMessage;

    @FXML
    private Button btnSubmit;

    @FXML
    private Label lblMessage;

    private final NotificationService notificationService = NotificationService.getInstance();
    private final AdminRepository adminRepository = AdminRepository.getInstance();

    /**
     * Initializes the controller and sets up the category ComboBox.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize category ComboBox with predefined categories
        cmbCategory.setItems(FXCollections.observableArrayList(
            "Problema Técnico",
            "Consulta General",
            "Seguimiento de Envío",
            "Facturación",
            "Sugerencia",
            "Otro"
        ));
    }

    /**
     * Handles the submit button action.
     * Validates the form and sends notifications to all administrators.
     */
    @FXML
    private void handleSubmit() {
        Logger.info("[SupportController] Submit button clicked");

        // Validate form
        if (!validateForm()) {
            return;
        }

        // Get form data
        String name = txtName.getText().trim();
        String email = txtEmail.getText().trim();
        String subject = txtSubject.getText().trim();
        String category = cmbCategory.getValue();
        String message = txtMessage.getText().trim();

        // Create notification message
        String notificationMessage = String.format(
            "Nueva solicitud de soporte:\n" +
            "De: %s (%s)\n" +
            "Categoría: %s\n" +
            "Asunto: %s\n" +
            "Mensaje: %s",
            name, email, category, subject, message
        );

        // Send notification to all admins
        List<Admin> admins = adminRepository.getAdmins();
        int notificationsSent = 0;

        for (Admin admin : admins) {
            try {
                notificationService.addNotification(
                    admin.getId(),
                    "Solicitud de Soporte",
                    notificationMessage,
                    NotificationType.INFO
                );
                notificationsSent++;
                Logger.info("[SupportController] Notification sent to admin: " + admin.getEmail());
            } catch (Exception e) {
                Logger.error("[SupportController] Failed to send notification to admin: " + admin.getEmail(), e);
            }
        }

        Logger.info("[SupportController] Support request submitted. Notifications sent: " + notificationsSent);

        // Show success message
        showMessage("✓ Solicitud enviada exitosamente. Nuestro equipo te contactará pronto.", "success");

        // Clear form
        clearForm();
    }

    /**
     * Validates the support form.
     *
     * @return true if valid, false otherwise
     */
    private boolean validateForm() {
        // Validate name
        if (txtName.getText() == null || txtName.getText().trim().isEmpty()) {
            showMessage("⚠ Por favor ingresa tu nombre", "error");
            txtName.requestFocus();
            return false;
        }

        // Validate email
        String email = txtEmail.getText();
        if (email == null || email.trim().isEmpty()) {
            showMessage("⚠ Por favor ingresa tu correo electrónico", "error");
            txtEmail.requestFocus();
            return false;
        }

        if (!isValidEmail(email)) {
            showMessage("⚠ Por favor ingresa un correo electrónico válido", "error");
            txtEmail.requestFocus();
            return false;
        }

        // Validate subject
        if (txtSubject.getText() == null || txtSubject.getText().trim().isEmpty()) {
            showMessage("⚠ Por favor ingresa un asunto", "error");
            txtSubject.requestFocus();
            return false;
        }

        // Validate category
        if (cmbCategory.getValue() == null || cmbCategory.getValue().isEmpty()) {
            showMessage("⚠ Por favor selecciona una categoría", "error");
            cmbCategory.requestFocus();
            return false;
        }

        // Validate message
        if (txtMessage.getText() == null || txtMessage.getText().trim().isEmpty()) {
            showMessage("⚠ Por favor ingresa un mensaje", "error");
            txtMessage.requestFocus();
            return false;
        }

        if (txtMessage.getText().trim().length() < 10) {
            showMessage("⚠ El mensaje debe tener al menos 10 caracteres", "error");
            txtMessage.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Validates email format.
     *
     * @param email the email to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }

    /**
     * Shows a message to the user.
     *
     * @param message the message text
     * @param type    "success" or "error"
     */
    private void showMessage(String message, String type) {
        lblMessage.setText(message);
        lblMessage.getStyleClass().clear();
        lblMessage.getStyleClass().addAll("form-message", "form-message-" + type);
        lblMessage.setVisible(true);
        lblMessage.setManaged(true);

        // Hide message after 5 seconds
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                javafx.application.Platform.runLater(() -> {
                    lblMessage.setVisible(false);
                    lblMessage.setManaged(false);
                });
            } catch (InterruptedException e) {
                Logger.error("[SupportController] Error hiding message", e);
            }
        }).start();
    }

    /**
     * Clears the form fields.
     */
    private void clearForm() {
        txtName.clear();
        txtEmail.clear();
        txtSubject.clear();
        cmbCategory.setValue(null);
        txtMessage.clear();
    }
}
