package co.edu.uniquindio.poo.proyectofiinal2025_2.Controller;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Util.NavigationUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Map;

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

    @FXML private Button btnDashboard;
    @FXML private Button btnManageUsers;
    @FXML private Button btnManageShipments;
    @FXML private Button btnReports;

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
     * @param url The location used to resolve relative paths for the root object, or {@code null} if not known.
     * @param resourceBundle The resources used to localize the root object, or {@code null} if not used.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        log("AdminSidebarController initialized.");
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
                btnDashboard, "AdminDashboard.fxml",
                btnManageUsers, "ManageUsers.fxml",
                btnManageShipments, "ManageShipments.fxml",
                btnReports, "Reports.fxml"
        );

        navigationMap.forEach(this::bindNavigation);
    }

    // =================================================================================================================
    // Generic Helper
    // =================================================================================================================

    /**
     * Binds a button to navigate to a specific FXML view when clicked.
     * <p>
     * If the button reference is {@code null}, no action will be bound and a warning is logged.
     * </p>
     *
     * @param button   The {@link Button} to which the navigation action will be assigned.
     * @param viewName The name of the FXML view file to navigate to (e.g., "AdminDashboard.fxml").
     */
    private void bindNavigation(Button button, String viewName) {
        if (button != null) {
            button.setOnAction(event -> NavigationUtil.navigate(indexController, viewName, getClass()));
        } else {
            System.err.println("[AdminSidebarController] Warning: Tried to bind navigation to a null button for view: " + viewName);
        }
    }
}
