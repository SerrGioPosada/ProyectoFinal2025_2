package co.edu.uniquindio.poo.proyectofiinal2025_2.Controller;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Services.AuthenticationService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * <p>Controller for the Login view (Login.fxml).</p>
 * <p>Handles user input for email and password, and uses the central
 * AuthenticationService to perform the login action.</p>
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

    private final AuthenticationService authService = AuthenticationService.getInstance();
    private IndexController indexController;

    public void setIndexController(IndexController indexController) {
        this.indexController = indexController;
    }

    /**
     * Initializes the controller. This method is automatically called
     * after the FXML file has been loaded.
     */
    @FXML
    public void initialize() {
        // Add a listener to the login button to trigger the login process
        btnLoginPane.setOnAction(event -> handleLogin());
    }

    /**
     * Handles the login button click event. It retrieves user input,
     * calls the AuthenticationService to validate credentials, and notifies
     * the IndexController on success or displays an error on failure.
     */
    private void handleLogin() {
        String email = txtEmail.getText();
        String password = txtPassword.getText();

        // Basic validation to ensure fields are not empty
        if (email.isEmpty() || password.isEmpty()) {
            showError("Email and password cannot be empty.");
            return;
        }

        // Use the central authentication service to log in
        boolean loginSuccess = authService.login(email, password);

        if (loginSuccess) {
            // On success, notify the main controller
            if (indexController != null) {
                indexController.onLoginSuccess();
            }
        } else {
            // On failure, show an error message to the user
            showError("Invalid email or password. Please try again.");
        }
    }

    /**
     * Displays an error message in the UI.
     *
     * @param message The error message to display.
     */
    private void showError(String message) {
        lblError.setText(message);
        lblError.setVisible(true);
    }
}
