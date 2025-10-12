package co.edu.uniquindio.poo.proyectofiinal2025_2.Controller;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.dto.PersonCreationData;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Services.UserService;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.util.UUID;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Controller for the user registration view (Signup.fxml).
 * <p>
 * This class handles the user interface for creating a new account. Its responsibilities include:
 * <ul>
 *     <li>Capturing user input for name, email, password, etc.</li>
 *     <li>Performing real-time validation on input fields as the user loses focus.</li>
 *     <li>Orchestrating the final registration process via the {@link UserService}.</li>
 *     <li>Providing clear feedback to the user (e.g., validation errors, success messages).</li>
 *     <li>Managing UI effects like password visibility toggles.</li>
 * </ul>
 * </p>
 */

public class SignupController {

    // =================================================================================================================
    // FXML Fields
    // =================================================================================================================

    @FXML private TextField txtName;
    @FXML private TextField txtLastName;
    @FXML private TextField txtEmail;
    @FXML private TextField txtPhone;
    @FXML private PasswordField txtPassword;
    @FXML private TextField txtPasswordVisible;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private TextField txtConfirmPasswordVisible;
    @FXML private Button btnRegister;
    @FXML private Label lblError;
    @FXML private Label lblNameError;
    @FXML private Label lblLastNameError;
    @FXML private Label lblEmailError;
    @FXML private Label lblPhoneError;
    @FXML private Label lblPasswordError;
    @FXML private Label lblConfirmPasswordError;
    @FXML private Label googleSignupLabel;
    @FXML private Label lblAlreadyRegistered;
    @FXML private ImageView imgTogglePassword;
    @FXML private ImageView imgToggleConfirmPassword;

    // =================================================================================================================
    // Dependencies and State
    // =================================================================================================================

    private final UserService userService = UserService.getInstance();
    private IndexController indexController;

    private final BooleanProperty isPasswordVisible = new SimpleBooleanProperty(false);
    private final BooleanProperty isConfirmPasswordVisible = new SimpleBooleanProperty(false);

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10}$");

    // =================================================================================================================
    // Initialization & Setup
    // =================================================================================================================

    /**
     * Initializes the controller after its root element has been completely processed.
     */
    @FXML
    public void initialize() {
        setupEventHandlers();
        setupValidationListeners();
        setupPasswordToggles();
    }

    /**
     * Injected by the parent controller to establish communication for navigation.
     * @param indexController The main application controller.
     */
    public void setIndexController(IndexController indexController) {
        System.out.println("IndexController has been set in SignupController.");
        this.indexController = indexController;
    }

    /**
     * Binds the action events of the view's interactive elements to their corresponding handler methods.
     */
    private void setupEventHandlers() {
        btnRegister.setOnAction(event -> handleRegister());
        googleSignupLabel.setOnMouseClicked(event -> handleGoogleSignup());
        lblAlreadyRegistered.setOnMouseClicked(event -> handleAlreadyRegistered());
    }

    /**
     * Attaches focus-lost listeners to all input fields to trigger real-time validation.
     */
    private void setupValidationListeners() {
        addValidationListener(txtName, this::validateName);
        addValidationListener(txtLastName, this::validateLastName);
        addValidationListener(txtEmail, this::validateEmail);
        addValidationListener(txtPhone, this::validatePhone);
        addValidationListener(txtPassword, this::validatePassword);
        addValidationListener(txtPasswordVisible, this::validatePassword);
        addValidationListener(txtConfirmPassword, this::validateConfirmPassword);
        addValidationListener(txtConfirmPasswordVisible, this::validateConfirmPassword);
    }

    /**
     * Configures the password and confirm password fields to toggle visibility.
     */
    private void setupPasswordToggles() {
        setupPasswordToggle(txtPassword, txtPasswordVisible, imgTogglePassword, isPasswordVisible);
        setupPasswordToggle(txtConfirmPassword, txtConfirmPasswordVisible, imgToggleConfirmPassword, isConfirmPasswordVisible);
    }

    // =================================================================================================================
    // Core Registration Logic & Event Handlers
    // =================================================================================================================

    /**
     * Handles the main registration flow when the 'Register' button is clicked.
     */
    private void handleRegister() {
        if (!isFormValid()) {
            showError("Please correct the errors in the form.");
            System.err.println("Registration aborted due to validation errors.");
            return;
        }

        System.out.println("Form is valid. Creating user data object...");
        PersonCreationData data = new PersonCreationData.Builder()
                .withId(UUID.randomUUID().toString())
                .withName(txtName.getText().trim())
                .withLastName(txtLastName.getText().trim())
                .withEmail(txtEmail.getText().trim())
                .withPhone(txtPhone.getText().trim())
                .withPassword(txtPassword.getText())
                .build();

        boolean success = userService.registerUser(data);

        if (!success) {
            System.err.println("Registration failed: Email already exists.");
            showError("This email address is already registered.");
            return;
        }

        showSuccess("Registration successful! You can now log in.");
        // Close the window after a short delay
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                Platform.runLater(this::closeWindow);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    /**
     * Handles the Google Sign-Up label click event.
     */
    private void handleGoogleSignup() {
        showError("Google Sign-Up is not yet implemented.");
    }

    /**
     * Handles the 'Already Registered' label click event, closing the signup window
     * and loading the login view.
     */
    private void handleAlreadyRegistered() {
        closeWindow();
        if (indexController == null) return;
        Platform.runLater(() -> indexController.loadView("Login.fxml"));
    }

    // =================================================================================================================
    // Field Validation Logic
    // =================================================================================================================

    /**
     * Runs all validation methods and returns true only if all are successful.
     * @return {@code true} if all fields are valid, {@code false} otherwise.
     */
    private boolean isFormValid() {
        // The order matters. It ensures all validation messages are shown at once.
        return Stream.of(
                validateName(),
                validateLastName(),
                validateEmail(),
                validatePhone(),
                validatePassword(),
                validateConfirmPassword()
        ).allMatch(isValid -> isValid);
    }

    private boolean validateName() {
        return validateField(txtName.getText(), lblNameError, "Name is required.", "Name must be at least 2 characters.", name -> name.length() >= 2);
    }

    private boolean validateLastName() {
        return validateField(txtLastName.getText(), lblLastNameError, "Last name is required.", "Last name must be at least 2 characters.", lastName -> lastName.length() >= 2);
    }

    private boolean validateEmail() {
        return validateField(txtEmail.getText(), lblEmailError, "Email is required.", "Invalid email format (e.g., user@domain.com).", email -> EMAIL_PATTERN.matcher(email).matches());
    }

    private boolean validatePhone() {
        return validateField(txtPhone.getText(), lblPhoneError, "Phone number is required.", "Phone number must be 10 digits.", phone -> PHONE_PATTERN.matcher(phone).matches());
    }

    private boolean validatePassword() {
        String password = isPasswordVisible.get() ? txtPasswordVisible.getText() : txtPassword.getText();
        return validateField(password, lblPasswordError, "Password is required.", "Password must be at least 6 characters.", pass -> pass.length() >= 6);
    }

    private boolean validateConfirmPassword() {
        String password = isPasswordVisible.get() ? txtPasswordVisible.getText() : txtPassword.getText();
        String confirmPassword = isConfirmPasswordVisible.get() ? txtConfirmPasswordVisible.getText() : txtConfirmPassword.getText();
        return validateField(confirmPassword, lblConfirmPasswordError, "Password confirmation is required.", "Passwords do not match.", pass -> pass.equals(password));
    }

    // =================================================================================================================
    // UI Helper Methods
    // =================================================================================================================

    /**
     * A generic helper to attach a focus-lost validation listener to a text field.
     * @param field The text field to listen to.
     * @param validationLogic The validation method to execute.
     */
    private void addValidationListener(Control field, Supplier<Boolean> validationLogic) {
        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) { // When focus is lost
                validationLogic.get();
            }
        });
    }

    /**
     * A generic helper to configure the visibility toggle for a pair of password fields.
     */
    private void setupPasswordToggle(PasswordField passwordField, TextField visibleField, ImageView toggleIcon, BooleanProperty visibilityFlag) {
        passwordField.textProperty().bindBidirectional(visibleField.textProperty());
        updateToggleIcon(toggleIcon, visibilityFlag.get());
        toggleIcon.setOnMouseClicked(event -> {
            visibilityFlag.set(!visibilityFlag.get());
            visibleField.setVisible(visibilityFlag.get());
            visibleField.setManaged(visibilityFlag.get());
            passwordField.setVisible(!visibilityFlag.get());
            passwordField.setManaged(!visibilityFlag.get());
            updateToggleIcon(toggleIcon, visibilityFlag.get());
        });
    }

    /**
     * Updates a toggle icon based on the visibility state.
     */
    private void updateToggleIcon(ImageView iconView, boolean isVisible) {
        String iconPath = isVisible
                ? "/co/edu/uniquindio/poo/proyectofiinal2025_2/Images/eye-open.png"
                : "/co/edu/uniquindio/poo/proyectofiinal2025_2/Images/eye-closed.png";
        try {
            iconView.setImage(new Image(getClass().getResourceAsStream(iconPath)));
        } catch (Exception e) {
            System.err.println("Error loading toggle icon: " + e.getMessage());
        }
    }

    /**
     * A generic validation helper that shows or hides an error label based on a predicate.
     */
    private boolean validateField(String text, Label errorLabel, String emptyMessage, String invalidMessage, java.util.function.Predicate<String> validator) {
        String trimmedText = text.trim();
        if (trimmedText.isEmpty()) {
            showFieldError(errorLabel, emptyMessage);
            return false;
        }
        if (!validator.test(trimmedText)) {
            showFieldError(errorLabel, invalidMessage);
            return false;
        }
        hideFieldError(errorLabel);
        return true;
    }

    /**
     * Displays a specific field validation error message.
     */
    private void showFieldError(Label errorLabel, String message) {
        if (errorLabel == null) return;
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    /**
     * Hides a specific field validation error message.
     */
    private void hideFieldError(Label errorLabel) {
        if (errorLabel == null) return;
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    /**
     * Displays a general error message at the bottom of the form.
     */
    private void showError(String message) {
        showMessage(message, "#ff6b6b"); // Red for error
    }

    /**
     * Displays a general success message at the bottom of the form.
     */
    private void showSuccess(String message) {
        showMessage(message, "#51cf66"); // Green for success
    }

    /**
     * Displays a message in the main error label with a specific color.
     */
    private void showMessage(String message, String color) {
        if (lblError == null) return;
        lblError.setText(message);
        lblError.setStyle("-fx-text-fill: " + color + ";");
        lblError.setVisible(true);
        lblError.setManaged(true);
    }

    /**
     * Closes the current window.
     */
    private void closeWindow() {
        if (btnRegister == null || btnRegister.getScene() == null || btnRegister.getScene().getWindow() == null) return;
        Stage stage = (Stage) btnRegister.getScene().getWindow();
        stage.close();
    }
}
