package co.edu.uniquindio.poo.proyectofiinal2025_2.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the default user sidebar (UserSidebar.fxml).
 * <p>
 * Extends BaseSidebarController to inherit common sidebar functionalities.
 * Handles navigation events for both authenticated and unauthenticated users,
 * dynamically adjusting the visibility of buttons based on the user's login status.
 * </p>
 */
public class UserSidebarController extends BaseSidebarController {

    // =================================================================================================================
    // FXML Fields
    // =================================================================================================================

    @FXML private Button btnLogin;
    @FXML private Button btnSignup;
    @FXML private Button btnProfile;
    @FXML private Button btnNewShipment;
    @FXML private Button btnOrders;

    // =================================================================================================================
    // Initialization
    // =================================================================================================================

    /**
     * Initializes the controller after its root element has been completely processed.
     * Sets button visibility and binds actions.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle); // Initialize base controller features
        updateButtonVisibility();
        setupButtonActions();
    }

    // =================================================================================================================
    // Event Handlers
    // =================================================================================================================

    /**
     * Handles the Login button click by loading the login view.
     */
    private void handleLogin() {
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
        if (indexController == null) {
            System.err.println("Cannot open signup window: IndexController is null.");
            return;
        }
        indexController.openSignupWindow();
    }

    /**
     * Handles the Profile button click.
     */
    private void handleProfile() {
        System.out.println("Profile button clicked.");
        indexController.loadView("ProfileView.fxml");
    }

    /**
     * Handles the New Shipment button click.
     */
    private void handleNewShipment() {
        System.out.println("New Shipment button clicked.");
        indexController.loadView("CreateShipment.fxml");
    }

    /**
     * Handles the Orders button click.
     */
    private void handleOrders() {
        System.out.println("Orders button clicked.");
        indexController.loadView("MyShipments.fxml");
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
