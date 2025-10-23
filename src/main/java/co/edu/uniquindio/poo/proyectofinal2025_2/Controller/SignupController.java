package co.edu.uniquindio.poo.proyectofinal2025_2.Controller;

import co.edu.uniquindio.poo.proyectofinal2025_2.Model.dto.PersonCreationData;
import co.edu.uniquindio.poo.proyectofinal2025_2.Services.UserService;
import co.edu.uniquindio.poo.proyectofinal2025_2.Util.UtilController.FXUtil;
import co.edu.uniquindio.poo.proyectofinal2025_2.Util.UtilController.MessageUtil;
import co.edu.uniquindio.poo.proyectofinal2025_2.Util.UtilModel.ValidationUtil;
import co.edu.uniquindio.poo.proyectofinal2025_2.Util.UtilService.IdGenerationUtil;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Controller for the user registration view (Signup.fxml).
 * <p>
 * Responsibilities include:
 * <ul>
 *     <li>Handling the registration form fields and validation.</li>
 *     <li>Providing real-time validation feedback for each input.</li>
 *     <li>Managing password visibility toggles.</li>
 *     <li>Interacting with the {@link UserService} to register new users.</li>
 *     <li>Providing placeholder handlers for Google sign-up and navigation to login view.</li>
 * </ul>
 * </p>
 */
public class SignupController {

    // =================================================================================================================
    // FXML Fields
    // =================================================================================================================

    private final UserService userService = UserService.getInstance();
    private final BooleanProperty isPasswordVisible = new SimpleBooleanProperty(false);
    private final BooleanProperty isConfirmPasswordVisible = new SimpleBooleanProperty(false);
    @FXML
    private TextField txtName;
    @FXML
    private TextField txtLastName;
    @FXML
    private TextField txtEmail;
    @FXML
    private TextField txtPhone;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private TextField txtPasswordVisible;
    @FXML
    private PasswordField txtConfirmPassword;
    @FXML
    private TextField txtConfirmPasswordVisible;
    @FXML
    private Button btnRegister;
    @FXML
    private Label lblError;
    @FXML
    private Label lblNameError;
    @FXML
    private Label lblLastNameError;
    @FXML
    private Label lblEmailError;
    @FXML
    private Label lblPhoneError;
    @FXML
    private Label lblPasswordError;
    @FXML
    private Label lblConfirmPasswordError;
    @FXML
    private Label googleSignupLabel;

    // =================================================================================================================
    // Dependencies and State
    // =================================================================================================================

    @FXML
    private Label lblAlreadyRegistered;
    @FXML
    private ImageView imgTogglePassword;
    @FXML
    private ImageView imgToggleConfirmPassword;
    private IndexController indexController;

    // =================================================================================================================
    // Initialization & Setup
    // =================================================================================================================

    /**
     * Initializes the controller and sets up event handlers, validation listeners, and password toggles.
     */
    @FXML
    public void initialize() {
        setupEventHandlers();
        setupValidationListeners();
        setupPasswordToggles();
    }

    /**
     * Injects the main {@link IndexController} reference for navigation purposes.
     *
     * @param indexController The main IndexController instance.
     */
    public void setIndexController(IndexController indexController) {
        this.indexController = indexController;
    }

    /**
     * Sets up button click handlers and label click handlers.
     */
    private void setupEventHandlers() {
        btnRegister.setOnAction(event -> handleRegister());
        googleSignupLabel.setOnMouseClicked(event -> handleGoogleSignup());
        lblAlreadyRegistered.setOnMouseClicked(event -> handleAlreadyRegistered());

        // Add Enter key support for registration with smart navigation
        setupSmartEnterNavigation();
    }

    /**
     * Sets up smart Enter key navigation between fields.
     * If current field is filled, moves to next empty field.
     * If all fields are filled, submits the form.
     */
    private void setupSmartEnterNavigation() {
        if (txtName != null) {
            txtName.setOnKeyPressed(event -> {
                if (event.getCode().toString().equals("ENTER")) {
                    if (!txtName.getText().isEmpty()) {
                        txtLastName.requestFocus();
                    }
                }
            });
        }

        if (txtLastName != null) {
            txtLastName.setOnKeyPressed(event -> {
                if (event.getCode().toString().equals("ENTER")) {
                    if (!txtLastName.getText().isEmpty()) {
                        txtEmail.requestFocus();
                    }
                }
            });
        }

        if (txtEmail != null) {
            txtEmail.setOnKeyPressed(event -> {
                if (event.getCode().toString().equals("ENTER")) {
                    if (!txtEmail.getText().isEmpty()) {
                        txtPhone.requestFocus();
                    }
                }
            });
        }

        if (txtPhone != null) {
            txtPhone.setOnKeyPressed(event -> {
                if (event.getCode().toString().equals("ENTER")) {
                    if (!txtPhone.getText().isEmpty()) {
                        if (isPasswordVisible.get()) {
                            txtPasswordVisible.requestFocus();
                        } else {
                            txtPassword.requestFocus();
                        }
                    }
                }
            });
        }

        if (txtPassword != null) {
            txtPassword.setOnKeyPressed(event -> {
                if (event.getCode().toString().equals("ENTER")) {
                    if (!txtPassword.getText().isEmpty()) {
                        txtConfirmPassword.requestFocus();
                    }
                }
            });
        }

        if (txtPasswordVisible != null) {
            txtPasswordVisible.setOnKeyPressed(event -> {
                if (event.getCode().toString().equals("ENTER")) {
                    if (!txtPasswordVisible.getText().isEmpty()) {
                        txtConfirmPasswordVisible.requestFocus();
                    }
                }
            });
        }

        if (txtConfirmPassword != null) {
            txtConfirmPassword.setOnKeyPressed(event -> {
                if (event.getCode().toString().equals("ENTER")) {
                    // Check if all fields are filled
                    if (areAllFieldsFilled()) {
                        handleRegister();
                    }
                }
            });
        }

        if (txtConfirmPasswordVisible != null) {
            txtConfirmPasswordVisible.setOnKeyPressed(event -> {
                if (event.getCode().toString().equals("ENTER")) {
                    // Check if all fields are filled
                    if (areAllFieldsFilled()) {
                        handleRegister();
                    }
                }
            });
        }
    }

    /**
     * Checks if all required fields are filled.
     */
    private boolean areAllFieldsFilled() {
        return !txtName.getText().isEmpty() &&
               !txtLastName.getText().isEmpty() &&
               !txtEmail.getText().isEmpty() &&
               !txtPhone.getText().isEmpty() &&
               (isPasswordVisible.get() ?
                   !txtPasswordVisible.getText().isEmpty() && !txtConfirmPasswordVisible.getText().isEmpty() :
                   !txtPassword.getText().isEmpty() && !txtConfirmPassword.getText().isEmpty());
    }

    /**
     * Configures listeners for real-time input validation for all form fields.
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
     * Sets up the password visibility toggle buttons for both password fields.
     */
    private void setupPasswordToggles() {
        FXUtil.setupPasswordToggle(txtPassword, txtPasswordVisible, imgTogglePassword, isPasswordVisible, getClass());
        FXUtil.setupPasswordToggle(txtConfirmPassword, txtConfirmPasswordVisible, imgToggleConfirmPassword, isConfirmPasswordVisible, getClass());
    }

    // =================================================================================================================
    // Registration Logic
    // =================================================================================================================

    /**
     * Handles the user registration process:
     * <ul>
     *     <li>Validates the form.</li>
     *     <li>Creates a {@link PersonCreationData} object.</li>
     *     <li>Calls {@link UserService} to register the user.</li>
     *     <li>Displays error or success messages.</li>
     *     <li>Closes the window after a short delay on success.</li>
     * </ul>
     */
    private void handleRegister() {
        if (!isFormValid()) {
            MessageUtil.showError(lblError, "Please correct the errors in the form.");
            return;
        }

        PersonCreationData data = new PersonCreationData.Builder()
                .withId(IdGenerationUtil.generateId())
                .withName(txtName.getText().trim())
                .withLastName(txtLastName.getText().trim())
                .withEmail(txtEmail.getText().trim())
                .withPhone(txtPhone.getText().trim())
                .withPassword(txtPassword.getText())
                .build();

        boolean success = userService.registerUser(data);

        if (!success) {
            MessageUtil.showError(lblError, "This email address is already registered.");
            return;
        }

        MessageUtil.showSuccess(lblError, "Registration successful! You can now log in.");

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
     * Placeholder handler for Google sign-up.
     * <p>
     * Currently displays a not-implemented message.
     * </p>
     */
    private void handleGoogleSignup() {
        MessageUtil.showError(lblError, "Google Sign-Up is not yet implemented.");
    }

    /**
     * Handles the action when the user clicks "Already registered?".
     * <p>
     * Closes the current window and navigates to the login view.
     * </p>
     */
    private void handleAlreadyRegistered() {
        closeWindow();
        if (indexController != null) {
            Platform.runLater(() -> indexController.loadView("Login.fxml"));
        }
    }

    // =================================================================================================================
    // Validation
    // =================================================================================================================

    /**
     * Validates the entire form by running all individual field validators.
     *
     * @return {@code true} if all fields are valid, {@code false} otherwise.
     */
    private boolean isFormValid() {
        return Stream.of(
                validateName(),
                validateLastName(),
                validateEmail(),
                validatePhone(),
                validatePassword(),
                validateConfirmPassword()
        ).allMatch(Boolean::booleanValue);
    }

    private boolean validateName() {
        return validateField(txtName.getText(), lblNameError, "Name is required.", "Name must be at least 2 characters.", ValidationUtil::isValidName);
    }

    private boolean validateLastName() {
        return validateField(txtLastName.getText(), lblLastNameError, "Last name is required.", "Last name must be at least 2 characters.", ValidationUtil::isValidName);
    }

    private boolean validateEmail() {
        return validateField(txtEmail.getText(), lblEmailError, "Email is required.", "Invalid email format.", ValidationUtil::isValidEmail);
    }

    private boolean validatePhone() {
        return validateField(txtPhone.getText(), lblPhoneError, "Phone number is required.", "Phone number must be 10 digits.", ValidationUtil::isValidPhone);
    }

    private boolean validatePassword() {
        String password = isPasswordVisible.get() ? txtPasswordVisible.getText() : txtPassword.getText();
        return validateField(password, lblPasswordError, "Password is required.", "Password must be at least 6 characters.", ValidationUtil::isValidPassword);
    }

    private boolean validateConfirmPassword() {
        String password = isPasswordVisible.get() ? txtPasswordVisible.getText() : txtPassword.getText();
        String confirmPassword = isConfirmPasswordVisible.get() ? txtConfirmPasswordVisible.getText() : txtConfirmPassword.getText();
        return validateField(confirmPassword, lblConfirmPasswordError, "Password confirmation is required.", "Passwords do not match.", value -> ValidationUtil.passwordsMatch(password, value));
    }

    // =================================================================================================================
    // Utility Methods
    // =================================================================================================================

    /**
     * Adds a focus listener to a field to trigger validation when it loses focus.
     *
     * @param field           The input control to validate.
     * @param validationLogic A supplier that runs the validation logic and returns true/false.
     */
    private void addValidationListener(Control field, Supplier<Boolean> validationLogic) {
        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) validationLogic.get();
        });
    }

    /**
     * Validates a single field and shows/hides error messages accordingly.
     *
     * @param text           The field text to validate.
     * @param errorLabel     The label to display the error message.
     * @param emptyMessage   The message to display if the field is empty.
     * @param invalidMessage The message to display if the field is invalid.
     * @param validator      A predicate that returns true if the value is valid.
     * @return {@code true} if the field is valid, {@code false} otherwise.
     */
    private boolean validateField(String text, Label errorLabel, String emptyMessage, String invalidMessage, java.util.function.Predicate<String> validator) {
        String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            MessageUtil.showError(errorLabel, emptyMessage);
            return false;
        }
        if (!validator.test(trimmed)) {
            MessageUtil.showError(errorLabel, invalidMessage);
            return false;
        }
        MessageUtil.hide(errorLabel);
        return true;
    }

    /**
     * Closes the current registration window.
     */
    private void closeWindow() {
        if (btnRegister != null && btnRegister.getScene() != null && btnRegister.getScene().getWindow() != null) {
            Stage stage = (Stage) btnRegister.getScene().getWindow();
            stage.close();
        }
    }
}
