package co.edu.uniquindio.poo.ProyectoFinal2025_2.Controller;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Order;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.OrderStatus;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.User;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.AuthenticationService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.OrderService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.DialogUtil;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.NavigationUtil;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for the User Dashboard view (UserDashboard.fxml).
 * <p>
 * This controller manages the user's main dashboard, displaying
 * current statistics about their orders and shipments.
 * </p>
 */
public class UserDashboardController implements Initializable {

    // =================================================================================================================
    // FXML Fields - Header
    // =================================================================================================================

    @FXML private Label lblWelcome;
    @FXML private Label lblLastUpdated;

    // =================================================================================================================
    // FXML Fields - Statistics Cards
    // =================================================================================================================

    @FXML private Label lblTotalOrders;
    @FXML private Label lblPendingOrders;
    @FXML private Label lblProcessingOrders;
    @FXML private Label lblCompletedOrders;

    // =================================================================================================================
    // FXML Fields - Quick Actions
    // =================================================================================================================

    @FXML private VBox btnNewShipment;
    @FXML private VBox btnMyShipments;
    @FXML private VBox btnTrackShipment;
    @FXML private VBox btnManageAddresses;

    // =================================================================================================================
    // Services
    // =================================================================================================================

    private final AuthenticationService authService;
    private final OrderService orderService;
    private User currentUser;
    private IndexController indexController;

    // =================================================================================================================
    // Constructor
    // =================================================================================================================

    /**
     * Default constructor. Initializes required services.
     */
    public UserDashboardController() {
        this.authService = AuthenticationService.getInstance();
        this.orderService = new OrderService();
    }

    // =================================================================================================================
    // Initialization
    // =================================================================================================================

    /**
     * Initializes the controller. Called automatically after FXML loading.
     * <p>
     * Loads the current user data and populates the dashboard with statistics.
     * </p>
     *
     * @param url            The location used to resolve relative paths.
     * @param resourceBundle The resources used to localize the root object.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Logger.info("Initializing UserDashboardController...");

        loadUserData();
        loadStatistics();
        setupQuickActions();
        updateLastRefreshTime();

        Logger.info("UserDashboardController initialized successfully.");
    }

    /**
     * Injects the IndexController reference for navigation.
     *
     * @param indexController The main IndexController instance.
     */
    public void setIndexController(IndexController indexController) {
        this.indexController = indexController;
    }

    // =================================================================================================================
    // Data Loading Methods
    // =================================================================================================================

    /**
     * Loads the current user from the authentication service.
     */
    private void loadUserData() {
        if (authService.getCurrentPerson() instanceof User user) {
            this.currentUser = user;
            lblWelcome.setText("Bienvenido, " + user.getName());
        } else {
            Logger.error("Current user is not a User!");
            DialogUtil.showError("Error", "No se pudo cargar la información del usuario.");
        }
    }

    /**
     * Loads and displays statistics related to user orders.
     */
    private void loadStatistics() {
        if (currentUser == null) {
            return;
        }

        List<Order> orders = orderService.getOrdersByUser(currentUser.getId());

        int total = orders.size();
        long pending = orders.stream().filter(o -> o.getStatus() == OrderStatus.AWAITING_PAYMENT).count();
        long processing = orders.stream().filter(o -> o.getStatus() == OrderStatus.PENDING_APPROVAL || o.getStatus() == OrderStatus.APPROVED).count();
        long completed = orders.stream().filter(o -> o.getStatus() == OrderStatus.CANCELLED).count();

        lblTotalOrders.setText(String.valueOf(total));
        lblPendingOrders.setText(String.valueOf(pending));
        lblProcessingOrders.setText(String.valueOf(processing));
        lblCompletedOrders.setText(String.valueOf(completed));

        // Ensure programmatic styling is applied to all stat labels
        applyStatLabelStyles();
    }

    /**
     * Applies programmatic styles to statistic labels to ensure proper formatting.
     */
    private void applyStatLabelStyles() {
        // Apply styles to value labels
        String valueStyle = "-fx-text-fill: #2196F3; -fx-font-size: 32px; -fx-font-weight: bold;";
        lblTotalOrders.setStyle(valueStyle);
        lblPendingOrders.setStyle(valueStyle.replace("#2196F3", "#FFA726"));
        lblProcessingOrders.setStyle(valueStyle.replace("#2196F3", "#42A5F5"));
        lblCompletedOrders.setStyle(valueStyle.replace("#2196F3", "#4CAF50"));

        Logger.info("Programmatic styles applied to dashboard stat labels.");
    }

    /**
     * Sets up quick action card handlers.
     */
    private void setupQuickActions() {
        if (btnNewShipment != null) {
            btnNewShipment.setOnMouseClicked(event -> handleNewShipment());
        }
        if (btnMyShipments != null) {
            btnMyShipments.setOnMouseClicked(event -> handleMyShipments());
        }
        if (btnTrackShipment != null) {
            btnTrackShipment.setOnMouseClicked(event -> handleTrackShipment());
        }
        if (btnManageAddresses != null) {
            btnManageAddresses.setOnMouseClicked(event -> handleManageAddresses());
        }
    }

    /**
     * Updates the last refresh timestamp label.
     */
    private void updateLastRefreshTime() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        lblLastUpdated.setText("Última actualización: " + timestamp);
    }

    // =================================================================================================================
    // Event Handlers
    // =================================================================================================================

    /**
     * Handles the refresh button click. Reloads all dashboard data.
     */
    @FXML
    private void handleRefresh() {
        Logger.info("Refreshing user dashboard...");
        loadUserData();
        loadStatistics();
        updateLastRefreshTime();
        DialogUtil.showSuccess("Actualizado", "Dashboard actualizado correctamente.");
    }

    /**
     * Handles the new shipment quick action.
     */
    private void handleNewShipment() {
        if (indexController != null) {
            NavigationUtil.navigate(indexController, "CreateShipmentWizard.fxml", CreateShipmentWizardController.class);
        }
    }

    /**
     * Handles the my shipments quick action.
     */
    private void handleMyShipments() {
        if (indexController != null) {
            NavigationUtil.navigate(indexController, "MyShipments.fxml", MyShipmentsController.class);
        }
    }

    /**
     * Handles the track shipment quick action.
     */
    private void handleTrackShipment() {
        if (indexController != null) {
            NavigationUtil.navigate(indexController, "TrackShipment.fxml", TrackShipmentController.class);
        }
    }

    /**
     * Handles the manage addresses quick action.
     */
    private void handleManageAddresses() {
        if (indexController != null) {
            indexController.loadView("ManageAddresses.fxml");
        } else {
            NavigationUtil.navigate(indexController, "ManageAddresses.fxml", getClass());
        }
    }
}
