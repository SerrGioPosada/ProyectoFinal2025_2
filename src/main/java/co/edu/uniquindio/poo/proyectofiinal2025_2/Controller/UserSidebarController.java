package co.edu.uniquindio.poo.proyectofiinal2025_2.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the default user sidebar (UserSidebar.fxml).
 * <p>
 * This class extends {@link BaseSidebarController} to inherit common sidebar functionalities.
 * It is responsible for handling navigation events for both authenticated and unauthenticated users,
 * dynamically adjusting the visibility of buttons based on the user's login status.
 * </p>
 */
public class UserSidebarController extends BaseSidebarController {

    // =================================================================================================================
    // FXML Fields
    // =================================================================================================================

    @FXML private Button btnLogin;
    @FXML private Button btnSignup;
    @FXML private Button btnLogout;
    @FXML private Button btnProfile;
    @FXML private Button btnNewShipment;
    @FXML private Button btnOrders;

    // =================================================================================================================
    // Initialization
    // =================================================================================================================

    /**
     * Initializes the controller after its root element has been completely processed.
     *
     * @param url The location used to resolve relative paths for the root object, or {@code null} if not known.
     * @param resourceBundle The resources used to localize the root object, or {@code null} if not used.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle); // Initialize base controller features
        System.out.println("UserSidebarController initializing...");
        updateButtonVisibility();
        setupButtonActions();
        System.out.println("UserSidebarController initialized successfully.");
    }

    // =================================================================================================================
    // Event Handlers
    // =================================================================================================================

    /**
     * Handles the Login button click by loading the login view.
     */
    private void handleLogin() {
        System.out.println("Login button clicked.");
        if (indexController == null) {
            System.err.println("Cannot load login view: IndexController is null.");
            return;
        }
        indexController.loadView("Login.fxml");
    }

    /**
     * Handles the Sign Up button click by opening the signup modal window.
     */
    private void handleSignup() {
        System.out.println("Signup button clicked.");
        if (indexController == null) {
            System.err.println("Cannot open signup window: IndexController is null.");
            return;
        }
        indexController.openSignupWindow();
    }

    /**
     * Handles the Logout button click.
     * This will eventually log the user out and refresh the UI.
     */
    private void handleLogout() {
        System.out.println("Logout button clicked. (TODO: Implement logout logic)");
        // TODO: Call authService.logout() and then indexController.onLogout() to refresh the main view.
    }

    /**
     * Handles the Profile button click.
     */
    private void handleProfile() {
        System.out.println("Profile button clicked. (TODO: Implement navigation to profile view)");
        // TODO: Navigate to a detailed user profile view: indexController.loadView("ProfileView.fxml");
    }

    /**
     * Handles the New Shipment button click.
     */
    private void handleNewShipment() {
        System.out.println("New Shipment button clicked. (TODO: Implement navigation to shipment creation)");
        // TODO: Navigate to the new shipment form: indexController.loadView("CreateShipmentView.fxml");
    }

    /**
     * Handles the Orders button click.
     */
    private void handleOrders() {
        System.out.println("Orders button clicked. (TODO: Implement navigation to orders history)");
        // TODO: Navigate to the user's order history: indexController.loadView("MyShipmentsView.fxml");
    }

    // =================================================================================================================
    // Private Helper Methods
    // =================================================================================================================

    /**
     * Binds the action event of each button to its corresponding handler method.
     */
    private void setupButtonActions() {
        btnLogin.setOnAction(event -> handleLogin());
        btnSignup.setOnAction(event -> handleSignup());
        btnLogout.setOnAction(event -> handleLogout());
        btnProfile.setOnAction(event -> handleProfile());
        btnNewShipment.setOnAction(event -> handleNewShipment());
        btnOrders.setOnAction(event -> handleOrders());
    }

    /**
     * Sets the visibility of sidebar buttons based on the user's authentication status.
     */
    private void updateButtonVisibility() {
        boolean isLoggedIn = authService.isPersonLoggedIn();
        System.out.println("Updating button visibility. User is logged in: " + isLoggedIn);

        // Buttons visible when logged OUT
        setButtonVisibility(btnLogin, !isLoggedIn);
        setButtonVisibility(btnSignup, !isLoggedIn);

        // Buttons visible when logged IN
        setButtonVisibility(btnLogout, isLoggedIn);
        setButtonVisibility(btnProfile, isLoggedIn);
        setButtonVisibility(btnNewShipment, isLoggedIn);
        setButtonVisibility(btnOrders, isLoggedIn);
    }

    /**
     * Utility method to control both the visibility and layout management of a button.
     *
     * @param button  The button to modify.
     * @param visible true to make the button visible and managed, false to hide it.
     */
    private void setButtonVisibility(Button button, boolean visible) {
        if (button == null) return;
        button.setVisible(visible);
        button.setManaged(visible);
    }
}
