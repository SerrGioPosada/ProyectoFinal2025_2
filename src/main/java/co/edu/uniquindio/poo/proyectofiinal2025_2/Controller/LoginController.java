package co.edu.uniquindio.poo.proyectofiinal2025_2.Controller;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.User;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.dto.PersonCreationData;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Enums.PersonType;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Factory.PersonFactory;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Repositories.UserRepository;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Services.AuthenticationService;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Services.GoogleOAuthService;
import javafx.animation.TranslateTransition;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Controller for the Login view (Login.fxml).
 * Handles user input for email and password, and uses the central
 * AuthenticationService to perform the login action. Also supports Google Sign-In.
 */
public class LoginController {

    // --- FXML UI Components ---
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private TextField txtPasswordVisible;
    @FXML private Button btnLoginPane;
    @FXML private Label lblError;
    @FXML private Label googleLoginLabel;
    @FXML private Label lblForgotPassword;
    @FXML private Label lblEmailFloat;
    @FXML private Label lblPasswordFloat;
    @FXML private ImageView imgTogglePassword;
    @FXML private CheckBox chkKeepSignedIn;

    // --- Dependencies & Services ---
    private final AuthenticationService authService = AuthenticationService.getInstance();
    private final UserRepository userRepository = UserRepository.getInstance();
    private final PersonFactory personFactory = new PersonFactory();
    private final GoogleOAuthService googleOAuthService = new GoogleOAuthService();
    private IndexController indexController;

    // --- State Variables ---
    private boolean isPasswordVisible = false;
    private boolean isTogglingPassword = false;

    // =================================================================================
    //                           INITIALIZATION & SETUP
    // =================================================================================

    /**
     * Injected by the parent controller to establish communication.
     * @param indexController The main application controller.
     */
    public void setIndexController(IndexController indexController) {
        this.indexController = indexController;
    }

    /**
     * Initializes the controller after its root element has been completely processed.
     */
    @FXML
    public void initialize() {
        setupEventHandlers();
        setupFloatingLabels();
        setupPasswordToggle();
    }

    /**
     * Sets up the primary event handlers for the login view's interactive elements.
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
    }

    // =================================================================================
    //                             CORE LOGIN LOGIC
    // =================================================================================

    /**
     * Handles the traditional login button click event by validating credentials.
     */
    private void handleTraditionalLogin() {
        String email = txtEmail.getText();
        String password = isPasswordVisible ? txtPasswordVisible.getText() : txtPassword.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Email and password cannot be empty.");
            return;
        }

        boolean loginSuccess = authService.login(email, password);

        if (loginSuccess) {
            if (indexController != null) {
                indexController.onLoginSuccess();
            }
        } else {
            showError("Invalid email or password. Please try again.");
        }
    }

    /**
     * Handles Google login using the OAuth2 service.
     */
    private void handleGoogleLogin() {
        if (googleLoginLabel != null) {
            googleLoginLabel.setDisable(true); // Prevent multiple clicks
        }
        showError("Opening browser for Google Sign-In...");

        googleOAuthService.authenticate()
                .thenAccept(userInfo -> {
                    Platform.runLater(() -> {
                        processExternalUser(userInfo.getName(), userInfo.getEmail());
                        if (googleLoginLabel != null) {
                            googleLoginLabel.setDisable(false);
                        }
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        showError("Google Sign-In failed: " + ex.getMessage());
                        if (googleLoginLabel != null) {
                            googleLoginLabel.setDisable(false);
                        }
                    });
                    ex.printStackTrace();
                    return null;
                });
    }

    /**
     * Handles the 'Forgot Password' label click event.
     * (Placeholder for future implementation, e.g., navigating to a recovery view).
     */
    private void handleForgotPassword() {
        // Example: if (indexController != null) indexController.loadView("ForgotPassword.fxml");
        System.out.println("Redirecting to password recovery view...");
    }

    /**
     * Processes user info from an external provider (like Google).
     * If the user exists, it logs them in. If not, it creates a new user account automatically,
     * saves it to the repository, and then logs them in.
     *
     * @param name  The user's name from the external provider.
     * @param email The user's email from the external provider.
     */
    private void processExternalUser(String name, String email) {
        userRepository.findByEmail(email).ifPresentOrElse(
                // IF the user ALREADY EXISTS, simply log them in.
                existingUser -> {
                    authService.setAuthenticatedUser(existingUser);
                    System.out.println("Existing user logged in: " + existingUser.getEmail());
                },
                // IF the user DOES NOT EXIST...
                () -> {
                    // 1. Prepare the data for the new user.
                    PersonCreationData newData = new PersonCreationData();
                    newData.setName(name);
                    newData.setEmail(email);
                    newData.setPassword("oauth_google_user_" + System.currentTimeMillis());

                    // 2. Create the User object.
                    User newUser = (User) personFactory.createPerson(PersonType.USER, newData);

                    // 3. Add the new user to the repository.
                    //    This will call saveToFile() internally.
                    userRepository.addUser(newUser);
                    System.out.println("New user created and saved: " + newUser.getEmail());

                    // 4. Now, log in with the newly created user.
                    authService.setAuthenticatedUser(newUser);
                }
        );

        // Finally, notify the main controller that the login was successful.
        if (indexController != null) {
            indexController.onLoginSuccess();
        }
    }

    // =================================================================================
    //                     UI ANIMATIONS & VISIBILITY TOGGLE
    // =================================================================================

    /**
     * Configures the floating label animations for the email and password fields.
     */
    private void setupFloatingLabels() {
        // Email floating label setup
        if (txtEmail != null && lblEmailFloat != null) {
            setupFieldListeners(txtEmail, lblEmailFloat, "EMAIL");
        }
        // Password floating labels setup (for both visible and hidden fields)
        if (txtPassword != null && txtPasswordVisible != null && lblPasswordFloat != null) {
            setupFieldListeners(txtPassword, lblPasswordFloat, "PASSWORD");
            setupFieldListeners(txtPasswordVisible, lblPasswordFloat, "PASSWORD");
        }
    }

    /**
     * Helper method to attach focus and text listeners to a field for the floating label effect.
     * @param field The text input control (TextField or PasswordField).
     * @param label The floating label associated with the field.
     * @param prompt The prompt text to restore when the field is empty and unfocused.
     */
    private void setupFieldListeners(Control field, Label label, String prompt) {
        label.setOpacity(0);
        label.setTranslateY(35);

        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (isTogglingPassword) return; // Ignore focus changes during toggle
            String text = (field instanceof TextField) ? ((TextField) field).getText() : ((PasswordField) field).getText();
            if (newVal) {
                animateFloatingLabel(label, field, true, prompt);
            } else if (text.isEmpty()) {
                animateFloatingLabel(label, field, false, prompt);
            }
        });

        if (field instanceof TextField) {
            ((TextField) field).textProperty().addListener((obs, oldVal, newVal) -> {
                if (isTogglingPassword) return;
                if (!newVal.isEmpty() && label.getOpacity() < 0.5) {
                    animateFloatingLabel(label, field, true, prompt);
                }
            });
        } else if (field instanceof PasswordField) {
            ((PasswordField) field).textProperty().addListener((obs, oldVal, newVal) -> {
                if (isTogglingPassword) return;
                if (!newVal.isEmpty() && label.getOpacity() < 0.5) {
                    animateFloatingLabel(label, field, true, prompt);
                }
            });
        }
    }

    /**
     * Animates a floating label up or down based on the focus state.
     * @param label The label to animate.
     * @param field The associated text field.
     * @param moveUp True to move up, false to move down.
     * @param promptText The original prompt text.
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
        if (imgTogglePassword != null) {
            updatePasswordToggleIcon();
            imgTogglePassword.setOnMouseClicked(event -> togglePasswordVisibility());
            if (txtPassword != null && txtPasswordVisible != null) {
                txtPassword.textProperty().bindBidirectional(txtPasswordVisible.textProperty());
            }
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

        // Use Platform.runLater to defer the flag change until after the current UI pulse
        Platform.runLater(() -> isTogglingPassword = false);
    }

    /**
     * Updates the password toggle icon to reflect the current visibility state (open/closed eye).
     */
    private void updatePasswordToggleIcon() {
        if (imgTogglePassword != null) {
            String iconPath = isPasswordVisible
                    ? "/co/edu/uniquindio/poo/proyectofiinal2025_2/Images/eye-open.png"
                    : "/co/edu/uniquindio/poo/proyectofiinal2025_2/Images/eye-closed.png";
            try {
                imgTogglePassword.setImage(new Image(getClass().getResourceAsStream(iconPath)));
            } catch (Exception e) {
                System.err.println("Error loading password toggle icon: " + e.getMessage());
            }
        }
    }

    // =================================================================================
    //                           UTILITY & HELPER METHODS
    // =================================================================================

    /**
     * Displays an error message in the UI for a limited duration.
     * @param message The error message to display.
     */
    private void showError(String message) {
        if (lblError != null) {
            lblError.setText(message);
            lblError.setVisible(true);
            lblError.setManaged(true);

            // Hide the error message after 5 seconds
            new Thread(() -> {
                try {
                    Thread.sleep(5000);
                    Platform.runLater(() -> {
                        lblError.setVisible(false);
                        lblError.setManaged(false);
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
                }
            }).start();
        }
    }

    /**
     * Gets the current window (Stage) from a UI element.
     * @return The current Stage.
     */
    private Stage getStage() {
        return (Stage) btnLoginPane.getScene().getWindow();
    }
}