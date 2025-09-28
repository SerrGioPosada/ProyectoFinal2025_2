package co.edu.uniquindio.poo.proyectofiinal2025_2.Controller;

import co.edu.uniquindio.poo.proyectofiinal2025_2.MainApp;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Services.AuthenticationService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

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
    private Button btnLogin;

    @FXML
    private Label lblError;

    private final AuthenticationService authService = AuthenticationService.getInstance();

    /**
     * Initializes the controller. This method is automatically called
     * after the FXML file has been loaded.
     */
    @FXML
    public void initialize() {
        // Add a listener to the login button to trigger the login process
        btnLogin.setOnAction(event -> handleLogin());
    }

    /**
     * Handles the login button click event. It retrieves user input,
     * calls the AuthenticationService to validate credentials, and navigates
     * to the main application view on success or displays an error on failure.
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
            // On success, navigate to the main application view
            navigateToMainView();
        } else {
            // On failure, show an error message to the user
            showError("Invalid email or password. Please try again.");
        }
    }

    /**
     * Navigates the user to the main application window after a successful login.
     */
    private void navigateToMainView() {
        try {
            Stage stage = (Stage) btnLogin.getScene().getWindow();
            FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("View/Index.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            stage.setScene(scene);
            stage.setTitle("My Project Final 2025-2");
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error: Failed to load the main application view.");
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
