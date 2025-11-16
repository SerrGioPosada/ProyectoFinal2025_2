package co.edu.uniquindio.poo.ProyectoFinal2025_2.Controller;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.NavigationUtil;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Controller for the administrator-specific sidebar (AdminSidebar.fxml).
 * <p>
 * This controller defines navigation actions for admin-related views and inherits
 * shared behavior (such as animations, profile image handling, and authentication)
 * from {@link BaseSidebarController}.
 * </p>
 */

public class AdminSidebarController extends BaseSidebarController {

    // =================================================================================================================
    // FXML Fields
    // =================================================================================================================

    @FXML
    private Button btnDashboard;
    @FXML
    private Button btnManageUsers;
    @FXML
    private Button btnManageDelivery;
    @FXML
    private Button btnManageVehicles;
    @FXML
    private Button btnManageOrders;
    @FXML
    private Button btnManageShipments;
    @FXML
    private Button btnReports;
    @FXML
    private Button btnNotifications;
    @FXML
    private Button btnProfile;
    @FXML
    private Button btnLogout;

    // =================================================================================================================
    // Initialization
    // =================================================================================================================

    /**
     * Initializes the AdminSidebar controller.
     * <p>
     * This method first calls {@link BaseSidebarController#initialize(URL, ResourceBundle)}
     * to set up shared sidebar behavior (animations, user image, etc.), and then
     * binds navigation logic for all admin buttons.
     * </p>
     *
     * @param url            The location used to resolve relative paths for the root object, or {@code null} if not known.
     * @param resourceBundle The resources used to localize the root object, or {@code null} if not used.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        log("AdminSidebarController initialized.");
        setupButtonActions();
        setupNotificationBadge();
    }

    /**
     * Sets up the notification badge on the notifications button.
     * The badge shows the count of unread notifications and updates in real-time.
     */
    private void setupNotificationBadge() {
        if (btnNotifications != null) {
            createNotificationBadge(btnNotifications);
            setupNotificationBadgeListener();
            log("Notification badge initialized for admin");
        }
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
        // Bind each button to its view
        bindNavigation(btnDashboard, "AdminDashboard.fxml");
        bindNavigation(btnManageUsers, "ManageUsers.fxml");
        bindNavigation(btnManageDelivery, "ManageDeliveryPersons.fxml");
        bindNavigation(btnManageVehicles, "ManageVehicles.fxml");
        bindNavigation(btnManageOrders, "AdminOrderManagement.fxml");
        bindNavigation(btnManageShipments, "ShipmentManagement.fxml");
        bindNavigation(btnReports, "Reports.fxml");
        bindNavigation(btnNotifications, "NotificationsCenter.fxml");
        bindNavigation(btnProfile, "AdminProfile.fxml");

        // Logout button has special behavior (doesn't navigate to a view)
        if (btnLogout != null) {
            btnLogout.setOnAction(event -> handleLogout());
        }
    }

    /**
     * Handles the Logout button click - logs out the admin and reloads the application.
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
     * <p>
     * If the button reference is {@code null}, no action will be bound and a warning is logged.
     * Sets the button as active when clicked.
     * </p>
     *
     * @param button   The {@link Button} to which the navigation action will be assigned.
     * @param viewName The name of the FXML view file to navigate to (e.g., "AdminDashboard.fxml").
     */
    private void bindNavigation(Button button, String viewName) {
        if (button != null) {
            button.setOnAction(event -> {
                setActiveButton(button);
                NavigationUtil.navigate(indexController, viewName, getClass());

                // Clear contextual filters when navigating from sidebar
                clearContextualFiltersForView(viewName);
            });
        } else {
            Logger.warn("[AdminSidebarController] Warning: Tried to bind navigation to a null button for view: " + viewName);
        }
    }

    /**
     * Clears contextual filters for views that support filtering.
     * This ensures that when navigating from the sidebar, no filters from previous
     * contextual navigation (e.g., from ManageUsers) remain active.
     *
     * @param viewName The name of the view being navigated to
     */
    private void clearContextualFiltersForView(String viewName) {
        if (indexController == null) return;

        // Use Platform.runLater to ensure the view is loaded before clearing filters
        javafx.application.Platform.runLater(() -> {
            try {
                Object controller = indexController.getCurrentController();
                if (controller == null) return;

                switch (viewName) {
                    case "AdminOrderManagement.fxml":
                        if (controller instanceof AdminOrderManagementController orderController) {
                            orderController.clearContextualFilter();
                        }
                        break;
                    case "ShipmentManagement.fxml":
                        if (controller instanceof ShipmentManagementController shipmentController) {
                            shipmentController.clearContextualFilter();
                        }
                        break;
                    case "ManageVehicles.fxml":
                        if (controller instanceof ManageVehiclesController vehiclesController) {
                            vehiclesController.clearContextualFilter();
                        }
                        break;
                    // Add more cases here for other views that support contextual filtering
                }
            } catch (Exception e) {
                Logger.error("Error clearing contextual filter for " + viewName, e);
            }
        });
    }

    /**
     * Sets the active button based on the current view name.
     * Called by IndexController when loading a view.
     *
     * @param viewName The name of the view file (e.g., "AdminDashboard.fxml")
     */
    public void setActiveView(String viewName) {
        Button buttonToActivate = switch (viewName) {
            case "AdminDashboard.fxml" -> btnDashboard;
            case "ManageUsers.fxml" -> btnManageUsers;
            case "ManageDeliveryPersons.fxml" -> btnManageDelivery;
            case "ManageVehicles.fxml" -> btnManageVehicles;
            case "AdminOrderManagement.fxml" -> btnManageOrders;
            case "ShipmentManagement.fxml" -> btnManageShipments;
            case "Reports.fxml" -> btnReports;
            case "NotificationsCenter.fxml" -> btnNotifications;
            case "AdminProfile.fxml" -> btnProfile;
            default -> null;
        };

        if (buttonToActivate != null) {
            setActiveButton(buttonToActivate);
        }
    }
}
