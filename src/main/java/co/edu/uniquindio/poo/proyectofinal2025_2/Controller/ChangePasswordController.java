package co.edu.uniquindio.poo.proyectofinal2025_2.Controller;

import co.edu.uniquindio.poo.proyectofinal2025_2.Model.AuthenticablePerson;
import co.edu.uniquindio.poo.proyectofinal2025_2.Services.AuthenticationService;
import co.edu.uniquindio.poo.proyectofinal2025_2.Util.UtilController.DialogUtil;
import co.edu.uniquindio.poo.proyectofinal2025_2.Util.UtilModel.Logger;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the Change Password view (ChangePassword.fxml).
 * <p>
 * Handles password change functionality with validation and security checks.
 * </p>
 */
public class ChangePasswordController implements Initializable {

    // =================================================================================================================
    // FXML Fields
    // =================================================================================================================

    @FXML
    private PasswordField txtCurrentPassword;
    @FXML
    private PasswordField txtNewPassword;
    @FXML
    private PasswordField txtConfirmPassword;

    @FXML
    private Label lblCurrentPasswordError;
    @FXML
    private Label lblNewPasswordError;
    @FXML
    private Label lblConfirmPasswordError;

    @FXML
    private ProgressBar passwordStrengthBar;
    @FXML
    private Label lblPasswordStrength;

    // =================================================================================================================
    // Services
    // =================================================================================================================

    private final AuthenticationService authService = AuthenticationService.getInstance();
    private AuthenticablePerson currentUser;
    private IndexController indexController;

    // =================================================================================================================
    // Initialization
    // =================================================================================================================

    /**
     * Initializes the controller and sets up password strength listener.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        currentUser = authService.getAuthenticatedUser();

        if (currentUser == null) {
            Logger.error("No user logged in. Cannot change password.");
            DialogUtil.showError("Error", "No hay usuario autenticado");
            return;
        }

        setupPasswordStrengthListener();
        Logger.info("ChangePasswordController initialized for user: " + currentUser.getId());
    }

    // =================================================================================================================
    // Setup Methods
    // =================================================================================================================

    /**
     * Sets up a listener on the new password field to update the strength indicator.
     */
    private void setupPasswordStrengthListener() {
        txtNewPassword.textProperty().addListener((observable, oldValue, newValue) -> {
            updatePasswordStrength(newValue);
        });
    }

    // =================================================================================================================
    // Event Handlers
    // =================================================================================================================

    /**
     * Handles the Save button click.
     * Validates inputs and changes the password if all checks pass.
     */
    @FXML
    private void handleSave() {
        clearErrors();

        if (!validateInputs()) {
            return;
        }

        // Verify current password
        if (!BCrypt.checkpw(txtCurrentPassword.getText(), currentUser.getPassword())) {
            showError(lblCurrentPasswordError, "La contrasena actual es incorrecta");
            return;
        }

        // Check if new password is different from current
        if (BCrypt.checkpw(txtNewPassword.getText(), currentUser.getPassword())) {
            showError(lblNewPasswordError, "La nueva contrasena debe ser diferente a la actual");
            return;
        }

        // Update password
        try {
            String newPasswordHash = BCrypt.hashpw(txtNewPassword.getText(), BCrypt.gensalt());
            currentUser.setPassword(newPasswordHash);

            // TODO: Save to repository using UserService when available
            // userService.updateUser(currentUser);

            DialogUtil.showSuccess("Exito", "Contrasena actualizada correctamente");
            Logger.info("Password changed for user: " + currentUser.getId());

            // Navigate back
            navigateBack();

        } catch (Exception e) {
            Logger.error("Failed to change password: " + e.getMessage());
            DialogUtil.showError("Error", "No se pudo cambiar la contrasena: " + e.getMessage());
        }
    }

    /**
     * Handles the Cancel button click.
     * Closes the window without saving changes.
     */
    @FXML
    private void handleCancel() {
        Logger.info("Password change cancelled by user");
        navigateBack();
    }

    /**
     * Handles the Back button click.
     */
    @FXML
    private void handleBack() {
        Logger.info("Back button clicked from ChangePassword");
        navigateBack();
    }

    /**
     * Sets the IndexController reference for navigation.
     */
    public void setIndexController(IndexController indexController) {
        this.indexController = indexController;
    }

    // =================================================================================================================
    // Validation Methods
    // =================================================================================================================

    /**
     * Validates all input fields.
     *
     * @return true if all validations pass, false otherwise
     */
    private boolean validateInputs() {
        boolean isValid = true;

        // Validate current password
        if (txtCurrentPassword.getText().trim().isEmpty()) {
            showError(lblCurrentPasswordError, "Ingresa tu contraseña actual");
            isValid = false;
        }

        // Validate new password
        String newPassword = txtNewPassword.getText();
        if (newPassword.trim().isEmpty()) {
            showError(lblNewPasswordError, "Ingresa una nueva contraseña");
            isValid = false;
        } else if (!isPasswordStrong(newPassword)) {
            showError(lblNewPasswordError, "La contraseña no cumple con los requisitos mínimos");
            isValid = false;
        }

        // Validate confirm password
        String confirmPassword = txtConfirmPassword.getText();
        if (confirmPassword.trim().isEmpty()) {
            showError(lblConfirmPasswordError, "Confirma tu nueva contraseña");
            isValid = false;
        } else if (!newPassword.equals(confirmPassword)) {
            showError(lblConfirmPasswordError, "Las contraseñas no coinciden");
            isValid = false;
        }

        return isValid;
    }

    /**
     * Checks if a password meets the strength requirements.
     *
     * @param password The password to check
     * @return true if password is strong enough, false otherwise
     */
    private boolean isPasswordStrong(String password) {
        if (password.length() < 8) return false;
        if (!password.matches(".*[A-Z].*")) return false; // At least one uppercase
        if (!password.matches(".*[a-z].*")) return false; // At least one lowercase
        if (!password.matches(".*\\d.*")) return false;    // At least one digit
        return true;
    }

    /**
     * Calculates password strength score (0-100).
     *
     * @param password The password to evaluate
     * @return Strength score from 0 to 100
     */
    private int calculatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) return 0;

        int score = 0;

        // Length scoring
        if (password.length() >= 8) score += 25;
        if (password.length() >= 12) score += 15;
        if (password.length() >= 16) score += 10;

        // Character variety scoring
        if (password.matches(".*[a-z].*")) score += 15; // Lowercase
        if (password.matches(".*[A-Z].*")) score += 15; // Uppercase
        if (password.matches(".*\\d.*")) score += 15;    // Digits
        if (password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) score += 15; // Special chars

        return Math.min(score, 100);
    }

    /**
     * Updates the password strength indicator based on the given password.
     *
     * @param password The password to evaluate
     */
    private void updatePasswordStrength(String password) {
        int strength = calculatePasswordStrength(password);
        double progress = strength / 100.0;

        passwordStrengthBar.setProgress(progress);

        // Update label and color
        if (strength == 0) {
            lblPasswordStrength.setText("");
            passwordStrengthBar.setStyle("-fx-accent: gray;");
        } else if (strength < 40) {
            lblPasswordStrength.setText("Débil");
            passwordStrengthBar.setStyle("-fx-accent: #ff6b6b;");
        } else if (strength < 70) {
            lblPasswordStrength.setText("Media");
            passwordStrengthBar.setStyle("-fx-accent: #ffa500;");
        } else {
            lblPasswordStrength.setText("Fuerte");
            passwordStrengthBar.setStyle("-fx-accent: #51cf66;");
        }
    }

    // =================================================================================================================
    // Helper Methods
    // =================================================================================================================

    /**
     * Shows an error message on a specific label.
     *
     * @param label   The label to display the error on
     * @param message The error message
     */
    private void showError(Label label, String message) {
        label.setText(message);
        label.setVisible(true);
        label.setManaged(true);
    }

    /**
     * Clears all error messages.
     */
    private void clearErrors() {
        lblCurrentPasswordError.setVisible(false);
        lblCurrentPasswordError.setManaged(false);
        lblNewPasswordError.setVisible(false);
        lblNewPasswordError.setManaged(false);
        lblConfirmPasswordError.setVisible(false);
        lblConfirmPasswordError.setManaged(false);
    }

    /**
     * Closes the current window.
     */
    private void closeWindow() {
        Stage stage = (Stage) txtCurrentPassword.getScene().getWindow();
        stage.close();
    }

    /**
     * Navigates back to the profile view.
     */
    private void navigateBack() {
        if (indexController != null) {
            // Check if user is admin or regular user and load appropriate profile
            if (authService.isCurrentPersonAdmin()) {
                indexController.loadView("AdminProfile.fxml");
            } else {
                indexController.loadView("UserProfile.fxml");
            }
        } else {
            // Fallback: close window if opened as modal
            closeWindow();
        }
    }
}
