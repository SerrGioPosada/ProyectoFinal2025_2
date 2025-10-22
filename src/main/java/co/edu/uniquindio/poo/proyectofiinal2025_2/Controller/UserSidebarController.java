package co.edu.uniquindio.poo.proyectofiinal2025_2.Controller;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Util.UtilModel.Logger;
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

    @FXML
    private Button btnLogin;
    @FXML
    private Button btnSignup;
    @FXML
    private Button btnProfile;
    @FXML
    private Button btnNewShipment;
    @FXML
    private Button btnOrders;
    @FXML
    private Button btnTrackShipment;
    @FXML
    private Button btnLogout;

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
            Logger.error("Cannot load login view: IndexController is null.");
            return;
        }
        indexController.loadView("Login.fxml");
    }

    /**
     * Handles the Sign Up button click by opening the signup modal window.
     */
    private void handleSignup() {
        if (indexController == null) {
            Logger.error("Cannot open signup window: IndexController is null.");
            return;
        }
        indexController.openSignupWindow();
    }

    /**
     * Handles the Profile button click.
     */
    private void handleProfile() {
        Logger.info("Profile button clicked.");
        indexController.loadView("UserProfile.fxml");
    }

    /**
     * Handles the New Shipment button click.
     */
    private void handleNewShipment() {
        Logger.info("New Shipment button clicked.");
        indexController.loadView("CreateShipment.fxml");
    }

    /**
     * Handles the Orders button click.
     */
    private void handleOrders() {
        Logger.info("Orders button clicked.");
        indexController.loadView("MyShipments.fxml");
    }

    /**
     * Handles the Track Shipment button click.
     */
    private void handleTrackShipment() {
        Logger.info("Track Shipment button clicked.");
        indexController.loadView("TrackShipment.fxml");
    }

    /**
     * Handles the Logout button click - logs out the user and reloads the application.
     */
    private void handleLogout() {
        Logger.info("Logout button clicked.");
        authService.logout();
        indexController.reloadApplication();
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
        btnTrackShipment.setOnAction(event -> handleTrackShipment());
        btnLogout.setOnAction(event -> handleLogout());
    }

    /**
     * Sets the visibility of sidebar buttons based on the user's authentication status.
     */
    private void updateButtonVisibility() {
        boolean isLoggedIn = authService.isPersonLoggedIn();
        Logger.info("Updating button visibility. User is logged in: " + isLoggedIn);

        // Buttons visible when logged OUT
        setButtonVisibility(btnLogin, !isLoggedIn);
        setButtonVisibility(btnSignup, !isLoggedIn);

        // Buttons visible when logged IN
        setButtonVisibility(btnProfile, isLoggedIn);
        setButtonVisibility(btnNewShipment, isLoggedIn);
        setButtonVisibility(btnOrders, isLoggedIn);
        setButtonVisibility(btnTrackShipment, isLoggedIn);
        setButtonVisibility(btnLogout, isLoggedIn);
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
