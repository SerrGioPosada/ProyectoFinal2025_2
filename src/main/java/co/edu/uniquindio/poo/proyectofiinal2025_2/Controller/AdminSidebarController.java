package co.edu.uniquindio.poo.proyectofiinal2025_2.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the administrator-specific sidebar (AdminSidebar.fxml).
 * <p>
 * This class extends {@link BaseSidebarController} to inherit common sidebar functionalities
 * like profile image display and animations. It is responsible for handling navigation
 * events triggered by the administrator-specific buttons, such as navigating to the dashboard,
 * user management, shipment management, and reports views.
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
    private Button btnManageShipments;

    @FXML
    private Button btnReports;

    // =================================================================================================================
    // Initialization
    // =================================================================================================================

    /**
     * Initializes the controller after its root element has been completely processed.
     * This method is called automatically by the FXMLLoader.
     *
     * @param url The location used to resolve relative paths for the root object, or {@code null} if the location is not known.
     * @param resourceBundle The resources used to localize the root object, or {@code null} if the root object was not localized.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        System.out.println("AdminSidebarController initialized.");
        setupButtonActions();
    }

    // =================================================================================================================
    // Event Handlers
    // =================================================================================================================

    /**
     * Navigates to the main admin dashboard view when the corresponding button is clicked.
     */
    private void handleDashboard() {
        System.out.println("Dashboard button clicked.");
        if (indexController == null) {
            System.err.println("Cannot load dashboard view: IndexController is null.");
            return;
        }
        String viewName = "AdminDashboard.fxml";
        indexController.loadView(viewName);
    }

    /**
     * Navigates to the user management view when the corresponding button is clicked.
     */
    private void handleManageUsers() {
        System.out.println("Manage Users button clicked.");
        if (indexController == null) {
            System.err.println("Cannot load manage users view: IndexController is null.");
            return;
        }
        String viewName = "ManageUsers.fxml";
        indexController.loadView(viewName);
    }

    /**
     * Navigates to the shipment management view when the corresponding button is clicked.
     */
    private void handleManageShipments() {
        System.out.println("Manage Shipments button clicked.");
        if (indexController == null) {
            System.err.println("Cannot load manage shipments view: IndexController is null.");
            return;
        }
        String viewName = "ManageShipmentsView.fxml";
        indexController.loadView(viewName);
    }

    /**
     * Navigates to the reports view when the corresponding button is clicked.
     */
    private void handleReports() {
        System.out.println("Reports button clicked.");
        if (indexController == null) {
            System.err.println("Cannot load reports view: IndexController is null.");
            return;
        }
        String viewName = "ReportsView.fxml";
        indexController.loadView(viewName);
    }

    // =================================================================================================================
    // Private Helper Methods
    // =================================================================================================================

    /**
     * Binds the action event of each button to its corresponding handler method.
     */
    private void setupButtonActions() {
        if (btnDashboard != null) {
            btnDashboard.setOnAction(event -> handleDashboard());
        }
        if (btnManageUsers != null) {
            btnManageUsers.setOnAction(event -> handleManageUsers());
        }
        if (btnManageShipments != null) {
            btnManageShipments.setOnAction(event -> handleManageShipments());
        }
        if (btnReports != null) {
            btnReports.setOnAction(event -> handleReports());
        }
    }
}
