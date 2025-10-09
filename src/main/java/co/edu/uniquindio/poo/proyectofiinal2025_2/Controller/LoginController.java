package co.edu.uniquindio.poo.proyectofiinal2025_2.Controller;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.dto.PersonCreationData;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Enums.PersonType;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Factory.PersonFactory;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Repositories.UserRepository;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Services.AuthenticationService;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Services.GoogleOAuthService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Controller for the Login view (Login.fxml).
 * Handles user input for email and password, and uses the central
 * AuthenticationService to perform the login action. Also supports Google Sign-In.
 */
public class LoginController {

    @FXML
    private TextField txtEmail;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private Button btnLoginPane;
    @FXML
    private Label lblError;
    @FXML
    private Label googleLoginLabel;

    private final AuthenticationService authService = AuthenticationService.getInstance();
    private final UserRepository userRepository = UserRepository.getInstance();
    private final PersonFactory personFactory = new PersonFactory();
    private final GoogleOAuthService googleOAuthService = new GoogleOAuthService(); // NUEVO

    private IndexController indexController;

    public void setIndexController(IndexController indexController) {
        this.indexController = indexController;
    }

    @FXML
    public void initialize() {
        if (btnLoginPane != null) {
            btnLoginPane.setOnAction(event -> handleTraditionalLogin());
        }
        if (googleLoginLabel != null) {
            googleLoginLabel.setOnMouseClicked(event -> handleGoogleLogin());
        }
    }

    /**
     * Handles the traditional login button click event.
     */
    private void handleTraditionalLogin() {
        String email = txtEmail.getText();
        String password = txtPassword.getText();

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
     * Handles Google login using OAuth2 with native implementation.
     */
    private void handleGoogleLogin() {
        // Deshabilitar el botón mientras se procesa
        if (googleLoginLabel != null) {
            googleLoginLabel.setDisable(true);
        }

        // Mostrar mensaje de espera
        showError("Opening browser for Google login...");

        // Ejecutar autenticación en background
        googleOAuthService.authenticate()
                .thenAccept(userInfo -> {
                    // Volver al hilo de JavaFX para actualizar la UI
                    Platform.runLater(() -> {
                        processExternalUser(userInfo.getName(), userInfo.getEmail());
                        if (googleLoginLabel != null) {
                            googleLoginLabel.setDisable(false);
                        }
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        showError("Google login failed: " + ex.getMessage());
                        if (googleLoginLabel != null) {
                            googleLoginLabel.setDisable(false);
                        }
                    });
                    ex.printStackTrace(); // Para debugging
                    return null;
                });
    }

    /**
     * Finds an existing user by email or creates a new one, then logs them in.
     */
    private void processExternalUser(String name, String email) {
        userRepository.findByEmail(email).ifPresentOrElse(
                existingUser -> authService.setAuthenticatedUser(existingUser),
                () -> {
                    PersonCreationData newData = new PersonCreationData();
                    newData.setName(name);
                    newData.setEmail(email);
                    newData.setPassword("oauth_google_user_" + System.currentTimeMillis());
                    authService.setAuthenticatedUser(personFactory.createPerson(PersonType.USER, newData));
                }
        );

        if (indexController != null) {
            indexController.onLoginSuccess();
        }
    }

    /**
     * Displays an error message in the UI.
     */
    private void showError(String message) {
        if(lblError != null) {
            lblError.setText(message);
            lblError.setVisible(true);
        }
    }
}