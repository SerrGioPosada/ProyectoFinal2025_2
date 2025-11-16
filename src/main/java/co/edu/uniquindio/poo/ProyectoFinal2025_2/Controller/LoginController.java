package co.edu.uniquindio.poo.ProyectoFinal2025_2.Controller;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.AuthenticationService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.GoogleOAuthService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.UserService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.StringUtil;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

/**
 * Controller for the user login view (Login.fxml).
 * <p>
 * This class manages the user interface for authentication. Its responsibilities include:
 * <ul>
 *     <li>Handling user input for email and password.</li>
 *     <li>Orchestrating traditional login via the {@link AuthenticationService}.</li>
 *     <li>Initiating the Google Sign-In flow via the {@link GoogleOAuthService}.</li>
 *     <li>Managing UI effects, such as floating labels and password visibility toggles.</li>
 *     <li>Communicating with the main {@link IndexController} upon successful login.</li>
 * </ul>
 * </p>
 */
public class LoginController {

    // =================================================================================================================
    // FXML Fields
    // =================================================================================================================

    private final AuthenticationService authService = AuthenticationService.getInstance();
    private final UserService userService = UserService.getInstance();
    private final GoogleOAuthService googleOAuthService = new GoogleOAuthService();
    @FXML
    private TextField txtEmail;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private TextField txtPasswordVisible;
    @FXML
    private Button btnLoginPane;
    @FXML
    private Label lblError;
    @FXML
    private Label googleLoginLabel;
    @FXML
    private Label lblForgotPassword;

    // =================================================================================================================
    // Dependencies and State
    // =================================================================================================================

    @FXML
    private Label lblEmailFloat;
    @FXML
    private Label lblPasswordFloat;
    @FXML
    private ImageView imgTogglePassword;
    @FXML
    private CheckBox chkKeepSignedIn;
    private IndexController indexController;

    private boolean isPasswordVisible = false;
    private boolean isTogglingPassword = false; // Flag to prevent focus listeners from firing during toggle

    // =================================================================================================================
    // Initialization & Setup
    // =================================================================================================================

    /**
     * Initializes the controller after its root element has been completely processed.
     * This method is called automatically by the FXMLLoader.
     */
    @FXML
    public void initialize() {
        setupEventHandlers();
        setupFloatingLabels();
        setupPasswordToggle();
        initializeErrorLabel();
    }

    /**
     * Initializes the error label to be hidden with opacity 0.
     */
    private void initializeErrorLabel() {
        if (lblError != null) {
            lblError.setOpacity(0.0);
            lblError.setMaxHeight(0);
            lblError.setManaged(false);
        }
    }

    /**
     * Injected by the parent controller to establish communication for navigation.
     *
     * @param indexController The main application controller.
     */
    public void setIndexController(IndexController indexController) {
        Logger.debug("IndexController has been set in LoginController.");
        this.indexController = indexController;
    }

    /**
     * Binds the action events of the view's interactive elements to their corresponding handler methods.
     */
    private void setupEventHandlers() {
        if (btnLoginPane != null) {
            btnLoginPane.setOnAction(event -> handleTraditionalLogin());
        }
        if (googleLoginLabel != null) {
            googleLoginLabel.setOnMouseClicked(event -> handleGoogleLogin());
        }
        if (lblForgotPassword != null) {
            lblForgotPassword.setOnMouseClicked(event -> handleForgotPassword());
        }

        // Add Enter key support for login with smart navigation
        if (txtEmail != null) {
            txtEmail.setOnKeyPressed(event -> {
                if (event.getCode().toString().equals("ENTER")) {
                    // If email is empty, stay; if password is empty, move to password
                    if (StringUtil.isNullOrEmpty(txtEmail.getText())) {
                        return;
                    }
                    // Move to password field
                    if (isPasswordVisible) {
                        txtPasswordVisible.requestFocus();
                    } else {
                        txtPassword.requestFocus();
                    }
                }
            });
        }
        if (txtPassword != null) {
            txtPassword.setOnKeyPressed(event -> {
                if (event.getCode().toString().equals("ENTER")) {
                    // If both fields are filled, submit; otherwise stay
                    if (!StringUtil.isNullOrEmpty(txtEmail.getText()) &&
                        !StringUtil.isNullOrEmpty(txtPassword.getText())) {
                        handleTraditionalLogin();
                    }
                }
            });
        }
        if (txtPasswordVisible != null) {
            txtPasswordVisible.setOnKeyPressed(event -> {
                if (event.getCode().toString().equals("ENTER")) {
                    // If both fields are filled, submit; otherwise stay
                    if (!StringUtil.isNullOrEmpty(txtEmail.getText()) &&
                        !StringUtil.isNullOrEmpty(txtPasswordVisible.getText())) {
                        handleTraditionalLogin();
                    }
                }
            });
        }
    }

    // =================================================================================================================
    // Core Login Logic & Event Handlers
    // =================================================================================================================

    /**
     * Handles the traditional login flow when the main login button is clicked.
     */
    private void handleTraditionalLogin() {
        String email = txtEmail.getText();
        String password = isPasswordVisible ? txtPasswordVisible.getText() : txtPassword.getText();
        Logger.info("Attempting traditional login for email: " + email);

        if (StringUtil.isNullOrEmpty(email) || StringUtil.isNullOrEmpty(password)) {
            Logger.warn("Login attempt with empty credentials");
            showError("Email and password cannot be empty.");
            return;
        }

        boolean loginSuccess = authService.login(email, password);
        Logger.info("Login result: " + (loginSuccess ? "SUCCESS" : "FAILURE"));

        if (!loginSuccess) {
            Logger.warn("Login failed for email: " + email);
            showError("Invalid email or password. Please try again.");
            return;
        }

        if (indexController == null) {
            Logger.error("CRITICAL: Login was successful, but IndexController is null. Cannot navigate.");
            return;
        }

        indexController.onLoginSuccess();
    }

    /**
     * Initiates the Google Sign-In flow when the corresponding label is clicked.
     */
    private void handleGoogleLogin() {
        Logger.info("Initiating Google Sign-In flow...");
        if (googleLoginLabel != null) {
            googleLoginLabel.setDisable(true); // Prevent multiple clicks
        }
        showError("Opening browser for Google Sign-In...");

        googleOAuthService.authenticate()
                .thenAccept(userInfo -> Platform.runLater(() -> {
                    Logger.info("Google Sign-In successful. Processing user info...");
                    processExternalUser(userInfo.getName(), userInfo.getEmail());
                    if (googleLoginLabel != null) googleLoginLabel.setDisable(false);
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        Logger.error("Google Sign-In flow failed.", (Exception) ex);
                        showError("Google Sign-In failed: " + ex.getMessage());
                        if (googleLoginLabel != null) googleLoginLabel.setDisable(false);
                    });
                    return null;
                });
    }

    /**
     * Handles the 'Forgot Password' label click event.
     * Navigates to the ForgotPassword view.
     */
    private void handleForgotPassword() {
        Logger.info("Navigating to Forgot Password view");

        if (indexController != null) {
            indexController.loadView("ForgotPassword.fxml");
        } else {
            Logger.error("IndexController not set - cannot navigate to Forgot Password");
        }
    }

    // =================================================================================================================
    // User Processing
    // =================================================================================================================

    /**
     * Processes user information from an external provider (e.g., Google).
     * If the user exists, it logs them in. If not, it creates a new user account and then logs them in.
     *
     * @param name  The user's name from the external provider.
     * @param email The user's email from the external provider.
     */
    private void processExternalUser(String name, String email) {
        Logger.info("Processing external user: " + email);

        // Use UserService to handle OAuth user registration/retrieval
        var user = userService.registerOAuthUser(name, email);

        if (user != null) {
            authService.setAuthenticatedUser(user);
        } else {
            Logger.error("Failed to register/retrieve OAuth user");
            showError("Failed to process Google Sign-In");
            return;
        }

        if (indexController == null) {
            Logger.error("CRITICAL: External login was successful, but IndexController is null. Cannot navigate.");
            return;
        }
        indexController.onLoginSuccess();
    }

    // =================================================================================================================
    // UI Logic: Floating Labels & Password Toggle
    // =================================================================================================================

    /**
     * Configures the floating label animations for the email and password fields.
     */
    private void setupFloatingLabels() {
        if (txtEmail != null && lblEmailFloat != null) {
            setupFieldListeners(txtEmail, lblEmailFloat, "CORREO ELECTRÓNICO");
        }
        if (txtPassword != null && txtPasswordVisible != null && lblPasswordFloat != null) {
            setupFieldListeners(txtPassword, lblPasswordFloat, "CONTRASEÑA");
            setupFieldListeners(txtPasswordVisible, lblPasswordFloat, "CONTRASEÑA");
        }
    }

    /**
     * Attaches focus and text listeners to a field to create the floating label effect.
     * The label stays visible when the field contains text, even after losing focus.
     *
     * @param field  The text input control (TextField or PasswordField).
     * @param label  The floating label associated with the field.
     * @param prompt The prompt text to restore when the field is empty and unfocused.
     */
    private void setupFieldListeners(Control field, Label label, String prompt) {
        label.setOpacity(0);
        label.setTranslateY(35);

        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (isTogglingPassword) return;
            String text = (field instanceof TextField) ? ((TextField) field).getText() : ((PasswordField) field).getText();
            if (newVal) {
                // On focus: always animate label up
                animateFloatingLabel(label, field, true, prompt);
            } else if (text.isEmpty()) {
                // On blur: only hide label if field is empty
                animateFloatingLabel(label, field, false, prompt);
            }
            // If field has text, label stays visible (don't animate down)
        });

        TextInputControl textInput = (TextInputControl) field;
        textInput.textProperty().addListener((obs, oldVal, newVal) -> {
            if (isTogglingPassword) return;
            if (!newVal.isEmpty() && label.getOpacity() < 0.5) {
                // If text is added and label is hidden, show it
                animateFloatingLabel(label, field, true, prompt);
            } else if (newVal.isEmpty() && !field.isFocused()) {
                // If text is cleared and field is not focused, hide label
                animateFloatingLabel(label, field, false, prompt);
            }
        });
    }

    /**
     * Animates a floating label up or down based on the focus state.
     *
     * @param label      The label to animate.
     * @param field      The associated text field.
     * @param moveUp     True to move the label up, false to move it down.
     * @param promptText The original prompt text to restore.
     */
    private void animateFloatingLabel(Label label, Control field, boolean moveUp, String promptText) {
        TranslateTransition translate = new TranslateTransition(Duration.millis(200), label);
        FadeTransition fade = new FadeTransition(Duration.millis(200), label);

        if (moveUp) {
            translate.setToY(0);
            fade.setToValue(1.0);
            if (field instanceof TextInputControl) ((TextInputControl) field).setPromptText(null);
        } else {
            translate.setToY(35);
            fade.setToValue(0.0);
            fade.setOnFinished(e -> {
                if (field instanceof TextInputControl) ((TextInputControl) field).setPromptText(promptText);
            });
        }
        translate.play();
        fade.play();
    }

    /**
     * Sets up the functionality for the password visibility toggle icon.
     */
    private void setupPasswordToggle() {
        if (imgTogglePassword == null) return;

        updatePasswordToggleIcon();
        imgTogglePassword.setOnMouseClicked(event -> togglePasswordVisibility());

        if (txtPassword != null && txtPasswordVisible != null) {
            txtPassword.textProperty().bindBidirectional(txtPasswordVisible.textProperty());
        }
    }

    /**
     * Toggles the visibility of the password field between a PasswordField (hidden) and a TextField (visible).
     */
    private void togglePasswordVisibility() {
        isTogglingPassword = true;
        isPasswordVisible = !isPasswordVisible;

        txtPassword.setManaged(!isPasswordVisible);
        txtPassword.setVisible(!isPasswordVisible);
        txtPasswordVisible.setManaged(isPasswordVisible);
        txtPasswordVisible.setVisible(isPasswordVisible);

        updatePasswordToggleIcon();

        Platform.runLater(() -> isTogglingPassword = false);
    }

    /**
     * Updates the password toggle icon to reflect the current visibility state (open/closed eye).
     */
    private void updatePasswordToggleIcon() {
        if (imgTogglePassword == null) return;

        String iconPath = isPasswordVisible
                ? "/co/edu/uniquindio/poo/ProyectoFinal2025_2/Images/eye-open.png"
                : "/co/edu/uniquindio/poo/ProyectoFinal2025_2/Images/eye-closed.png";
        try {
            imgTogglePassword.setImage(new Image(getClass().getResourceAsStream(iconPath)));
        } catch (Exception e) {
            Logger.error("Error loading password toggle icon: " + e.getMessage(), e);
        }
    }

    // =================================================================================================================
    // Utility Methods
    // =================================================================================================================

    /**
     * Displays a temporary error message in the UI.
     *
     * @param message The error message to display.
     */
    private void showError(String message) {
        Logger.warn("UI_ERROR: " + message);
        if (lblError == null) return;

        co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.MessageUtil.showError(lblError, message);
    }
}
