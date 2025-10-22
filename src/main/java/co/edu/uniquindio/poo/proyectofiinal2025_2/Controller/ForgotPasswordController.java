package co.edu.uniquindio.poo.proyectofiinal2025_2.Controller;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.dto.PasswordResetDTO;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Services.PasswordResetService;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Util.UtilController.DialogUtil;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Util.UtilModel.Logger;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Util.UtilModel.PasswordValidator;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the Forgot Password view.
 * <p>
 * Handles the two-step password reset process:
 * 1. User identity verification (email + name)
 * 2. New password creation with strength validation
 * </p>
 * <p>
 * This controller reuses utility classes (PasswordValidator, DialogUtil)
 * and services (PasswordResetService) to maintain clean separation of concerns.
 * </p>
 */
public class ForgotPasswordController implements Initializable {

    // =========================================================================================
    // FXML INJECTED FIELDS - Step Indicators
    // =========================================================================================

    @FXML private VBox step1Container;
    @FXML private VBox step2Container;

    // =========================================================================================
    // FXML INJECTED FIELDS - Verification Form (Step 1)
    // =========================================================================================

    @FXML private VBox verificationForm;
    @FXML private TextField txtEmail;
    @FXML private TextField txtName;
    @FXML private Button btnVerify;
    @FXML private Label lblVerificationError;
    @FXML private Label lblEmailFloat;
    @FXML private Label lblNameFloat;

    // =========================================================================================
    // FXML INJECTED FIELDS - Password Reset Form (Step 2)
    // =========================================================================================

    @FXML private VBox passwordResetForm;
    @FXML private PasswordField txtPassword;
    @FXML private TextField txtPasswordVisible;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private TextField txtConfirmPasswordVisible;
    @FXML private ImageView imgTogglePassword;
    @FXML private ImageView imgToggleConfirmPassword;
    @FXML private ProgressBar progressPasswordStrength;
    @FXML private Label lblPasswordStrength;
    @FXML private Button btnResetPassword;
    @FXML private Label lblResetError;
    @FXML private Label lblPasswordFloat;
    @FXML private Label lblConfirmPasswordFloat;

    // =========================================================================================
    // FXML INJECTED FIELDS - Success Message
    // =========================================================================================

    @FXML private VBox successMessage;
    @FXML private Label lblBackToLogin;

    // =========================================================================================
    // DEPENDENCIES & STATE
    // =========================================================================================

    private final PasswordResetService passwordResetService = new PasswordResetService();
    private PasswordResetDTO resetDTO;
    private IndexController indexController;
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    // =========================================================================================
    // INITIALIZATION
    // =========================================================================================

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Logger.info("Initializing ForgotPasswordController");

        resetDTO = new PasswordResetDTO();
        setupPasswordToggle();
        setupPasswordStrengthIndicator();
        setupFloatingLabels();

        Logger.info("ForgotPasswordController initialized successfully");
    }

    /**
     * Sets the reference to IndexController for navigation.
     *
     * @param indexController The main index controller
     */
    public void setIndexController(IndexController indexController) {
        this.indexController = indexController;
        Logger.info("IndexController reference set in ForgotPasswordController");
    }

    // =========================================================================================
    // SETUP METHODS
    // =========================================================================================

    /**
     * Sets up password visibility toggle functionality.
     * Loads eye icons and binds click events to toggle password visibility.
     */
    private void setupPasswordToggle() {
        try {
            // Load eye icons
            Image eyeOpenIcon = new Image(getClass().getResourceAsStream(
                "/co/edu/uniquindio/poo/proyectofiinal2025_2/Images/eye-open.png"));
            Image eyeClosedIcon = new Image(getClass().getResourceAsStream(
                "/co/edu/uniquindio/poo/proyectofiinal2025_2/Images/eye-closed.png"));

            imgTogglePassword.setImage(eyeClosedIcon);
            imgToggleConfirmPassword.setImage(eyeClosedIcon);

            // Password toggle
            imgTogglePassword.setOnMouseClicked(event -> {
                isPasswordVisible = !isPasswordVisible;
                togglePasswordVisibility(txtPassword, txtPasswordVisible, imgTogglePassword,
                                       eyeOpenIcon, eyeClosedIcon, isPasswordVisible);
            });

            // Confirm password toggle
            imgToggleConfirmPassword.setOnMouseClicked(event -> {
                isConfirmPasswordVisible = !isConfirmPasswordVisible;
                togglePasswordVisibility(txtConfirmPassword, txtConfirmPasswordVisible,
                                       imgToggleConfirmPassword, eyeOpenIcon, eyeClosedIcon,
                                       isConfirmPasswordVisible);
            });

            Logger.info("Password toggle setup completed");
        } catch (Exception e) {
            Logger.error("Failed to setup password toggle: " + e.getMessage());
        }
    }

    /**
     * Toggles visibility between PasswordField and TextField.
     *
     * @param passwordField The PasswordField component
     * @param textField The TextField component
     * @param imageView The ImageView for the toggle icon
     * @param eyeOpen The eye-open icon
     * @param eyeClosed The eye-closed icon
     * @param isVisible Whether password should be visible
     */
    private void togglePasswordVisibility(PasswordField passwordField, TextField textField,
                                         ImageView imageView, Image eyeOpen, Image eyeClosed,
                                         boolean isVisible) {
        if (isVisible) {
            textField.setText(passwordField.getText());
            textField.setVisible(true);
            textField.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            imageView.setImage(eyeOpen);
        } else {
            passwordField.setText(textField.getText());
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            textField.setVisible(false);
            textField.setManaged(false);
            imageView.setImage(eyeClosed);
        }
    }

    /**
     * Sets up real-time password strength indicator.
     * Updates progress bar and label as user types the password.
     */
    private void setupPasswordStrengthIndicator() {
        txtPassword.textProperty().addListener((observable, oldValue, newValue) -> {
            updatePasswordStrength(newValue);
        });

        txtPasswordVisible.textProperty().addListener((observable, oldValue, newValue) -> {
            updatePasswordStrength(newValue);
        });
    }

    /**
     * Updates the password strength indicator based on the current password.
     *
     * @param password The current password value
     */
    private void updatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            progressPasswordStrength.setProgress(0);
            lblPasswordStrength.setText("");
            progressPasswordStrength.getStyleClass().removeAll(
                "password-strength-weak", "password-strength-medium", "password-strength-strong");
            return;
        }

        PasswordValidator.PasswordStrength strength = PasswordValidator.calculateStrength(password);
        double progress = PasswordValidator.getStrengthPercentage(password) / 100.0;

        progressPasswordStrength.setProgress(progress);
        lblPasswordStrength.setText("Fortaleza: " + strength.getDisplayName());

        // Update style class based on strength
        progressPasswordStrength.getStyleClass().removeAll(
            "password-strength-weak", "password-strength-medium", "password-strength-strong");

        switch (strength) {
            case WEAK -> progressPasswordStrength.getStyleClass().add("password-strength-weak");
            case MEDIUM -> progressPasswordStrength.getStyleClass().add("password-strength-medium");
            case STRONG -> progressPasswordStrength.getStyleClass().add("password-strength-strong");
        }
    }

    /**
     * Sets up floating label animations for input fields.
     */
    private void setupFloatingLabels() {
        setupFloatingLabel(txtEmail, lblEmailFloat);
        setupFloatingLabel(txtName, lblNameFloat);
        setupFloatingLabel(txtPassword, lblPasswordFloat);
        setupFloatingLabel(txtConfirmPassword, lblConfirmPasswordFloat);
    }

    /**
     * Binds floating label behavior to a text field.
     *
     * @param textField The text field to bind
     * @param label The floating label
     */
    private void setupFloatingLabel(TextField textField, Label label) {
        textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (isNowFocused || !textField.getText().isEmpty()) {
                label.setOpacity(1.0);
                label.setTranslateY(0);
                label.getStyleClass().add("floating-label-top-active");
            } else {
                label.setOpacity(0);
                label.setTranslateY(35);
                label.getStyleClass().remove("floating-label-top-active");
            }
        });
    }

    // =========================================================================================
    // EVENT HANDLERS
    // =========================================================================================

    /**
     * Handles the verification step.
     * Validates user email and name against the database.
     */
    @FXML
    private void handleVerify() {
        Logger.info("Verification initiated");

        // Clear previous errors
        hideError(lblVerificationError);

        // Get input values
        String email = txtEmail.getText().trim();
        String name = txtName.getText().trim();

        // Validate inputs
        if (email.isEmpty()) {
            showError(lblVerificationError, "Por favor ingresa tu email");
            return;
        }

        if (name.isEmpty()) {
            showError(lblVerificationError, "Por favor ingresa tu nombre para verificación");
            return;
        }

        // Update DTO
        resetDTO.setEmail(email);
        resetDTO.setName(name);

        // Verify identity through service
        PasswordResetService.VerificationResult result = passwordResetService.verifyUserIdentity(resetDTO);

        if (result.isSuccess()) {
            Logger.info("User verification successful");
            showStep2();
        } else {
            Logger.warn("User verification failed: " + result.getMessage());
            showError(lblVerificationError, result.getMessage());
        }
    }

    /**
     * Handles the password reset step.
     * Validates new password and updates it in the database.
     */
    @FXML
    private void handleResetPassword() {
        Logger.info("Password reset initiated");

        // Clear previous errors
        hideError(lblResetError);

        // Get password values
        String newPassword = isPasswordVisible ?
            txtPasswordVisible.getText() : txtPassword.getText();
        String confirmPassword = isConfirmPasswordVisible ?
            txtConfirmPasswordVisible.getText() : txtConfirmPassword.getText();

        // Update DTO
        resetDTO.setNewPassword(newPassword);
        resetDTO.setConfirmPassword(confirmPassword);

        // Validate through service
        PasswordResetService.ResetResult result = passwordResetService.resetPassword(resetDTO);

        if (result.isSuccess()) {
            Logger.info("Password reset successful");
            showSuccessMessage();
        } else {
            Logger.warn("Password reset failed: " + result.getMessage());
            showError(lblResetError, result.getMessage());
        }
    }

    /**
     * Handles navigation back to login screen.
     */
    @FXML
    private void handleBackToLogin() {
        Logger.info("Navigating back to login");

        if (indexController != null) {
            indexController.loadView("Login.fxml");
        } else {
            Logger.error("IndexController not set - cannot navigate back to login");
            DialogUtil.showError("Error", "No se pudo volver al login");
        }
    }

    // =========================================================================================
    // UI TRANSITION METHODS
    // =========================================================================================

    /**
     * Transitions from Step 1 (verification) to Step 2 (new password).
     */
    private void showStep2() {
        // Fade out step 1
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), verificationForm);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(event -> {
            verificationForm.setVisible(false);
            verificationForm.setManaged(false);

            // Update step indicators
            step1Container.setOpacity(0.5);
            step2Container.setOpacity(1.0);

            // Show step 2
            passwordResetForm.setVisible(true);
            passwordResetForm.setManaged(true);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), passwordResetForm);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();

            // Focus on password field
            Platform.runLater(() -> txtPassword.requestFocus());
        });
        fadeOut.play();
    }

    /**
     * Shows the success message after password reset.
     */
    private void showSuccessMessage() {
        // Fade out step 2
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), passwordResetForm);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(event -> {
            passwordResetForm.setVisible(false);
            passwordResetForm.setManaged(false);

            // Hide step indicators
            step1Container.setVisible(false);
            step1Container.setManaged(false);
            step2Container.setVisible(false);
            step2Container.setManaged(false);

            // Show success message
            successMessage.setVisible(true);
            successMessage.setManaged(true);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), successMessage);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        fadeOut.play();
    }

    // =========================================================================================
    // UTILITY METHODS
    // =========================================================================================

    /**
     * Shows an error message in the specified label.
     *
     * @param errorLabel The label to display the error
     * @param message The error message
     */
    private void showError(Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    /**
     * Hides an error label.
     *
     * @param errorLabel The label to hide
     */
    private void hideError(Label errorLabel) {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}
