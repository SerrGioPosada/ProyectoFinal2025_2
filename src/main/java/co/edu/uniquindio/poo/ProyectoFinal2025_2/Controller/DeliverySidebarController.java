package co.edu.uniquindio.poo.ProyectoFinal2025_2.Controller;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.NavigationUtil;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Controller for the delivery person-specific sidebar (DeliverySidebar.fxml).
 * <p>
 * This controller defines navigation actions for delivery-related views and inherits
 * shared behavior (such as animations, profile image handling, and authentication)
 * from {@link BaseSidebarController}.
 * </p>
 */
public class DeliverySidebarController extends BaseSidebarController {

    // =================================================================================================================
    // FXML Fields
    // =================================================================================================================

    @FXML
    private Button btnDashboard;
    @FXML
    private Button btnMyShipments;
    @FXML
    private Button btnRouteMap;
    @FXML
    private Button btnHistory;
    @FXML
    private Button btnMyVehicles;
    @FXML
    private Button btnProfile;
    @FXML
    private Button btnLogout;

    // =================================================================================================================
    // Initialization
    // =================================================================================================================

    /**
     * Initializes the DeliverySidebar controller.
     * <p>
     * This method first calls {@link BaseSidebarController#initialize(URL, ResourceBundle)}
     * to set up shared sidebar behavior (animations, user image, etc.), and then
     * binds navigation logic for all delivery person buttons.
     * </p>
     *
     * @param url            The location used to resolve relative paths for the root object, or {@code null} if not known.
     * @param resourceBundle The resources used to localize the root object, or {@code null} if not used.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        log("DeliverySidebarController initialized.");
        setupButtonActions();
    }


    // =================================================================================================================
    // Event Binding
    // =================================================================================================================

    /**
     * Binds navigation actions for each sidebar button to its corresponding FXML view.
     * <p>
     * Each button triggers a scene navigation handled by {@link NavigationUtil#navigate}.
     * </p>
     */
    private void setupButtonActions() {
        Map<Button, String> navigationMap = Map.of(
                btnDashboard, "DeliveryDashboard.fxml",
                btnMyShipments, "DeliveryShipments.fxml",
                btnRouteMap, "DeliveryRouteMap.fxml",
                btnHistory, "DeliveryHistory.fxml",
                btnMyVehicles, "DeliveryMyVehicles.fxml",
                btnProfile, "DeliveryProfile.fxml"
        );

        navigationMap.forEach(this::bindNavigation);

        // Logout button has special behavior (doesn't navigate to a view)
        if (btnLogout != null) {
            btnLogout.setOnAction(event -> handleLogout());
        }
    }

    /**
     * Handles the Logout button click - logs out the delivery person and reloads the application.
     */
    private void handleLogout() {
        Logger.info("Logout button clicked.");
        authService.logout();
        indexController.reloadApplication();
    }

    // =================================================================================================================
    // Generic Helper
    // =================================================================================================================

    /**
     * Binds a button to navigate to a specific FXML view when clicked.
     * Sets the button as active when clicked.
     * <p>
     * If the button reference is {@code null}, no action will be bound and a warning is logged.
     * </p>
     *
     * @param button   The {@link Button} to which the navigation action will be assigned.
     * @param viewName The name of the FXML view file to navigate to (e.g., "DeliveryDashboard.fxml").
     */
    private void bindNavigation(Button button, String viewName) {
        if (button != null) {
            button.setOnAction(event -> {
                setActiveButton(button);
                NavigationUtil.navigate(indexController, viewName, getClass());
            });
        } else {
            Logger.warn("[DeliverySidebarController] Warning: Tried to bind navigation to a null button for view: " + viewName);
        }
    }

    /**
     * Sets the active button based on the current view name.
     * Called by IndexController when loading a view.
     *
     * @param viewName The name of the view file (e.g., "DeliveryDashboard.fxml")
     */
    public void setActiveView(String viewName) {
        Button buttonToActivate = switch (viewName) {
            case "DeliveryDashboard.fxml" -> btnDashboard;
            case "DeliveryShipments.fxml" -> btnMyShipments;
            case "DeliveryRouteMap.fxml" -> btnRouteMap;
            case "DeliveryHistory.fxml" -> btnHistory;
            case "DeliveryMyVehicles.fxml" -> btnMyVehicles;
            case "DeliveryProfile.fxml" -> btnProfile;
            default -> null;
        };

        if (buttonToActivate != null) {
            setActiveButton(buttonToActivate);
        }
    }
}
