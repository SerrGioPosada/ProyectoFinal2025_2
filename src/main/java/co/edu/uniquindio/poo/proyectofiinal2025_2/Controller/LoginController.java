package co.edu.uniquindio.poo.proyectofiinal2025_2.Controller;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.dto.PersonCreationData;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Enums.PersonType;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Factory.PersonFactory;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Repositories.UserRepository;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Services.AuthenticationService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import one.jpro.platform.auth.core.oauth2.provider.GoogleAuthenticationProvider;

/**
 * <p>Controller for the Login view (Login.fxml).</p>
 * <p>Handles user input for email and password, and uses the central
 * AuthenticationService to perform the login action. Also supports Google Sign-In.</p>
 */
public class LoginController {

    // --- Componentes de la UI ---
    @FXML
    private TextField txtEmail;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private Button btnLoginPane;
    @FXML
    private Label lblError;
    @FXML
    private Label googleLoginLabel; // <-- AÑADIDO: Para el login con Google

    // --- Dependencias de tu sistema ---
    private final AuthenticationService authService = AuthenticationService.getInstance();
    private final UserRepository userRepository = UserRepository.getInstance(); // <-- AÑADIDO
    private final PersonFactory personFactory = new PersonFactory();             // <-- AÑADIDO
    private IndexController indexController;

    /**
     * Sets the reference to the main IndexController.
     * @param indexController The instance of the main IndexController.
     */
    public void setIndexController(IndexController indexController) {
        this.indexController = indexController;
    }

    /**
     * Initializes the controller. This method is automatically called
     * after the FXML file has been loaded.
     */
    @FXML
    public void initialize() {
        // Asigna la acción al botón de login tradicional
        if (btnLoginPane != null) {
            btnLoginPane.setOnAction(event -> handleTraditionalLogin());
        }
        // <-- AÑADIDO: Asigna la acción a la etiqueta de login con Google
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
     * Handles Google login using OAuth2 and loopback for desktop applications.
     */
    private void handleGoogleLogin() {
        // 1 Client ID y Client Secret de tu app de escritorio
        String clientId = "307704039867-2piv7j9num96kuai0j2e3gja7e6ud0i8.apps.googleusercontent.com";
        String clientSecret = "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";

        // 2 Crear el proveedor de autenticación Google
        GoogleAuthenticationProvider provider = new GoogleAuthenticationProvider(getStage(), clientId, clientSecret);

        // 3 Configurar para usar loopback (necesario para apps de escritorio)
        provider.getOptions().setUseLoopbackIpAddress(true);

        // 4 Iniciar autenticación
        provider.authenticate().thenAccept(credentials -> {
            // Esto se ejecuta cuando Google devuelve las credenciales
            Platform.runLater(() -> {
                processExternalUser(credentials.getName(), credentials.getEmail());
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> showError("Google login failed: " + ex.getMessage()));
            return null;
        });
    }

    /**
     * Finds an existing user by email or creates a new one, then logs them in.
     * @param name The name provided by the external provider.
     * @param email The email provided by the external provider.
     */
    private void processExternalUser(String name, String email) { // <-- AÑADIDO
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
     * @param message The error message to display.
     */
    private void showError(String message) {
        if(lblError != null) {
            lblError.setText(message);
            lblError.setVisible(true);
        }
    }

    /**
     * Gets the current window (Stage) from a UI element.
     *
     * @return The current Stage.
     */
    private Stage getStage() {
        return (Stage) btnLoginPane.getScene().getWindow();
    }
}
